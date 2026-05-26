package com.blood.service;

import com.blood.model.enumformat.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Blood.BloodBagDetailResponse;
import com.blood.dto.BloodRequest.*;
import com.blood.dto.BloodRequest.NotiMess;
import com.blood.model.*;
import com.blood.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BloodRequestService {
    private final BloodRequestRepository bloodRequestRepository;
    private final HospitalRepository hospitalRepository;
    private final RequestDetailRepository requestDetailRepository;
    private final BloodBagRepository bloodBagRepository;
    private final ExportDetailRepository exportDetailRepository;
    private final ExportLogRepository exportLogRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ActionLogService actionLogService;
    private final ReturnLogRepository returnLogRepository;

    @Transactional
    public String requestBlood(RequestBloodRequest rq){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users currentUser = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không xác định danh tính người dùng"));
        Hospital currentHospital = currentUser.getHospital();

        BloodRequest bloodRequest = new BloodRequest();
        bloodRequest.setHospital(currentHospital);
        bloodRequest.setRequestDate(LocalDateTime.now());
        bloodRequest.setDeadlineDate(rq.getDeadlineDate());
        bloodRequest.setPriority(rq.getPriority());
        bloodRequest.setStatus(BloodRequestStatus.CHO_DUYET);
        
        List<RequestDetail> requestDetails = new ArrayList<>();
        for (DetailRequest detailDTO: rq.getDetails()){
            RequestDetail detail = new RequestDetail();
            detail.setBloodType(detailDTO.getBloodType());
            detail.setProductType(detailDTO.getProductType());
            detail.setVolume(detailDTO.getVolume());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setBloodRequest(bloodRequest);
            requestDetails.add(detail);
        }

        bloodRequest.setRequestDetails(requestDetails);
        bloodRequestRepository.save(bloodRequest);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", bloodRequest);


        String requestedBloods = rq.getDetails().stream()
                .map(d -> d.getBloodType() + " (" + d.getQuantity() + " túi)")
                .collect(Collectors.joining(", "));

        if (rq.getPriority() == Priority.KHAN_CAP) {

            long hoursUntilDeadline = Duration.between(LocalDateTime.now(), rq.getDeadlineDate().atStartOfDay()).toHours();
            if (hoursUntilDeadline > 48) {
                throw new RuntimeException("LỖI: Yêu cầu Khẩn cấp chỉ áp dụng cho các ca cần máu trong vòng 48 giờ tới. Vui lòng chọn mức độ Bình thường hoặc điều chỉnh lại thời gian");
            }

            NotiMess noti = new NotiMess(
                    "YÊU CẦU KHẨN CẤP",
                    "CẦN GẤP MÁU " + requestedBloods + " TÌNH TRẠNG: ",
                     rq.getPriority(),
                    " TỚI BỆNH VIỆN: " + currentHospital.getHospitalName() + " THỜI GIAN CẦN: ",
                     bloodRequest.getDeadlineDate());

            messagingTemplate.convertAndSend("/topic/urgent-requests", noti);

            log.info("Đã phát thông báo WebSocket cho yêu cầu khẩn cấp");
        }

        // Gửi WebSocket thông báo cho tất cả yêu cầu (thông thường & khẩn cấp) qua kênh chung
        NotiMess generalNoti = new NotiMess(
                rq.getPriority() == Priority.KHAN_CAP ? "YÊU CẦU KHẨN CẤP" : "YÊU CẦU MỚI",
                rq.getPriority() == Priority.KHAN_CAP
                        ? "🏥 Yêu cầu khẩn cấp từ " + currentHospital.getHospitalName() + ": Cần gấp " + requestedBloods
                        : "🏥 Yêu cầu mới từ " + currentHospital.getHospitalName() + ": Cần " + requestedBloods,
                rq.getPriority(),
                currentHospital.getHospitalName(),
                bloodRequest.getDeadlineDate()
        );
        messagingTemplate.convertAndSend("/topic/blood-requests", generalNoti);
        log.info("Đã phát thông báo WebSocket qua kênh /topic/blood-requests");

        return "Gửi phiếu thành công";
    }

    @Transactional
    public String updateBloodRequest(Integer requestId, RequestBloodRequest rq) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không xác định danh tính người dùng"));
        Hospital currentHospital = currentUser.getHospital();

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu máu"));

        if (!bloodRequest.getHospital().getHospitalId().equals(currentHospital.getHospitalId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa yêu cầu máu này");
        }

        if (bloodRequest.getStatus() != BloodRequestStatus.CHO_DUYET) {
            throw new RuntimeException("Chỉ được sửa yêu cầu đang chờ duyệt");
        }

        if (bloodRequest.getPriority() == Priority.KHAN_CAP) {
            throw new RuntimeException("Đơn khẩn cấp không cho phép chỉnh sửa. Vui lòng liên hệ hotline hoặc tạo đơn mới.");
        }

        if (Boolean.TRUE.equals(bloodRequest.getIsEdited())) {
            throw new RuntimeException("Yêu cầu này đã được chỉnh sửa một lần trước đó. Không thể chỉnh sửa tiếp lần thứ hai. Vui lòng hủy yêu cầu hiện tại và tạo một yêu cầu hoàn toàn mới để tiếp tục.");
        }

        if (rq.getPriority() == Priority.KHAN_CAP) {
            throw new RuntimeException("Không được tự động chuyển đổi trạng thái đơn sang Khẩn cấp từ chức năng chỉnh sửa thông thường.");
        }

        // Capture old state for action logging
        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("requestId", bloodRequest.getRequestId());
        oldData.put("deadlineDate", bloodRequest.getDeadlineDate());
        oldData.put("priority", bloodRequest.getPriority());
        oldData.put("status", bloodRequest.getStatus());
        List<Map<String, Object>> oldDetails = bloodRequest.getRequestDetails().stream().map(d -> {
            Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("bloodType", d.getBloodType());
            dm.put("productType", d.getProductType() != null ? d.getProductType().name() : null);
            dm.put("volume", d.getVolume());
            dm.put("quantity", d.getQuantity());
            return dm;
        }).collect(Collectors.toList());
        oldData.put("details", oldDetails);

        // Delete old details
        requestDetailRepository.deleteAll(bloodRequest.getRequestDetails());
        bloodRequest.getRequestDetails().clear();

        // Update fields
        bloodRequest.setRequestDate(LocalDateTime.now()); // FIFO reset
        bloodRequest.setDeadlineDate(rq.getDeadlineDate());
        bloodRequest.setPriority(rq.getPriority());
        bloodRequest.setIsEdited(true); // Mark as edited

        List<RequestDetail> newDetails = new ArrayList<>();
        for (DetailRequest detailDTO : rq.getDetails()) {
            RequestDetail detail = new RequestDetail();
            detail.setBloodType(detailDTO.getBloodType());
            detail.setProductType(detailDTO.getProductType());
            detail.setVolume(detailDTO.getVolume());
            detail.setQuantity(detailDTO.getQuantity());
            detail.setBloodRequest(bloodRequest);
            newDetails.add(detail);
        }

        bloodRequest.setRequestDetails(newDetails);
        bloodRequestRepository.save(bloodRequest);

        // Capture new state for action logging
        Map<String, Object> newData = new LinkedHashMap<>();
        newData.put("requestId", bloodRequest.getRequestId());
        newData.put("deadlineDate", bloodRequest.getDeadlineDate());
        newData.put("priority", bloodRequest.getPriority());
        newData.put("status", bloodRequest.getStatus());
        List<Map<String, Object>> newDetailsLog = newDetails.stream().map(d -> {
            Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("bloodType", d.getBloodType());
            dm.put("productType", d.getProductType() != null ? d.getProductType().name() : null);
            dm.put("volume", d.getVolume());
            dm.put("quantity", d.getQuantity());
            return dm;
        }).collect(Collectors.toList());
        newData.put("details", newDetailsLog);

        // Log to action_log
        actionLogService.log(ActionType.UPDATE_REQUEST, "BloodRequest", String.valueOf(requestId), oldData, newData, "Bệnh viện cập nhật yêu cầu máu (Hàng xếp FIFO được thiết lập lại)");

        // Send WebSocket notification for updated request
        String requestedBloods = rq.getDetails().stream()
                .map(d -> d.getBloodType() + " (" + d.getQuantity() + " túi)")
                .collect(Collectors.joining(", "));

        NotiMess updateNoti = new NotiMess(
                "YÊU CẦU ĐÃ SỬA",
                "🏥 Yêu cầu máu #" + requestId + " từ " + currentHospital.getHospitalName() + " đã được cập nhật: " + requestedBloods + " (Đã xếp lại hàng FIFO)",
                bloodRequest.getPriority(),
                currentHospital.getHospitalName(),
                bloodRequest.getDeadlineDate()
        );
        messagingTemplate.convertAndSend("/topic/blood-requests", updateNoti);
        log.info("Đã phát thông báo WebSocket sửa đơn qua kênh /topic/blood-requests");

        return "Cập nhật yêu cầu thành công. Đơn hàng của bạn đã được xếp lại hàng từ đầu theo thứ tự FIFO.";
    }

    @Transactional
    public String cancelBloodRequest(Integer requestId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không xác định danh tính người dùng"));
        Hospital currentHospital = currentUser.getHospital();

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu máu"));

        if (!bloodRequest.getHospital().getHospitalId().equals(currentHospital.getHospitalId())) {
            throw new RuntimeException("Bạn không có quyền hủy yêu cầu máu này");
        }

        if (bloodRequest.getStatus() != BloodRequestStatus.CHO_DUYET) {
            throw new RuntimeException("Chỉ được hủy yêu cầu đang chờ duyệt");
        }

        // Capture old state for action logging
        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("requestId", bloodRequest.getRequestId());
        oldData.put("status", bloodRequest.getStatus());

        // Update status to DA_HUY
        bloodRequest.setStatus(BloodRequestStatus.DA_HUY);
        bloodRequestRepository.save(bloodRequest);

        // Capture new state
        Map<String, Object> newData = new LinkedHashMap<>();
        newData.put("requestId", bloodRequest.getRequestId());
        newData.put("status", bloodRequest.getStatus());

        // Log to action_log
        actionLogService.log(ActionType.REVIEW_REQUEST, "BloodRequest", String.valueOf(requestId), oldData, newData, "Bệnh viện tự hủy yêu cầu máu");

        // Send WebSocket notification for canceled request
        NotiMess cancelNoti = new NotiMess(
                "YÊU CẦU ĐÃ HỦY",
                "❌ Yêu cầu máu #" + requestId + " từ " + currentHospital.getHospitalName() + " đã bị hủy.",
                bloodRequest.getPriority(),
                currentHospital.getHospitalName(),
                bloodRequest.getDeadlineDate()
        );
        messagingTemplate.convertAndSend("/topic/blood-requests", cancelNoti);
        log.info("Đã phát thông báo WebSocket hủy đơn qua kênh /topic/blood-requests");

        return "Hủy yêu cầu thành công";
    }

    public List<ListRequestBloodResponse> getListRequest(String hospitalName, BloodRequestStatus status, String priority){
        List<BloodRequest> bloodRequests = bloodRequestRepository.findRequestsWithFilters(hospitalName, status, priority);

        return bloodRequests.stream().map(requests -> {
            List<DetailRequest> detailRequests = requests.getRequestDetails().stream().map(details -> {
                DetailRequest dto = new DetailRequest();
                dto.setDetailId(details.getDetailId());
                dto.setBloodType(details.getBloodType());
                dto.setProductType(details.getProductType());
                dto.setVolume(details.getVolume());
                dto.setQuantity(details.getQuantity());
                dto.setApprovedQuantity(details.getApprovedQuantity());
                dto.setDeadline(details.getBloodRequest().getDeadlineDate());
                return dto;
            }).collect(Collectors.toList());
            return ListRequestBloodResponse.builder()
                    .requestId(requests.getRequestId())
                    .hospitalName(requests.getHospital().getHospitalName())
                    .hospitalId(requests.getHospital().getHospitalId())
                    .deadlineDate(requests.getDeadlineDate())
                    .priority(requests.getPriority())
                    .requestedDate(requests.getRequestDate())
                    .status(requests.getStatus())
                    .detailRequests(detailRequests)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<ListRequestBloodResponse> getMyListRequest(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users currentUser = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không xác định danh tính người dùng"));
        Hospital currentHospital = hospitalRepository.findByUserId(currentUser.getId()).orElseThrow(() -> new RuntimeException("Tài khoản đang sử dụng không đúng của bệnh viện"));
        Integer realHospitalId = currentHospital.getHospitalId();
        List<BloodRequest> bloodRequests = bloodRequestRepository.findByHospital_HospitalIdCustomOrder(realHospitalId);
        return bloodRequests.stream().map(requests -> {
            List<DetailRequest> detailRequests = requests.getRequestDetails().stream().map(details -> {
                DetailRequest dto = new DetailRequest();
                dto.setDetailId(details.getDetailId());
                dto.setBloodType(details.getBloodType());
                dto.setProductType(details.getProductType());
                dto.setVolume(details.getVolume());
                dto.setQuantity(details.getQuantity());
                dto.setApprovedQuantity(details.getApprovedQuantity());
                return dto;
            }).collect(Collectors.toList());
            return ListRequestBloodResponse.builder()
                    .requestId(requests.getRequestId())
                    .hospitalName(requests.getHospital().getHospitalName())
                    .hospitalId(requests.getHospital().getHospitalId())
                    .deadlineDate(requests.getDeadlineDate())
                    .priority(requests.getPriority())
                    .requestedDate(requests.getRequestDate())
                    .status(requests.getStatus())
                    .detailRequests(detailRequests)
                    .build();
        }).collect(Collectors.toList());
    }

    public String reviewRequest(Integer requestId, ReviewRequestDTO rq) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        int totalRequested = 0;
        int totalAproved = 0;

        BloodRequestStatus oldStatus = bloodRequest.getStatus();
        List<Map<String, Object>> oldDetailsList = new ArrayList<>();

        for (ReviewDetailDTO dto: rq.getApprovedDetails()) {
            RequestDetail detail = requestDetailRepository.findById(dto.getDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin chi tiết"));

            Map<String, Object> detailMap = new LinkedHashMap<>();
            detailMap.put("detailId", detail.getDetailId());
            detailMap.put("bloodType", detail.getBloodType());
            detailMap.put("productType", detail.getProductType() != null ? detail.getProductType().name() : null);
            detailMap.put("quantity", detail.getQuantity());
            detailMap.put("approvedQuantity", detail.getApprovedQuantity());
            oldDetailsList.add(detailMap);

            detail.setApprovedQuantity(dto.getApprovedQuantity());
            requestDetailRepository.save(detail);
            log.info("UPDATE: Approved quantity updated for RequestDetail ID: {}, New Approved Quantity: {}", detail.getDetailId(), dto.getApprovedQuantity());
            totalAproved += dto.getApprovedQuantity();
            totalRequested += detail.getQuantity();
        }

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("requestId", requestId);
        oldData.put("status", oldStatus);
        oldData.put("approvedDetails", oldDetailsList);

        if (bloodRequest.getStatus() != BloodRequestStatus.CHO_DUYET && bloodRequest.getStatus() != BloodRequestStatus.DA_TU_CHOI && bloodRequest.getStatus() != BloodRequestStatus.DA_DUYET_TOAN_BO && bloodRequest.getStatus() != BloodRequestStatus.DA_DUYET_MOT_PHAN) {
            throw new RuntimeException("Chỉ được duyệt các đơn đang chờ duyệt");
        }

        if (totalAproved > totalRequested) {
            throw new RuntimeException("Kiểm tra lại số lượng túi duyệt");
        } else if (totalAproved == 0) {
            bloodRequest.setStatus(BloodRequestStatus.DA_TU_CHOI);
        } else if (totalAproved == totalRequested) {
            bloodRequest.setStatus(BloodRequestStatus.DA_DUYET_TOAN_BO);
        } else if (totalAproved < totalRequested) {
            bloodRequest.setStatus(BloodRequestStatus.DA_DUYET_MOT_PHAN);
        }
        bloodRequestRepository.save(bloodRequest);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", bloodRequest);

        // Ghi ActionLog
        Map<String, Object> reviewData = new LinkedHashMap<>();
        reviewData.put("requestId", requestId);
        reviewData.put("newStatus", bloodRequest.getStatus());
        reviewData.put("approvedDetails", rq.getApprovedDetails());
        actionLogService.log(ActionType.REVIEW_REQUEST,
                "BloodRequest", String.valueOf(requestId), oldData, reviewData);

        return "Cập nhật thành công";
    }

    @Transactional(readOnly = true)
    public RequestDetailResponse getRequestDetail(Integer requestId) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        List<DetailRequest> requestedItems = bloodRequest.getRequestDetails().stream().map(d -> {
            DetailRequest dto = new DetailRequest();
            dto.setDetailId(d.getDetailId());
            dto.setBloodType(d.getBloodType());
            dto.setProductType(d.getProductType());
            dto.setVolume(d.getVolume());
            dto.setQuantity(d.getQuantity());
            dto.setApprovedQuantity(d.getApprovedQuantity());
            return dto;
        }).collect(Collectors.toList());

        RequestDetailResponse.RequestDetailResponseBuilder builder = RequestDetailResponse.builder()
                .requestId(bloodRequest.getRequestId())
                .hospitalName(bloodRequest.getHospital().getHospitalName())
                .hospitalId(bloodRequest.getHospital().getHospitalId())
                .priority(bloodRequest.getPriority())
                .status(bloodRequest.getStatus())
                .deadlineDate(bloodRequest.getDeadlineDate())
                .requestedDate(bloodRequest.getRequestDate())
                .requestedItems(requestedItems);

        exportLogRepository.findByBloodRequest_RequestId(requestId).ifPresent(log -> {
            String exportedBy = (log.getManager() != null) ? log.getManager().getFullName() : "N/A";

            List<RequestDetailResponse.ExportedBagDTO> bags = log.getExportDetails().stream().map(ed -> {
                BloodBag bag = ed.getBloodBag();
                return RequestDetailResponse.ExportedBagDTO.builder()
                        .bloodBagId(bag.getBloodBagId())
                        .bloodType(bag.getBloodType())
                        .rhFactor(bag.getRhFactor())
                        .productType(bag.getProductType())
                        .volume(bag.getVolume())
                        .expiredAt(bag.getExpiredAt())
                        .storageLocation(bag.getSafeStorageEquipmentName())
                        .status(bag.getStatus() != null ? bag.getStatus().name() : null)
                        .returnReasonNote(bag.getReturnReasonNote())
                        .build();
            }).collect(Collectors.toList());

            builder.exportDate(log.getExportDate())
                   .exportedBy(exportedBy)
                   .exportedBags(bags);
        });

        return builder.build();
    }

    @Transactional
    public String exportBlood(Integer requestId, ExportBloodRequest rq) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu máu"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Users currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không xác nhận được danh tính người dùng"));

        Staff currentStaff = staffRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Danh tính không xác thực"));

        ExportLog exportLog = new ExportLog();
        exportLog.setManager(currentStaff);
        exportLog.setExportDate(LocalDateTime.now());
        exportLog.setBloodRequest(bloodRequest);
        exportLog = exportLogRepository.save(exportLog);

        Map<Integer, Integer> scanCounts = new HashMap<>();
        for (RequestDetail detail : bloodRequest.getRequestDetails()) {
            scanCounts.put(detail.getDetailId(), 0);
        }

        List<BloodBag> bagsToSave = new ArrayList<>();
        List<ExportDetail> exportDetailsToSave = new ArrayList<>();

        BloodRequestStatus oldRequestStatus = bloodRequest.getStatus();
        List<Map<String, Object>> oldBagsInfo = new ArrayList<>();

        for (Integer bagId : rq.getBloodBagId()) {
            BloodBag bloodBag = bloodBagRepository.findById(bagId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));

            Map<String, Object> bagMap = new LinkedHashMap<>();
            bagMap.put("bloodBagId", bloodBag.getBloodBagId());
            bagMap.put("bagCode", bloodBag.getBagCode());
            bagMap.put("status", bloodBag.getStatus() != null ? bloodBag.getStatus().name() : null);
            bagMap.put("equipmentId", bloodBag.getStorageEquipment() != null ? bloodBag.getStorageEquipment().getEquipmentId() : null);
            bagMap.put("equipmentName", bloodBag.getStorageEquipment() != null ? bloodBag.getStorageEquipment().getName() : null);
            oldBagsInfo.add(bagMap);

            if (bloodBag.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Túi máu " + bagId + " đã quá hạn sử dụng, yêu cầu kiểm tra lại");
            }

            boolean isReady = bloodBag.getStatus() == BloodBagStatus.SAN_SANG;
            boolean isEmergencyWholeBlood = bloodBag.getProductType() == ProductType.MAU_TOAN_PHAN
                    && bloodBag.getStatus() == BloodBagStatus.CHO_TACH_CHIET;

            if (!isReady && !isEmergencyWholeBlood) {
                throw new RuntimeException("Túi máu " + bagId + " chưa sẵn sàng để xuất kho");
            }

            boolean isMatched = false;
            for (RequestDetail detail : bloodRequest.getRequestDetails()) {
                String rqType = "";
                String rqRh = "";
                if (detail.getBloodType() != null && detail.getBloodType().length() >= 2) {
                    int length = detail.getBloodType().length();
                    rqRh = detail.getBloodType().substring(length - 1);
                    rqType = detail.getBloodType().substring(0, length - 1);
                }

                if (detail.getProductType() == (bloodBag.getProductType()) && rqType.equalsIgnoreCase(bloodBag.getBloodType()) &&
                        rqRh.equalsIgnoreCase(bloodBag.getRhFactor()) && detail.getVolume().equals(bloodBag.getVolume())) {
                    int currentCount = scanCounts.get(detail.getDetailId());
                    if (currentCount < detail.getApprovedQuantity()) {
                        scanCounts.put(detail.getDetailId(), currentCount + 1);
                        isMatched = true;

                        bloodBag.setStatus(BloodBagStatus.DA_XUAT);
                        bloodBag.setStorageEquipment(null);
                        bagsToSave.add(bloodBag);

                        ExportDetail exportDetail = new ExportDetail();
                        exportDetail.setExportLog(exportLog);
                        exportDetail.setBloodBag(bloodBag);
                        exportDetailsToSave.add(exportDetail);

                        break;
                    }
                }
            }
            if (!isMatched) {
                throw new RuntimeException("Túi máu " + bagId + " không đúng yêu cầu hoặc đã quá số lượng");
            }
        }

        for (RequestDetail detail : bloodRequest.getRequestDetails()) {
            if (scanCounts.get(detail.getDetailId()) < detail.getApprovedQuantity()) {
                throw new RuntimeException("Chưa quét đủ số lượng cho " + detail.getProductType() + " " + detail.getBloodType());
            }
        }

        bloodBagRepository.saveAll(bagsToSave);
        log.info("CREATE/UPDATE: Bulk state change successfully saved for list of size: {}", bagsToSave != null ? bagsToSave.size() : 0);
        exportDetailRepository.saveAll(exportDetailsToSave);
        log.info("CREATE/UPDATE: Bulk state change successfully saved for list of size: {}", exportDetailsToSave != null ? exportDetailsToSave.size() : 0);

        bloodRequest.setStatus(BloodRequestStatus.DANG_VAN_CHUYEN);
        bloodRequestRepository.save(bloodRequest);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", bloodRequest);

        // Capture oldData Map
        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("requestId", requestId);
        oldData.put("requestStatus", oldRequestStatus);
        oldData.put("exportedBags", oldBagsInfo);

        // Ghi ActionLog
        Map<String, Object> exportData = new LinkedHashMap<>();
        exportData.put("requestId", requestId);
        exportData.put("exportedBy", currentStaff.getFullName());
        exportData.put("exportedBagIds", rq.getBloodBagId());
        exportData.put("totalBags", bagsToSave.size());
        exportData.put("newRequestStatus", BloodRequestStatus.DANG_VAN_CHUYEN);
        actionLogService.log(ActionType.EXPORT_BLOOD,
                "BloodRequest", String.valueOf(requestId), oldData, exportData);

        return "Xuất kho thành công";
    }

    @Transactional(readOnly = true)
    public BloodBagDetailResponse scanBloodBag(Integer requestId, String bagCode) {
        BloodBag bloodBag = bloodBagRepository.findByBagCode(bagCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        if (bloodBag.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Túi máu đã quá hạn sử dụng");
        }

        boolean isReady = bloodBag.getStatus() == BloodBagStatus.SAN_SANG;
        boolean isEmergencyWholeBlood = bloodBag.getProductType() == ProductType.MAU_TOAN_PHAN
                && bloodBag.getStatus() == BloodBagStatus.CHO_TACH_CHIET;

        if (!isReady && !isEmergencyWholeBlood) {
            throw new RuntimeException("Túi máu chưa sẵn sàng để xuất kho (Trạng thái hiện tại: " + bloodBag.getStatus() + ")");
        }

        boolean isMatched = false;
        for (RequestDetail detail : bloodRequest.getRequestDetails()) {
            String rqType = "";
            String rqRh = "";
            if (detail.getBloodType() != null && detail.getBloodType().length() >= 2) {
                int length = detail.getBloodType().length();
                rqRh = detail.getBloodType().substring(length - 1);
                rqType = detail.getBloodType().substring(0, length - 1);
            }

            if (detail.getProductType() == bloodBag.getProductType()
                    && rqType.equalsIgnoreCase(bloodBag.getBloodType())
                    && rqRh.equalsIgnoreCase(bloodBag.getRhFactor())
                    && detail.getVolume().equals(bloodBag.getVolume())) {
                isMatched = true;
                break;
            }
        }

        if (!isMatched) {
            throw new RuntimeException("Túi máu không khớp với bất kỳ yêu cầu nào trong đơn hàng này");
        }

        return BloodBagDetailResponse.builder()
                .productType(bloodBag.getProductType())
                .bloodType(bloodBag.getBloodType())
                .rhFactor(bloodBag.getRhFactor())
                .donorName(bloodBag.getRegistration().getDonor().getFullName())
                .collectedAt(bloodBag.getCollectedAt())
                .expirationDate(bloodBag.getExpiredAt())
                .actualVolume(bloodBag.getVolume())
                .storageLocation(bloodBag.getSafeStorageEquipmentName())
                .status(bloodBag.getStatus())
                .build();
    }

    public List<ListBloodBagMatchRequestResponse> findBagsByBloodRequest(Integer requestId) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        List<ListBloodBagMatchRequestResponse> result = new ArrayList<>();

        for (RequestDetail detail : bloodRequest.getRequestDetails()) {
            if (detail.getApprovedQuantity() == null || detail.getApprovedQuantity() <= 0) continue;

            String rqRh = "";
            String rqType = "";
            if (detail.getBloodType() != null && detail.getBloodType().length() >= 2) {
                int length = detail.getBloodType().length();
                rqRh = detail.getBloodType().substring(length - 1);
                rqType = detail.getBloodType().substring(0, length - 1);
            }

            int limit = detail.getApprovedQuantity() + 5;
            Pageable pageable = PageRequest.of(0, limit);

            List<BloodBag> matchingBags = bloodBagRepository.findBagsForExport(
                    detail.getProductType(), rqType, rqRh, detail.getVolume(), pageable);

            for (int i = 0; i < matchingBags.size(); i++) {
                BloodBag bag = matchingBags.get(i);
                ListBloodBagMatchRequestResponse dto = ListBloodBagMatchRequestResponse.builder()
                        .bloodBagId(bag.getBloodBagId())
                        .productType(bag.getProductType())
                        .bloodType(bag.getBloodType())
                        .rhFactor(bag.getRhFactor())
                        .volume(bag.getVolume())
                        .expiryDate(bag.getExpiredAt())
                        .bagCode(bag.getBagCode())
                        .storageLocation(bag.getSafeStorageEquipmentName())
                        .isSuggested(i < detail.getApprovedQuantity())
                        .build();
                result.add(dto);
            }
        }
        return result;
    }

    @Transactional
    public String trackOrder(Integer requestId, HospitalDeliveryResponse response) {

        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu máu"));

        List<BloodBag> allBags = bloodBagRepository.findBloodBagsByRequestId(requestId);
        List<RejectedBagInfo> rejectedList = (response != null) ? response.getRejectedBags() : null;

        String message = "";

        if (rejectedList == null || rejectedList.isEmpty()) {
            bloodRequest.setStatus(BloodRequestStatus.DA_NHAN);
            allBags.forEach(bag -> {
                bag.setStatus(BloodBagStatus.DA_DUOC_SU_DUNG);
                bag.setReturnReasonNote(null);
            });
            message = "Đã xác nhận nhận toàn bộ đơn hàng.";
        }
        else {
            Map<Integer, String> rejectedMap = rejectedList.stream()
                    .collect(Collectors.toMap(RejectedBagInfo::getBloodBagId, RejectedBagInfo::getReason));

            if (rejectedList.size() == allBags.size()) {
                bloodRequest.setStatus(BloodRequestStatus.HOAN_TRA_TOAN_BO);
                allBags.forEach(bag -> {
                    bag.setStatus(BloodBagStatus.HOAN_TRA);
                    bag.setReturnReasonNote(rejectedMap.get(bag.getBloodBagId()));
                });
                message = "Đã ghi nhận từ chối toàn bộ đơn hàng.";
            }
            else {
                bloodRequest.setStatus(BloodRequestStatus.DA_NHAN_MOT_PHAN);
                for (BloodBag bag : allBags) {
                    if (rejectedMap.containsKey(bag.getBloodBagId())) {
                        bag.setStatus(BloodBagStatus.HOAN_TRA);
                        bag.setReturnReasonNote(rejectedMap.get(bag.getBloodBagId()));
                    } else {
                        bag.setStatus(BloodBagStatus.DA_DUOC_SU_DUNG);
                        bag.setReturnReasonNote(null);
                    }
                }
                message = "Đã xác nhận nhận hàng một phần. Có " + rejectedList.size() + " túi bị hoàn trả.";
            }
            List<ReturnLog> returnLogs = rejectedList.stream().map(rejected -> {
                BloodBag bag = allBags.stream()
                        .filter(b -> b.getBloodBagId().equals(rejected.getBloodBagId()))
                        .findFirst()
                        .orElse(null);

                ReturnLog log = new ReturnLog();
                log.setBloodBag(bag);
                log.setHospital(bloodRequest.getHospital());
                log.setReason(rejected.getReason());
                log.setActionTaken(ReturnStatus.DANG_CHO);
                log.setReturnOrderId(requestId);
                log.setStaff(null);
                return log;
            }).collect(Collectors.toList());

            returnLogRepository.saveAll(returnLogs);
            log.info("CREATE: Saved {} ReturnLog entries for requestId={}", returnLogs.size(), requestId);
        }

        bloodRequestRepository.save(bloodRequest);
        bloodBagRepository.saveAll(allBags);

        return message;
    }
}
