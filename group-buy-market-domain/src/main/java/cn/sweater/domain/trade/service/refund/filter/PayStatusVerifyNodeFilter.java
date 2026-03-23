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
 * 当本地订单状态为 CREATE(0) 时，调用支付宝 alipay.trade.close 关闭该笔交易：
 * - 关单成功（true）：支付宝确认未支付，且从此拒绝用户对该订单的任何付款请求，TOCTOU 窗口彻底关闭，继续走未支付退款。
 * - 关单失败（false，subCode=ACQ.TRADE_HAS_SUCCESS）：说明用户已经付款，修正本地状态为 COMPLETE，走已支付退款。
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

        if (dynamicContext.getMarketPayOrderEntity() == null) {
            log.warn("PayStatusVerifyNodeFilter: marketPayOrderEntity 为空，跳过核查 outTradeNo:{}", outTradeNo);
            return next(command, dynamicContext);
        }

        TradeOrderStatusEnumVO localStatus = dynamicContext.getMarketPayOrderEntity().getTradeOrderStatusEnumVO();

        if (TradeOrderStatusEnumVO.CREATE.equals(localStatus)) {
            log.info("PayStatusVerifyNodeFilter: 本地状态为 CREATE，调用支付宝关单 outTradeNo:{}", outTradeNo);
            boolean closed = tradePort.closePayOrder(outTradeNo);

            if (!closed) {
                // 关单失败 = 订单已支付，修正本地状态
                log.info("PayStatusVerifyNodeFilter: 关单失败，订单已支付，修正状态 CREATE→COMPLETE outTradeNo:{}", outTradeNo);
                tradeRepository.syncOrderStatus2Complete(outTradeNo);
                dynamicContext.getMarketPayOrderEntity().setTradeOrderStatusEnumVO(TradeOrderStatusEnumVO.COMPLETE);
            }
            // closed=true：支付宝已关单，后续用户无法再付款，状态保持 CREATE 继续走未支付退款
        }

        return next(command, dynamicContext);
    }
}
