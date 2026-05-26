package com.blood.service;

import com.blood.dto.Donor.DonorStatResponse;
import com.blood.repository.*;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Admin.*;
import com.blood.model.*;
import com.blood.model.enumformat.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatService {

    private final BloodBagRepository bloodBagRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorRepository donorRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final EventAssignmentRepository eventAssignmentRepository;
    private final StorageEquipmentRepository storageEquipmentRepository;

    public GeneralStatResponse getStatForAdmin() {
        long totalBloodBag = bloodBagRepository.countAllBloodBags();
        long totalEvent = eventRepository.countByStatus(EventStatus.SAP_TOI);
        long totalRequest = bloodRequestRepository.countByStatus(BloodRequestStatus.CHO_DUYET);

        LocalDateTime threshold = LocalDateTime.now().plusDays(10);
        long totalExpiringBloodBag = bloodBagRepository.countExpiringBloodBags(threshold);

        return GeneralStatResponse.builder()
                .totalBloodBag(totalBloodBag)
                .totalEvent(totalEvent)
                .totalRequest(totalRequest)
                .totalExpiringBloodBag(totalExpiringBloodBag)
                .build();
    }

    public DonorStatForAdminResponse getDonorStatForAdmin() {
        long totalDonor = donorRepository.count();
        long activeDonor = donorRepository.countByStatus(UserStatus.ACTIVE);

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long donatedThisMonth = eventRegistrationRepository.countByStatusAndCreatedAtAfter(EventRegisStatus.DA_LAY_MAU, startOfMonth);

        return DonorStatForAdminResponse.builder()
                .totalDonor(totalDonor)
                .activeDonor(activeDonor)
                .donatedThisMonth(donatedThisMonth)
                .build();
    }

    public List<BloodTypeStatResponse> getBloodTypeStat() {
        List<BloodCountProjection> projections = bloodBagRepository.countAvailableBloodBags();
        List<BloodTypeStatResponse> result = new ArrayList<>();
        for (BloodCountProjection p : projections) {
            result.add(BloodTypeStatResponse.builder()
                    .bloodType(p.getBloodType())
                    .rhFactor(p.getRhFactor())
                    .total(p.getTotal() != null ? p.getTotal() : 0L)
                    .build());
        }
        return result;
    }

    public EventStatListResponse getEventStat() {
        List<Events> events = eventRepository.findTop5ByOrderByStartDateDesc();
        List<EventStatResponse> eventStats = new ArrayList<>();

        for (Events event : events) {
            int registeredCount = eventRegistrationRepository.countByEvents_EventIdAndStatusNot(
                    event.getEventId(), EventRegisStatus.DA_HUY);
            int actualCount = eventRegistrationRepository.countByEvents_EventIdAndStatusIn(
                    event.getEventId(), Arrays.asList(EventRegisStatus.TU_CHOI, EventRegisStatus.HOAN_THANH, EventRegisStatus.THAT_BAI));

            eventStats.add(EventStatResponse.builder()
                    .eventId(event.getEventId())
                    .eventName(event.getEventName())
                    .registeredCount(registeredCount)
                    .actualCount(actualCount)
                    .build());
        }

        return EventStatListResponse.builder()
                .events(eventStats)
                .build();
    }

    public StaffStatResponse getStaffStat() {
        long totalStaff = staffRepository.count();
        long techStaff = staffRepository.countByPositionIn(
                Arrays.asList(Position.KY_THUAT, Position.Y_TA, Position.BAC_SI)
        );
        long inventoryStaff = staffRepository.countByPosition(Position.QUAN_LY_KHO);
        long activeStaff = staffRepository.countByStatus(UserStatus.ACTIVE);

        return StaffStatResponse.builder()
                .totalStaff(totalStaff)
                .techStaff(techStaff)
                .inventoryStaff(inventoryStaff)
                .activeStaff(activeStaff)
                .build();
    }

    public HospitalStatResponse getHospitalStat() {
        long totalHospital = hospitalRepository.count();
        long activeHospital = hospitalRepository.countByUser_Status(UserStatus.ACTIVE);
        long totalRequest = bloodRequestRepository.count();
        long approvedRequest = bloodRequestRepository.countByStatus(BloodRequestStatus.DA_DUYET_TOAN_BO);

        return HospitalStatResponse.builder()
                .totalHospital(totalHospital)
                .activeHospital(activeHospital)
                .totalRequest(totalRequest)
                .approvedRequest(approvedRequest)
                .build();
    }

    public BloodInventoryStatResponse getBloodInventoryStat() {
        long totalBloodBag = bloodBagRepository.countAllBloodBags();
        long pendingTestBloodBag = bloodBagRepository.countByStatus(BloodBagStatus.CHO_XET_NGHIEM);

        LocalDateTime threshold = LocalDateTime.now().plusDays(10);
        long expiringBloodBag = bloodBagRepository.countExpiringBloodBags(threshold);
        long expiredBloodBag = bloodBagRepository.countExpiredBloodBags();

        return BloodInventoryStatResponse.builder()
                .totalBloodBag(totalBloodBag)
                .pendingTestBloodBag(pendingTestBloodBag)
                .expiringBloodBag(expiringBloodBag)
                .expiredBloodBag(expiredBloodBag)
                .build();
    }

    public EventStatusStatResponse getEventStatusStat() {
        long upcomingEvent = eventRepository.countByStatus(EventStatus.SAP_TOI);
        long ongoingEvent = eventRepository.countByStatus(EventStatus.DANG_MO);
        long completedEvent = eventRepository.countByStatus(EventStatus.DA_DONG);

        return EventStatusStatResponse.builder()
                .upcomingEvent(upcomingEvent)
                .ongoingEvent(ongoingEvent)
                .completedEvent(completedEvent)
                .build();
    }

    public StorageEquipmentStatResponse getStorageEquipmentStat() {
        long totalEquipment = storageEquipmentRepository.count();
        long activeEquipment = storageEquipmentRepository.countByStatus(EquipmentStatus.ACTIVE);

        List<StorageEquipment> activeList = storageEquipmentRepository.findByStatus(EquipmentStatus.ACTIVE);
        long nearlyFullEquipment = 0;
        for (StorageEquipment se : activeList) {
            int currentLoad = bloodBagRepository.countActiveBagsInEquipment(se.getEquipmentId(), BloodBagStatus.SAN_SANG);
            if (se.getMaxCapacity() != null && currentLoad >= se.getMaxCapacity() * 0.9) {
                nearlyFullEquipment++;
            }
        }

        long maintenanceEquipment = storageEquipmentRepository.countByStatus(EquipmentStatus.MAINTENANCE);

        return StorageEquipmentStatResponse.builder()
                .totalEquipment(totalEquipment)
                .activeEquipment(activeEquipment)
                .nearlyFullEquipment(nearlyFullEquipment)
                .maintenanceEquipment(maintenanceEquipment)
                .build();
    }

    public StaffGeneralStatResponse getStaffGeneralStat() {
        long pendingTestBloodBag = bloodBagRepository.countByStatus(BloodBagStatus.CHO_XET_NGHIEM);
        long pendingStorageBloodBag = bloodBagRepository.countByStatus(BloodBagStatus.CHO_BAO_QUAN);
        long pendingRequest = bloodRequestRepository.countByStatus(BloodRequestStatus.CHO_DUYET);

        LocalDateTime threshold = LocalDateTime.now().plusDays(10);
        long expiringBloodBag = bloodBagRepository.countExpiringBloodBags(threshold);
        long expiredBloodBag = bloodBagRepository.countExpiredBloodBags();

        return StaffGeneralStatResponse.builder()
                .pendingTestBloodBag(pendingTestBloodBag)
                .pendingStorageBloodBag(pendingStorageBloodBag)
                .pendingRequest(pendingRequest)
                .expiringBloodBag(expiringBloodBag)
                .expiredBloodBag(expiredBloodBag)
                .build();
    }

    public DonorStatResponse getDonorStat() {
        Users user = getCurrentUser();
        Donor donor = donorRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        long totalTimeDonate = eventRegistrationRepository.countByDonorDonorIdAndStatus(
                donor.getDonorId(), EventRegisStatus.DA_LAY_MAU);

        return DonorStatResponse.builder()
                .bloodType(donor.getBloodType())
                .rhFactor(donor.getRhFactor())
                .totalTimeDonate(totalTimeDonate)
                .build();
    }

    private Users getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
