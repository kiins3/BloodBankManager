package com.blood.service;

import com.blood.dto.Admin.EventAssignmentRequest;
import com.blood.model.enumformat.*;
import com.blood.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Admin.AssignmentStaffResponse;
import com.blood.dto.Admin.MyAssignmentResponse;
import com.blood.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignmentService {

    private final EventRepository eventRepository;
    private final EventAssignmentRepository eventAssignmentRepository;
    private final StaffRepository staffRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public AssignmentStaffResponse getAssignmentStaff(Integer eventId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        List<EventAssignment> assignments = eventAssignmentRepository.findByEvents_EventIdAndStatus(eventId, UserStatus.ACTIVE);
        List<AssignmentStaffResponse.StaffInfo> staffList = new ArrayList<>();

        for (EventAssignment assignment : assignments) {
            staffList.add(AssignmentStaffResponse.StaffInfo.builder()
                    .staffId(assignment.getStaff().getStaffId())
                    .fullName(assignment.getStaff().getFullName())
                    .position(assignment.getStaff().getPosition() != null ? assignment.getStaff().getPosition() : null)
                    .build());
        }

        return AssignmentStaffResponse.builder()
                .eventId(eventId)
                .eventName(event.getEventName())
                .assignedStaff(staffList)
                .build();
    }

    @Transactional
    public String syncStaffToEvent(EventAssignmentRequest request) {

        Events event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        if (event.getStatus() == EventStatus.DA_DONG || event.getStatus() == EventStatus.DA_HUY) {
            throw new RuntimeException("Không thể phân công nhân sự cho sự kiện đã kết thúc hoặc đã hủy.");
        }

        List<EventAssignmentRequest.StaffRoleDetail> requestedAssignments = request.getAssignments();

        Set<Integer> requestedStaffIds = new HashSet<>();
        for (EventAssignmentRequest.StaffRoleDetail detail : requestedAssignments) {
            if (!requestedStaffIds.add(detail.getStaffId())) {
                throw new RuntimeException(
                        "Danh sách phân công có nhân viên bị trùng lặp (staffId: " + detail.getStaffId() + ")"
                );
            }
        }

        List<EventAssignment> currentAssignments =
                eventAssignmentRepository.findByEvents_EventId(event.getEventId());

        Map<Integer, EventAssignment> currentMap = currentAssignments.stream()
                .collect(Collectors.toMap(ea -> ea.getStaff().getStaffId(), ea -> ea));

        Map<Integer, Staff> staffMap = staffRepository.findAllById(requestedStaffIds).stream()
                .collect(Collectors.toMap(Staff::getStaffId, staff -> staff));

        if (staffMap.size() != requestedStaffIds.size()) {
            throw new RuntimeException("Một hoặc nhiều nhân viên không tồn tại trong hệ thống");
        }

        List<Integer> newStaffIds = requestedStaffIds.stream()
                .filter(id -> !currentMap.containsKey(id))
                .collect(Collectors.toList());

        if (!newStaffIds.isEmpty()) {
            List<Integer> busyStaffIds = eventAssignmentRepository.findBusyStaffIds(
                    newStaffIds,
                    UserStatus.ACTIVE,
                    List.of(EventStatus.SAP_TOI, EventStatus.DANG_MO),
                    event.getEventId(),
                    event.getStartDate(),
                    event.getEndDate()
            );

            if (!busyStaffIds.isEmpty()) {
                String busyNames = busyStaffIds.stream()
                        .map(id -> staffMap.get(id).getFullName())
                        .collect(Collectors.joining(", "));
                throw new RuntimeException(
                        "Các nhân viên sau đang bận ở sự kiện khác trong cùng thời gian: " + busyNames
                );
            }
        }

        for (EventAssignmentRequest.StaffRoleDetail detail : requestedAssignments) {
            Staff staff = staffMap.get(detail.getStaffId());
            if (detail.getRole() == AssignmentRole.KHAM_SANG_LOC
                    && staff.getPosition() != Position.BAC_SI) {
                throw new RuntimeException(
                        "Nhân viên " + staff.getFullName() + " không có chuyên môn Bác sĩ."
                );
            }
        }

        List<EventAssignment> assignmentsToSave = new ArrayList<>();
        List<EventAssignment> newAssignmentsToMail = new ArrayList<>();

        for (EventAssignment existing : currentAssignments) {
            if (!requestedStaffIds.contains(existing.getStaff().getStaffId())) {
                existing.setStatus(UserStatus.INACTIVE);
                assignmentsToSave.add(existing);
            }
        }

        for (EventAssignmentRequest.StaffRoleDetail detail : requestedAssignments) {
            Integer sId = detail.getStaffId();
            Staff staff = staffMap.get(sId);
            AssignmentRole role = detail.getRole();

            EventAssignment assignment = currentMap.get(sId);
            if (assignment != null) {
                assignment.setRole(role);
                assignment.setStatus(UserStatus.ACTIVE);
                assignmentsToSave.add(assignment);
            } else {
                EventAssignment newAssignment = new EventAssignment();
                newAssignment.setEvents(event);
                newAssignment.setStaff(staff);
                newAssignment.setRole(role);
                newAssignment.setStatus(UserStatus.ACTIVE);
                assignmentsToSave.add(newAssignment);
                newAssignmentsToMail.add(newAssignment);
            }
        }

        eventAssignmentRepository.saveAll(assignmentsToSave);

        String eventName = event.getEventName();
        String eventDate = event.getStartDate().toString();

        for (EventAssignment ea : newAssignmentsToMail) {
            try {
                String staffEmail = ea.getStaff().getUser().getEmail();
                String subject = "[QUAN TRỌNG] THÔNG BÁO PHÂN CÔNG NHIỆM VỤ: " + eventName;
                String content = String.format(
                        "Kính gửi %s,\n\n" +
                                "Bạn vừa được điều động tham gia công tác tại: '%s'.\n" +
                                "Thời gian: %s\n" +
                                "Nhiệm vụ: %s\n\n" +
                                "Trân trọng.",
                        ea.getStaff().getFullName(),
                        eventName,
                        eventDate,
                        ea.getRole().getDisplayName()
                );
                emailService.sendEmail(staffEmail, subject, content);
            } catch (Exception e) {
                log.error("Lỗi gửi mail phân công cho staff {}", ea.getStaff().getStaffId(), e);
            }
        }
        return "Đồng bộ phân công nhân sự thành công!";
    }

    public MyAssignmentResponse getMyAssignment() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng từ Token"));
        Staff staff = staffRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Tài khoản này chưa được cấu hình là Nhân viên (Staff)"));
        List<EventAssignment> assignments = eventAssignmentRepository.findActiveAssignmentsByStaffId(staff.getStaffId());

        List<MyAssignmentResponse.EventAssignmentInfo> assignmentInfos = new ArrayList<>();

        for (EventAssignment assignment : assignments) {
            com.blood.model.Events event = assignment.getEvents();

            int registeredCount = eventRegistrationRepository.countByEvents_EventIdAndStatus(event.getEventId(), EventRegisStatus.DA_DANG_KY);
            int actualCount = eventRegistrationRepository.countByEvents_EventIdAndStatus(event.getEventId(), EventRegisStatus.DA_LAY_MAU);

            assignmentInfos.add(MyAssignmentResponse.EventAssignmentInfo.builder()
                    .eventId(event.getEventId())
                    .eventName(event.getEventName())
                    .startDate(event.getStartDate() != null ? event.getStartDate().toString() : null)
                    .endDate(event.getEndDate() != null ? event.getEndDate().toString() : null)
                    .location(event.getLocation())
                    .eventStatus(event.getStatus() != null ? event.getStatus() : null)
                    .role(assignment.getRole())
                    .registeredCount(registeredCount)
                    .actualCount(actualCount)
                    .build());
        }

        return MyAssignmentResponse.builder()
                .assignments(assignmentInfos)
                .build();
    }

    /*@Transactional
    public String removeStaffFromEvent(Integer eventId, Integer staffId) {
        Events event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện"));

        if (event.getStatus() != EventStatus.SAP_TOI) {
            throw new RuntimeException("Chỉ có thể thay đổi/gỡ nhân sự khi sự kiện chưa bắt đầu (Sắp tới).");
        }

        EventAssignment assignment = eventAssignmentRepository.findByEvents_EventIdAndStaff_StaffId(eventId, staffId)
                .orElseThrow(() -> new RuntimeException("Nhân viên này không có nhiệm vụ trong sự kiện hiện tại."));

        if (assignment.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Nhân viên này đã được gỡ khỏi sự kiện từ trước.");
        }

        assignment.setStatus(UserStatus.INACTIVE);
        eventAssignmentRepository.save(assignment);

        String staffEmail = assignment.getStaff().getUser().getEmail();
        String subject = "[THÔNG BÁO] HỦY PHÂN CÔNG NHIỆM VỤ: " + event.getEventName();
        String content = "Kính gửi " + assignment.getStaff().getFullName() + ",\n\n" +
                "Lịch phân công của bạn tại chiến dịch '" + event.getEventName() + "' đã được ban quản lý hủy bỏ.\n" +
                "Bạn không cần có mặt tại sự kiện này nữa.\n\n" +
                "Trân trọng.";

        emailService.sendEmail(staffEmail, subject, content);

        log.info("INFO: Removed staff ID {} from Event ID {}", staffId, eventId);
        return "Đã gỡ phân công nhân viên thành công.";
    }*/
}
