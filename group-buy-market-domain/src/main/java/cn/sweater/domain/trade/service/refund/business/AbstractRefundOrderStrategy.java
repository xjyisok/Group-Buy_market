package cn.sweater.domain.trade.service.refund.business;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.model.valobj.TeamRefundSuccess;
import cn.sweater.domain.trade.service.ITradeTaskService;
import cn.sweater.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import cn.sweater.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
@Service
@Slf4j
public abstract class AbstractRefundOrderStrategy implements IRefundOrderStrategy {
    @Resource
    private ITradeRepository tradeRepository;
    @Resource
    private ITradeTaskService tradeTaskService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    protected void sendRefundOrderMessage(NotifyTaskEntity notifyTaskEntity) {
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
    protected void refund2StockRecovery(TeamRefundSuccess teamRefundSuccess) {
        String recoveryTeamStockKey= TradeRuleFilterFactory.generateRecoveryTeamStockKey(teamRefundSuccess.getTeamId(),teamRefundSuccess.getActivityId());
        tradeRepository.refund2Recovery(recoveryTeamStockKey,teamRefundSuccess);
    }
}
