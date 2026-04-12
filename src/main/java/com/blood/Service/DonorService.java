package com.blood.Service;

import com.blood.DTO.Donor.DonorCheckedInResponse;
import com.blood.DTO.Donor.DonorResponse;
import com.blood.DTO.Donor.GetListDonorResponse;
import com.blood.DTO.Donor.VisitorRegistRequest;
import com.blood.DTO.Profile.UpdateDonorProfileRequest;
import com.blood.Model.*;
import com.blood.Repository.DonorRepository;
import com.blood.Repository.EventRegistrationRepository;
import com.blood.Repository.EventRepository;
import com.blood.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EmailService emailService;

    public String updateDonorProfile (String email, UpdateDonorProfileRequest rq) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Donor donor = donorRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (rq.getFullName() != null) { donor.setFullName(rq.getFullName()); }
        if (rq.getDob() != null) { donor.setDob(rq.getDob()); }
        if (rq.getGender() != null) { donor.setGender(rq.getGender()); }
        if (rq.getAddress() != null) { donor.setAddress(rq.getAddress()); }
        if (rq.getBloodType() != null) { donor.setBloodType(rq.getBloodType()); }
        if (rq.getRhFactor() != null) { donor.setRhFactor(rq.getRhFactor()); }
        donorRepository.save(donor);

        return "Cập nhật thông tin thành công";
    }

    public List<GetListDonorResponse> getAllDonors(){
        List <Donor> donors = donorRepository.findAll();
        return donors.stream().map(donor -> convertToDTO(donor)).collect(Collectors.toList());
    }

    public GetListDonorResponse convertToDTO(Donor donor){
        String email = "";
        UserStatus userStatus = UserStatus.ACTIVE;
        if (donor.getUser() != null) {
            email = donor.getUser().getEmail();
            userStatus = donor.getStatus();
        }

        List <EventRegistration> history = donor.getRegistration();
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
                email,
                donor.getBloodType(),
                donor.getRhFactor(),
                donor.getUser().getStatus(),
                totalDonations,
                lastDonationDate
        );
    }

    public List<DonorCheckedInResponse> getListDonorsByStatus(Integer eventId, String status){
        List<EventRegistration> registrations = eventRegistrationRepository.findByEvents_EventIdAndStatus(eventId, status);

        return registrations.stream().map(reg -> {
            Donor donor = reg.getDonor();
            return DonorCheckedInResponse.builder()
                    .fullName(donor.getFullName())
                    .dob(donor.getDob())
                    .gender(donor.getGender())
                    .phone(donor.getPhone())
                    .address(donor.getAddress())
                    .build();
        })
                .collect(Collectors.toList());
    }

    @Transactional
    public String registForVisitor(Integer eventId, VisitorRegistRequest rq) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));

        Donor donor = donorRepository.findByCccd(rq.getCccd());

        if (donor == null) {
            donor = new Donor();
            donor.setFullName(rq.getFullName());
            donor.setCccd(rq.getCccd());
            donor.setEmail(rq.getEmail());
            donor.setDob(rq.getDob());
            donor.setGender(rq.getGender());
            donor.setPhone(rq.getPhone());
            donor.setAddress(rq.getAddress());
            if (rq.getBloodType() != null && rq.getBloodType().trim().isEmpty()) {
                donor.setBloodType(null);
            }

            if (rq.getRhFactor() != null && rq.getRhFactor().trim().isEmpty()) {
                donor.setRhFactor(null);
            }
            donor.setStatus(UserStatus.KHACH_VANG_LAI);
            donor = donorRepository.save(donor);
        } else {
            donor.setPhone(rq.getPhone());
            donor.setAddress(rq.getAddress());
            donor.setEmail(rq.getEmail());
            donor =  donorRepository.save(donor);
        }

        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setDonor(donor);
        eventRegistration.setEvents(event);
        eventRegistration.setStatus(EventRegisStatus.CHO_KHAM);
        eventRegistration = eventRegistrationRepository.save(eventRegistration);

        if (rq.getEmail() != null && !rq.getEmail().isEmpty()) {
            emailService.sendEmail(rq.getEmail(), "THƯ CẢM ƠN", "Ban tổ chức xin chân thành cảm ơn bạn đã đóng góp cho cộng đồng");
        } else {
            System.out.println("DONG BAO: Khong gui mail vi Email bi NULL hoac Rong!");
        }

        return "Đăng ký thành công";
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
            System.out.println("Đang gửi mail cho: " + donor.getFullName() + " | Email: " + donor.getEmail() + " | Nhóm máu: " + donor.getBloodType() + donor.getRhFactor());
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
}
