package cn.sweater.trigger.job;

import cn.sweater.domain.trade.service.ITradeSettlementOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class GroupBuyUrlJob {
    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;
    @Scheduled(cron="0/15 * * * * ?")
    public void exec(){
        try{
            Map<String,Integer> result=tradeSettlementOrderService.execSettlementNotifyJob();
            log.info("定时任务回调通知平团完结任务结束result:{}",result);
        }catch(Exception e){
            log.error("定时任务回调通知平团完结任务失败",e);
            e.printStackTrace();
        }
    }
}
