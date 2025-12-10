package cn.sweater.domain.trade.model.aggergate;

import cn.sweater.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupBuyRefundAggregate {
    private TradeRefundOrderEntity tradeRefundOrderEntity;
    private GroupBuyProgressVO groupBuyProgressVO;
    private GroupBuyOrderEnumVO groupBuyOrderEnumVO;
    public static GroupBuyRefundAggregate buildUnpaid2RefundAggregate(TradeRefundOrderEntity orderEntity,Integer lockCount) {
        GroupBuyRefundAggregate groupBuyRefundAggregate = new GroupBuyRefundAggregate();
        groupBuyRefundAggregate.setTradeRefundOrderEntity(orderEntity);
        groupBuyRefundAggregate.setGroupBuyProgressVO(GroupBuyProgressVO.builder()
                        .lockCount(lockCount)
                .build());
        return groupBuyRefundAggregate;
    }
    public static GroupBuyRefundAggregate buildpaid2RefundAggregate(TradeRefundOrderEntity orderEntity,Integer lockCount,Integer completeCount) {
        GroupBuyRefundAggregate groupBuyRefundAggregate = new GroupBuyRefundAggregate();
        groupBuyRefundAggregate.setTradeRefundOrderEntity(orderEntity);
        groupBuyRefundAggregate.setGroupBuyProgressVO(GroupBuyProgressVO.builder()
                .lockCount(lockCount)
                .completeCount(completeCount)
                .build());
        return groupBuyRefundAggregate;
    }
    public static GroupBuyRefundAggregate buildpaidTeam2RefundAggregate(TradeRefundOrderEntity orderEntity, Integer lockCount
            , Integer completeCount, GroupBuyOrderEnumVO groupBuyOrderEnumVO) {
        GroupBuyRefundAggregate groupBuyRefundAggregate = new GroupBuyRefundAggregate();
        groupBuyRefundAggregate.setTradeRefundOrderEntity(orderEntity);
        groupBuyRefundAggregate.setGroupBuyProgressVO(GroupBuyProgressVO.builder()
                .lockCount(lockCount)
                .completeCount(completeCount)
                .build());
        groupBuyRefundAggregate.setGroupBuyOrderEnumVO(groupBuyOrderEnumVO);
        return groupBuyRefundAggregate;
    }

}
