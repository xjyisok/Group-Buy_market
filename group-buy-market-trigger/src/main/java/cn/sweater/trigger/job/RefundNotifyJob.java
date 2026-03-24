package cn.sweater.trigger.job;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 退款消息投递任务：扫描 notify_task 中退款类消息并投递到 MQ
 */
@Slf4j
@Service
public class RefundNotifyJob {

    @Resource
    private ITradeRepository tradeRepository;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(fixedDelay = 10000)
    public void exec() {
        RLock lock = redissonClient.getLock("group_buy_market_refund_notify_job_exec");
        boolean acquired = false;
        try {
            acquired = lock.tryLock(2, TimeUnit.SECONDS);
            if (!acquired) return;

            List<NotifyTaskEntity> taskList = tradeRepository.queryUnExecutedRefundNotifyTaskList();
            if (taskList == null || taskList.isEmpty()) return;

            log.info("退款消息投递任务：待处理 {} 条", taskList.size());

            for (NotifyTaskEntity task : taskList) {
                try {
                    tradeRepository.publishRefundMessage(task);
                    tradeRepository.updateNotifyTaskStatusSuccess(task.getUuid());
                    log.info("退款消息投递成功 uuid:{}", task.getUuid());
                } catch (Exception e) {
                    if (task.getNotifyCount() < 5) {
                        tradeRepository.updateNotifyTaskStatusRetry(task.getUuid());
                    } else {
                        tradeRepository.updateNotifyTaskStatusError(task.getUuid());
                    }
                    log.error("退款消息投递失败 uuid:{}", task.getUuid(), e);
                }
            }

        } catch (InterruptedException e) {
            log.error("退款消息投递任务异常", e);
            throw new RuntimeException(e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
