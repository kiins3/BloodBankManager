package com.blood.Service;

import com.blood.DTO.Blood.DonationRequest;
import com.blood.DTO.Donor.DonorResponse;
import com.blood.DTO.Event.RegistrationRequest;
import com.blood.DTO.Event.RegistrationResponse;
import com.blood.DTO.Event.ScreeningRequest;
import com.blood.DTO.Event.TicketResponse;
import com.blood.Model.BloodBag;
import com.blood.Model.Donor;
import com.blood.Model.EventRegistration;
import com.blood.Model.Events;
import com.blood.Repository.BloodBagRepository;
import com.blood.Repository.DonorRepository;
import com.blood.Repository.EventRegistrationRepository;
import com.blood.Repository.EventRepository;
import com.blood.helper.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class RegistrationService {
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    DonorRepository donorRepository;

    @Autowired
    BloodBagRepository bloodBagRepository;

    //Dang ky tham gia
    public String registration(Integer eventId, Integer donorId) {
        Events event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Sự kiện không tồn tại"));

        if (!"SAP_TOI".equals(event.getStatus())){
            throw new RuntimeException(("Sự kiện chưa mở"));
        }

        boolean alreadyRegistered = eventRegistrationRepository.existsByEvents_EventIdAndDonor_DonorId(eventId, donorId);
        if (alreadyRegistered) {
            throw new RuntimeException("Bạn đã đăng ký tham rồi");
        }

        int currentCount = eventRegistrationRepository.countByEvents_EventIdAndStatus(eventId, event.getStatus());
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

        return "Đăng ký thành công";
    }

    //Xem ve da dang ky
    public TicketResponse getMyTickets(Integer eventId, Integer donorId) {
        EventRegistration registration = eventRegistrationRepository.findByEvents_EventIdAndDonor_DonorId(eventId, donorId)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký tham gia sự kiện"));

        String code = registration.getTicketCode();
        String qrCode = QRCodeGenerator.generateQRCode(code, 250, 250);

        return TicketResponse.builder()
                .eventName(registration.getEvents().getEventName())
                .ticketCode(code)
                .qrCode(qrCode)
                .status(registration.getStatus())
                .build();
    }

    //Checkin tai quay
    @Transactional
    public DonorResponse checkin(Integer eventId, String ticketCode) {
        EventRegistration eventRegistration = eventRegistrationRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new RuntimeException("Vé không tồn tại"));

        Events event =eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        if (!eventRegistration.getEvents().getEventId().equals(eventId)){
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
                .fullName(eventRegistration.getDonor().getFullName())
                .gender(eventRegistration.getDonor().getGender())
                .dob(eventRegistration.getDonor().getDob())
                .address(eventRegistration.getDonor().getAddress())
                .phone(eventRegistration.getDonor().getPhone())
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

        return "Lấy máu thành công";
    }
}
