package cn.sweater.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.domain.trade.adapter.port.ITradePort;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.sweater.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.domain.trade.service.refund.factory.RefundRuleFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 临界并发支付状态核查节点
 * <p>
 * 仅对超时退单（TIMEOUT）生效：
 * 系统在拼团超时那一刻发起退单，此时用户可能恰好完成了支付宝付款，存在竞态窗口。
 * 通过调用支付宝 alipay.trade.close 来确认支付状态：
 * - 关单成功（true）：支付宝确认未支付，窗口彻底关闭，继续走未支付退款。
 * - 关单失败（false，subCode=ACQ.TRADE_HAS_SUCCESS）：用户刚刚付款成功，修正本地状态为 COMPLETE，
 *   由 RefundPermissionCheckNodeFilter 在下次执行时拦截（TIMEOUT 不允许处理已支付单），
 *   等待补偿任务处理。
 *
 * 用户主动退单（USER）直接跳过：用户自己点退款不会同时在付款，不存在竞态。
 * </p>
 */
@Service
@Slf4j
public class PayStatusVerifyNodeFilter implements ILogicHandler<TradeRefundCommandEntity, RefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {

    @Resource
    private ITradePort tradePort;
    @Resource
    private ITradeRepository tradeRepository;

    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity command,
                                           RefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        String outTradeNo = command.getOutTradeNo();

        // 用户主动退单：用户自己发起退款，不存在同时付款的竞态，直接跳过
        if (TradeRefundCommandEntity.RefundInitiatorEnum.USER.equals(command.getRefundInitiator())) {
            return next(command, dynamicContext);
        }

        if (dynamicContext.getMarketPayOrderEntity() == null) {
            log.warn("PayStatusVerifyNodeFilter: marketPayOrderEntity 为空，跳过核查 outTradeNo:{}", outTradeNo);
            return next(command, dynamicContext);
        }

        TradeOrderStatusEnumVO localStatus = dynamicContext.getMarketPayOrderEntity().getTradeOrderStatusEnumVO();

        // 超时退单：本地状态为 CREATE 时，调支付宝关单确认是否存在临界支付
        if (TradeOrderStatusEnumVO.CREATE.equals(localStatus)) {
            log.info("PayStatusVerifyNodeFilter: 超时退单-本地状态为 CREATE，调用支付宝关单核查 outTradeNo:{}", outTradeNo);
            boolean closed = tradePort.closePayOrder(outTradeNo);

            if (!closed) {
                // 关单失败 = 用户在超时瞬间完成了支付，修正本地状态
                // 超时退单无权处理已支付单，直接返回 FORBIDDEN
                // 该订单后续由补偿扫描任务（TeamCompensateRefundJob）关团后统一处理
                log.info("PayStatusVerifyNodeFilter: 关单失败，用户临界支付成功，修正状态 CREATE→COMPLETE，超时退单终止 outTradeNo:{}", outTradeNo);
                tradeRepository.syncOrderStatus2Complete(outTradeNo);
                return TradeRefundBehaviorEntity.builder()
                        .userId(command.getUserId())
                        .orderId(dynamicContext.getMarketPayOrderEntity().getOrderId())
                        .teamId(dynamicContext.getMarketPayOrderEntity().getTeamId())
                        .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.FORBIDDEN)
                        .build();
            }
            // closed=true：支付宝确认未支付，继续走未支付退款
        }

        return next(command, dynamicContext);
    }
}
