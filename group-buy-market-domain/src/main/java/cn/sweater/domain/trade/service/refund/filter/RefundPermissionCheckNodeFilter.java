package cn.sweater.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.sweater.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.domain.trade.service.refund.factory.RefundRuleFilterFactory;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 退单权限守卫节点
 * <p>
 * 超时退单（TIMEOUT）：只允许处理
 *   1. PROGRESS 团 + CREATE 支付单（未支付，关团前）
 *   2. FAIL 团   + COMPLETE 支付单（已支付，关团后由补偿任务兜底，此处拦截）
 *    实际上超时退单只处理关团前的未支付单，关团后的已支付单交给 TeamCompensateRefundJob
 *    因此超时退单只放行 PROGRESS+CREATE，其余全部 FORBIDDEN
 *
 * 用户主动退单（USER）：禁止处理 FAIL 状态的团（已关团，归补偿任务处理）
 * </p>
 */
@Service
@Slf4j
public class RefundPermissionCheckNodeFilter implements ILogicHandler<TradeRefundCommandEntity, RefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {
//    @Resource
//    private ITradeRepository tradeRepository;
    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity command,
                                           RefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
//        GroupBuyTeamEntity groupBuyTeamEntity = tradeRepository.queryGroupBuyTeamByTeamId(teamId);
//        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);
        MarketPayOrderEntity order = dynamicContext.getMarketPayOrderEntity();
        GroupBuyTeamEntity team = dynamicContext.getGroupBuyTeamEntity();
        TradeOrderStatusEnumVO orderStatus = order.getTradeOrderStatusEnumVO();
        GroupBuyOrderEnumVO teamStatus = team.getStatus();

        if (TradeRefundCommandEntity.RefundInitiatorEnum.TIMEOUT.equals(command.getRefundInitiator())) {
            // 超时退单：只允许关团前的未支付单（PROGRESS + CREATE）
            boolean allowed = GroupBuyOrderEnumVO.PROGRESS.equals(teamStatus)
                    && TradeOrderStatusEnumVO.CREATE.equals(orderStatus);
            if (!allowed) {
                log.warn("超时退单权限拒绝 userId:{} outTradeNo:{} teamStatus:{} orderStatus:{}",
                        command.getUserId(), command.getOutTradeNo(), teamStatus, orderStatus);
                return TradeRefundBehaviorEntity.builder()
                        .userId(command.getUserId())
                        .orderId(order.getOrderId())
                        .teamId(order.getTeamId())
                        .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.FORBIDDEN)
                        .build();
            }
        } else {
            // 用户主动退单：FAIL 状态的团已关团，归补偿任务处理，用户无法退
            if (GroupBuyOrderEnumVO.FAIL.equals(teamStatus)) {
                log.warn("用户退单权限拒绝：团已关闭 userId:{} outTradeNo:{} teamStatus:{}",
                        command.getUserId(), command.getOutTradeNo(), teamStatus);
                return TradeRefundBehaviorEntity.builder()
                        .userId(command.getUserId())
                        .orderId(order.getOrderId())
                        .teamId(order.getTeamId())
                        .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.FORBIDDEN)
                        .build();
            }
        }

        return next(command, dynamicContext);
    }
}
