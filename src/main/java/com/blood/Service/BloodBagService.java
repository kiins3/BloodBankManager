package com.blood.Service;

import com.blood.DTO.Blood.ListBloodBagResponse;
import com.blood.DTO.Blood.SeparateBloodRequest;
import com.blood.DTO.Blood.TestRequest;
import com.blood.Model.*;
import com.blood.Repository.*;
import com.blood.helper.QRCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BloodBagService {
    @Autowired
    private BloodBagRepository bloodBagRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StorageEquipmentRepository storageEquipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DonorRepository donorRepository;

    public List<ListBloodBagResponse> getListBloodBag(Integer bloodBagId, String bloodType, String rhFactor,
                                                      String productType, String status) {

        productType = (productType != null && !productType.trim().isEmpty()) ? productType.trim() : null;
        status = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

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

    @Transactional
    public String testResult(Integer bloodBagId, TestRequest rq) {
        BloodBag bloodBag = bloodBagRepository.findById(bloodBagId).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByEmail(currentUsername).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        Staff staff = staffRepository.findByUser(user).orElseThrow(()
                -> new RuntimeException("Không tìm thấy nhân viên"));

        Donor donor = bloodBag.getRegistration().getDonor();

        if (!bloodBag.getStatus().equalsIgnoreCase("CHO_XET_NGHIEM")) {
            throw new RuntimeException("Túi máu không hợp lệ");
        }

        TestResult testResult = new TestResult();

        testResult.setBloodBag(bloodBag);
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

        boolean isSafe = rq.getHiv().equalsIgnoreCase("Âm tính") &&
                rq.getHbv().equalsIgnoreCase("Âm tính") &&
                rq.getHcv().equalsIgnoreCase("Âm tính") &&
                rq.getSyphilis().equalsIgnoreCase("Âm tính") &&
                rq.getMalaria().equalsIgnoreCase("Âm tính");

        if (isSafe) {
            testResult.setFinalConclusion("AN TOÀN");
            bloodBag.setStatus("CHO_TACH_CHIET");
        } else {
            testResult.setFinalConclusion("KHÔNG AN TOÀN");
            bloodBag.setStatus("CHO_HUY");
        }

        testResultRepository.save(testResult);
        bloodBagRepository.save(bloodBag);
        return "Cập nhật thành công";
    }

    public ResponseEntity<?> sendEmail(Integer test_id, boolean forceResend) {
        TestResult testResult = testResultRepository.findById(test_id).orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả"));
        BloodBag bloodBag = testResult.getBloodBag();

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

        boolean isSafe = testResult.getHiv().equalsIgnoreCase("Âm tính") &&
                testResult.getHbv().equalsIgnoreCase("Âm tính") &&
                testResult.getHcv().equalsIgnoreCase("Âm tính") &&
                testResult.getSyphilis().equalsIgnoreCase("Âm tính") &&
                testResult.getMalaria().equalsIgnoreCase("Âm tính");

        // 1. Phân loại An toàn / Không an toàn trước
        if (isSafe) {
            testResult.setFinalConclusion("An toàn");
            bloodBag.setStatus("CHO_TACH_CHIET");
        } else {
            testResult.setFinalConclusion("Không an toàn");
            bloodBag.setStatus("CHO_HUY");
        }

        // 2. Chuẩn bị nội dung mail tùy theo trạng thái
        String emailBody = "";
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

        // 3. Tiến hành gửi mail nếu có email
        if (donor.getEmail() != null && !donor.getEmail().isEmpty()) {
            emailService.sendEmail(donor.getEmail(), "KẾT QUẢ XÉT NGHIỆM HIẾN MÁU", emailBody);
        } else {
            System.out.println("CẢNH BÁO: Người hiến máu không có email, bỏ qua bước gửi mail.");
        }

        // 4. Lưu database (chạy cho cả 2 trường hợp an toàn và không an toàn)
        testResult.setEmailSent(true);
        testResultRepository.save(testResult);
        bloodBagRepository.save(bloodBag);

        return ResponseEntity.ok("Cập nhật và gửi mail thành công!");
    }

    private void validateComponentVolume(Integer parentVolume, String componentType, Integer actualVolume) {
        if (actualVolume <= 0) return;

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
            String typeName = componentType.equals("RBC") ? "TUI_HONG_CAU" :
                    componentType.equals("PLS") ? "TUI_HUYET_TUONG" : "TUI_TIEU_CAU";

            throw new RuntimeException("Cảnh báo: Dung tích " + typeName + " (" + actualVolume + "ml) bất thường đối với túi mẹ " + parentVolume + "ml. Khoảng hợp lệ là từ " + minAllowed + "ml đến " + maxAllowed + "ml. Vui lòng kiểm tra lại số liệu");
        }
    }

    @Transactional
    public String separateBlood(Integer bloodBagId, SeparateBloodRequest rq) {
        BloodBag bloodBag = bloodBagRepository.findById(bloodBagId).orElseThrow(() -> new RuntimeException("Không tìm thấy túi máu"));

        if (!bloodBag.getProductType().equalsIgnoreCase("MAU_TOAN_PHAN") || !bloodBag.getStatus().equalsIgnoreCase("CHO_TACH_CHIET")){
            throw new RuntimeException("Chỉ được tách túi máu toàn phần đã xét nghiệm");
        }

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
        validateComponentVolume(parentVolume, "RBC", rq.getRedCellVolume());
        validateComponentVolume(parentVolume, "PLS", rq.getPlasmaVolume());

        String baseCode = "BG-" + bloodBagId;
        if (rq.getRedCellVolume()>0) {
            BloodBag redCellBag = new BloodBag();
            redCellBag.setParentBagId(bloodBagId);
            redCellBag.setBagCode(baseCode + "-RBC");
            redCellBag.setVolume(bloodBag.getVolume());
            redCellBag.setProductVolume(rq.getRedCellVolume());
            redCellBag.setProductType("TUI_HONG_CAU");
            redCellBag.setRhFactor(bloodBag.getRhFactor());
            redCellBag.setBloodType(bloodBag.getBloodType());
            redCellBag.setCollectedAt(bloodBag.getCollectedAt());
            redCellBag.setExpiredAt(bloodBag.getCollectedAt().plusDays(42));
            redCellBag.setTestResult(bloodBag.getTestResult());
            redCellBag.setRegistration(bloodBag.getRegistration());
            redCellBag.setStatus("CHO_BAO_QUAN");
            bloodBagRepository.save(redCellBag);
        }

        if (rq.getPlasmaVolume()>0) {
            BloodBag plasma = new BloodBag();
            plasma.setParentBagId(bloodBagId);
            plasma.setBagCode(baseCode + "-PLS");
            plasma.setVolume(bloodBag.getVolume());
            plasma.setProductVolume(rq.getPlasmaVolume());
            plasma.setProductType("TUI_HUYET_TUONG");
            plasma.setRhFactor(bloodBag.getRhFactor());
            plasma.setBloodType(bloodBag.getBloodType());
            plasma.setCollectedAt(bloodBag.getCollectedAt());
            plasma.setExpiredAt(bloodBag.getCollectedAt().plusDays(365));
            plasma.setTestResult(bloodBag.getTestResult());
            plasma.setRegistration(bloodBag.getRegistration());
            plasma.setStatus("CHO_BAO_QUAN");
            bloodBagRepository.save(plasma);
        }

        if (/*hoursPassed <= 8 &&*/ rq.getPlateletsVolume()>0) {
            validateComponentVolume(parentVolume, "PLT", rq.getPlateletsVolume());
            BloodBag plateletsBag = new BloodBag();
            plateletsBag.setParentBagId(bloodBagId);
            plateletsBag.setBagCode(baseCode + "-PLT");
            plateletsBag.setVolume(bloodBag.getVolume());
            plateletsBag.setProductVolume(rq.getPlateletsVolume());
            plateletsBag.setProductType("TUI_TIEU_CAU");
            plateletsBag.setRhFactor(bloodBag.getRhFactor());
            plateletsBag.setBloodType(bloodBag.getBloodType());
            plateletsBag.setCollectedAt(bloodBag.getCollectedAt());
            plateletsBag.setExpiredAt(bloodBag.getCollectedAt().plusDays(5));
            plateletsBag.setTestResult(bloodBag.getTestResult());
            plateletsBag.setRegistration(bloodBag.getRegistration());
            plateletsBag.setStatus("CHO_BAO_QUAN");

            bloodBagRepository.save(plateletsBag);
        }

        bloodBag.setStatus("ĐA TACH CHIET");
        bloodBagRepository.save(bloodBag);

        return "Tách chiết thành công";
    }

    @Transactional
    public String storageBlood(List<Integer> bloodBagId, Integer equipmentId) {
        StorageEquipment storageEquipment = storageEquipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tủ lưu"));

        List<BloodBag> bloodBag = bloodBagRepository.findAllById(bloodBagId);

        int currentLoad = bloodBagRepository.countByStorageEquipment_EquipmentId(equipmentId);

        if (currentLoad + bloodBag.size() > storageEquipment.getMaxCapacity()) {
            throw new RuntimeException("Tủ " + storageEquipment.getName() + " không còn chỗ chứa! Chỉ còn " +
                    (storageEquipment.getMaxCapacity() - currentLoad) + " chỗ trống");
        }

        for (BloodBag bag : bloodBag) {
            if (!bag.getStatus().equals("CHO_BAO_QUAN")){
                throw new RuntimeException("Túi máu " + bag.getBloodBagId() + " không hợp lệ để nhập kho");
            }

            if (!bag.getProductType().equals(storageEquipment.getProductType())){
                throw new RuntimeException("Không thể cất chế phẩm " + bag.getProductType() + " vào tủ chuyên dụng của " + storageEquipment.getProductType());
            }

            bag.setStorageEquipment(storageEquipment);
            bag.setStatus("SAN_SANG");
        }

        bloodBagRepository.saveAll(bloodBag);

        return "Đã lưu kho thành công";
    }

    public String discardBlood(List<Integer> bloodBagId) {
        List<BloodBag> bloodBag = bloodBagRepository.findAllById(bloodBagId);
        for (BloodBag bag : bloodBag) {
            if (!bag.getStatus().equals("CHO_HUY")) {
                bag.setStatus("DA_HUY");
                bloodBagRepository.save(bag);
            }
        }
        return "Cập nhật thành công";
    }

}
