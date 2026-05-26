package com.blood.service;

import com.blood.dto.StorageEquipment.*;
import com.blood.model.Staff;
import com.blood.model.Users;
import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.EquipmentStatus;
import com.blood.repository.StaffRepository;
import com.blood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.ProductType;
import com.blood.model.StorageEquipment;
import com.blood.repository.BloodBagRepository;
import com.blood.repository.StorageEquipmentRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageEquipmentService {

    private final StorageEquipmentRepository storageEquipmentRepository;
    private final BloodBagRepository bloodBagRepository;
    private final ActionLogService actionLogService;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public List<ListStorageEquipmentResponse> getListStorageEquipment(Integer bloodBagId, ProductType productType){
        ProductType targetProductType = productType;
        if (bloodBagId != null) {
            com.blood.model.BloodBag bag = bloodBagRepository.findById(bloodBagId).orElse(null);
            if (bag != null) {
                targetProductType = bag.getProductType();
            }
        }
        List<StorageEquipment> storageEquipment = storageEquipmentRepository.findWithFilter(null, targetProductType);

        return storageEquipment.stream().map(equipment -> {
            int currentLoad = bloodBagRepository.countActiveBagsInEquipment(equipment.getEquipmentId(), BloodBagStatus.SAN_SANG);
            return ListStorageEquipmentResponse.builder()
                    .equipmentId(equipment.getEquipmentId())
                    .name(equipment.getName())
                    .maxCapacity(equipment.getMaxCapacity())
                    .standard(equipment.getStandard())
                    .productType(equipment.getProductType())
                    .currentLoad(currentLoad)
                    .status(equipment.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListStorageEquipmentResponse> getActiveEquipments() {
        List<StorageEquipment> activeEquipments = storageEquipmentRepository.findByStatus(EquipmentStatus.ACTIVE);

        return activeEquipments.stream().map(eq -> {
            int currentLoad = bloodBagRepository.countActiveBagsInEquipment(eq.getEquipmentId(), BloodBagStatus.SAN_SANG);

            return ListStorageEquipmentResponse.builder()
                    .equipmentId(eq.getEquipmentId())
                    .name(eq.getName())
                    .productType(eq.getProductType())
                    .maxCapacity(eq.getMaxCapacity())
                    .currentLoad(currentLoad)
                    .status(eq.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    public String createStorageEquipment(CreateEquipmentRequest rq){
        StorageEquipment storageEquipment = new StorageEquipment();
        storageEquipment.setName(rq.getName());
        storageEquipment.setProductType(rq.getProductType());
        storageEquipment.setStandard(rq.getStandard());
        storageEquipment.setMaxCapacity(rq.getMaxCapacity());
        storageEquipment.setStatus(EquipmentStatus.ACTIVE);
        storageEquipmentRepository.save(storageEquipment);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", storageEquipment);

        return "Tạo tủ mới thành công";
    }

    public String updateStorageEquipment(Integer equipmentId, UpdateEquipmentRequest rq) {
        StorageEquipment equipment = storageEquipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("equipmentId", equipmentId);
        oldData.put("equipmentName", equipment.getName());
        oldData.put("maxCapacity", equipment.getMaxCapacity());
        oldData.put("status", equipment.getStatus());

        int currentLoad = bloodBagRepository.countActiveBagsInEquipment(equipmentId, BloodBagStatus.SAN_SANG);
        if (rq.getMaxCapacity() != null) {
            if (rq.getMaxCapacity() < currentLoad) {
                return "Không thể giảm sức chứa xuống thấp hơn số lượng máu đang được lưu trữ";
            }
            equipment.setMaxCapacity(rq.getMaxCapacity());
        }

        if (rq.getStatus() != null) {
            if ((equipment.getStatus() == EquipmentStatus.INACTIVE) && currentLoad > 0) {
                return "Không thể vô hiệu hóa thiết bị này";
            }
            equipment.setStatus(rq.getStatus());
        }

        storageEquipmentRepository.save(equipment);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", equipment);

        Map<String, Object> equipmentData = new LinkedHashMap<>();
        equipmentData.put("equipmentId", equipmentId);
        equipmentData.put("equipmentName", equipment.getName());
        equipmentData.put("maxCapacity", equipment.getMaxCapacity());
        equipmentData.put("status", equipment.getStatus());

        actionLogService.log(ActionType.UPDATE_EQUIPMENT,
                "StorageEquipment", String.valueOf(equipmentId), oldData, equipmentData);

        return "Cập nhật thành công";
    }

    @Transactional
    public String reportBrokenEquipment(Integer equipmentId, ReportBrokenRequest rq) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(currentUsername).orElseThrow();
        Staff staff = staffRepository.findByUser(user).orElseThrow();

        StorageEquipment equipment = storageEquipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tủ lưu trữ"));

        if (equipment.getStatus() == EquipmentStatus.MAINTENANCE) {
            throw new RuntimeException("Tủ này đang trong trạng thái báo hỏng/bảo trì rồi.");
        }

        int currentLoad = bloodBagRepository.countActiveBagsInEquipment(equipmentId, BloodBagStatus.SAN_SANG);
        if (currentLoad > 0) {
            throw new RuntimeException("Tủ vẫn đang chứa " + currentLoad + " túi máu! Vui lòng dùng chức năng 'Chuyển Kho' để sơ tán hết túi máu trước khi báo hỏng.");
        }

        equipment.setStatus(EquipmentStatus.MAINTENANCE);
        storageEquipmentRepository.save(equipment);

        try {
            AdminAlertNoti noti = new AdminAlertNoti(
                    "SỰ CỐ THIẾT BỊ",
                    "Tủ " + equipment.getName() + " vừa bị báo hỏng bởi " + staff.getFullName() + ". Lý do: " + rq.getReason()
            );
            messagingTemplate.convertAndSend("/topic/admin/alerts", noti);
        } catch (Exception e) {
            log.error("Lỗi gửi cảnh báo thiết bị cho Admin: {}", e.getMessage());
        }

        return "Đã báo cáo sự cố thành công! Admin sẽ tiếp nhận và xử lý.";
    }
}
