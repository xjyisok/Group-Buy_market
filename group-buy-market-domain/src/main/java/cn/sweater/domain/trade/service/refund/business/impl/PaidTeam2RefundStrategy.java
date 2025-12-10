package cn.sweater.domain.trade.service.refund.business.impl;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyRefundAggregate;
import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.sweater.domain.trade.service.ITradeTaskService;
import cn.sweater.domain.trade.service.refund.business.IRefundOrderStrategy;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import cn.sweater.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Service("paidTeam2RefundStrategy")
@Slf4j
public class PaidTeam2RefundStrategy implements IRefundOrderStrategy {
    @Resource
    private ITradeRepository tradeRepository;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private ITradeTaskService tradeTaskService;
    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        GroupBuyTeamEntity groupBuyTeamEntity=tradeRepository.queryGroupBuyTeamByTeamId(tradeRefundOrderEntity.getTeamId());
        Integer completeCount=groupBuyTeamEntity.getCompleteCount();
        GroupBuyOrderEnumVO groupBuyOrderEnumVO=1==completeCount?GroupBuyOrderEnumVO.FAIL:GroupBuyOrderEnumVO.COMPLETE_FAIL;
        NotifyTaskEntity notifyTaskEntity=tradeRepository.paidTeam2Refund(GroupBuyRefundAggregate.buildpaidTeam2RefundAggregate(tradeRefundOrderEntity,-1,-1
        ,groupBuyOrderEnumVO));
        // 2. 发送MQ消息
        if (null != notifyTaskEntity) {
            threadPoolExecutor.execute(() -> {
                Map<String, Integer> notifyResultMap = null;
                try {
                    notifyResultMap = tradeTaskService.execNotifyJob(notifyTaskEntity);
                    log.info("回调通知交易退单 result:{}", JSON.toJSONString(notifyResultMap));
                } catch (Exception e) {
                    log.error("回调通知交易退单失败 result:{}", JSON.toJSONString(notifyResultMap), e);
                    throw new AppException(e.getMessage());
                }
            });
        }
    }
}
