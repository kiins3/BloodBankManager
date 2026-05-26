package com.blood.service;

import com.blood.dto.Staff.*;
import com.blood.model.EventAssignment;
import com.blood.model.Users;
import com.blood.repository.EventAssignmentRepository;
import com.blood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.Position;
import com.blood.model.Staff;
import com.blood.model.enumformat.UserStatus;
import com.blood.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final EventAssignmentRepository eventAssignmentRepository;
    private final ActionLogService actionLogService;

    public Page<StaffResponse> getAllStaff(String keyword, Position position, UserStatus status, Pageable pageable) {
        String searchKey = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<Staff> staffPage = staffRepository.findStaffsWithFilters(searchKey, position, status, pageable);

        return staffPage.map(this::convertToDTO);
    }

    @Transactional
    public String updateStaff(UpdateStaffRequest rq) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Staff staff = user.getStaff();

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("staffId", staff.getStaffId());
        oldData.put("fullName", staff.getFullName());
        oldData.put("phone", staff.getPhone());
        oldData.put("gender", staff.getGender());
        oldData.put("address", staff.getAddress());
        oldData.put("email", staff.getUser() != null ? staff.getUser().getEmail() : null);

        staff.setFullName(rq.getFullName());
        staff.setDob(rq.getDob());
        staff.setGender(rq.getGender());
        staff.setAddress(rq.getAddress());
        staff.setPhone(rq.getPhone());
        if (staff.getUser() != null) {
            staff.getUser().setEmail(rq.getEmail());
        }
        staff.setCccd(rq.getCccd());

        staffRepository.save(staff);
        log.info("Staff updated successfully");

        Map<String, Object> updatedData = new LinkedHashMap<>();
        updatedData.put("staffId", staff.getStaffId());
        updatedData.put("fullName", staff.getFullName());
        updatedData.put("phone", staff.getPhone());
        updatedData.put("gender", staff.getGender());
        updatedData.put("address", staff.getAddress());
        updatedData.put("email", staff.getUser() != null ? staff.getUser().getEmail() : null);

        actionLogService.log(ActionType.UPDATE_STAFF_PROFILE,
                "Staff", String.valueOf(staff.getStaffId()), oldData, updatedData);

        return "Cập nhật thành công";
    }

    public StaffDetailResponse getStaffDetail(Integer staffId) {
        Staff staff = staffRepository.findById(staffId).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        return StaffDetailResponse.builder()
                .staffId(staffId)
                .address(staff.getAddress())
                .dob(staff.getDob())
                .gender(staff.getGender())
                .email(staff.getUser() != null ? staff.getUser().getEmail() : "Chưa liên kết tài khoản")                .fullName(staff.getFullName())
                .position(staff.getPosition())
                .phone(staff.getPhone())
                .status(staff.getStatus())
                .build();
    }

    @Transactional
    public String updateStaffForAdmin(Integer id, UpdateStaffForAdminRequest rq) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));

        // Capture original state
        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("staffId", staff.getStaffId());
        oldData.put("fullName", staff.getFullName());
        oldData.put("phone", staff.getPhone());
        oldData.put("gender", staff.getGender());
        oldData.put("address", staff.getAddress());
        oldData.put("email", staff.getUser() != null ? staff.getUser().getEmail() : null);

        staff.setFullName(rq.getFullName());
        staff.setDob(rq.getDob());
        staff.setGender(rq.getGender());
        staff.setAddress(rq.getAddress());
        staff.setPhone(rq.getPhone());
        if (staff.getUser() != null) {
            staff.getUser().setEmail(rq.getEmail());
        }

        staffRepository.save(staff);
        log.info("Updated staff successfully");

        // Capture new state
        Map<String, Object> updatedData = new LinkedHashMap<>();
        updatedData.put("staffId", staff.getStaffId());
        updatedData.put("fullName", staff.getFullName());
        updatedData.put("phone", staff.getPhone());
        updatedData.put("gender", staff.getGender());
        updatedData.put("address", staff.getAddress());
        updatedData.put("email", staff.getUser() != null ? staff.getUser().getEmail() : null);

        actionLogService.log(ActionType.UPDATE_STAFF_PROFILE,
                "Staff", String.valueOf(staff.getStaffId()), oldData, updatedData);

        return "Cập nhật thành công";
    }

    public Page<StaffHistoryResponse> getStaffHistory(Integer staffId, Pageable pageable) {
        Page<EventAssignment> assignments = eventAssignmentRepository
                .findByStaff_StaffIdOrderByEvents_StartDateDesc(staffId, pageable);

        return assignments.map(ea -> StaffHistoryResponse.builder()
                .eventId(ea.getEvents().getEventId())
                .eventName(ea.getEvents().getEventName())
                .startDate(ea.getEvents().getStartDate())
                .location(ea.getEvents().getLocation())
                .role(ea.getRole())
                .assignmentStatus(ea.getStatus())
                .build());
    }

    public StaffResponse convertToDTO(Staff staff) {
        String email = "";
        if (staff.getUser() != null) {
            email = staff.getUser().getEmail();
        }

        return StaffResponse.builder()
                .staffId(staff.getStaffId())
                .fullName(staff.getFullName())
                .phone(staff.getPhone())
                .email(email)
                .position(staff.getPosition())
                .status(staff.getStatus())
                .build();
    }

    public List<StaffAvailabilityResponse> getSmartStaffList() {
        return staffRepository.findSmartStaffListForAssignment();
    }
}
