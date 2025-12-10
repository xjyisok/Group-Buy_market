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
    @Scheduled(fixedDelay = 15000)  // 或 cron 也行，但 fixedDelay 更安全
    public void exec() {

        RLock lock = redissonClient.getLock("group_buy_market_notify_job_exec");

        // 使用无 leaseTime 的 tryLock，自动续期，绝不会锁丢失
        boolean acquired = false;
        try {
            acquired = lock.tryLock(2, TimeUnit.SECONDS); // 最多等待 2 秒，leaseTime = infinite（自动续期）
            if (!acquired) {
                return;
            }

            Map<String, Integer> result = tradeTaskService.execNotifyJob();
            log.info("定时任务回调通知平团完结任务结束 result: {}", JSON.toJSONString(result));

        } catch (Exception e) {
            log.error("定时任务回调通知平团完结任务失败", e);
        } finally {
            // 必须只判断是否由当前线程持有锁
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
