package cn.sweater.trigger.job;

import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderListDetailEntity;
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

@Slf4j
@Service
public class TimeOutRefundJob {
    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @Resource
    private RedissonClient redissonClient;

    @Scheduled(cron = "0/15 * * * * ?")
    public void exec() {
        RLock lock = redissonClient.getLock("group_buy_market_timeout_refund_job_exec");
        try {
            boolean isLocked = lock.tryLock(3, 60, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("没有抢占到锁，本次任务结束");
                return;
            }
            log.info("超时订单退单任务现在开始");
            List<UserGroupBuyOrderListDetailEntity> userGroupBuyOrderDetailEntityList = tradeRefundOrderService.queryTimeOutUnpaidOrder();
            if (userGroupBuyOrderDetailEntityList.size() == 0) {
                log.info("当前没有未支付超时订单任务结束");
                return;
            }
            int successCount = 0;
            int failCount = 0;
            for (UserGroupBuyOrderListDetailEntity timeOutOrderList : userGroupBuyOrderDetailEntityList) {
                try {
                    TradeRefundCommandEntity tradeRefundCommandEntity = new TradeRefundCommandEntity();
                    tradeRefundCommandEntity.setOutTradeNo(timeOutOrderList.getOutTradeNo());
                    tradeRefundCommandEntity.setSource(timeOutOrderList.getSource());
                    tradeRefundCommandEntity.setChannel(timeOutOrderList.getChannel());
                    tradeRefundCommandEntity.setUserId(timeOutOrderList.getUserId());
                    tradeRefundOrderService.refundOrder(tradeRefundCommandEntity);
                    successCount++;
                    log.info("超时订单退单成功，用户ID：{}，交易单号：{}", timeOutOrderList.getUserId(), timeOutOrderList.getOutTradeNo());
                } catch (Exception e) {
                    failCount++;
                    log.error("超时订单退单失败，用户ID：{}，交易单号：{}，错误信息：{}", timeOutOrderList.getUserId(), timeOutOrderList.getOutTradeNo(), e);
                }
            }
            log.info("超时订单退单任务执行成功 成功：{}失败：{}", successCount, failCount);
        } catch (InterruptedException e) {
            log.error("超时退单定时任务执行异常", e);
            throw new RuntimeException(e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
