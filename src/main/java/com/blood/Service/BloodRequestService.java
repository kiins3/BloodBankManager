package com.blood.Service;

import com.blood.DTO.Blood.BloodBagDetailResponse;
import com.blood.DTO.BloodRequest.*;
import com.blood.Model.*;
import com.blood.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BloodRequestService {
    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private RequestDetailRepository requestDetailRepository;
    @Autowired
    private BloodBagRepository bloodBagRepository;

    @Autowired
    private ExportDetailRepository exportDetailRepository;

    @Autowired
    private ExportLogRepository exportLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Transactional
    public String requestBlood(Integer hospitalId, RequestBloodRequest rq){
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh viện"));

        BloodRequest bloodRequest = new BloodRequest();
        bloodRequest.setHospital(hospital);
        bloodRequest.setRequestDate(LocalDateTime.now());
        bloodRequest.setDeadlineDate(rq.getDeadlineDate());
        bloodRequest.setPriority(rq.getPriority());
        bloodRequest.setStatus("CHO_DUYET");
        
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

        return "Gửi phiếu thành công";
    }

    public List<ListRequestBloodResponse> getListRequest(String hospitalName, String status){
        List<BloodRequest> bloodRequests = bloodRequestRepository.findWithFilters(hospitalName, status);

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
        System.out.println("hospital id : " + realHospitalId);
        List<BloodRequest> bloodRequests = bloodRequestRepository.findByHospital_HospitalId(realHospitalId);
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

        for (ReviewDetailDTO dto: rq.getApprovedDetails()) {
            RequestDetail detail = requestDetailRepository.findById(dto.getDetailId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin chi tiết"));
            detail.setApprovedQuantity(dto.getApprovedQuantity());
            requestDetailRepository.save(detail);
            totalAproved += dto.getApprovedQuantity();
            totalRequested += detail.getQuantity();
        }
        if (!bloodRequest.getStatus().equalsIgnoreCase("CHO_DUYET") && !bloodRequest.getStatus().equalsIgnoreCase("DA_TU_CHOI") && !bloodRequest.getStatus().equalsIgnoreCase("DA_DUYET_TOAN_BO") && !bloodRequest.getStatus().equalsIgnoreCase("DA_DUYET_MOT_PHAN")) {
            throw new RuntimeException("Chỉ được duyệt các đơn đang chờ duyệt");
        }

        if (totalAproved > totalRequested) {
            throw new RuntimeException("Kiểm tra lại số lượng túi duyệt");
        } else if (totalAproved == 0) {
            bloodRequest.setStatus("DA_TU_CHOI");
        } else if (totalAproved == totalRequested) {
            bloodRequest.setStatus("DA_DUYET_TOAN_BO");
        } else if (totalAproved < totalRequested) {
            bloodRequest.setStatus("DA_DUYET_MOT_PHAN");
        }
        bloodRequestRepository.save(bloodRequest);
        return "Cập nhật thành công";
    }

    public String trackOrder(Integer requestId) {
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu máu"));

        bloodRequest.setStatus("DA_NHAN");
        bloodRequestRepository.save(bloodRequest);
        return "Cập nhật thành công";
    }

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

        for (Integer bagId : rq.getBloodBagId()) {
            BloodBag bloodBag = bloodBagRepository.findById(bagId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));

            if (bloodBag.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Túi máu " + bagId + " đã quá hạn sử dụng, yêu cầu kiểm tra lại");
            }

            boolean isReady = bloodBag.getStatus().equalsIgnoreCase("SAN_SANG");
            boolean isEmergencyWholeBlood = bloodBag.getProductType().equalsIgnoreCase("MAU_TOAN_PHAN")
                    && bloodBag.getStatus().equalsIgnoreCase("CHO_TACH_CHIET");

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

                if (detail.getProductType().equalsIgnoreCase(bloodBag.getProductType()) && rqType.equalsIgnoreCase(bloodBag.getBloodType()) &&
                        rqRh.equalsIgnoreCase(bloodBag.getRhFactor()) && detail.getVolume().equals(bloodBag.getVolume())) {
                    int currentCount = scanCounts.get(detail.getDetailId());
                    if (currentCount < detail.getApprovedQuantity()) {
                        scanCounts.put(detail.getDetailId(), currentCount + 1);
                        isMatched = true;

                        bloodBag.setStatus("DA_XUAT");
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
        exportDetailRepository.saveAll(exportDetailsToSave);

        bloodRequest.setStatus("DANG_VAN_CHUYEN");
        bloodRequestRepository.save(bloodRequest);

        return "Xuất kho thành công";
    }

    @Transactional (readOnly = true)
    public BloodBagDetailResponse scanBloodBag(Integer requestId, String bagCode) {
        BloodBag bloodBag = bloodBagRepository.findByBagCode(bagCode).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));
        BloodRequest bloodRequest = bloodRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        boolean isBelongToThisOrder = exportDetailRepository.existsByBloodBag_BloodBagIdAndExportLog_BloodRequest_RequestId(
                bloodBag.getBloodBagId(), requestId
        );

        if (!isBelongToThisOrder) {
            throw new RuntimeException("Túi máu này không thuộc danh sách xuất kho");
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

            // Lấy nhiều hơn số lượng cần một chút để người dùng có thêm lựa chọn
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
                        .isSuggested(i < detail.getApprovedQuantity()) // Đánh dấu gợi ý FEFO
                        .build();
                result.add(dto);
            }
        }
        return result;
    }
}
