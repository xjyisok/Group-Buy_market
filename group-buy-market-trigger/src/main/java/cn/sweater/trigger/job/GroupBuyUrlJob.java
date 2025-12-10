package cn.sweater.trigger.job;

import cn.sweater.domain.trade.service.ITradeSettlementOrderService;
import cn.sweater.domain.trade.service.ITradeTaskService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GroupBuyUrlJob {
    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ITradeTaskService tradeTaskService;
    @Scheduled(cron="0/15 * * * * ?")
    public void exec(){
        // 为什么加锁？分布式应用N台机器部署互备（一个应用实例挂了，还有另外可用的），任务调度会有N个同时执行，那么这里需要增加抢占机制，谁抢占到谁就执行。完毕后，下一轮继续抢占。
        RLock lock = redissonClient.getLock("group_buy_market_notify_job_exec");
        try{
            boolean isLocked = lock.tryLock(3, 0, TimeUnit.SECONDS);
            if(!isLocked){
                return;
            }
            Map<String,Integer> result=tradeTaskService.execNotifyJob();
            log.info("定时任务回调通知平团完结任务结束result:{}", JSON.toJSONString(result));
        }catch(Exception e){
            log.error("定时任务回调通知平团完结任务失败",e);
            e.printStackTrace();
        }finally{
            if(lock.isLocked()&&lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
