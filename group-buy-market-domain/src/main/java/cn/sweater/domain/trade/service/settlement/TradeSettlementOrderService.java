package cn.sweater.domain.trade.service.settlement;

import cn.sweater.domain.trade.adapter.port.ITradePort;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyTeamSettlementAggregate;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.service.ITradeSettlementOrderService;
import cn.sweater.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import cn.sweater.types.design.framework.link.model2.chain.BusinessLinkedList;
import cn.sweater.types.enums.NotifyTaskHttpEnumVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TradeSettlementOrderService implements ITradeSettlementOrderService {
    @Resource
    private ITradeRepository tradeRepository;
    @Resource
    private ITradePort tradePort;
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
        Map<String,Integer>notifyResultMap=execSettlementNotifyJob(tradeSettlementRuleFilterBackEntity.getTeamId());
        log.info("回调通知拼团完结 result:{}", JSON.toJSONString(notifyResultMap));
        return TradePaySettlementEntity.builder()
                .source(tradePaySuccessEntity.getSource())
                .channel(tradePaySuccessEntity.getChannel())
                .userId(tradePaySuccessEntity.getUserId())
                .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build();

    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob() throws Exception {
        log.info("拼团交易-执行结算通知任务");

        // 查询未执行任务
        List<NotifyTaskEntity> notifyTaskEntityList = tradeRepository.queryUnExecutedNotifyTaskList();
        //System.out.println(notifyTaskEntityList.size());
        return getStringIntegerMap(notifyTaskEntityList);
    }

    private Map<String, Integer> getStringIntegerMap(List<NotifyTaskEntity> notifyTaskEntityList) throws Exception {
        int successCount = 0, errorCount = 0, retryCount = 0;
        for (NotifyTaskEntity notifyTask : notifyTaskEntityList) {
            // 回调处理 success 成功，error 失败
            String response = tradePort.groupBuyNotify(notifyTask);

            // 更新状态判断&变更数据库表回调任务状态
            if (NotifyTaskHttpEnumVO.SUCCESS.getCode().equals(response)) {
                int updateCount = tradeRepository.updateNotifyTaskStatusSuccess(notifyTask.getTeamId());
                if (1 == updateCount) {
                    successCount += 1;
                }
            } else if (NotifyTaskHttpEnumVO.ERROR.getCode().equals(response)) {
                if (notifyTask.getNotifyCount() < 5) {
                    int updateCount = tradeRepository.updateNotifyTaskStatusError(notifyTask.getTeamId());
                    if (1 == updateCount) {
                        errorCount += 1;
                    }
                } else {
                    int updateCount = tradeRepository.updateNotifyTaskStatusRetry(notifyTask.getTeamId());
                    if (1 == updateCount) {
                        retryCount += 1;
                    }
                }
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("waitCount", notifyTaskEntityList.size());
        resultMap.put("successCount", successCount);
        resultMap.put("errorCount", errorCount);
        resultMap.put("retryCount", retryCount);

        return resultMap;
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception {
        log.info("拼团交易-执行结算通知任务teamId:{}", teamId);
        List<NotifyTaskEntity> notifyTaskEntityList = tradeRepository.queryUnExecutedNotifyTaskList(teamId);
        return getStringIntegerMap(notifyTaskEntityList);
    }
}
