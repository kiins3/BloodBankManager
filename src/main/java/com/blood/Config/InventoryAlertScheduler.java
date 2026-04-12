package com.blood.Config;

import com.blood.Model.BloodBag;
import com.blood.Repository.BloodBagRepository;
import com.blood.Repository.BloodCountProjection;
import com.blood.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class InventoryAlertScheduler {

    @Value("${inventory.alert.threshold}")
    private Integer alertThreshold;

    @Value("${admin.contact.email}")
    private String adminEmail;

    @Autowired
    private BloodBagRepository bloodBagRepository;
    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 00 7 * * *")
    public void dailyInventoryCheck() {
        System.out.println("Bat dau quet ton kho...");

        List<BloodCountProjection> inventoryCounts = bloodBagRepository.countAvailableBloodBags();


        if (inventoryCounts == null || inventoryCounts.isEmpty()) {
            System.out.println("Gửi mail cảnh báo KHO TRỐNG cho Admin...");
            String emptyMail = "<h2 style='color: red;'>CẢNH BÁO ĐỎ: KHO TRỐNG HOÀN TOÀN</h2>"
                    + "<p>Kho máu hiện tại không còn túi máu nào sẵn sàng (0 túi)! Đề nghị xử lý khẩn cấp.</p>";
            emailService.sendEmail(adminEmail, "CẢNH BÁO ĐỎ: KHO TRỐNG HOÀN TOÀN", emptyMail);
            return;
        }

        StringBuilder alertMessage = new StringBuilder();
        alertMessage.append("<h2>BÁO CÁO TỒN KHO MÁU</h2>");
        alertMessage.append("<p>Dưới đây là số lượng các túi máu đang ở trạng thái sẵn sàng:</p>");
        alertMessage.append("<ul>");

        boolean needAlert = false;
        int totalAllBags = 0;

        for (BloodCountProjection item : inventoryCounts) {
            String bloodGroup = item.getBloodType() + item.getRhFactor();
            Integer total = item.getTotal();
            totalAllBags += total;

            alertMessage.append("<li><b>Nhóm ").append(bloodGroup).append(":</b> ").append(total).append(" túi</li>");

            if (total < alertThreshold) {
                needAlert = true;
            }
        }

        alertMessage.append("</ul>");

        if (inventoryCounts.size() < 8) {
            needAlert = true;
            alertMessage.append("<p style='color: orange;'><b>LƯU Ý:</b> Có một số nhóm máu đang bị cạn kiệt hoàn toàn (0 túi) trong kho!</p>");
        }

        if (needAlert) {
            String subject = "CẢNH BÁO KHO THIẾU MÁU";
            alertMessage.append("<hr>");
            alertMessage.append("<p style='color: red; font-size: 16px;'><b>CẢNH BÁO NGHIÊM TRỌNG:</b></p>");
            alertMessage.append("<p>Có nhóm máu đang dưới mức an toàn (<strong>< ").append(alertThreshold).append(" túi</strong>).</p>");
            alertMessage.append("<p><i>Đề nghị ban quản trị tổ chức chiến dịch hiến máu gấp!</i></p>");

            System.out.println("Gửi mail cảnh báo cho Admin...");
            emailService.sendEmail(adminEmail, subject, alertMessage.toString());
        } else {
            System.out.println("Tổng kho hiện có " + totalAllBags + " túi. Các nhóm đều trên mức an toàn.");
        }
    }

    @Scheduled(cron = "0 01 23 * * *")
    public void alertExpiredBags() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(48);

        List<BloodBag> problemBags = bloodBagRepository.findExpiringAndExpiredBags(threshold);

        if (!problemBags.isEmpty()) {
            StringBuilder content = new StringBuilder();
            content.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
            content.append("<h2 style='color: #d9534f;'>BÁO CÁO CẢNH BÁO HẠN SỬ DỤNG TÚI MÁU</h2>");
            content.append("<p>Kính gửi bộ phận quản lý kho,</p>");
            content.append("<p>Hệ thống ghi nhận các túi máu sau cần được xử lý gấp:</p>");

            StringBuilder expiredContent = new StringBuilder();
            expiredContent.append("<h3 style='color: #c9302c; border-bottom: 1px solid #c9302c; padding-bottom: 5px;'>❌ DANH SÁCH ĐÃ HẾT HẠN (CẦN TIÊU HỦY GẤP)</h3>");
            expiredContent.append("<ul>");

            StringBuilder expiringContent = new StringBuilder();
            expiringContent.append("<h3 style='color: #f0ad4e; border-bottom: 1px solid #f0ad4e; padding-bottom: 5px;'>⚠️ DANH SÁCH SẮP HẾT HẠN (TRONG 48H TỚI)</h3>");
            expiringContent.append("<ul>");

            boolean hasExpired = false;
            boolean hasExpiring = false;

            for (BloodBag bag : problemBags) {
                String bagInfo = String.format(
                        "<li style='margin-bottom: 8px;'><strong>Mã:</strong> %s | <strong>Nhóm:</strong> <span style='color: red;'>%s%s</span> | <strong>Loại:</strong> %s | <strong>HSD:</strong> %s</li>",
                        bag.getBagCode(), bag.getBloodType(), bag.getRhFactor(), bag.getProductType(), bag.getExpiredAt()
                );

                if (bag.getExpiredAt().isBefore(now)) {
                    expiredContent.append(bagInfo);
                    hasExpired = true;
                } else {
                    expiringContent.append(bagInfo);
                    hasExpiring = true;
                }
            }

            expiredContent.append("</ul>");
            expiringContent.append("</ul>");

            if (hasExpired) content.append(expiredContent);
            if (hasExpiring) content.append(expiringContent);

            content.append("<br><hr>");
            content.append("<p style='font-size: 12px; color: #777;'><i>Đây là email tự động từ Hệ thống quản lý Ngân hàng máu. Vui lòng không trả lời email này.</i></p>");
            content.append("</body></html>");

            emailService.sendEmail("nthuphuong2004@gmail.com", "KHẨN CẤP: CẢNH BÁO HẠN SỬ DỤNG TÚI MÁU", content.toString());
        }
    }
}
