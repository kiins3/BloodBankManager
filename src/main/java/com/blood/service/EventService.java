package com.blood.service;

import com.blood.config.RabbitMQConfig;
import com.blood.dto.Admin.EventDetailResponse;
import com.blood.dto.Event.CreateEventRequest;
import com.blood.dto.Event.EventResponse;
import com.blood.dto.Event.UpdateEventRequest;
import com.blood.model.EventAssignment;
import com.blood.model.EventRegistration;
import com.blood.model.enumformat.EventRegisStatus;
import com.blood.model.enumformat.EventStatus;
import com.blood.model.Events;
import com.blood.model.enumformat.UserStatus;
import com.blood.repository.EventAssignmentRepository;
import com.blood.repository.EventRegistrationRepository;
import com.blood.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;
    private final EventAssignmentRepository eventAssignmentRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    public List<EventResponse> getEventList() {
        List<Events> events = eventRepository.findAllEventsSortedByStatus();
        return events.stream().map(event -> convertToDTOGetList(event)).collect(Collectors.toList());
    }

    public List<EventResponse> getComingEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Events> events = eventRepository.findByEndDateAfter(now);

        return events.stream()
                .filter(e -> e.getStatus() == EventStatus.SAP_TOI || e.getStatus() == EventStatus.DANG_MO)
                .map(event -> convertToDTOGetList(event))
                .collect(Collectors.toList());
    }

    public EventDetailResponse getEventDetail(Integer eventId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        int registeredCount = eventRegistrationRepository.countByEvents_EventIdAndStatusNot(eventId, EventRegisStatus.DA_HUY);
        int actualCount = eventRegistrationRepository.countByEvents_EventIdAndStatusIn(eventId, List.of(EventRegisStatus.DA_LAY_MAU, EventRegisStatus.HOAN_THANH));

        return EventDetailResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .location(event.getLocation())
                .startDate(event.getStartDate() != null ? event.getStartDate().toString() : null)
                .endDate(event.getEndDate() != null ? event.getEndDate().toString() : null)
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .targetAmount(event.getTargetAmount())
                .registeredCount(registeredCount)
                .actualCount(actualCount)
                .build();
    }


    public EventResponse convertToDTOGetList(Events event){
        int count = 0;
        if (event.getRegistrations() != null) {
            count = (int) event.getRegistrations().stream()
                    .filter(reg -> reg.getStatus() != EventRegisStatus.DA_HUY)
                    .count();
        }

        EventStatus currentStatus = event.getStatus();
        LocalDateTime date = LocalDateTime.now();

        if (event.getEndDate().isBefore(date) && currentStatus != EventStatus.DA_HUY) {
            currentStatus = EventStatus.DA_DONG;
        } else if (event.getStartDate().isBefore(date) && event.getEndDate().isAfter(date) && currentStatus != EventStatus.DA_HUY) {
            currentStatus = EventStatus.DANG_MO;
        }

        return EventResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .targetAmount(event.getTargetAmount())
                .currentAmount(count)
                .status(currentStatus).build();
    }

    public String createEvent(CreateEventRequest rq) {
        LocalDateTime now = LocalDateTime.now();

        /*if (rq.getStartDate().isBefore(now.plusDays(3))) {
            throw new RuntimeException("Phải tạo sự kiện trước ít nhất 3 ngày để truyền thông.");
        }*/

        if (rq.getEndDate().isBefore(rq.getStartDate())) {
            throw new RuntimeException("Ngày kết thúc không hợp lệ.");
        }

        boolean hasOverlap = eventRepository.existsOverlappingEvent(rq.getLocation(), rq.getStartDate(), rq.getEndDate());
        if (hasOverlap) {
            throw new RuntimeException("Địa điểm này đã có sự kiện khác diễn ra trong thời gian được chọn.");
        }

        Events event = new Events();
        event.setEventName(rq.getEventName());
        event.setLocation(rq.getLocation());
        event.setStartDate(rq.getStartDate());
        event.setEndDate(rq.getEndDate());
        event.setTargetAmount(rq.getTargetAmount());
        event.setStatus(EventStatus.SAP_TOI);
        Events savedEvent = eventRepository.save(event);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, savedEvent.getEventId());
        log.info("Đã gửi sự kiện ID {} vào RabbitMQ để xử lý gửi mail", savedEvent.getEventId());

        return "Tạo sự kiện thành công";
    }

    @Transactional
    public EventResponse updateEvent(Integer id, UpdateEventRequest rq) {
        Events event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch hiến máu"));

        if (event.getStatus() == EventStatus.DANG_MO || event.getStatus() == EventStatus.DA_DONG) {
            throw new RuntimeException("Không thể chỉnh sửa chiến dịch đang diễn ra hoặc đã kết thúc");
        }
        if (event.getStatus() == EventStatus.DA_HUY) {
            throw new RuntimeException("Không thể chỉnh sửa chiến dịch đã bị hủy");
        }

        if (rq.getEventName() != null) event.setEventName(rq.getEventName());
        if (rq.getLocation() != null) event.setLocation(rq.getLocation());
        if (rq.getStartDate() != null) event.setStartDate(rq.getStartDate());
        if (rq.getEndDate() != null) event.setEndDate(rq.getEndDate());


        Events updatedEvent = eventRepository.save(event);
        log.info("INFO: Cập nhật thông tin chiến dịch ID: {}", id);

        return convertToDTO(updatedEvent);
    }

    @Transactional
    public void cancelEvent(Integer eventId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chiến dịch hiến máu"));

        if (event.getStatus() != EventStatus.SAP_TOI) {
            throw new RuntimeException("Chỉ có thể hủy chiến dịch chưa diễn ra");
        }

        LocalDateTime now = LocalDateTime.now();
        long hoursUntilEvent = ChronoUnit.HOURS.between(now, event.getStartDate());

        if (hoursUntilEvent < 48) {
            throw new RuntimeException("Không thể hủy! Chỉ được phép hủy chiến dịch trước ít nhất 48 giờ để đảm bảo thông báo cho người hiến máu.");
        }

        event.setStatus(EventStatus.DA_HUY);
        eventRepository.save(event);

        List<EventAssignment> assignments = eventAssignmentRepository.findByEvents_EventId(eventId);

        for (EventAssignment ea : assignments) {
            ea.setStatus(UserStatus.INACTIVE);
        }
        eventAssignmentRepository.saveAll(assignments);

        log.info("Đã hủy thành công sự kiện và vô hiệu hóa {} nhiệm vụ phân công.", assignments.size());

        List<String> registeredEmails = eventRepository.findEmailsByEventId(eventId);

        if (!registeredEmails.isEmpty()) {
            String subject = "THÔNG BÁO HỦY SỰ KIỆN HIẾN MÁU: " + event.getEventName();
            String content = "Kính gửi anh/chị,\n\n" +
                    "Chúng tôi rất tiếc phải thông báo rằng sự kiện hiến máu '" + event.getEventName() +
                    "' dự kiến diễn ra vào " + event.getStartDate() + " đã bị hủy do một số lý do khách quan.\n\n" +
                    "Cảm ơn tinh thần tương thân tương ái của anh/chị. Hẹn gặp lại ở các sự kiện sau!";

            for (String email : registeredEmails) {
                emailService.sendEmail(email, subject, content);
            }
            log.info("INFO: Đã gửi email thông báo hủy chiến dịch ID: {} cho {} người đăng ký.", eventId, registeredEmails.size());
        }
    }

    @Transactional
    public void completeEvent(Integer eventId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        if (event.getStatus() != EventStatus.DANG_MO && event.getStatus() != EventStatus.SAP_TOI) {
            throw new RuntimeException("Chỉ có thể hoàn thành sự kiện đang hoạt động.");
        }

        event.setStatus(EventStatus.DA_DONG);
        eventRepository.save(event);

        List<EventAssignment> assignments = eventAssignmentRepository.findByEvents_EventId(eventId);
        for (EventAssignment ea : assignments) {
            ea.setStatus(UserStatus.INACTIVE);
        }
        eventAssignmentRepository.saveAll(assignments);

        List<EventRegistration> registrations = eventRegistrationRepository.findByEvents_EventId(eventId);
        int completedCount = 0;
        int expiredCount = 0;

        for (EventRegistration reg : registrations) {
            EventRegisStatus currentStatus = reg.getStatus();

            if (currentStatus == EventRegisStatus.DA_LAY_MAU) {
                reg.setStatus(EventRegisStatus.HOAN_THANH);
                completedCount++;
            }

            else if (currentStatus == EventRegisStatus.DA_DANG_KY ||
                    currentStatus == EventRegisStatus.CHO_KHAM ||
                    currentStatus == EventRegisStatus.DONG_Y) {
                reg.setStatus(EventRegisStatus.DA_HET_HAN);
                expiredCount++;
            }
        }
        eventRegistrationRepository.saveAll(registrations);

        log.info("INFO: Sự kiện '{}' (ID: {}) đã được đóng.", event.getEventName(), eventId);
        log.info("INFO: Đã giải phóng {} nhân sự. Cập nhật {} vé HOÀN THÀNH, {} vé HẾT HẠN.",
                assignments.size(), completedCount, expiredCount);

    }

    private EventResponse convertToDTO(Events event) {
        int count = 0;
        if (event.getRegistrations() != null) {
            count = event.getRegistrations().size();
        }
        return EventResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .targetAmount(event.getTargetAmount())
                .currentAmount(count)
                .status(event.getStatus())
                .build();
    }

}
