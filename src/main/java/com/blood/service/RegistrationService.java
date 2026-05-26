package com.blood.service;

import com.blood.dto.Donor.VisitorRegisRequest;
import com.blood.model.enumformat.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Blood.DonationRequest;
import com.blood.dto.Donor.DonorResponse;
import com.blood.dto.Event.*;
import com.blood.model.*;
import com.blood.repository.*;
import com.blood.helper.QRCodeGenerator;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final DonorRepository donorRepository;
    private final BloodBagRepository bloodBagRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    //Dang ky tham gia
    @Transactional
    public String registration(Integer eventId) {
        Users user = getCurrentUser();
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));
        Donor donor = donorRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy người hiến"));

        if (donor.getBlockBookingUntil() != null && donor.getBlockBookingUntil().isAfter(LocalDateTime.now())) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), donor.getBlockBookingUntil());
            throw new RuntimeException("Tài khoản của bạn đang bị tạm khóa chức năng đặt lịch trực tuyến do vi phạm chính sách hủy lịch quá 3 lần/tháng. Vui lòng thử lại sau " + (daysLeft + 1) + " ngày hoặc đến đăng ký trực tiếp tại quầy vãng lai.");
        }

        if (donor.getDob() == null || donor.getGender() == null || donor.getAddress() == null ||
                donor.getAddress().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng cập nhật đầy đủ thông tin cá nhân trước khi đăng ký hiến máu.");
        }

        LocalDate birthDate = donor.getDob();
        LocalDate eventDate = event.getStartDate().toLocalDate();
        long age = java.time.temporal.ChronoUnit.YEARS.between(birthDate, eventDate);

        if (age < 18) {
            throw new RuntimeException("Chưa đủ điều kiện: Bạn phải đủ 18 tuổi tính đến ngày diễn ra sự kiện (" + event.getStartDate().toLocalDate() + ") mới được tham gia hiến máu.");
        }

        LocalDateTime startOfDay = event.getStartDate().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        boolean hasSameDayEvent = eventRegistrationRepository.existsByDonor_DonorIdAndStatusAndEvents_StartDateBetween(
                donor.getDonorId(),
                EventRegisStatus.DA_DANG_KY,
                startOfDay,
                endOfDay
        );

        LocalDateTime checkStart = event.getStartDate().minusDays(84);
        LocalDateTime checkEnd = event.getEndDate().plusDays(84);

        List<EventRegisStatus> activeStatuses = Arrays.asList(
                EventRegisStatus.DA_DANG_KY,
                EventRegisStatus.DA_LAY_MAU
        );

        boolean violate84DaysRule = eventRegistrationRepository.hasRegistrationWithin84Days(
                donor.getDonorId(), activeStatuses, checkStart, checkEnd
        );

        if (violate84DaysRule) {
            throw new RuntimeException("Không đủ điều kiện sức khỏe: Khoảng cách giữa 2 lần hiến máu tối thiểu là 84 ngày. Bạn đã có lịch hẹn hoặc đã hiến máu trong khoảng thời gian này.");
        }

        if (hasSameDayEvent) {
            throw new RuntimeException("Bạn đã đăng ký một sự kiện khác diễn ra trong cùng ngày này. Để đảm bảo sức khỏe, bạn không thể tham gia 2 nơi.");
        }

        if (event.getStatus() != EventStatus.SAP_TOI && event.getStatus() != EventStatus.DANG_MO) {
            throw new RuntimeException(("Sự kiện chưa mở"));
        }

        boolean alreadyRegistered = eventRegistrationRepository.existsByEvents_EventIdAndDonor_DonorIdAndStatusNot(eventId, donor.getDonorId(), EventRegisStatus.DA_HUY);
        if (alreadyRegistered) {
            throw new RuntimeException("Bạn đã đăng ký tham gia rồi");
        }

        int currentCount = eventRegistrationRepository.countByEvents_EventIdAndStatus(eventId, EventRegisStatus.DA_DANG_KY);
        if (currentCount >= event.getTargetAmount()){
            throw new RuntimeException("Sự kiện này đã hết lượt đăng ký");
        }

        String generatedCode = "HM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setEvents(event);
        eventRegistration.setDonor(donor);
        eventRegistration.setTicketCode(generatedCode);
        eventRegistration.setCreatedAt(LocalDateTime.now());
        eventRegistration.setStatus(EventRegisStatus.DA_DANG_KY);

        eventRegistrationRepository.save(eventRegistration);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", eventRegistration);

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
                    + "<img src=\"cid:qrcode\" alt=\"QR Code\" />"
                    + "<p>Khi tới bạn hãy nhớ đem theo mã QR này (hoặc mở trong trang cá nhân) hoặc CCCD để xác nhận thông tin.</p>"
                    + "<p>Chúng tôi xin chân thành cảm ơn.</p>"
                    + "</body></html>";

            emailService.sendEmailWithQrCode(donor.getEmail(), subject, body,base64QrCode,"qrcode");
        }

        try {
            NotiMessDonorCheckIn noti = new NotiMessDonorCheckIn(
                    eventRegistration.getDonor().getFullName(),
                    eventRegistration.getTicketCode(),
                    "Có người đăng ký mới. Đang chờ checkin!"
            );

            String topicDestination = "/topic/event/" + eventId + "/new-visitor";
            messagingTemplate.convertAndSend(topicDestination, noti);

            log.info("Đã phát thông báo WebSocket có khách mới đăng ký online{}", eventId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi WebSocket thông báo khách: {}", e.getMessage());
        }

        return "Đăng ký thành công";
    }

    //HUY DANG KY
    @Transactional
    public String cancelRegistration(Integer registrationId) {
        Users user = getCurrentUser();
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký"));

        if (!registration.getDonor().getDonorId().equals(user.getDonor().getDonorId())) {
            throw new RuntimeException("Bạn không có quyền thao tác trên vé này");
        }

        if (registration.getStatus() == EventRegisStatus.DA_HUY) {
            throw new RuntimeException("Vé này đã được hủy trước đó");
        }

        Events event = registration.getEvents();
        if (event.getStatus() == EventStatus.DA_DONG) {
            throw new RuntimeException("Sự kiện đã kết thúc, không thể hủy");
        }

        registration.setStatus(EventRegisStatus.DA_HUY);
        eventRegistrationRepository.save(registration);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", registration);

        if (user.getDonor().getStatus() != UserStatus.KHACH_VANG_LAI) {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            long cancelCount = eventRegistrationRepository.countCancellationsInTimeRange(
                    user.getDonor().getDonorId(),
                    EventRegisStatus.DA_HUY,
                    thirtyDaysAgo
            );

            if (cancelCount >= 3) {
                user.getDonor().setBlockBookingUntil(LocalDateTime.now().plusDays(30));
                donorRepository.save(user.getDonor());
                log.warn("PENALTY: Khóa quyền đăng ký của Donor ID: {} trong 30 ngày do hủy {} lần.", user.getDonor().getDonorId(), cancelCount);

                return "Hủy đăng ký thành công. Tài khoản của bạn đã bị tạm khóa chức năng đăng ký trước 30 ngày do hủy lịch quá 3 lần trong tháng.";
            } else if (cancelCount == 2) {
                return "Hủy đăng ký thành công. Cảnh báo: Bạn đã hủy lịch 2 lần trong vòng 30 ngày qua. Nếu hủy thêm 1 lần nữa, tài khoản sẽ bị khóa quyền đăng ký trước trong 30 ngày.";
            }
        }

        try {
            NotiMessDonorCheckIn noti = new NotiMessDonorCheckIn(
                    registration.getDonor().getFullName(),
                    registration.getTicketCode(),
                    "Có người dùng vừa hủy đăng ký!"
            );

            String topicDestination = "/topic/event/" + registration.getEvents().getEventId() + "/new-visitor";
            messagingTemplate.convertAndSend(topicDestination, noti);

            log.info("Đã phát thông báo WebSocket có khách mới hủy đăng ký {}", registration.getEvents().getEventId());
        } catch (Exception e) {
            log.error("Lỗi khi gửi WebSocket thông báo khách: {}", e.getMessage());
        }

        return "Hủy đăng ký thành công";
    }

    public List<TicketSummaryResponse> getAllMyTickets(EventRegisStatus status) {

        Users currentUser = getCurrentUser();
        Donor currentDonor = donorRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Tài khoản chưa được liên kết với hồ sơ hiến máu"));

        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        List<EventRegistration> registrations;
        if (status != null) {
            registrations = eventRegistrationRepository.findValidTicketsByDonorIdWithStatus(
                    currentDonor.getDonorId(), oneMonthAgo, status);
        } else {
            registrations = eventRegistrationRepository.findValidTicketsByDonorId(
                    currentDonor.getDonorId(), oneMonthAgo);
        }

        return registrations.stream()
                .map(reg -> TicketSummaryResponse.builder()
                        .registrationId(reg.getRegistrationId())
                        .eventId(reg.getEvents().getEventId())
                        .eventName(reg.getEvents().getEventName())
                        .startDate(reg.getEvents().getStartDate())
                        .status(reg.getEvents().getStatus())
                        .registrationStatus(reg.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    //Xem chi tiet ve da dang ky
    public TicketResponse getMyTicketDetail(Integer eventId) {
        Users user = getCurrentUser();
        EventRegistration registration = eventRegistrationRepository
                .findByEvents_EventIdAndDonor_DonorIdAndStatusNot(eventId, user.getDonor().getDonorId(), EventRegisStatus.DA_HUY)
                .orElseThrow(() -> new RuntimeException("Bạn chưa có vé hợp lệ cho sự kiện này (hoặc vé đã bị hủy)"));

        String code = registration.getTicketCode();
        String qrCode = QRCodeGenerator.generateQRCode(code, 250, 250);

        return TicketResponse.builder()
                .eventName(registration.getEvents().getEventName())
                .ticketCode(code)
                .qrCode(qrCode)
                .location(registration.getEvents().getLocation())
                .donorName(registration.getDonor().getFullName())
                .startDate(registration.getEvents().getStartDate())
                .endDate(registration.getEvents().getEndDate())
                .status(registration.getStatus())
                .build();
    }

    public List<DonorResponse> getAllDonorsOfEvent(Integer eventId) {
        eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));
        List<EventRegistration> registrations = eventRegistrationRepository
                .findActiveRegistrationsByEventId(eventId, EventRegisStatus.DA_HUY);
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

        if (eventRegistration.getStatus() == EventRegisStatus.CHO_KHAM){
            throw new RuntimeException("Vé đã được sử dụng");
        }
        if (eventRegistration.getStatus() == EventRegisStatus.DA_HUY){
            throw new RuntimeException("Vé đã hủy");
        }
        if (eventRegistration.getStatus() != EventRegisStatus.DA_DANG_KY){
            throw new RuntimeException("Vé không hợp lệ");
        }

        if (event.getStartDate().isAfter(LocalDateTime.now())){
            throw new RuntimeException("Sự kiện chưa diễn ra");
        }

        if (event.getEndDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Sự kiện đã kết thúc");
        }

        eventRegistration.setStatus(EventRegisStatus.CHO_KHAM);
        eventRegistrationRepository.save(eventRegistration);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", eventRegistration);

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

    //dang ky cho khach vang lai
    @Transactional
    public String regisForVisitor(Integer eventId, VisitorRegisRequest rq) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));

        if (rq.getDob() == null) {
            throw new RuntimeException("Vui lòng nhập ngày tháng năm sinh của khách vãng lai.");
        }

        LocalDate birthDate = rq.getDob();
        LocalDate eventDate = event.getStartDate().toLocalDate();

        long age = java.time.temporal.ChronoUnit.YEARS.between(birthDate, eventDate);

        if (age < 18) {
            throw new RuntimeException("Đăng ký thất bại! Khách hàng chưa đủ 18 tuổi đăng ký hiến máu (Hiện tại: " + age + " tuổi).");
        }

        Donor donorByCccd = donorRepository.findByCccd(rq.getCccd());
        Donor donorByPhone = donorRepository.findByPhone(rq.getPhone());

        if (donorByCccd != null && donorByPhone != null && !donorByCccd.getDonorId().equals(donorByPhone.getDonorId())) {
            throw new RuntimeException("Số điện thoại này đã được đăng ký cho một CCCD khác trong hệ thống!");
        }

        Donor donor = (donorByCccd != null) ? donorByCccd : (donorByPhone != null ? donorByPhone : null);

        if (donor == null) {
            donor = new Donor();
            donor.setCccd(rq.getCccd());
            donor.setPhone(rq.getPhone());
            donor.setStatus(UserStatus.KHACH_VANG_LAI);
        } else {
            if (donor.getCccd() == null) donor.setCccd(rq.getCccd());
            if (donor.getPhone() == null) donor.setPhone(rq.getPhone());
            if (donor.getStatus() == UserStatus.INACTIVE) {
                donor.setStatus(UserStatus.KHACH_VANG_LAI);
            }
        }

        donor.setFullName(rq.getFullName());
        donor.setDob(rq.getDob());
        donor.setGender(rq.getGender());
        donor.setAddress(rq.getAddress());
        donor.setEmail(rq.getEmail());
        if (rq.getBloodType() != null && !rq.getBloodType().trim().isEmpty()) donor.setBloodType(rq.getBloodType());
        if (rq.getRhFactor() != null && !rq.getRhFactor().trim().isEmpty()) donor.setRhFactor(rq.getRhFactor());

        donor = donorRepository.save(donor);

        boolean isRegistered = eventRegistrationRepository.existsByEvents_EventIdAndDonor_DonorIdAndStatusNot(
                eventId,
                donor.getDonorId(),
                EventRegisStatus.DA_HUY
        );

        if (isRegistered) {
            throw new RuntimeException("Khách có CCCD " + rq.getCccd() + " hoặc số điện thoại " + rq.getPhone() + " đã đăng ký sự kiện này và đang trong hàng đợi!");
        }

        LocalDateTime checkStart = event.getStartDate().minusDays(84);
        LocalDateTime checkEnd = event.getEndDate().plusDays(84);
        List<EventRegisStatus> activeStatuses = Arrays.asList(EventRegisStatus.DA_DANG_KY, EventRegisStatus.DA_LAY_MAU);

        boolean violate84DaysRule = eventRegistrationRepository.hasRegistrationWithin84Days(
                donor.getDonorId(), activeStatuses, checkStart, checkEnd
        );
        if (violate84DaysRule) {
            throw new RuntimeException("Không đủ điều kiện: Khách này đã hiến máu hoặc có lịch hẹn trong vòng 84 ngày qua!");
        }

        String generatedCode = "HM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setDonor(donor);
        eventRegistration.setEvents(event);
        eventRegistration.setTicketCode(generatedCode);
        eventRegistration.setStatus(EventRegisStatus.CHO_KHAM);
        eventRegistrationRepository.save(eventRegistration);

        if (rq.getEmail() != null && !rq.getEmail().trim().isEmpty()) {
            try {
                emailService.sendEmail(rq.getEmail(), "THƯ CẢM ƠN", "Ban tổ chức xin chân thành cảm ơn bạn đã đóng góp cho cộng đồng");
            } catch (Exception e) {
                log.error("DONG BAO: Lỗi gửi mail cho {} nhưng vẫn tiếp tục transaction", rq.getEmail(), e);
            }
        } else {
            log.info("DONG BAO: Không gửi mail vì Email rỗng!");
        }

        try {
            String bloodTypeStr = (donor.getBloodType() != null) ? donor.getBloodType() : "Chưa rõ";
            VisitorNotiMess noti = new VisitorNotiMess(
                    donor.getFullName(),
                    eventRegistration.getTicketCode(),
                    bloodTypeStr,
                    "Có khách vãng lai mới vừa đăng ký. Đang chờ khám!"
            );

            String topicDestination = "/topic/event/" + eventId + "/new-visitor";
            messagingTemplate.convertAndSend(topicDestination, noti);

            log.info("Đã phát thông báo WebSocket có khách mới tại Event {}", eventId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi WebSocket thông báo khách vãng lai: {}", e.getMessage());
        }

        return "Đăng ký thành công";
    }

    //Huy dang ky khach vang lai (chi cho phep huy khi chua duoc kham/huy tu cho)
    @Transactional
    public String cancelVisitorRegistration(Integer registrationId) {
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đăng ký"));

        if (registration.getStatus() == EventRegisStatus.DA_HUY) {
            throw new RuntimeException("Phiếu đăng ký đã được hủy trước đó");
        }

        if (registration.getStatus() != EventRegisStatus.CHO_KHAM) {
            throw new RuntimeException("Không thể hủy phiếu đăng ký đã được xử lý");
        }

        Events event = registration.getEvents();
        if (event.getStatus() == EventStatus.DA_DONG) {
            throw new RuntimeException("Sự kiện đã kết thúc, không thể hủy");
        }

        Donor donor = registration.getDonor();
        if (donor.getStatus() == UserStatus.KHACH_VANG_LAI) {
            boolean hasOtherRegistrations = eventRegistrationRepository.existsByDonorDonorIdAndStatusNot(
                    donor.getDonorId(), EventRegisStatus.DA_HUY);
            if (!hasOtherRegistrations) {
                donor.setStatus(UserStatus.INACTIVE);
                donorRepository.save(donor);
                log.info("CREATE/UPDATE: Donor {} marked as DA_VO_HIEU due to no active registrations", donor.getDonorId());
            }
        }

        registration.setStatus(EventRegisStatus.DA_HUY);
        eventRegistrationRepository.save(registration);

        try {
            String bloodTypeStr = (donor.getBloodType() != null) ? donor.getBloodType() : "Chưa rõ";
            VisitorNotiMess noti = new VisitorNotiMess(
                    donor.getFullName(),
                    bloodTypeStr,
                    registration.getTicketCode(),
                    "Có khách vừa hủy đăng ký!"
            );

            String topicDestination = "/topic/event/" + registration.getEvents().getEventId() + "/cancel-visitor";
            messagingTemplate.convertAndSend(topicDestination, noti);
        } catch (Exception e) {
            log.error("Lỗi khi gửi WebSocket: {}", e.getMessage());
        }

        log.info("CREATE/UPDATE: Visitor registration {} cancelled successfully", registrationId);

        return "Hủy đăng ký thành công";
    }

    //Kham sang loc
    @Transactional
    public String saveScreeningResult(Integer regisId, ScreeningRequest rq) {
        EventRegistration eventRegistration = eventRegistrationRepository.findById(regisId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đăng ký"));

        if (!eventRegistration.getDonor().getDonorId().equals(rq.getDonorId())) {
            throw new RuntimeException("Lỗi sai lệch thông tin người hiến");
        }

        List<EventRegisStatus> editableStatuses = Arrays.asList(
                EventRegisStatus.CHO_KHAM,
                EventRegisStatus.DONG_Y,
                EventRegisStatus.TU_CHOI
        );

        if (!editableStatuses.contains(eventRegistration.getStatus())){
            throw new RuntimeException("Không thể chỉnh sửa! Người hiến chưa đến trạm khám, hoặc đã chuyển sang trạm lấy máu.");
        }

        eventRegistration.setWeight(rq.getWeight());
        eventRegistration.setHemoglobin(rq.getHemoglobin());
        eventRegistration.setHeartRate(rq.getHeartRate());
        eventRegistration.setExpectedVolume(rq.getExpectedVolume());
        eventRegistration.setBloodPressure(rq.getBloodPressure());
        eventRegistration.setRejectionReason(rq.getRejectionReason());

        if (rq.getIsEligible()) {
            eventRegistration.setStatus(EventRegisStatus.DONG_Y);
        } else {
            eventRegistration.setStatus(EventRegisStatus.TU_CHOI);
        }

        eventRegistrationRepository.save(eventRegistration);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", eventRegistration);

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

        if (registration.getStatus() != EventRegisStatus.DONG_Y) {
            throw new RuntimeException("Không đủ điều kiện hiến máu hoặc chưa khám");
        }

        int realVolume = calculateNominalVolume(registration.getExpectedVolume(), rq.getActualVolume());
        if (rq.getIsSuccess().equals(true)) {
            BloodBag newBag = new BloodBag();
            newBag.setRegistration(registration);
            newBag.setCollectedAt(LocalDateTime.now());
            newBag.setProductType(ProductType.MAU_TOAN_PHAN);
            newBag.setStorageEquipment(null);
            newBag.setVolume(realVolume);
            newBag.setStatus(BloodBagStatus.CHO_XET_NGHIEM);
            bloodBagRepository.save(newBag);
            log.info("CREATE/UPDATE: State change successfully saved for entity: {}", newBag);

            registration.setStatus(EventRegisStatus.DA_LAY_MAU);
            registration.setActualVolume(rq.getActualVolume());
            log.info("CREATE/UPDATE: State change successfully saved for entity: {}", registration);
        } else if (rq.getIsSuccess().equals(false)) {
            registration.setStatus(EventRegisStatus.THAT_BAI);
            return "Lấy máu thất bại";
        }
        eventRegistrationRepository.save(registration);
        return "Lấy máu thành công";
    }

    private Users getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
