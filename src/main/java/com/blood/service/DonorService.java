package com.blood.service;

import com.blood.dto.Donor.*;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Profile.UpdateDonorProfileRequest;
import com.blood.model.*;
import com.blood.model.enumformat.EventRegisStatus;
import com.blood.model.enumformat.UserStatus;
import com.blood.repository.DonorRepository;
import com.blood.repository.EventRegistrationRepository;
import com.blood.repository.EventRepository;
import com.blood.repository.TestResultRepository;
import com.blood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorService {

    private final DonorRepository donorRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TestResultRepository testResultRepository;

    public DonorHealthRecordResponse getMyHealthRecord() {
        Users user = getCurrentUser();
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Donor not found"));
        List<EventRegistration> registrations = eventRegistrationRepository.findByDonor_DonorIdOrderByCreatedAtDesc(donor.getDonorId());
        long totalDonation = registrations.stream()
                .filter(reg -> reg.getStatus() == EventRegisStatus.DA_LAY_MAU)
                .count();

        long totalVolumeMl = registrations.stream()
                .filter(reg -> reg.getStatus() == EventRegisStatus.DA_LAY_MAU && reg.getActualVolume() != null)
                .mapToInt(reg -> reg.getActualVolume() != null ? reg.getActualVolume() : 0)
                .sum();
        log.info("Fetching health record for donorId: {}", donor.getDonorId());
        return DonorHealthRecordResponse.builder()
                .bloodType(donor.getBloodType())
                .rhFactor(donor.getRhFactor())
                .totalDonation(totalDonation)
                .totalVolumeMl(totalVolumeMl)
                .build();
    }

    public DonorHistoryResponse getMyHistory() {
        Users user = getCurrentUser();
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Donor not found"));
        List<EventRegistration> registrations = eventRegistrationRepository.findByDonor_DonorIdOrderByCreatedAtDesc(donor.getDonorId());

        List<DonorHistoryResponse.DonationHistory> historyList = new ArrayList<>();
        log.info("Fetching history for donorId: {}", donor.getDonorId());

        for (EventRegistration reg : registrations) {
            if (reg.getStatus() == EventRegisStatus.DA_HUY) continue;

            Events event = reg.getEvents();
            if (event == null) continue;

            historyList.add(DonorHistoryResponse.DonationHistory.builder()
                    .registrationId(reg.getRegistrationId())
                    .donationDate(reg.getEvents().getStartDate())
                    .location(event.getLocation())
                    .volumeMl(reg.getActualVolume() != null ? reg.getActualVolume() : reg.getExpectedVolume())
                    .status(reg.getStatus() != null ? reg.getStatus() : null)
                    .eventName(event.getEventName())
                    .build());
        }

        return DonorHistoryResponse.builder()
                .history(historyList)
                .build();
    }

    public HistoryDetailResponse getHistoryDetail(Integer registrationId) {
        Users user = getCurrentUser();
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Donor not found"));
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký"));

        if (!registration.getDonor().getDonorId().equals(donor.getDonorId())) {
            throw new RuntimeException("Bạn không có quyền xem thông tin này");
        }

        Events event = registration.getEvents();
        HistoryDetailResponse.ScreeningInfo screeningInfo = null;
        log.info("Fetching history detail for registrationId: {} by donorId: {}", registrationId, donor.getDonorId());
        if (registration.getWeight() != null || registration.getHemoglobin() != null) {
            screeningInfo = HistoryDetailResponse.ScreeningInfo.builder()
                    .weight(registration.getWeight() != null ? registration.getWeight().doubleValue() : null)
                    .hemoglobin(registration.getHemoglobin() != null ? registration.getHemoglobin().doubleValue() : null)
                    .bloodPressure(registration.getBloodPressure())
                    .heartRate(registration.getHeartRate())
                    .build();
        }

        HistoryDetailResponse.TestResultInfo testResultInfo = null;
        if (registration.getBloodBag() != null && !registration.getBloodBag().isEmpty()) {
            BloodBag firstBag = registration.getBloodBag().get(0);
            var testResultOpt = testResultRepository.findByBloodBag(firstBag);
            if (testResultOpt.isPresent()) {
                TestResult testResult = testResultOpt.get();
                testResultInfo = HistoryDetailResponse.TestResultInfo.builder()
                        .hiv(testResult.getHiv() != null ? testResult.getHiv().name() : null)
                        .hbv(testResult.getHbv() != null ? testResult.getHbv().name() : null)
                        .hcv(testResult.getHcv() != null ? testResult.getHcv().name() : null)
                        .syphilis(testResult.getSyphilis() != null ? testResult.getSyphilis().name() : null)
                        .malaria(testResult.getMalaria() != null ? testResult.getMalaria().name() : null)
                        .finalConclusion(testResult.getFinalConclusion())
                        .resultDate(testResult.getResultDate())
                        .build();
            }
        }

        return HistoryDetailResponse.builder()
                .registrationId(registration.getRegistrationId())
                .eventName(event != null ? event.getEventName() : null)
                .donationDate(registration.getCreatedAt())
                .location(event != null ? event.getLocation() : null)
                .expectedVolume(registration.getExpectedVolume())
                .actualVolume(registration.getActualVolume())
                .status(registration.getStatus() != null ? registration.getStatus() : null)
                .rejectionReason(registration.getRejectionReason())
                .screeningInfo(screeningInfo)
                .testResultInfo(testResultInfo)
                .build();
    }

    @Transactional
    public String updateDonorForAdmin(Integer id, UpdateDonorForAdminRequest rq) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người hiến với ID: " + id));

        donor.setFullName(rq.getFullName());
        donor.setDob(rq.getDob());
        donor.setGender(rq.getGender());
        donor.setPhone(rq.getPhone());
        donor.setAddress(rq.getAddress());
        donor.setEmail(rq.getEmail());

        donorRepository.save(donor);
        return "Update donor successfully!";
    }

    public String updateDonorProfile (UpdateDonorProfileRequest rq) {
        Users user = getCurrentUser();
        Donor donor = donorRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (rq.getFullName() != null) { donor.setFullName(rq.getFullName()); }
        if (rq.getDob() != null) { donor.setDob(rq.getDob()); }
        if (rq.getGender() != null) { donor.setGender(rq.getGender()); }
        if (rq.getAddress() != null) { donor.setAddress(rq.getAddress()); }
        if (rq.getBloodType() != null) { donor.setBloodType(rq.getBloodType()); }
        if (rq.getRhFactor() != null) { donor.setRhFactor(rq.getRhFactor()); }
        donorRepository.save(donor);
        log.info("Donor profile updated successfully for email: {}", user.getEmail());
        return "Cập nhật thông tin thành công";
    }

    public Page<GetListDonorResponse> getAllDonors(String keyword, String bloodType, String rhFactor, UserStatus status, Pageable pageable) {
        String searchKey = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String type = (bloodType != null && !bloodType.trim().isEmpty()) ? bloodType.trim() : null;
        String rh = (rhFactor != null && !rhFactor.trim().isEmpty()) ? rhFactor.trim().replace(" ", "+") : null;

        Page<Donor> donorPage = donorRepository.findDonorsWithFilters(searchKey, type, rh, status, pageable);
        return donorPage.map(this::convertToDTO);
    }

    public DonorDetailResponse getDonorDetail(Integer id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người hiến máu với ID: " + id));

        long totalDonations = 0;
        LocalDateTime lastDate = null;

        List<EventRegistration> history = donor.getRegistration();
        if (history != null && !history.isEmpty()) {
            List<EventRegistration> completed = history.stream()
                    .filter(reg -> reg.getStatus() == EventRegisStatus.DA_LAY_MAU || reg.getStatus() == EventRegisStatus.HOAN_THANH)
                    .toList();
            totalDonations = completed.size();
            lastDate = completed.stream()
                    .map(reg -> reg.getEvents().getStartDate())
                    .max(LocalDateTime::compareTo).orElse(null);
        }

        boolean hasUnsafeTest = false;
        if (donor.getDonorId() != null) {
            long unsafeCount = testResultRepository.countUnsafeTestResults(donor.getDonorId());
            hasUnsafeTest = (unsafeCount > 0);
        }

        UserStatus userStatus = donor.getUser() != null ? donor.getUser().getStatus() : UserStatus.ACTIVE;
        String displayStatus;
        if (userStatus == UserStatus.INACTIVE) {
            displayStatus = "Tạm khóa";
        } else if (hasUnsafeTest) {
            displayStatus = "Không đủ điều kiện";
        } else if (lastDate == null) {
            displayStatus = "Đủ điều kiện";
        } else {
            LocalDateTime nextEligibleDate = lastDate.plusDays(84);
            if (LocalDateTime.now().isBefore(nextEligibleDate)) {
                displayStatus = "Không đủ điều kiện";
            } else {
                displayStatus = "Đủ điều kiện";
            }
        }

        return DonorDetailResponse.builder()
                .donorId(donor.getDonorId())
                .fullName(donor.getFullName())
                .phone(donor.getPhone())
                .email(donor.getUser() != null ? donor.getUser().getEmail() : "Chưa đăng ký email")
                .userStatus(userStatus)
                .bloodType(donor.getBloodType())
                .rhFactor(donor.getRhFactor())
                .dob(donor.getDob() != null ? donor.getDob().toString() : null)
                .gender(donor.getGender())
                .address(donor.getAddress())
                .totalDonations(totalDonations)
                .lastDonationDate(lastDate)
                .hasUnsafeTest(hasUnsafeTest)
                .displayStatus(displayStatus)
                .build();
    }

    public Page<DonorHistoryResponse.DonationHistory> getDonorHistoryForAdmin(Integer donorId, Pageable pageable) {
        Page<EventRegistration> historyPage = eventRegistrationRepository.findByDonor_DonorIdOrderByEvents_StartDateDesc(donorId, pageable);

        return historyPage.map(reg -> {
            Events event = reg.getEvents();
            return DonorHistoryResponse.DonationHistory.builder()
                    .registrationId(reg.getRegistrationId())
                    .donationDate(event != null && event.getStartDate() != null ? event.getStartDate() : reg.getCreatedAt())
                    .location(event != null ? event.getLocation() : null)
                    .volumeMl(reg.getActualVolume() != null ? reg.getActualVolume() : reg.getExpectedVolume())
                    .status(reg.getStatus())
                    .eventName(event != null ? event.getEventName() : null)
                    .build();
        });
    }

    public GetListDonorResponse convertToDTO(Donor donor) {
        // 1. Lấy email và status TRỰC TIẾP từ bảng Donor
        String email = donor.getEmail();
        UserStatus userStatus = donor.getStatus();

        // (Tùy chọn) Dự phòng: Nếu bảng Donor không có email, thì thử lấy từ bảng User
        if ((email == null || email.trim().isEmpty()) && donor.getUser() != null) {
            email = donor.getUser().getEmail();
        }
        // (Tùy chọn) Dự phòng: Nếu bảng Donor không có status, thì thử lấy từ bảng User
        if (userStatus == null && donor.getUser() != null) {
            userStatus = donor.getUser().getStatus();
        }

        List<EventRegistration> history = donor.getRegistration();
        long totalDonations = 0;
        LocalDateTime lastDonationDate = null;

        if (history != null && !history.isEmpty()) {
            List<EventRegistration> completedEvents = history.stream()
                    .filter(reg -> reg.getStatus() == EventRegisStatus.HOAN_THANH)
                    .toList();

            totalDonations = completedEvents.size();

            lastDonationDate = completedEvents.stream()
                    .map(reg -> reg.getEvents().getStartDate())
                    .max(LocalDateTime::compareTo).orElse(null);
        }

        return new GetListDonorResponse(
                donor.getDonorId(),
                donor.getFullName(),
                donor.getPhone(),
                email != null ? email : "",
                donor.getBloodType(),
                donor.getRhFactor(),
                userStatus,
                totalDonations,
                lastDonationDate
        );
    }
    public List<DonorCheckedInResponse> getListDonorsByStatus(Integer eventId, EventRegisStatus status){
        List<EventRegistration> registrations;
        if (status != null) {
            registrations = eventRegistrationRepository.findByEvents_EventIdAndStatus(eventId, status);
        } else {
            registrations = eventRegistrationRepository.findActiveRegistrationsByEventId(eventId, EventRegisStatus.DA_HUY);
        }

        return registrations.stream().map(reg -> {
            Donor donor = reg.getDonor();
            return DonorCheckedInResponse.builder()
                    .fullName(donor.getFullName())
                    .dob(donor.getDob())
                    .gender(donor.getGender())
                    .phone(donor.getPhone())
                    .address(donor.getAddress())
                    .email(donor.getEmail() != null ? donor.getEmail() : (donor.getUser() != null ? donor.getUser().getEmail() : ""))
                    .status(reg.getStatus() != null ? reg.getStatus().name() : null)
                    .build();
        })
                .collect(Collectors.toList());
    }

    public String callForBloodDonation(String bloodType, String rhFactor) {
        LocalDateTime safeDate = LocalDateTime.now().minusDays(0);

        List<Donor> targetDonors = donorRepository.findEligibleDonorsToCall(bloodType, rhFactor, safeDate);

        if (targetDonors.isEmpty()) {
            return "Không tìm thấy người hiến máu nào đủ điều kiện lúc này!";
        }

        String subject = "KHẨN CẤP: Ngân hàng máu đang cạn kiệt nhóm máu " + bloodType + rhFactor;

        int count = 0;
        for (Donor donor : targetDonors) {
            log.info("Đang gửi mail cho: " + donor.getFullName() + " | Email: " + donor.getEmail() + " | Nhóm máu: " + donor.getBloodType() + donor.getRhFactor());
            if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {
                String body = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Bệnh viện đang rơi vào tình trạng thiếu hụt trầm trọng nhóm máu <strong>%s%s</strong>.</p>
                        <p>Hệ thống ghi nhận bạn đã đủ điều kiện sức khỏe để hiến máu trở lại. Mong bạn bớt chút thời gian đến hỗ trợ chúng tôi cứu người!</p>
                        <p>Để biết thêm thông tin, xin liên hệ Tổng đài chăm sóc người hiến máu: <strong>19000001</strong> hoặc Email: <a href="mailto:hethongmau@gmail.com">hethongmau@gmail.com</a>.</p>
                        <p>Trân trọng.</p>
                    </div>
                    """.formatted(donor.getFullName(), bloodType, rhFactor);
                emailService.sendEmail(donor.getEmail(), subject, body);
                count++;
            }
        }

        return "Đã phát lệnh gửi " + count + " email kêu gọi thành công!";
    }



    private Users getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
