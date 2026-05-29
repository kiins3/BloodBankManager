package com.blood.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.blood.dto.Blood.*;
import com.blood.model.*;
import com.blood.model.enumformat.ActionType;
import com.blood.model.enumformat.BloodBagStatus;
import com.blood.model.enumformat.ProductType;
import com.blood.model.enumformat.TestResultValue;
import com.blood.repository.*;
import com.blood.helper.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BloodBagService {

    private final BloodBagRepository bloodBagRepository;
    private final TestResultRepository testResultRepository;
    private final StaffRepository staffRepository;
    private final StorageEquipmentRepository storageEquipmentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ActionLogService actionLogService;
    public List<ListBloodBagResponse> getListBloodBag(Integer bloodBagId, String bloodType, String rhFactor,
                                                      ProductType productType, BloodBagStatus status) {

        String type = (bloodType != null && !bloodType.trim().isEmpty()) ? bloodType.trim() : null;
        String rh = (rhFactor != null && !rhFactor.trim().isEmpty())
                ? rhFactor.trim().replace(" ", "+")
                : null;

        List<BloodBag> bloodBags = bloodBagRepository.findWithFilters(bloodBagId, type, rh, productType, status);
        return bloodBags.stream().map(bag -> {
            String displayBloodGroup = "--";
            if (bag.getBloodType() != null && bag.getRhFactor() != null) {
                displayBloodGroup = bag.getBloodType() + bag.getRhFactor();
            }
                return ListBloodBagResponse.builder()
                .bloodBagId(bag.getBloodBagId())
                .bloodFactor(displayBloodGroup)
                .bloodType(bag.getBloodType())
                .rhFactor(bag.getRhFactor())
                .collectedAt(bag.getCollectedAt())
                .productType(bag.getProductType())
                .storageLocation(bag.getSafeStorageEquipmentName())
                .status(bag.getStatus())
                .build();
        })
                .collect(Collectors.toList());
    }
    @Transactional (readOnly = true)
    public BloodBagDetailResponse getBloodbagDetails(Integer bloodBagId) {
        BloodBag bloodBag = bloodBagRepository.findById(bloodBagId).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));
        Donor donor = bloodBag.getRegistration().getDonor();
        Optional<TestResult> testResultOpt = testResultRepository.findByBloodBag(bloodBag);
        
        if (!testResultOpt.isPresent()) {
            if (bloodBag.getParentBagId() != null) {
                Optional<BloodBag> parentBagOpt = bloodBagRepository.findById(bloodBag.getParentBagId());
                if (parentBagOpt.isPresent()) {
                    testResultOpt = testResultRepository.findByBloodBag(parentBagOpt.get());
                }
            }
            if (!testResultOpt.isPresent() && bloodBag.getRegistration() != null && bloodBag.getRegistration().getBloodBag() != null) {
                for (BloodBag otherBag : bloodBag.getRegistration().getBloodBag()) {
                    if (otherBag.getProductType() == ProductType.MAU_TOAN_PHAN) {
                        testResultOpt = testResultRepository.findByBloodBag(otherBag);
                        if (testResultOpt.isPresent()) {
                            break;
                        }
                    }
                }
            }
        }

        String barcodeImageBase64 = null;
        String bagCode = bloodBag.getBagCode();

        if (bagCode != null && !bagCode.trim().isEmpty()) {
            try {
                barcodeImageBase64 = QRCodeGenerator.generateBarcodeImageBase64(bagCode, 300, 100);
            } catch (Exception e) {
                log.error("LỖI HỆ THỐNG: Không thể tạo Barcode cho mã túi máu [{}]: {}", bagCode, e.getMessage());
                barcodeImageBase64 = "";
            }
        } else {
            log.warn("CẢNH BÁO: Túi máu ID {} hiện chưa có mã BagCode (Mã vạch)", bloodBagId);
            barcodeImageBase64 = "";
        }
        BloodBagDetailResponse dto = new BloodBagDetailResponse();

        dto.setDonorName(donor.getFullName());
        dto.setCollectedAt(bloodBag.getCollectedAt());
        dto.setActualVolume(bloodBag.getVolume());
        dto.setExpirationDate(bloodBag.getExpiredAt());
        dto.setBagCode(bloodBag.getBagCode());
        dto.setBarcodeBase64(barcodeImageBase64);
        dto.setBloodType(bloodBag.getBloodType());
        dto.setRhFactor(bloodBag.getRhFactor());

          if (testResultOpt.isPresent()) {
            TestResult testResult = testResultOpt.get();
            dto.setHiv(testResult.getHiv());
            dto.setHbv(testResult.getHbv());
            dto.setHcv(testResult.getHcv());
            dto.setSyphilis(testResult.getSyphilis());
            dto.setMalaria(testResult.getMalaria());
            dto.setFinalConclusion(testResult.getFinalConclusion());
        } else {
            dto.setHiv(TestResultValue.CHUA_CO);
            dto.setHbv(TestResultValue.CHUA_CO);
            dto.setHcv(TestResultValue.CHUA_CO);
            dto.setSyphilis(TestResultValue.CHUA_CO);
            dto.setMalaria(TestResultValue.CHUA_CO);
            dto.setFinalConclusion("CHƯA XÉT NGHIỆM");
        }

        dto.setStorageLocation(bloodBag.getSafeStorageEquipmentName());
        dto.setStatus(bloodBag.getStatus());
        dto.setProductType(bloodBag.getProductType());

        return dto;
    }

    @Transactional
    public ResponseEntity<?> testResult(Integer bloodBagId, TestRequest rq, boolean forceUpdate) {
        BloodBag bloodBag = bloodBagRepository.findById(bloodBagId).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(currentUsername).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        Staff staff = staffRepository.findByUser(user).orElseThrow(()
                -> new RuntimeException("Không tìm thấy nhân viên"));

        Donor donor = bloodBag.getRegistration().getDonor();
        Optional<TestResult> existingTestResult = testResultRepository.findByBloodBag(bloodBag);

        if (existingTestResult.isPresent() && !forceUpdate) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "CONFIRM_REQUIRED");
            response.put("message", "Túi máu này đã có kết quả xét nghiệm trước đó. Bạn có chắc chắn muốn ghi đè kết quả mới không?");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (!existingTestResult.isPresent() && (bloodBag.getStatus() != BloodBagStatus.CHO_XET_NGHIEM)) {
            throw new RuntimeException("Túi máu không hợp lệ");
        }

        Map<String, Object> oldTestData = null;
        if (existingTestResult.isPresent()) {
            TestResult oldResult = existingTestResult.get();
            oldTestData = new LinkedHashMap<>();
            oldTestData.put("bloodBagId", bloodBagId);
            oldTestData.put("bloodType", bloodBag.getBloodType());
            oldTestData.put("rhFactor", bloodBag.getRhFactor());
            oldTestData.put("hiv", oldResult.getHiv());
            oldTestData.put("hbv", oldResult.getHbv());
            oldTestData.put("hcv", oldResult.getHcv());
            oldTestData.put("syphilis", oldResult.getSyphilis());
            oldTestData.put("malaria", oldResult.getMalaria());
            oldTestData.put("finalConclusion", oldResult.getFinalConclusion());
            oldTestData.put("bagStatus", bloodBag.getStatus());
        }

        TestResult testResult = existingTestResult.orElse(new TestResult());

        testResult.setBloodBag(bloodBag);

        if (rq.getIsPhysicalError() != null && rq.getIsPhysicalError()) {
            testResult.setHiv(TestResultValue.CHUA_CO);
            testResult.setHbv(TestResultValue.CHUA_CO);
            testResult.setHcv(TestResultValue.CHUA_CO);
            testResult.setSyphilis(TestResultValue.CHUA_CO);
            testResult.setMalaria(TestResultValue.CHUA_CO);
            testResult.setFinalConclusion("Túi máu bị hỏng do sự cố kỹ thuật");
            testResult.setResultDate(LocalDateTime.now());
            testResult.setStaff(staff);

            bloodBag.setStatus(BloodBagStatus.CHO_HUY);

            testResultRepository.save(testResult);
            log.info("CREATE/UPDATE (PHYSICAL ERROR): State change successfully saved for entity: {}", testResult);
            bloodBagRepository.save(bloodBag);
            log.info("CREATE/UPDATE (PHYSICAL ERROR): State change successfully saved for entity: {}", bloodBag);

            Map<String, Object> newTestData = new LinkedHashMap<>();
            newTestData.put("bloodBagId", bloodBagId);
            newTestData.put("bloodType", rq.getBloodType());
            newTestData.put("rhFactor", rq.getRhFactor());
            newTestData.put("hiv", TestResultValue.CHUA_CO);
            newTestData.put("hbv", TestResultValue.CHUA_CO);
            newTestData.put("hcv", TestResultValue.CHUA_CO);
            newTestData.put("syphilis", TestResultValue.CHUA_CO);
            newTestData.put("malaria", TestResultValue.CHUA_CO);
            newTestData.put("finalConclusion", testResult.getFinalConclusion());
            newTestData.put("bagStatus", bloodBag.getStatus());
            actionLogService.log(ActionType.UPDATE_TEST_RESULT,
                    "TestResult", String.valueOf(bloodBagId), oldTestData, newTestData);

            if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {
                String collectionDate = bloodBag.getCollectedAt() != null ? bloodBag.getCollectedAt().toString() : "N/A";
                String emailBody = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <p>Viện Huyết học – Truyền máu Trung ương xin trân trọng cảm ơn Anh/Chị đã dành thời gian tham gia hiến máu vào ngày <strong>%s</strong>.</p>
                        <p>Rất tiếc, vì sự cố kỹ thuật ngoài ý muốn (túi máu bị hỏng vật lý), đơn vị máu của Anh/Chị không thể tiếp tục tiến hành xét nghiệm và sử dụng điều trị.</p>
                        <p>Chúng tôi vô cùng xin lỗi vì sự bất tiện này và rất mong tiếp tục nhận được sự hỗ trợ, đồng hành của Anh/Chị trong các chương trình hiến máu tiếp theo.</p>
                        <p>Chúc Anh/Chị và gia đình luôn mạnh khỏe, hạnh phúc.</p>
                        <p><b>Trân trọng cảm ơn!</b></p>
                        <p><b>VIỆN TRƯỞNG</b></p>
                        <p>Trần Hải Đăng</p>
                    </div>
                    """.formatted(collectionDate);
                try {
                    emailService.sendEmail(donor.getEmail(), "KẾT QUẢ XÉT NGHIỆM HIẾN MÁU", emailBody);
                    testResult.setEmailSent(true);
                    testResultRepository.save(testResult);
                    log.info("Gửi email thông báo hỏng vật lý thành công cho người hiến {}", donor.getEmail());
                } catch (Exception e) {
                    log.error("Không thể gửi email thông báo hỏng vật lý cho người hiến {}: {}", donor.getEmail(), e.getMessage());
                }
            } else {
                log.info("CẢNH BÁO: Người hiến máu không có email, bỏ qua bước gửi mail.");
            }

            return ResponseEntity.ok("Cập nhật thành công (Túi máu bị hỏng do sự cố kỹ thuật)");
        }

        bloodBag.setBloodType(rq.getBloodType());
        donor.setBloodType(rq.getBloodType());
        bloodBag.setRhFactor(rq.getRhFactor());
        donor.setRhFactor(rq.getRhFactor());
        testResult.setHiv(rq.getHiv());
        testResult.setHbv(rq.getHbv());
        testResult.setHcv(rq.getHcv());
        testResult.setResultDate(LocalDateTime.now());
        testResult.setSyphilis(rq.getSyphilis());
        testResult.setMalaria(rq.getMalaria());
        testResult.setStaff(staff);

        boolean isSafe = rq.getHiv() == TestResultValue.AM_TINH &&
                rq.getHbv() == TestResultValue.AM_TINH &&
                rq.getHcv() == TestResultValue.AM_TINH &&
                rq.getSyphilis() == TestResultValue.AM_TINH &&
                rq.getMalaria() == TestResultValue.AM_TINH;

        if (isSafe) {
            testResult.setFinalConclusion("AN TOÀN");
            bloodBag.setBagCode("BG" + bloodBag.getBloodBagId());
            bloodBag.setStatus(BloodBagStatus.CHO_TACH_CHIET);
        } else {
            testResult.setFinalConclusion("KHÔNG AN TOÀN");
            bloodBag.setStatus(BloodBagStatus.CHO_HUY);
        }

        testResultRepository.save(testResult);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", testResult);
        bloodBagRepository.save(bloodBag);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", bloodBag);

        Map<String, Object> newTestData = new LinkedHashMap<>();
        newTestData.put("bloodBagId", bloodBagId);
        newTestData.put("bloodType", rq.getBloodType());
        newTestData.put("rhFactor", rq.getRhFactor());
        newTestData.put("hiv", rq.getHiv());
        newTestData.put("hbv", rq.getHbv());
        newTestData.put("hcv", rq.getHcv());
        newTestData.put("syphilis", rq.getSyphilis());
        newTestData.put("malaria", rq.getMalaria());
        newTestData.put("finalConclusion", testResult.getFinalConclusion());
        newTestData.put("bagStatus", bloodBag.getStatus());
        actionLogService.log(ActionType.UPDATE_TEST_RESULT,
                "TestResult", String.valueOf(bloodBagId), oldTestData, newTestData);

        return ResponseEntity.ok("Cập nhật thành công");
    }

    public ResponseEntity<?> sendEmail(Integer bloodBagId, boolean forceResend) {
        BloodBag bloodBag = bloodBagRepository.findById(bloodBagId).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));
        TestResult testResult = bloodBag.getTestResult();
        Donor donor = bloodBag.getRegistration().getDonor();

        if (donor == null) {
            throw new RuntimeException("Túi máu chưa được gắn với người hiến nào");
        }
        if (testResult.isEmailSent() && !forceResend) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "CONFIRM_REQUIRED");
            response.put("message", "Kết quả này đã được gửi email trước đó. Bạn có chắc chắn muốn gửi lại không?");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        LocalDateTime nextTime = bloodBag.getCollectedAt().plusDays(84);
        String collectionDate = bloodBag.getCollectedAt().toString();
        String nextTimeString = nextTime.toString();

        boolean isPhysicalError = "Túi máu bị hỏng do sự cố kỹ thuật".equals(testResult.getFinalConclusion());
        String emailBody = "";

        if (isPhysicalError) {
            emailBody = """
                <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <p>Viện Huyết học – Truyền máu Trung ương xin trân trọng cảm ơn Anh/Chị đã dành thời gian tham gia hiến máu vào ngày <strong>%s</strong>.</p>
                    <p>Rất tiếc, vì sự cố kỹ thuật ngoài ý muốn (túi máu bị hỏng vật lý), đơn vị máu của Anh/Chị không thể tiếp tục tiến hành xét nghiệm và sử dụng điều trị.</p>
                    <p>Chúng tôi vô cùng xin lỗi vì sự bất tiện này và rất mong tiếp tục nhận được sự hỗ trợ, đồng hành của Anh/Chị trong các chương trình hiến máu tiếp theo.</p>
                    <p>Chúc Anh/Chị và gia đình luôn mạnh khỏe, hạnh phúc.</p>
                    <p><b>Trân trọng cảm ơn!</b></p>
                    <p><b>VIỆN TRƯỞNG</b></p>
                    <p>Trần Hải Đăng</p>
                </div>
                """.formatted(collectionDate);
        } else {
            String aboGroup = testResult.getBloodBag().getBloodType() != null ? testResult.getBloodBag().getBloodType() : "Chưa có KQ";
            String rhGroup = testResult.getBloodBag().getRhFactor() != null ? testResult.getBloodBag().getRhFactor(): "Chưa có KQ";

            String resultTableHtml = """
            <table border="1" cellpadding="5" cellspacing="0" style="border-collapse: collapse; width: 100%%; max-width: 800px; text-align: center;">
                <thead style="background-color: #f2f2f2;">
                    <tr>
                        <th>Stt</th>
                        <th>Tên xét nghiệm</th>
                        <th>Kết quả</th>
                    </tr>
                </thead>
                <tbody>
                    <tr><td colspan="3" style="text-align: left;"><b>I. KẾT QUẢ XÉT NGHIỆM HUYẾT THANH HỌC</b></td></tr>
                    <tr><td>1.</td><td style="text-align: left;">Nhóm máu hệ ABO</td><td><b>%s</b></td></tr>
                    <tr><td>2.</td><td style="text-align: left;">Nhóm máu hệ Rh</td><td><b>%s</b></td></tr>
                    <tr><td colspan="3" style="text-align: left;"><b>II. KẾT QUẢ XÉT NGHIỆM SÀNG LỌC TÁC NHÂN LÂY TRUYỀN QUA ĐƯỜNG MÁU</b></td></tr>
                    <tr><td>3.</td><td style="text-align: left;">Sàng lọc virus Viêm gan B</td><td>%s</td></tr>
                    <tr><td>4.</td><td style="text-align: left;">Sàng lọc virus Viêm gan C</td><td>%s</td></tr>
                    <tr><td>5.</td><td style="text-align: left;">Sàng lọc virus HIV</td><td>%s</td></tr>
                    <tr><td>6.</td><td style="text-align: left;">Sàng lọc vi khuẩn Giang mai</td><td>%s</td></tr>
                    <tr><td>7.</td><td style="text-align: left;">Sàng lọc Ký sinh trùng Sốt rét</td><td>%s</td></tr>
                </tbody>
            </table>
            """.formatted(aboGroup, rhGroup, testResult.getHbv(), testResult.getHcv(), testResult.getHiv(), testResult.getSyphilis(), testResult.getMalaria());

            boolean isSafe = testResult.getHiv() == TestResultValue.AM_TINH &&
                    testResult.getHbv() == TestResultValue.AM_TINH &&
                    testResult.getHcv() == TestResultValue.AM_TINH &&
                    testResult.getSyphilis() == TestResultValue.AM_TINH &&
                    testResult.getMalaria() == TestResultValue.AM_TINH;

            if (isSafe) {
                testResult.setFinalConclusion("An toàn");
                bloodBag.setStatus(BloodBagStatus.CHO_TACH_CHIET);
            } else {
                testResult.setFinalConclusion("Không an toàn");
                bloodBag.setStatus(BloodBagStatus.CHO_HUY);
            }

            if (isSafe) {
                emailBody = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <p>Viện Huyết học – Truyền máu Trung ương xin trân trọng cảm ơn Anh/Chị đã dành thời gian tham gia hiến máu vào ngày <strong>%s</strong>.</p>
                        <p>Đơn vị máu quý giá của Anh/Chị hiến tặng đã được Viện thực hiện đầy đủ các xét nghiệm sàng lọc máu theo quy định và <strong>được sử dụng cho người bệnh</strong>.</p>
                        <p>Thông tin về kết quả xét nghiệm sàng lọc máu cụ thể như sau:</p>
                        %s
                        <p>Anh/chị có thể hiến máu lần tiếp theo kể từ ngày <strong>%s</strong>. Mọi thông tin tư vấn về hiến máu, xin liên hệ Tổng đài chăm sóc người hiến máu: 19000001 hoặc Email: hethongmau@gmail.com.</p>
                        <p>Viện rất mong tiếp tục nhận được sự quan tâm, đồng hành của Anh/Chị trong thời gian tiếp theo. Chúc Anh/Chị và gia đình luôn mạnh khỏe, hạnh phúc.</p>
                        <p><b>Trân trọng cảm ơn!</b></p>
                        <p><b>VIỆN TRƯỞNG</b></p>
                        <p>Trần Hải Đăng</p>
                    </div>
                    """.formatted(collectionDate, resultTableHtml,nextTimeString);
            } else {
                emailBody = """
                    <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <p>Viện Huyết học – Truyền máu Trung ương xin trân trọng cảm ơn Anh/Chị đã dành thời gian tham gia hiến máu vào ngày <strong>%s</strong>.</p>
                        <p>Đơn vị máu của Anh/Chị đã được Viện thực hiện các xét nghiệm sàng lọc máu theo quy định. Thông tin về kết quả xét nghiệm sàng lọc máu cụ thể như sau:</p>
                        %s
                        <p style="color: red;"><b>Lưu ý:</b> Do có kết quả chưa đạt tiêu chuẩn trong quá trình sàng lọc, đơn vị máu này tạm thời không được sử dụng. Xin Anh/Chị vui lòng liên hệ Tổng đài 19000001 hoặc đến trực tiếp cơ sở y tế để được bác sĩ tư vấn chi tiết về kết quả này.</p>
                        <p>Chúc Anh/Chị và gia đình luôn mạnh khỏe.</p>
                        <p><b>Trân trọng!</b></p>
                    </div>
                    """.formatted(collectionDate, resultTableHtml);
            }
        }

        if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {
            emailService.sendEmail(donor.getEmail(), "KẾT QUẢ XÉT NGHIỆM HIẾN MÁU", emailBody);
        } else {
            log.info("CẢNH BÁO: Người hiến máu không có email, bỏ qua bước gửi mail.");
        }

        testResult.setEmailSent(true);
        testResultRepository.save(testResult);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", testResult);
        bloodBagRepository.save(bloodBag);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", bloodBag);

        return ResponseEntity.ok("Cập nhật và gửi mail thành công!");
    }

    public class VolumeWarningException extends RuntimeException {
        public VolumeWarningException(String message) {
            super(message);
        }
    }

    private void validateComponentVolume(Integer parentVolume, String componentType, Integer actualVolume, String overrideReason) {
        if (actualVolume == null || actualVolume <= 0) return;

        Integer minAllowed = 0;
        Integer maxAllowed = 0;

        if (parentVolume == 250) {
            if (componentType.equals("RBC")) { minAllowed = 120; maxAllowed = 200; }
            else if (componentType.equals("PLS")) { minAllowed = 80; maxAllowed = 150; }
            else if (componentType.equals("PLT")) { minAllowed = 15; maxAllowed = 45; }
        }
        else if (parentVolume == 350) {
            if (componentType.equals("RBC")) { minAllowed = 190; maxAllowed = 290; }
            else if (componentType.equals("PLS")) { minAllowed = 110; maxAllowed = 190; }
            else if (componentType.equals("PLT")) { minAllowed = 20; maxAllowed = 70; }
        }
        else if (parentVolume == 450) {
            if (componentType.equals("RBC")) { minAllowed = 250; maxAllowed = 360; }
            else if (componentType.equals("PLS")) { minAllowed = 150; maxAllowed = 250; }
            else if (componentType.equals("PLT")) { minAllowed = 30; maxAllowed = 80; }
        } else {
            return;
        }

        if (actualVolume < minAllowed || actualVolume > maxAllowed) {
            if (overrideReason == null || overrideReason.trim().isEmpty()) {
                String typeName = componentType.equals("RBC") ? "TUI_HONG_CAU" :
                        componentType.equals("PLS") ? "TUI_HUYET_TUONG" : "TUI_TIEU_CAU";

                throw new VolumeWarningException(
                        "Cảnh báo: Dung tích " + typeName + " (" + actualVolume + "ml) nằm ngoài khoảng chuẩn (" +
                                minAllowed + "-" + maxAllowed + "ml). Vui lòng xác nhận và nhập lý do ngoại lệ để tiếp tục lưu."
                );
            }

        }
    }

    @Transactional
    public String separateBlood(Integer bloodBagId, SeparateBloodRequest rq) {
        BloodBag bloodBag = bloodBagRepository.findById(bloodBagId).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));

        if (bloodBag.getProductType() != ProductType.MAU_TOAN_PHAN || bloodBag.getStatus() != BloodBagStatus.CHO_TACH_CHIET) {
            throw new RuntimeException("Chỉ được tách túi máu toàn phần đã xét nghiệm");
        }

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("bloodBagId", bloodBagId);
        oldData.put("bagCode", bloodBag.getBagCode());
        oldData.put("status", bloodBag.getStatus());
        oldData.put("productType", bloodBag.getProductType() != null ? bloodBag.getProductType().name() : null);

        LocalDateTime now = LocalDateTime.now();
        long hoursPassed = Duration.between(bloodBag.getCollectedAt(), now).toHours();
        /*if (hoursPassed >= 24) {
            throw new RuntimeException("Đã quá thời gian tách chiết chế phẩm");
        }*/

        Integer actualBloodCollected = bloodBag.getRegistration().getActualVolume();
        //14ml chong dong cho moi 100ml mau
        Integer anticoagulantVolume = (int) (bloodBag.getVolume() * 0.14);
        //gioi han toi da tui mau me + 5ml sai so con dong lai
        Integer absoluteMaxVolume = actualBloodCollected + anticoagulantVolume + 5;
        Integer totalVolume = rq.getRedCellVolume() + rq.getPlasmaVolume() + rq.getPlateletsVolume();
        if (totalVolume > absoluteMaxVolume) {
            throw new RuntimeException("Kiểm tra lại dung tích chế phẩm");
        }

        Integer parentVolume = bloodBag.getVolume();
        String baseCode = bloodBag.getBagCode();

        TestResult parentTestResult = testResultRepository.findByBloodBag(bloodBag).orElse(null);

        if (rq.getRedCellVolume() > 0) {
            validateComponentVolume(parentVolume, "RBC", rq.getRedCellVolume(), rq.getOverrideReason());
            BloodBag redCellBag = createChildBag(bloodBag, baseCode + "-RBC", ProductType.TUI_HONG_CAU, rq.getRedCellVolume(), 42);
            bloodBagRepository.save(redCellBag);
            cloneAndSaveTestResult(redCellBag, parentTestResult);
            log.info("CREATE: State change successfully saved for entity: {}", redCellBag);
        }

        if (rq.getPlasmaVolume() > 0) {
            validateComponentVolume(parentVolume, "PLS", rq.getPlasmaVolume(), rq.getOverrideReason());
            BloodBag plasmaBag = createChildBag(bloodBag, baseCode + "-PLS", ProductType.TUI_HUYET_TUONG, rq.getPlasmaVolume(), 365);
            bloodBagRepository.save(plasmaBag);
            cloneAndSaveTestResult(plasmaBag, parentTestResult);
            log.info("CREATE: State change successfully saved for entity: {}", plasmaBag);
        }

        if (/*hoursPassed <= 8 &&*/ rq.getPlateletsVolume() > 0) {
            validateComponentVolume(parentVolume, "PLT", rq.getPlateletsVolume(), rq.getOverrideReason());
            BloodBag plateletsBag = createChildBag(bloodBag, baseCode + "-PLT", ProductType.TUI_TIEU_CAU, rq.getPlateletsVolume(), 5);
            bloodBagRepository.save(plateletsBag);
            cloneAndSaveTestResult(plateletsBag, parentTestResult);
            log.info("CREATE: State change successfully saved for entity: {}", plateletsBag);
        }

        bloodBag.setStatus(BloodBagStatus.DA_TACH_CHIET);
        bloodBagRepository.save(bloodBag);
        log.info("UPDATE: State change successfully saved for entity: {}", bloodBag);

        Map<String, Object> separateData = new LinkedHashMap<>();
        separateData.put("parentBagId", bloodBagId);
        separateData.put("parentStatus", BloodBagStatus.DA_TACH_CHIET);
        separateData.put("redCellVolume", rq.getRedCellVolume());
        separateData.put("plasmaVolume", rq.getPlasmaVolume());
        separateData.put("plateletsVolume", rq.getPlateletsVolume());
        if (rq.getOverrideReason() != null && !rq.getOverrideReason().isEmpty()) {
            separateData.put("overrideReason", rq.getOverrideReason());
        }
        actionLogService.log(ActionType.SEPARATE_BLOOD, "BloodBag", String.valueOf(bloodBagId), oldData, separateData);

        return "Tách chiết thành công";
    }

    private BloodBag createChildBag(BloodBag parent, String childCode, ProductType type, Integer productVolume, int expireDays) {
        BloodBag childBag = new BloodBag();
        childBag.setParentBagId(parent.getBloodBagId());
        childBag.setBagCode(childCode);
        childBag.setVolume(parent.getVolume());
        childBag.setProductVolume(productVolume);
        childBag.setProductType(type);
        childBag.setRhFactor(parent.getRhFactor());
        childBag.setBloodType(parent.getBloodType());
        childBag.setCollectedAt(parent.getCollectedAt());
        childBag.setExpiredAt(parent.getCollectedAt().plusDays(expireDays));
        childBag.setTestResult(parent.getTestResult());
        childBag.setRegistration(parent.getRegistration());
        childBag.setStatus(BloodBagStatus.CHO_IN_NHAN);
        return childBag;
    }

    private void cloneAndSaveTestResult(BloodBag childBag, TestResult parentTestResult) {
        if (parentTestResult == null) return;

        TestResult newTestResult = new TestResult();
        newTestResult.setBloodBag(childBag);

        newTestResult.setHiv(parentTestResult.getHiv());
        newTestResult.setHbv(parentTestResult.getHbv());
        newTestResult.setHcv(parentTestResult.getHcv());
        newTestResult.setSyphilis(parentTestResult.getSyphilis());
        newTestResult.setMalaria(parentTestResult.getMalaria());
        newTestResult.setFinalConclusion(parentTestResult.getFinalConclusion());
        newTestResult.setResultDate(LocalDateTime.now());
        newTestResult.setStaff(parentTestResult.getStaff());

        testResultRepository.save(newTestResult);
    }

    @Transactional
    public PrintLabelBagResponse printLabelBag(Integer bloodBagId) {
        BloodBag bloodBag = bloodBagRepository.findById(bloodBagId).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));
        if (bloodBag.getStatus() != BloodBagStatus.CHO_IN_NHAN){
            throw new RuntimeException("Túi máu chưa sẵn sàng để in dán nhãn");
        }

        Map<String, Object> oldData = new LinkedHashMap<>();
        oldData.put("bloodBagId", bloodBagId);
        oldData.put("bagCode", bloodBag.getBagCode());
        oldData.put("status", BloodBagStatus.CHO_IN_NHAN);

        String barcodeImageBase64 = QRCodeGenerator.generateBarcodeImageBase64(bloodBag.getBagCode(), 300, 100);

        Map<String, Object> printData = new LinkedHashMap<>();
        printData.put("bloodBagId", bloodBagId);
        printData.put("bagCode", bloodBag.getBagCode());
        printData.put("oldStatus", BloodBagStatus.CHO_IN_NHAN);
        printData.put("newStatus", BloodBagStatus.CHO_BAO_QUAN);

        bloodBag.setStatus(BloodBagStatus.CHO_BAO_QUAN);
        bloodBagRepository.save(bloodBag);
        log.info("CREATE/UPDATE: State change successfully saved for entity: {}", bloodBag);

        actionLogService.log(ActionType.PRINT_LABEL_BAG,
                "BloodBag", String.valueOf(bloodBagId), oldData, printData);

        return PrintLabelBagResponse.builder()
                .bagCode(bloodBag.getBagCode())
                .barCodeBag(barcodeImageBase64)
                .bloodType(bloodBag.getBloodType())
                .rhFactor(bloodBag.getRhFactor())
                .productType(bloodBag.getProductType())
                .expiryDate(bloodBag.getExpiredAt())
                .volume(bloodBag.getProductVolume() != null ? bloodBag.getProductVolume() : bloodBag.getVolume())
                .build();
    }


    @Transactional
    public String storageBlood(List<Integer> bloodBagId, Integer equipmentId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(currentUsername).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        Staff staff = staffRepository.findByUser(user).orElseThrow(()
                -> new RuntimeException("Không tìm thấy nhân viên"));

        StorageEquipment storageEquipment = storageEquipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tủ lưu"));

        List<BloodBag> bloodBag = bloodBagRepository.findAllById(bloodBagId);

        Map<Integer, Map<String, Object>> oldDataMap = new LinkedHashMap<>();
        for (BloodBag bag : bloodBag) {
            Map<String, Object> oldBagData = new LinkedHashMap<>();
            oldBagData.put("bloodBagId", bag.getBloodBagId());
            oldBagData.put("bagCode", bag.getBagCode());
            oldBagData.put("status", bag.getStatus());
            oldBagData.put("storageEquipmentId", bag.getStorageEquipment() != null ? bag.getStorageEquipment().getEquipmentId() : null);
            oldBagData.put("storageEquipmentName", bag.getStorageEquipment() != null ? bag.getStorageEquipment().getName() : null);
            oldDataMap.put(bag.getBloodBagId(), oldBagData);
        }

        int currentLoad = bloodBagRepository.countActiveBagsInEquipment(equipmentId, BloodBagStatus.SAN_SANG);
        if (currentLoad + bloodBag.size() > storageEquipment.getMaxCapacity()) {
            throw new RuntimeException("Tủ " + storageEquipment.getName() + " không còn chỗ chứa! Chỉ còn " +
                    (storageEquipment.getMaxCapacity() - currentLoad) + " chỗ trống");
        }

        LocalDateTime currentTime = LocalDateTime.now();

        for (BloodBag bag : bloodBag) {
            if (bag.getStatus() != BloodBagStatus.CHO_BAO_QUAN){
                throw new RuntimeException("Túi máu " + bag.getBloodBagId() + " không hợp lệ để nhập kho");
            }

            if (!bag.getProductType().equals(storageEquipment.getProductType())){
                throw new RuntimeException("Không thể cất chế phẩm " + bag.getProductType() + " vào tủ chuyên dụng của " + storageEquipment.getProductType());
            }

            bag.setStorageEquipment(storageEquipment);
            bag.setStatus(BloodBagStatus.SAN_SANG);
            bag.setStoredAt(currentTime);
            bag.setStaff(staff);
        }

        bloodBagRepository.saveAll(bloodBag);
        log.info("CREATE/UPDATE: Bulk state change successfully saved for list of size: {}", bloodBag != null ? bloodBag.size() : 0);

        for (BloodBag bag : bloodBag) {
            Map<String, Object> storageData = new LinkedHashMap<>();
            storageData.put("bloodBagId", bag.getBloodBagId());
            storageData.put("bagCode", bag.getBagCode());
            storageData.put("equipmentId", equipmentId);
            storageData.put("equipmentName", storageEquipment.getName());
            storageData.put("newStatus", BloodBagStatus.SAN_SANG);
            actionLogService.log(ActionType.STORAGE_BLOOD,
                    "BloodBag", String.valueOf(bag.getBloodBagId()), oldDataMap.get(bag.getBloodBagId()), storageData);
        }

        return "Đã lưu kho thành công";
    }

    @Transactional
    public String transferStorage(List<Integer> bloodBagIds, Integer equipmentId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(currentUsername).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        Staff staff = staffRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        StorageEquipment targetEquipment = storageEquipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tủ lưu mục tiêu"));

        List<BloodBag> bloodBags = bloodBagRepository.findAllById(bloodBagIds);
        if (bloodBags.isEmpty()) {
            throw new RuntimeException("Danh sách túi máu chuyển kho trống!");
        }

        Map<Integer, Map<String, Object>> oldDataMap = new LinkedHashMap<>();
        for (BloodBag bag : bloodBags) {
            Map<String, Object> oldBagData = new LinkedHashMap<>();
            oldBagData.put("bloodBagId", bag.getBloodBagId());
            oldBagData.put("bagCode", bag.getBagCode());
            oldBagData.put("status", bag.getStatus());
            oldBagData.put("equipmentId", bag.getStorageEquipment() != null ? bag.getStorageEquipment().getEquipmentId() : null);
            oldBagData.put("equipmentName", bag.getStorageEquipment() != null ? bag.getStorageEquipment().getName() : null);
            oldDataMap.put(bag.getBloodBagId(), oldBagData);
        }

        int currentLoad = bloodBagRepository.countActiveBagsInEquipment(equipmentId, BloodBagStatus.SAN_SANG);
        if (currentLoad + bloodBags.size() > targetEquipment.getMaxCapacity()) {
            throw new RuntimeException("Tủ " + targetEquipment.getName() + " không đủ chỗ! Chỉ còn " +
                    (targetEquipment.getMaxCapacity() - currentLoad) + " chỗ trống, không thể nhận thêm " + bloodBags.size() + " túi.");
        }

        LocalDateTime currentTime = LocalDateTime.now();

        for (BloodBag bag : bloodBags) {
            if (bag.getStatus() != BloodBagStatus.SAN_SANG) {
                throw new RuntimeException("Túi máu #" + bag.getBloodBagId() + " không ở trạng thái 'SẴN SÀNG'. Không thể chuyển kho!");
            }

            if (!bag.getProductType().equals(targetEquipment.getProductType())) {
                throw new RuntimeException("Xung đột: Không thể chuyển chế phẩm " + bag.getProductType() +
                        " sang tủ chuyên dụng của " + targetEquipment.getProductType());
            }

            if (bag.getStorageEquipment() != null && bag.getStorageEquipment().getEquipmentId().equals(equipmentId)) {
                throw new RuntimeException("Túi máu #" + bag.getBloodBagId() + " đã nằm sẵn trong tủ này rồi.");
            }

            bag.setStorageEquipment(targetEquipment);
            bag.setStoredAt(currentTime);
            bag.setStaff(staff);
        }

        bloodBagRepository.saveAll(bloodBags);
        log.info("TRANSFER: Bulk storage transfer successfully saved for list of size: {} to equipment: {}", bloodBags.size(), equipmentId);

        for (BloodBag bag : bloodBags) {
            Map<String, Object> transferData = new LinkedHashMap<>();
            transferData.put("bloodBagId", bag.getBloodBagId());
            transferData.put("bagCode", bag.getBagCode());
            transferData.put("targetEquipmentId", equipmentId);
            transferData.put("targetEquipmentName", targetEquipment.getName());
            actionLogService.log(ActionType.TRANSFER_STORAGE,
                    "BloodBag", String.valueOf(bag.getBloodBagId()), oldDataMap.get(bag.getBloodBagId()), transferData);
        }

        return "Đã chuyển đổi kho bảo quản thành công cho " + bloodBags.size() + " túi máu.";
    }

    public String discardBlood(List<Integer> bloodBagId) {
        List<BloodBag> bloodBags = bloodBagRepository.findAllById(bloodBagId);

        Map<Integer, Map<String, Object>> oldDataMap = new LinkedHashMap<>();
        for (BloodBag bag : bloodBags) {
            Map<String, Object> oldBagData = new LinkedHashMap<>();
            oldBagData.put("bloodBagId", bag.getBloodBagId());
            oldBagData.put("bagCode", bag.getBagCode());
            oldBagData.put("status", bag.getStatus());
            oldDataMap.put(bag.getBloodBagId(), oldBagData);
        }

        for (BloodBag bag : bloodBags) {
            if (bag.getStatus() != BloodBagStatus.CHO_HUY) {
                throw new RuntimeException("Túi máu #" + bag.getBloodBagId() + " đang ở trạng thái '" + bag.getStatus() + "'. Chỉ được phép hủy những túi máu đang 'Chờ hủy'.");
            }
            bag.setStatus(BloodBagStatus.DA_HUY);
        }
        bloodBagRepository.saveAll(bloodBags);
        log.info("DISCARD: Bulk state change saved for {} bags", bloodBags.size());

        for (BloodBag bag : bloodBags) {
            Map<String, Object> discardData = new LinkedHashMap<>();
            discardData.put("bloodBagId", bag.getBloodBagId());
            discardData.put("bagCode", bag.getBagCode());
            discardData.put("newStatus", BloodBagStatus.DA_HUY);
            actionLogService.log(ActionType.DISCARD_BAG,
                    "BloodBag", String.valueOf(bag.getBloodBagId()), oldDataMap.get(bag.getBloodBagId()), discardData);
        }

        return "Đã hủy thành công " + bloodBags.size() + " túi máu";
    }

    @Transactional
    public String undoDiscardBlood(List<Integer> bloodBagIds) {
        List<BloodBag> bloodBags = bloodBagRepository.findAllById(bloodBagIds);
        if (bloodBags.isEmpty()) {
            throw new RuntimeException("Danh sách túi máu hoàn tác trống!");
        }

        Map<Integer, Map<String, Object>> oldDataMap = new LinkedHashMap<>();
        for (BloodBag bag : bloodBags) {
            Map<String, Object> oldBagData = new LinkedHashMap<>();
            oldBagData.put("bloodBagId", bag.getBloodBagId());
            oldBagData.put("bagCode", bag.getBagCode());
            oldBagData.put("status", bag.getStatus());
            oldDataMap.put(bag.getBloodBagId(), oldBagData);
        }

        LocalDateTime now = LocalDateTime.now();

        for (BloodBag bag : bloodBags) {
            if (bag.getStatus() != BloodBagStatus.DA_HUY && bag.getStatus() != BloodBagStatus.CHO_HUY) {
                throw new RuntimeException("Túi máu #" + bag.getBloodBagId() + " không thể hoàn tác!");
            }

            if (bag.getExpiredAt() != null && bag.getExpiredAt().isBefore(now)) {
                throw new RuntimeException("Lỗi túi máu #" + bag.getBloodBagId() + ": Túi máu này đã quá hạn sử dụng. Bắt buộc phải tiêu hủy!");
            }

            if (bag.getUpdatedAt() != null) {
                long hoursSinceDiscard = java.time.temporal.ChronoUnit.HOURS.between(bag.getUpdatedAt(), now);
                if (hoursSinceDiscard > 24) {
                    throw new RuntimeException("Lỗi túi máu #" + bag.getBloodBagId() + ": Đã quá thời gian cho phép hoàn tác (24 giờ). Túi máu này được mặc định đã tiêu hủy vật lý.");
                }
            }

            if (bag.getTestResult().getFinalConclusion() != null) {
                bag.getTestResult().setFinalConclusion(null);
            }
            bag.setStatus(BloodBagStatus.CHO_XET_NGHIEM);
        }

        bloodBagRepository.saveAll(bloodBags);
        log.info("UNDO DISCARD: Rollback status saved for {} bags", bloodBags.size());

        for (BloodBag bag : bloodBags) {
            Map<String, Object> undoData = new LinkedHashMap<>();
            undoData.put("bloodBagId", bag.getBloodBagId());
            undoData.put("bagCode", bag.getBagCode());
            undoData.put("oldStatus", BloodBagStatus.DA_HUY);
            undoData.put("newStatus", BloodBagStatus.CHO_HUY);
            actionLogService.log(ActionType.UNDO_DISCARD,
                    "BloodBag", String.valueOf(bag.getBloodBagId()), oldDataMap.get(bag.getBloodBagId()), undoData);
        }

        return "Đã hoàn tác trạng thái hủy thành công cho " + bloodBags.size() + " túi máu.";
    }
}
