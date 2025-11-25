package cn.sweater.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockMarketPayOrderResponseDTO {
    /**预购订单ID*/
    private String orderId;
    /**折扣*/
    private BigDecimal deductionPrice;
    /**交易订单状态*/
    private Integer tradeOrderStatus;

}
