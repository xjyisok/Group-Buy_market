package cn.sweater.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TaskNotifyCategoryEnumVO {
    TRADE_SETTLEMENT("trade_settlement","交易结算"),
    TRADE_UNPAID2REFUND("trade_unpaid2refund","交易退单-未支付未成团"),
    TRADE_PAID2REFUND("trade_paid2refund","交易退单-已支付未成团"),
    TRADE_PAID_TEAM2REFUND("trade_paid_team2refund","交易退单-已支付已成团");
    private String code;
    private String info;
}
