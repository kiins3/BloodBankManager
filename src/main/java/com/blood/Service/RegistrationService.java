package com.blood.Service;

import com.blood.DTO.Blood.DonationRequest;
import com.blood.DTO.Donor.DonorResponse;
import com.blood.DTO.Event.*;
import com.blood.Model.*;
import com.blood.Repository.*;
import com.blood.helper.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class RegistrationService {
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private BloodBagRepository bloodBagRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    //Dang ky tham gia
    @Transactional
    public String registration(Integer eventId, Integer donorId) {
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));

        if (!"SAP_TOI".equals(event.getStatus())){
            throw new RuntimeException(("Sự kiện chưa mở"));
        }

        boolean alreadyRegistered = eventRegistrationRepository.existsByEvents_EventIdAndDonor_DonorId(eventId, donorId);
        if (alreadyRegistered) {
            throw new RuntimeException("Bạn đã đăng ký tham gia rồi");
        }

        int currentCount = eventRegistrationRepository.countByEvents_EventIdAndStatus(eventId, "DA_DANG_KY");
        if (currentCount >= event.getTargetAmount()){
            throw new RuntimeException("Sự kiện này đã hết lượt đăng ký");
        }

        Donor donor = donorRepository.findById(donorId).orElseThrow(() -> new RuntimeException("Không tìm thấy người hiến"));

        String generatedCode = "HM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setEvents(event);
        eventRegistration.setDonor(donor);
        eventRegistration.setTicketCode(generatedCode);
        eventRegistration.setCreatedAt(LocalDateTime.now());
        eventRegistration.setStatus("DA_DANG_KY");

        eventRegistrationRepository.save(eventRegistration);

        if (donor.getEmail() != null && !donor.getEmail().isEmpty()){
            String subject = "ĐĂNG KÝ THAM GIA HIẾN MÁU THÀNH CÔNG";

            String base64QrCode = QRCodeGenerator.generateQRCode(eventRegistration.getTicketCode(), 250, 250);

            String body = "<html><body>"
                    + "<p>Cảm ơn bạn đã đăng ký tham gia chương trình hiến máu nhân đạo, dưới đây là thông tin vé của bạn:</p>"
                    + "<ul>"
                    + "<li><b>Chiến dịch:</b> " + eventRegistration.getEvents().getEventName() + "</li>"
                    + "<li><b>Thời gian:</b> " + eventRegistration.getEvents().getStartDate() + "</li>"
                    + "<li><b>Địa điểm:</b> " + eventRegistration.getEvents().getLocation() + "</li>"
                    + "</ul>"
                    + "<p><b>Vé của bạn:</b></p>"
                    // Nhúng ảnh Base64 vào HTML
                    + "<img src=\"data:image/png;base64," + base64QrCode + "\" alt=\"QR Code\" />"
                    + "<p>Khi tới bạn hãy nhớ đem theo mã QR này hoặc CCCD để xác nhận thông tin.</p>"
                    + "<p>Chúng tôi xin chân thành cảm ơn.</p>"
                    + "</body></html>";

            emailService.sendEmail(donor.getEmail(), subject, body);
        }

        return "Đăng ký thành công";
    }

    public List<TicketSummaryResponse> getAllMyTickets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Users currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không xác nhận được danh tính người dùng"));

        Donor currentDonor = donorRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Tài khoản chưa được liên kết với hồ sơ hiến máu"));

        List<EventRegistration> registrations = eventRegistrationRepository.findByDonor_DonorIdOrderByCreatedAtDesc(currentDonor.getDonorId());
        return registrations.stream().map(reg -> TicketSummaryResponse.builder()
                .eventId(reg.getEvents().getEventId())
                .eventName(reg.getEvents().getEventName())
                .startDate(reg.getEvents().getStartDate())
                .status(reg.getEvents().getStatus())
                .build()).collect(Collectors.toList());
    }

    //Xem chi tiet ve da dang ky
    public TicketResponse getMyTickets(Integer eventId, Integer donorId) {
        EventRegistration registration = eventRegistrationRepository.findByEvents_EventIdAndDonor_DonorId(eventId, donorId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký tham gia sự kiện"));

        String code = registration.getTicketCode();
        String qrCode = QRCodeGenerator.generateQRCode(code, 250, 250);

        return TicketResponse.builder()
                .eventName(registration.getEvents().getEventName())
                .ticketCode(code)
                .qrCode(qrCode)
                .donorName(registration.getDonor().getFullName())
                .startDate(registration.getEvents().getStartDate())
                .endDate(registration.getEvents().getEndDate())
                .status(registration.getStatus())
                .build();
    }

    public List<DonorResponse> getAllDonorsOfEvent(Integer eventId) {
        Events events = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));
        List<EventRegistration> registrations = eventRegistrationRepository.findByEvents_EventId(eventId);
        return registrations.stream().map(reg -> {
            Donor donor = reg.getDonor();
            return DonorResponse.builder()
                    .regisId(reg.getRegistrationId())
                    .donorId(donor.getDonorId())
                    .ticketCode(reg.getTicketCode())
                    .fullName(donor.getFullName())
                    .gender(donor.getGender())
                    .dob(donor.getDob())
                    .phone(donor.getPhone())
                    .address(donor.getAddress())
                    .status(reg.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    //Checkin tai quay
    @Transactional
    public DonorResponse checkin(Integer eventId, String ticketCode) {
        EventRegistration eventRegistration = eventRegistrationRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("Vé không tồn tại"));

        Events event = eventRegistration.getEvents();

        if (!event.getEventId().equals(eventId)){
            throw new RuntimeException("Vé hợp lệ nhưng không đúng sự kiện");
        }

        if (eventRegistration.getStatus().equals("CHO_KHAM")){
            throw new RuntimeException("Vé đã được sử dụng");
        }
        if (eventRegistration.getStatus().equals("DA_HUY")){
            throw new RuntimeException("Vé đã hủy");
        }
        if (!eventRegistration.getStatus().equals("DA_DANG_KY")){
            throw new RuntimeException("Vé không hợp lệ");
        }

        if (event.getStartDate().isAfter(LocalDateTime.now())){
            throw new RuntimeException("Sự kiện chưa diễn ra");
        }

        if (event.getEndDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Sự kiện đã kết thúc");
        }

        eventRegistration.setStatus("CHO_KHAM");
        eventRegistrationRepository.save(eventRegistration);

        return DonorResponse.builder()
                .regisId(eventRegistration.getRegistrationId())
                .donorId(eventRegistration.getDonor().getDonorId())
                .ticketCode(eventRegistration.getTicketCode())
                .fullName(eventRegistration.getDonor().getFullName())
                .gender(eventRegistration.getDonor().getGender())
                .dob(eventRegistration.getDonor().getDob())
                .address(eventRegistration.getDonor().getAddress())
                .phone(eventRegistration.getDonor().getPhone())
                .status(eventRegistration.getStatus())
                .build();
    }

    //Kham sang loc
    public String saveScreeningResult(Integer regisId, ScreeningRequest rq) {
        EventRegistration eventRegistration = eventRegistrationRepository.findById(regisId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đăng ký"));

        if (!eventRegistration.getDonor().getDonorId().equals(rq.getDonorId())) {
            throw new RuntimeException("Lỗi sai lệch thông tin người hiến");
        }

        if (!eventRegistration.getStatus().equals("CHO_KHAM")){
            throw new RuntimeException("Người hiến chưa checkin hoặc đã khám");
        }

        eventRegistration.setWeight(rq.getWeight());
        eventRegistration.setHemoglobin(rq.getHemoglobin());
        eventRegistration.setHeartRate(rq.getHeartRate());
        eventRegistration.setExpectedVolume(rq.getExpectedVolume());
        eventRegistration.setBloodPressure(rq.getBloodPressure());
        eventRegistration.setRejectionReason(rq.getRejectionReason());

        if (rq.getIsEligible()) {
            eventRegistration.setStatus("DONG_Y");
        } else {
            eventRegistration.setStatus("TU_CHOI");
        }

        eventRegistrationRepository.save(eventRegistration);

        return "Đã lưu kết quả";
    }

    public Integer calculateNominalVolume(int expectedVolume, int actualVolume) {
        if (actualVolume >= (expectedVolume * 0.9)) {
            return expectedVolume;
        }

        if (expectedVolume == 350) {
            if (actualVolume >= 250 && actualVolume < 315) {
                return 250;
            } else if (actualVolume < 250) {
                throw new RuntimeException("Lượng máu quá ít, không đạt tiêu chuẩn để lưu kho. Yêu cầu hủy túi máu!");
            }
        }

        if (expectedVolume == 450) {
            if (actualVolume >= 350 && actualVolume < 405) {
                return 350;
            } else if (actualVolume >= 250 && actualVolume < 350) {
                return 250;
            } else {
                throw new RuntimeException("Lượng máu quá ít, không đạt tiêu chuẩn để lưu kho. Yêu cầu hủy túi máu!");
            }
        }

        return actualVolume;
    }

    //Lay mau
    @Transactional
    public String donateBlood(Integer regisId, DonationRequest rq) {
        EventRegistration registration = eventRegistrationRepository.findById(regisId)
                .orElseThrow(() -> new RuntimeException("Vé đăng ký không hợp lệ"));

        if (!registration.getStatus().equals("DONG_Y")) {
            throw new RuntimeException("Không đủ điều kiện hiến máu hoặc chưa khám");
        }

        int realVolume = calculateNominalVolume(registration.getExpectedVolume(), rq.getActualVolume());
        if (rq.getIsSuccess().equals(true)) {
            BloodBag newBag = new BloodBag();
            newBag.setRegistration(registration);
            newBag.setCollectedAt(LocalDateTime.now());
            newBag.setProductType("MAU_TOAN_PHAN");
            newBag.setStorageEquipment(null);
            newBag.setVolume(realVolume);
            newBag.setStatus("CHO_XET_NGHIEM");
            bloodBagRepository.save(newBag);

            registration.setStatus("DA_HIEN");
            registration.setActualVolume(rq.getActualVolume());
            eventRegistrationRepository.save(registration);
        } else if (rq.getIsSuccess().equals(false)) {
            return "Lấy máu thất bại";
        }
        return "Lấy máu thành công";
    }
}
