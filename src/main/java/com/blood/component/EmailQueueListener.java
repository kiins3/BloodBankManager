package com.blood.component;

import com.blood.config.RabbitMQConfig;
import com.blood.model.Donor;
import com.blood.model.Events;
import com.blood.repository.DonorRepository;
import com.blood.repository.EventRepository;
import com.blood.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailQueueListener {

    private final EventRepository eventRepository;
    private final DonorRepository donorRepository;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEventEmailNotification(Integer eventId) {
        log.info("RabbitMQ nhận được yêu cầu gửi mail cho sự kiện ID: {}", eventId);

        Events event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return;

        String subject = "Sự kiện hiến máu mới: " + event.getEventName();
        String body = "Xin chào, sắp tới có sự kiện hiến máu tại " + event.getLocation() + " vào ngày " + event.getStartDate() + ". Mong bạn dành thời gian quan tâm và tham gia!";

        int pageSize = 100;
        int pageNumber = 0;
        Page<Donor> donorPage;

        do {
            donorPage = donorRepository.findAll(PageRequest.of(pageNumber, pageSize));

            for (Donor donor : donorPage.getContent()) {
                if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {

                    emailService.sendEmail(donor.getEmail(), subject, body);
                }
            }

            log.info("Đã xử lý xong trang {}/{}", pageNumber + 1, donorPage.getTotalPages());
            pageNumber++;

        } while (donorPage.hasNext());

        log.info("Hoàn tất chiến dịch gửi mail cho sự kiện ID: {}", eventId);
    }
}
