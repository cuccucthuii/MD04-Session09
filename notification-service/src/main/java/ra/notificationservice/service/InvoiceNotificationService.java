package ra.notificationservice.service;

import org.springframework.stereotype.Service;
import ra.notificationservice.event.OrderEvent;

@Service
public class InvoiceNotificationService {

    /**
     * Giả lập gửi hóa đơn — chỉ in console theo yêu cầu bài tập (không gửi email thật).
     */
    public void notifyInvoiceSent(OrderEvent event) {
        if (event.getOrderId() == null) {
            System.out.println("Hóa đơn cho đơn hàng [không xác định] đã được gửi tới khách hàng");
            return;
        }
        System.out.println(
                "Hóa đơn cho đơn hàng [" + event.getOrderId() + "] đã được gửi tới khách hàng"
        );
    }
}
