package com.blood.service;

import com.blood.config.WebSocketConfig;
import com.blood.dto.ReturnLog.*;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.BloodRequest.ReturnRequest;
import com.blood.model.*;
import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ReturnStatus;
import com.blood.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnLogService {

    private final ReturnLogRepository returnLogRepository;
    private final BloodBagRepository bloodBagRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final ActionLogService actionLogService;
    private final SimpMessagingTemplate messagingTemplate;

    // =========================================================================
    // 1. XEM DANH SÁCH HÀNG TRẢ LẠI
    // =========================================================================

    @Transactional(readOnly = true)
    public Page<ReturnLogResponse> getAllReturns(String hospitalName, ReturnStatus action, Pageable pageable) {

        Page<ReturnLog> logPage = returnLogRepository.searchReturns(hospitalName, action, pageable);

        return logPage.map(returnLogItem -> {
            ReturnLogResponse dto = new ReturnLogResponse();
            dto.setLogId(returnLogItem.getReturnOrderId());
            dto.setReason(returnLogItem.getReason());
            dto.setActionTaken(returnLogItem.getActionTaken());
            dto.setCreatedAt(returnLogItem.getCreatedAt());

            if (returnLogItem.getBloodBag() != null) {
                BloodBag bag = returnLogItem.getBloodBag();
                dto.setBloodBagId(bag.getBloodBagId());
                dto.setBloodType(bag.getBloodType());
                dto.setProductType(bag.getProductType());
                dto.setBagCode(bag.getBagCode());
                dto.setCurrentBagStatus(bag.getStatus());
            }

            if (returnLogItem.getHospital() != null) {
                dto.setHospitalName(returnLogItem.getHospital().getHospitalName());
            }

            if (returnLogItem.getStaff() != null) {
                dto.setStaffName(returnLogItem.getStaff().getFullName());
            }

            return dto;
        });
    }

    // =========================================================================
    // 2. XEM CHI TIẾT ĐƠN HOÀN TRẢ (theo returnOrderId hoặc nhóm logId)
    // =========================================================================

    @Transactional(readOnly = true)
    public ReturnOrderDetailResponse getReturnOrderDetail(Integer returnOrderId) {
        List<ReturnLog> logs = returnLogRepository.findByReturnOrderId(returnOrderId);

        if (logs.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn hoàn trả với ID: " + returnOrderId);
        }

        ReturnLog first = logs.get(0);

        ReturnOrderDetailResponse response = new ReturnOrderDetailResponse();
        response.setReturnOrderId(returnOrderId);
        response.setHospitalName(first.getHospital() != null ? first.getHospital().getHospitalName() : null);
        response.setCreatedAt(first.getCreatedAt());
        response.setProcessedBy(first.getStaff() != null ? first.getStaff().getFullName() : null);

        List<ReturnOrderDetailResponse.BagItem> bagItems = logs.stream().map(log -> {
            ReturnOrderDetailResponse.BagItem item = new ReturnOrderDetailResponse.BagItem();
            item.setLogId(log.getReturnOrderId());
            item.setReason(log.getReason());
            item.setActionTaken(log.getActionTaken());

            if (log.getBloodBag() != null) {
                BloodBag bag = log.getBloodBag();
                item.setBloodBagId(bag.getBloodBagId());
                item.setBagCode(bag.getBagCode());
                item.setBloodType(bag.getBloodType());
                item.setRhFactor(bag.getRhFactor());
                item.setProductType(bag.getProductType());
                item.setVolume(bag.getVolume());
                item.setExpiredAt(bag.getExpiredAt());
                item.setCurrentStatus(bag.getStatus());
                item.setExpired(bag.getExpiredAt() != null && LocalDateTime.now().isAfter(bag.getExpiredAt()));
            }

            return item;
        }).collect(Collectors.toList());

        response.setBags(bagItems);

        long pendingCount = bagItems.stream()
                .filter(b -> b.getActionTaken() == null || b.getActionTaken() == ReturnStatus.DANG_CHO)
                .count();
        response.setPendingCount((int) pendingCount);
        response.setTotalCount(bagItems.size());
        response.setAllProcessed(pendingCount == 0);

        return response;
    }

    // =========================================================================
    // 3. SCAN BARCODE — highlight túi trong danh sách đơn hoàn trả
    // =========================================================================

    @Transactional(readOnly = true)
    public BloodBagReturnInfoResponse scanBagForReturn(String bagCode) {
        BloodBag bloodBag = bloodBagRepository.findByBagCode(bagCode)
                .orElseThrow(() -> new RuntimeException("Mã vạch không hợp lệ hoặc không tìm thấy túi máu."));

        if (bloodBag.getStatus() != BloodBagStatus.DA_XUAT && bloodBag.getStatus() != BloodBagStatus.CHO_KIEM_DINH && bloodBag.getStatus() != BloodBagStatus.HOAN_TRA) {
            throw new RuntimeException("Túi máu này đang ở trạng thái '" + bloodBag.getStatus() +
                    "'. Chỉ túi máu ĐÃ XUẤT KHO, HOÀN TRẢ hoặc CHỜ KIỂM ĐỊNH mới có thể làm thủ tục hoàn trả.");
        }

        boolean isExpired = bloodBag.getExpiredAt() != null && LocalDateTime.now().isAfter(bloodBag.getExpiredAt());

        String warning;
        if (isExpired) {
            warning = "CẢNH BÁO: Túi máu đã HẾT HẠN SỬ DỤNG. Bắt buộc chuyển sang trạng thái CHỜ HỦY!";
        } else {
            warning = "Túi máu đủ điều kiện. Vui lòng kiểm tra thực tế và chọn thao tác phù hợp.";
        }

        return BloodBagReturnInfoResponse.builder()
                .bloodBagId(bloodBag.getBloodBagId())
                .bagCode(bloodBag.getBagCode())
                .bloodType(bloodBag.getBloodType())
                .rhFactor(bloodBag.getRhFactor())
                .productType(bloodBag.getProductType())
                .volume(bloodBag.getVolume())
                .expiredAt(bloodBag.getExpiredAt())
                .currentStatus(bloodBag.getStatus())
                .isExpired(isExpired)
                .warningMessage(warning)
                .build();
    }

    // =========================================================================
    // 4. XỬ LÝ KIỂM TRA HÀNG LOẠT — nhân viên tiếp nhận xác nhận từng túi/hàng loạt
    //    Kết quả: CHO_HUY hoặc CHO_KIEM_DINH. CHƯA nhập kho — để kỹ thuật viên xử lý.
    // =========================================================================

    @Transactional
    public BulkInspectResultResponse processBulkInspection(Integer processorId, BulkInspectRequest request) {

        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Danh sách kiểm tra trống!");
        }

        Staff staff = staffRepository.findById(processorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên xử lý"));

        int successCount = 0;
        int pendingHuy = 0;
        int pendingKiemDinh = 0;
        List<String> errors = new ArrayList<>();

        for (InspectItemRequest item : request.getItems()) {
            try {
                InspectResult result = inspectSingleBag(staff, item);
                successCount++;
                if (result == InspectResult.CHO_HUY) pendingHuy++;
                else pendingKiemDinh++;
            } catch (Exception e) {
                errors.add("Túi #" + item.getBloodBagId() + ": " + e.getMessage());
                log.warn("Lỗi khi kiểm tra túi {}: {}", item.getBloodBagId(), e.getMessage());
            }
        }

        boolean allDone = false;

        if (request.getReturnOrderId() != null) {
            allDone = checkAllLogsInOrderProcessed(request.getReturnOrderId());
            if (allDone) {
                notifyTechniciansAfterReturnInspection(request.getReturnOrderId(), pendingHuy, pendingKiemDinh);
            }
        }

        String summary = String.format(
                "Đã xử lý %d/%d túi. CHỜ HỦY: %d, CHỜ KIỂM ĐỊNH: %d.",
                successCount, request.getItems().size(), pendingHuy, pendingKiemDinh
        );

        return BulkInspectResultResponse.builder()
                .summary(summary)
                .successCount(successCount)
                .pendingHuyCount(pendingHuy)
                .pendingKiemDinhCount(pendingKiemDinh)
                .errors(errors)
                .allOrderProcessed(allDone)
                .build();
    }


    private InspectResult inspectSingleBag(Staff staff, InspectItemRequest item) {

        BloodBag bloodBag = bloodBagRepository.findById(item.getBloodBagId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));

        if (bloodBag.getStatus() != BloodBagStatus.DA_XUAT && bloodBag.getStatus() != BloodBagStatus.CHO_KIEM_DINH && bloodBag.getStatus() != BloodBagStatus.HOAN_TRA) {
            throw new RuntimeException("Túi [" + bloodBag.getBagCode() + "] không ở trạng thái hợp lệ để kiểm tra hoàn trả.");
        }

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("bloodBagId", bloodBag.getBloodBagId());
        oldData.put("bagCode", bloodBag.getBagCode());
        oldData.put("status", bloodBag.getStatus() != null ? bloodBag.getStatus().name() : null);

        boolean isExpired = bloodBag.getExpiredAt() != null && LocalDateTime.now().isAfter(bloodBag.getExpiredAt());
        String reason = item.getReason() != null ? item.getReason().toLowerCase() : "";
        boolean isTechnicalIssue = reason.contains("hỏng") || reason.contains("rách")
                || reason.contains("vỡ") || reason.contains("lỗi") || reason.contains("thủng");

        InspectResult result;
        ReturnStatus logAction;

        if (isExpired || isTechnicalIssue || item.getAction() == ReturnStatus.TIEU_HUY) {
            bloodBag.setStatus(BloodBagStatus.CHO_HUY);
            bloodBag.setStorageEquipment(null);
            logAction = ReturnStatus.TIEU_HUY;
            result = InspectResult.CHO_HUY;
            log.info("Túi [{}] → CHO_HUY (expired={}, technical={})", bloodBag.getBagCode(), isExpired, isTechnicalIssue);
        } else {
            bloodBag.setStatus(BloodBagStatus.CHO_KIEM_DINH);
            bloodBag.setStorageEquipment(null);
            logAction = ReturnStatus.KIEM_DINH;
            result = InspectResult.CHO_KIEM_DINH;
            log.info("Túi [{}] → CHO_KIEM_DINH", bloodBag.getBagCode());
        }

        bloodBagRepository.save(bloodBag);

        ReturnLog returnLog = returnLogRepository
                .findTopByBloodBagAndActionTaken(bloodBag, ReturnStatus.DANG_CHO)
                .orElseGet(() -> {
                    ReturnLog newLog = new ReturnLog();
                    newLog.setBloodBag(bloodBag);
                    newLog.setStaff(staff);
                    return newLog;
                });

        returnLog.setActionTaken(logAction);
        returnLog.setReason(item.getReason());
        returnLog.setStaff(staff);
        returnLogRepository.save(returnLog);

        Map<String, Object> newData = new LinkedHashMap<>();
        newData.put("bloodBagId", bloodBag.getBloodBagId());
        newData.put("action", logAction.name());
        newData.put("reason", item.getReason());
        newData.put("newBagStatus", bloodBag.getStatus().name());
        actionLogService.log(ActionType.PROCESS_RETURN, "BloodBag",
                String.valueOf(bloodBag.getBloodBagId()), oldData, newData);

        return result;
    }

    private boolean checkAllLogsInOrderProcessed(Integer returnOrderId) {
        List<ReturnLog> logs = returnLogRepository.findByReturnOrderId(returnOrderId);
        return logs.stream().allMatch(log -> log.getActionTaken() != null
                && log.getActionTaken() != ReturnStatus.DANG_CHO);
    }

    private void notifyTechniciansAfterReturnInspection(Integer returnOrderId, int pendingHuy, int pendingKiemDinh) {
        try {
            NotiMessReturnOrder noti = new NotiMessReturnOrder(
                    returnOrderId,
                    pendingKiemDinh,
                    pendingHuy,
                    String.format(
                            "Đơn hoàn trả #%d đã kiểm tra xong. %d túi CHỜ KIỂM ĐỊNH, %d túi CHỜ HỦY.",
                            returnOrderId, pendingKiemDinh, pendingHuy
                    )
            );

            String topicDestination = "/topic/return-order/" + returnOrderId + "/inspection-done";
            messagingTemplate.convertAndSend(topicDestination, noti);

            log.info("Đã phát thông báo WebSocket cho kỹ thuật viên — đơn hoàn trả #{}", returnOrderId);
        } catch (Exception e) {
            log.error("Lỗi khi gửi WebSocket thông báo kỹ thuật viên: {}", e.getMessage());
        }
    }

    private enum InspectResult {
        CHO_HUY, CHO_KIEM_DINH
    }
}