package cn.sweater.domain.trade.model.valobj;

import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Getter
@AllArgsConstructor
@NoArgsConstructor

public enum RefundTypeEnumVO {
    UNPAID_UNLOCK("unpaid_unlock", "unpaid2RefundStrategy", "未支付未成团"){
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO)&&TradeOrderStatusEnumVO.CREATE.equals(tradeOrderStatusEnumVO);
        }
    },

    PAID_UNFORMED("paid_unformed", "paid2RefundStrategy", "已支付未成团"){
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return GroupBuyOrderEnumVO.PROGRESS.equals(groupBuyOrderEnumVO)&&TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    },
    PAID_FORMED("paid_formed", "paidTeam2RefundStrategy", "已支付已成团"){
        @Override
        public boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO, TradeOrderStatusEnumVO tradeOrderStatusEnumVO) {
            return (GroupBuyOrderEnumVO.COMPLETE.equals(groupBuyOrderEnumVO)||GroupBuyOrderEnumVO.COMPLETE_FAIL.equals(groupBuyOrderEnumVO))&&TradeOrderStatusEnumVO.COMPLETE.equals(tradeOrderStatusEnumVO);
        }
    };
    private String code;
    private String strategy;
    private String info;
    public abstract boolean matches(GroupBuyOrderEnumVO groupBuyOrderEnumVO,TradeOrderStatusEnumVO tradeOrderStatusEnumVO);
    public static RefundTypeEnumVO getStrategy(GroupBuyOrderEnumVO groupBuyOrderEnumVO,TradeOrderStatusEnumVO tradeOrderStatusEnumVO){
        return Arrays.stream(values())
                .filter(refundType -> refundType.matches(groupBuyOrderEnumVO, tradeOrderStatusEnumVO))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("不支持的退款状态组合: groupBuyOrderStatus=" + groupBuyOrderEnumVO + ", tradeOrderStatus=" + tradeOrderStatusEnumVO));

    }
    public static RefundTypeEnumVO valueOf(Integer code) {
        switch (code) {
            case 1:
                return UNPAID_UNLOCK;
            case 2:
                return PAID_UNFORMED;
            case 3:
                return PAID_FORMED;
        }
        throw new RuntimeException("退单类型枚举值不存在: " + code);
    }


}
