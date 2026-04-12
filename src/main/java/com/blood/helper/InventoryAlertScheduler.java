package com.blood.helper;

import com.blood.Repository.BloodBagRepository;
import com.blood.Repository.BloodCountProjection;
import com.blood.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
            alertMessage.append("<hr>"); // Đường kẻ ngang
            alertMessage.append("<p style='color: red; font-size: 16px;'><b>CẢNH BÁO NGHIÊM TRỌNG:</b></p>");
            alertMessage.append("<p>Có nhóm máu đang dưới mức an toàn (<strong>< ").append(alertThreshold).append(" túi</strong>).</p>");
            alertMessage.append("<p><i>Đề nghị ban quản trị tổ chức chiến dịch hiến máu gấp!</i></p>");

            System.out.println("Gửi mail cảnh báo cho Admin...");
            emailService.sendEmail(adminEmail, subject, alertMessage.toString());
        } else {
            System.out.println("Tổng kho hiện có " + totalAllBags + " túi. Các nhóm đều trên mức an toàn.");
        }
    }
}
