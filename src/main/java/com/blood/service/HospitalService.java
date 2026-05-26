package com.blood.service;

import com.blood.dto.BloodRequest.BloodRequestSummaryResponse;
import com.blood.dto.BloodRequest.DetailRequest;
import com.blood.dto.BloodRequest.RequestDetailResponse;
import com.blood.dto.BloodRequest.ListRequestBloodResponse;
import com.blood.dto.Hospital.UpdateHospitalForAdminRequest;
import com.blood.dto.Hospital.HospitalDashboardStatResponse;
import com.blood.model.BloodRequest;
import com.blood.model.RequestDetail;
import com.blood.model.enumformat.BloodRequestStatus;
import com.blood.repository.BloodRequestRepository;
import com.blood.repository.RequestDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Hospital.HospitalResponse;
import com.blood.dto.Profile.UpdateHospitalProfileRequest;
import com.blood.model.Hospital;
import com.blood.model.enumformat.UserStatus;
import com.blood.model.Users;
import com.blood.repository.HospitalRepository;
import com.blood.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final RequestDetailRepository requestDetailRepository;

    public Page<HospitalResponse> getAllHospital(String keyword, UserStatus status, Pageable pageable) {
        String searchKey = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<Hospital> hospitalPage = hospitalRepository.findHospitalsWithFilters(searchKey, status, pageable);

        return hospitalPage.map(this::convertToDTO);
    }

    public HospitalResponse convertToDTO(Hospital hospital) {
        String email = "";
        UserStatus userStatus = UserStatus.ACTIVE;
        if (hospital.getUser() != null) {
            email = hospital.getUser().getEmail();
            userStatus = hospital.getUser().getStatus();
        }

        return HospitalResponse.builder()
                .hospitalId(hospital.getHospitalId())
                .hospitalName(hospital.getHospitalName())
                .address(hospital.getAddress())
                .hotline(hospital.getHotline())
                .email(email)
                .status(userStatus)
                .build();
    }

    public HospitalResponse getHospitalDetail(Integer hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return HospitalResponse.builder()
                .hospitalId(hospital.getHospitalId())
                .hospitalName(hospital.getHospitalName())
                .address(hospital.getAddress())
                .hotline(hospital.getHotline())
                .email(hospital.getUser().getEmail())
                .status(hospital.getUser().getStatus())
                .build();
    }

    @Transactional
    public String updateHospitalForAdmin(Integer hospitalId, UpdateHospitalForAdminRequest rq) {
        Hospital hospital = hospitalRepository.findById(hospitalId).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        hospital.setHospitalName(rq.getHospitalName());
        hospital.setAddress(rq.getAddress());
        hospital.setHotline(rq.getHotline());
        hospital.getUser().setEmail(rq.getEmail());

        hospitalRepository.save(hospital);
        return "Update hospital successfully!";
    }

    public void updateHospitalProfile (String email, UpdateHospitalProfileRequest rq) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Hospital hospital = hospitalRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (rq.getHospitalName() != null) { hospital.setHospitalName(rq.getHospitalName()); }
        if (rq.getAddress() != null) { hospital.setAddress(rq.getAddress()); }
        if (rq.getHotline() != null) { hospital.setHotline(rq.getHotline()); }

        hospitalRepository.save(hospital);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", hospital);
    }

    public Page<BloodRequestSummaryResponse> getRequestHistory(Integer hospitalId, Pageable pageable) {
        Page<BloodRequest> requests = bloodRequestRepository
                .findByHospital_HospitalIdOrderByRequestDateDesc(hospitalId, pageable);

        return requests.map(req -> {
            List<RequestDetail> detailEntities = requestDetailRepository.findByBloodRequest_RequestId(req.getRequestId());
            int total = detailEntities.stream().mapToInt(RequestDetail::getQuantity).sum();

            return BloodRequestSummaryResponse.builder()
                    .requestId(req.getRequestId())
                    .priority(req.getPriority())
                    .deadlineDate(req.getDeadlineDate())
                    .requestedDate(req.getRequestDate())
                    .status(req.getStatus())
                    .totalQuantity(total)
                    .build();
        });
    }

    public HospitalDashboardStatResponse hospitalStat() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Users currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không xác định danh tính người dùng"));
        Hospital currentHospital = hospitalRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Tài khoản đang sử dụng không đúng của bệnh viện"));
        Integer hospitalId = currentHospital.getHospitalId();

        long pendingCount = bloodRequestRepository.countByHospital_HospitalIdAndStatus(hospitalId, BloodRequestStatus.CHO_DUYET);
        long transitCount = bloodRequestRepository.countByHospital_HospitalIdAndStatus(hospitalId, BloodRequestStatus.DANG_VAN_CHUYEN);

        List<BloodRequest> recentRequests = bloodRequestRepository.findTop3ByHospital_HospitalIdOrderByRequestDateDesc(hospitalId);

        List<ListRequestBloodResponse> recentResponses = recentRequests.stream().map(req -> {
            List<DetailRequest> detailRequests = req.getRequestDetails().stream().map(details -> {
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
                    .requestId(req.getRequestId())
                    .hospitalName(req.getHospital().getHospitalName())
                    .deadlineDate(req.getDeadlineDate())
                    .priority(req.getPriority())
                    .requestedDate(req.getRequestDate())
                    .status(req.getStatus())
                    .detailRequests(detailRequests)
                    .build();
        }).collect(Collectors.toList());

        return HospitalDashboardStatResponse.builder()
                .pendingApprovalCount(pendingCount)
                .inTransitCount(transitCount)
                .recentRequests(recentResponses)
                .build();
    }
}
