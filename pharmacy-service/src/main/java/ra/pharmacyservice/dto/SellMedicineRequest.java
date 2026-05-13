package ra.pharmacyservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SellMedicineRequest {

    @NotNull
    private Long orderId;

    @NotNull
    private Long medicineId;

    @NotNull
    @Positive
    private Integer quantity;

    /** Tiền thuốc trước VAT — dùng cho hiển thị hóa đơn như luồng tính tiền hiện có */
    @NotNull
    private Double totalMoney;
}
