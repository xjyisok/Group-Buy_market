package cn.sweater.trigger.job;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 补偿扫描任务：关闭超时拼团并对团内所有未退款订单发起退单
 * <p>
 * 职责范围：
 * - 扫描 PROGRESS 且已超时的团，将其关闭为 FAIL
 * - 对关闭成功的团，查询团内所有未退款用户订单（status in 0,1）统一退单
 * - affected rows = 0 说明团在极限时刻被结算为 COMPLETE，跳过，不退款
 * - 已被成员全部退完的 FAIL 团（completeCount=0）不在本任务扫描范围（扫的是 PROGRESS 超时团）
 * </p>
 */
@Slf4j
@Service
public class TeamCompensateRefundJob {

    @Resource
    private ITradeRepository tradeRepository;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void exec() {
        RLock lock = redissonClient.getLock("group_buy_market_team_compensate_refund_job_exec");
        try {
            boolean isLocked = lock.tryLock(3, 60, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("补偿退单任务：未抢占到锁，本次跳过");
                return;
            }
            log.info("补偿退单任务：开始执行");

            List<String> timeOutTeamIds = tradeRepository.queryTimeOutProgressTeams();
            if (timeOutTeamIds == null || timeOutTeamIds.isEmpty()) {
                log.info("补偿退单任务：无超时进行中的拼团，任务结束");
                return;
            }

            for (String teamId : timeOutTeamIds) {
                try {
                    // 原子操作：关团 + 批量写 notify_task，同一事务保证原子性
                    // affected=0 说明团在临界时刻被结算为 COMPLETE，跳过
                    int count = tradeRepository.closeTeamAndInsertRefundTasks(teamId);
                    if (count == 0) {
                        log.info("补偿退单任务：团在临界时刻被结算成功，跳过 teamId:{}", teamId);
                    } else {
                        log.info("补偿退单任务：关团+写退款消息完成 teamId:{} 消息数:{}", teamId, count);
                    }
                } catch (Exception e) {
                    log.error("补偿退单任务：处理团异常 teamId:{}", teamId, e);
                }
            }
            log.info("补偿退单任务：执行结束");

        } catch (InterruptedException e) {
            log.error("补偿退单任务：执行异常", e);
            throw new RuntimeException(e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
