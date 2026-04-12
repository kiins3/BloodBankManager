package com.blood.Service;

import com.blood.DTO.StorageEquipment.CreateEquipmentRequest;
import com.blood.DTO.StorageEquipment.ListStorageEquipmentResponse;
import com.blood.DTO.StorageEquipment.UpdateEquipmentRequest;
import com.blood.Model.ProductType;
import com.blood.Model.StorageEquipment;
import com.blood.Repository.BloodBagRepository;
import com.blood.Repository.StorageEquipmentRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorageEquipmentService {
    @Autowired
    private StorageEquipmentRepository storageEquipmentRepository;

    @Autowired
    private BloodBagRepository bloodBagRepository;

    public List<ListStorageEquipmentResponse> getListStorageEquipment(Integer equipmentId, ProductType productType){
        List<StorageEquipment> storageEquipment = storageEquipmentRepository.findWithFilter(equipmentId, productType);

        return storageEquipment.stream().map(equipment -> {
            int currentLoad = bloodBagRepository.countByStorageEquipment_EquipmentId(equipmentId);

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

    public String createStorageEquipment(CreateEquipmentRequest rq){
        StorageEquipment storageEquipment = new StorageEquipment();
        storageEquipment.setName(rq.getName());
        storageEquipment.setProductType(rq.getProductType());
        storageEquipment.setStandard(rq.getStandard());
        storageEquipment.setMaxCapacity(rq.getMaxCapacity());
        storageEquipment.setStatus("ACTIVE");
        storageEquipmentRepository.save(storageEquipment);

        return "Tạo tủ mới thành công";
    }

    public String updateStorageEquipment(Integer equipmentId, UpdateEquipmentRequest rq) {
        StorageEquipment equipment = storageEquipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));

        int currentLoad = bloodBagRepository.countByStorageEquipment_EquipmentId(equipmentId);
        if (rq.getMaxCapacity() != null) {
            if (rq.getMaxCapacity() < currentLoad) {
                return "Không thể giảm sức chứa xuống thấp hơn số lượng máu đang được lưu trữ";
            }
            equipment.setMaxCapacity(rq.getMaxCapacity());
        }

        if (rq.getStatus() != null) {
            if (equipment.getStatus().equals("UNACTIVE") && currentLoad > 0) {
                return "Không thể vô hiệu hóa thiết bị này";
            }
            equipment.setStatus(rq.getStatus());
        }

        storageEquipmentRepository.save(equipment);
        return "Cập nhật thành công";
    }
}
