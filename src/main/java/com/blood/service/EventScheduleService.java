package com.blood.service;

import com.blood.model.enumformat.EventStatus;
import com.blood.model.Events;
import com.blood.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventScheduleService {

    private final EventRepository eventRepository;
    private final EventService eventService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoUpdateEventStatus() {
        LocalDateTime now = LocalDateTime.now();
        log.info("AUTO_TASK: START SCANNING EVENT...");

        List<Events> upcomingEvents = eventRepository.findByStatus(EventStatus.SAP_TOI);
        int openedCount = 0;
        for (Events e : upcomingEvents) {
            if (!now.isBefore(e.getStartDate())) {
                e.setStatus(EventStatus.DANG_MO);
                openedCount++;
            }
        }

        if (openedCount > 0) {
            eventRepository.saveAll(upcomingEvents);
            log.info("AUTO_TASK: Đã tự động MỞ {} sự kiện đến giờ.", openedCount);
        }

        List<Events> openEvents = eventRepository.findByStatus(EventStatus.DANG_MO);
        int closedCount = 0;
        for (Events e : openEvents) {
            if (now.isAfter(e.getEndDate())) {
                try {
                    eventService.completeEvent(e.getEventId());
                    closedCount++;
                } catch (Exception ex) {
                    log.error("AUTO_TASK: Lỗi khi đóng sự kiện {}: {}", e.getEventId(), ex.getMessage());
                }
            }
        }

        if (closedCount > 0) {
            log.info("AUTO_TASK: Đã tự động ĐÓNG {} sự kiện quá hạn.", closedCount);
        }
    }
}