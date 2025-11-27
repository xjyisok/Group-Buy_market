package cn.sweater.domain.trade.service.settlement;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyTeamSettlementAggregate;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.service.ITradeSettlementOrderService;
import cn.sweater.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import cn.sweater.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TradeSettlementOrderService implements ITradeSettlementOrderService {
    @Resource
    private ITradeRepository tradeRepository;
    @Resource(name = "tradeSettlementRuleFilter")
    private BusinessLinkedList<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity>
            tradeSettlementRuleFilter;
    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {
        log.info("拼团交易-支付订单结算:{} outTradeNo:{}", tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());
        // 1. 查询拼团信息
//        MarketPayOrderEntity marketPayOrderEntity=tradeRepository.queryNoPayMarketPayOrderByOutTradeNo(tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());
//        if(marketPayOrderEntity==null){
//            log.info("不存在的外部交易单号或者用户已经退单,无需结算userid：{}，outTradeNo{}", tradePaySuccessEntity.getUserId(), tradePaySuccessEntity.getOutTradeNo());
//            return null;
        TradeSettlementRuleFilterBackEntity tradeSettlementRuleFilterBackEntity = tradeSettlementRuleFilter.apply(
                TradeSettlementRuleCommandEntity.builder()
                        .source(tradePaySuccessEntity.getSource())
                        .channel(tradePaySuccessEntity.getChannel())
                        .userId(tradePaySuccessEntity.getUserId())
                        .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                        .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                        .build(), new TradeSettlementRuleFilterFactory.DynamicContext());
        //2. 查询组团信息
        GroupBuyTeamEntity groupBuyTeamEntity=tradeRepository.queryGroupBuyTeamByTeamId(tradeSettlementRuleFilterBackEntity.getTeamId());
        //3. 构建聚合对象
        GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate=GroupBuyTeamSettlementAggregate.builder()
                .userEntity(UserEntity.builder()
                        .userId(tradePaySuccessEntity.getUserId())
                        .build())
                .groupBuyTeamEntity(groupBuyTeamEntity)
                .tradePaySuccessEntity(tradePaySuccessEntity)
                .build();
        tradeRepository.settlementMarketPayOrder(groupBuyTeamSettlementAggregate);
        return TradePaySettlementEntity.builder()
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .userId(tradePaySuccessEntity.getUserId())
                .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build();

    }
}
