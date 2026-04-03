package com.blood.Service;

import com.blood.DTO.BloodRequest.*;
import com.blood.Model.*;
import com.blood.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
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
    private BloodRequestRepositoty bloodRequestRepositoty;

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
        bloodRequestRepositoty.save(bloodRequest);

        return "Gửi phiếu thành công";
    }

    public List<ListRequestBloodResponse> getListRequest(String hospitalName, String status){
        List<BloodRequest> bloodRequests = bloodRequestRepositoty.findWithFilters(hospitalName, status);

        return bloodRequests.stream().map(requests -> {
            List<DetailRequest> detailRequests = requests.getRequestDetails().stream().map(details -> {
                DetailRequest dto = new DetailRequest();
                dto.setBloodType(details.getBloodType());
                dto.setProductType(details.getProductType());
                dto.setVolume(details.getVolume());
                dto.setQuantity(details.getQuantity());
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
        List<BloodRequest> bloodRequests = bloodRequestRepositoty.findByHospital_HospitalId(realHospitalId);
        return bloodRequests.stream().map(requests -> {
            List<DetailRequest> detailRequests = requests.getRequestDetails().stream().map(details -> {
                DetailRequest dto = new DetailRequest();
                dto.setBloodType(details.getBloodType());
                dto.setProductType(details.getProductType());
                dto.setVolume(details.getVolume());
                dto.setQuantity(details.getQuantity());
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
        BloodRequest bloodRequest = bloodRequestRepositoty.findById(requestId)
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
        bloodRequestRepositoty.save(bloodRequest);
        return "Cập nhật thành công";
    }

    public String trackOrder(Integer requestId) {
        BloodRequest bloodRequest = bloodRequestRepositoty.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu máu"));

        bloodRequest.setStatus("DA_NHAN");
        bloodRequestRepositoty.save(bloodRequest);
        return "Cập nhật thành công";
    }

    @Transactional
    public String exportBlood(Integer requestId, ExportBloodRequest rq) {
        BloodRequest bloodRequest = bloodRequestRepositoty.findById(requestId)
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

            if (!bloodBag.getStatus().equalsIgnoreCase("SAN_SANG")) {
                throw new RuntimeException("Túi máu " + bagId + " chưa sẵn sàng để đưa vào sử dụng, kiểm tra lại");
            }

            if (bloodBag.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Túi máu " + bagId + " đã quá hạn sử dụng, yêu cầu kiểm tra lại");
            }

            if (bloodBag.getProductType().equalsIgnoreCase("MAU_TOAN_PHAN") && !bloodBag.getStatus().equalsIgnoreCase("CHO_TACH_CHIET")){
                throw new RuntimeException("Túi máu toàn phần " + bloodBag.getBloodBagId() + " có vấn đề");
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

                        bloodBag.setStatus("DA_SU_DUNG");
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
        bloodRequestRepositoty.save(bloodRequest);

        return "Xuất kho thành công";
    }
}
