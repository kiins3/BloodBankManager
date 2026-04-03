package com.blood.Service;

import com.blood.DTO.StorageEquipment.ListStorageEquipmentResponse;
import com.blood.Model.StorageEquipment;
import com.blood.Repository.BloodBagRepository;
import com.blood.Repository.StorageEquipmentRepository;
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

    public List<ListStorageEquipmentResponse> getListStorageEquipment(Integer equipmentId, String productType){
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
}
