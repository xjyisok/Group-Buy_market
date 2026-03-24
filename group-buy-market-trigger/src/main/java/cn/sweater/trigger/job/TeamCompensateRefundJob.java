package cn.sweater.trigger.job;

import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderListDetailEntity;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.sweater.domain.trade.service.ITradeRefundOrderService;
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
    private ITradeRefundOrderService tradeRefundOrderService;

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

            // 第一阶段：查询超时仍为 PROGRESS 的团
            List<String> timeOutTeamIds = tradeRepository.queryTimeOutProgressTeams();
            if (timeOutTeamIds == null || timeOutTeamIds.isEmpty()) {
                log.info("补偿退单任务：无超时进行中的拼团，任务结束");
                return;
            }

            for (String teamId : timeOutTeamIds) {
                try {
                    // 第二阶段：关团，affected rows=0 说明团已被结算为 COMPLETE，跳过
                    int affected = tradeRepository.closeTimeOutTeam(teamId);
                    if (affected == 0) {
                        log.info("补偿退单任务：团在临界时刻被结算成功，跳过退款 teamId:{}", teamId);
                        continue;
                    }

                    // 第三阶段：查询团内所有未退款订单
                    List<UserGroupBuyOrderListDetailEntity> unRefundedOrders =
                            tradeRepository.queryUnRefundedOrdersByTeamId(teamId);
                    if (unRefundedOrders == null || unRefundedOrders.isEmpty()) {
                        log.info("补偿退单任务：团内无未退款订单 teamId:{}", teamId);
                        continue;
                    }

                    int successCount = 0;
                    int failCount = 0;
                    for (UserGroupBuyOrderListDetailEntity order : unRefundedOrders) {
                        try {
                            TradeRefundCommandEntity command = new TradeRefundCommandEntity();
                            command.setUserId(order.getUserId());
                            command.setOutTradeNo(order.getOutTradeNo());
                            command.setSource(order.getSource());
                            command.setChannel(order.getChannel());
                            // 补偿任务不走责任链权限守卫，直接由 Repository 层路由
                            // status=0(未支付) → unpaid2Refund；status=1(已支付) → paid2Refund
                            tradeRefundOrderService.compensateRefundOrder(command, order.getStatus());
                            successCount++;
                            log.info("补偿退单成功 teamId:{} userId:{} outTradeNo:{} status:{}",
                                    teamId, order.getUserId(), order.getOutTradeNo(), order.getStatus());
                        } catch (Exception e) {
                            failCount++;
                            log.error("补偿退单失败 teamId:{} userId:{} outTradeNo:{}",
                                    teamId, order.getUserId(), order.getOutTradeNo(), e);
                        }
                    }
                    log.info("补偿退单任务：团处理完成 teamId:{} 成功:{} 失败:{}", teamId, successCount, failCount);

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
