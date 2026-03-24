package cn.sweater.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeRefundCommandEntity {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 外部交易单号
     */
    private String outTradeNo;

    /** 渠道 */
    private String source;

    /** 来源 */
    private String channel;

    /** 退单发起方类型 */
    private RefundInitiatorEnum refundInitiator;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public enum RefundInitiatorEnum {
        TIMEOUT("timeout", "超时系统退单"),
        USER("user", "用户主动退单"),
        ;
        private String code;
        private String info;
    }

}
