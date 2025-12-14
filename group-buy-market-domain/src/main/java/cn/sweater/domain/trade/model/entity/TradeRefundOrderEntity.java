package cn.sweater.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeRefundOrderEntity {
    private String userId;
    private String orderId;
    private String teamId;
    private Long activityId;
    private String outTradeNo;


}
