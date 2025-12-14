package cn.sweater.domain.trade.model.valobj;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamRefundSuccess {
    /**退单类型*/
    private String type;
    private String teamId;
    private String userId;
    private String orderId;
    private Long activityId;
    private String outTradeNo;

}
