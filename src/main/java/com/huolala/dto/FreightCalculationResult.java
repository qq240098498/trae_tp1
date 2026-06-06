package com.huolala.dto;

import com.huolala.entity.OrderFeeDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class FreightCalculationResult {
    private BigDecimal baseFreight;
    private BigDecimal mileageFee;
    private BigDecimal timeSlotSurcharge;
    private BigDecimal carryFee;
    private BigDecimal waitFee;
    private BigDecimal nightSurcharge;
    private BigDecimal floorSurcharge;
    private BigDecimal otherSurcharge;
    private BigDecimal totalAmount;
    private List<OrderFeeDetail> feeDetails;

    public FreightCalculationResult() {
        this.baseFreight = BigDecimal.ZERO;
        this.mileageFee = BigDecimal.ZERO;
        this.timeSlotSurcharge = BigDecimal.ZERO;
        this.carryFee = BigDecimal.ZERO;
        this.waitFee = BigDecimal.ZERO;
        this.nightSurcharge = BigDecimal.ZERO;
        this.floorSurcharge = BigDecimal.ZERO;
        this.otherSurcharge = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.feeDetails = new ArrayList<>();
    }

    public void calculateTotal() {
        this.totalAmount = baseFreight
                .add(mileageFee)
                .add(timeSlotSurcharge)
                .add(carryFee)
                .add(waitFee)
                .add(nightSurcharge)
                .add(floorSurcharge)
                .add(otherSurcharge);
    }
}
