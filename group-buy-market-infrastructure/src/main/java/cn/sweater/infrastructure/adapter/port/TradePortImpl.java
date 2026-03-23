package cn.sweater.infrastructure.adapter.port;

import cn.sweater.domain.trade.adapter.port.ITradePort;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.model.valobj.NotifyTypeEnumVO;
import cn.sweater.infrastructure.event.EventPublisher;
import cn.sweater.infrastructure.gateway.GroupBuyNotifyService;
import cn.sweater.infrastructure.redis.IRedisService;
import cn.sweater.types.enums.NotifyTaskHttpEnumVO;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class TradePortImpl implements ITradePort {
    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private EventPublisher publisher;
    @Resource
    private AlipayClient alipayClient;
    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
        RLock lock=redisService.getLock(notifyTask.lockKey());
        try{
            if(lock.tryLock(3,0, TimeUnit.SECONDS)){
                try{
                    System.out.println(notifyTask.getNotifyType());
                    if(notifyTask.getNotifyType().equals(NotifyTypeEnumVO.HTTP.getCode())) {
                        if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || notifyTask.getNotifyUrl().equals("暂无")) {
                            return NotifyTaskHttpEnumVO.SUCCESS.getCode();
                        }
                        return groupBuyNotifyService.GroupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                    }
                    else if(notifyTask.getNotifyType().equals(NotifyTypeEnumVO.MQ.getCode())) {
                        log.info("发送退单MQ消息"+notifyTask.getNotifyMQ()+notifyTask.getParameterJson());
                        publisher.publish(notifyTask.getNotifyMQ(), notifyTask.getParameterJson());
                        return NotifyTaskHttpEnumVO.SUCCESS.getCode();
                    }
                }finally{
                    if(lock.isLocked()&&lock.isHeldByCurrentThread()){
                    lock.unlock();
                    }
                }
            }
            return NotifyTaskHttpEnumVO.NULL.getCode();
        }
        catch(Exception e){
            Thread.currentThread().interrupt();
            return NotifyTaskHttpEnumVO.NULL.getCode();
        }
    }

    @Override
    public boolean closePayOrder(String outTradeNo) throws Exception {
        try {
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(outTradeNo);
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            request.setBizModel(model);
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            log.info("支付宝关单 outTradeNo:{} code:{} msg:{}", outTradeNo, response.getCode(), response.getMsg());
            if (response.isSuccess()) {
                // 关单成功：订单未支付，可以安全走未支付退款
                return true;
            }
            // 10000=成功, 40004=交易不存在, ACQ.TRADE_HAS_SUCCESS=已支付无法关闭
            String subCode = response.getSubCode();
            if ("ACQ.TRADE_HAS_SUCCESS".equals(subCode) || "TRADE_HAS_SUCCESS".equals(subCode)) {
                // 关单失败原因是已支付，需走已支付退款
                log.info("支付宝关单失败：订单已支付 outTradeNo:{}", outTradeNo);
                return false;
            }
            // 其他失败（交易不存在等）视为未支付，允许关单继续
            log.warn("支付宝关单返回未知subCode:{} outTradeNo:{}", subCode, outTradeNo);
            return true;
        } catch (Exception e) {
            log.error("支付宝关单异常 outTradeNo:{}", outTradeNo, e);
            // 异常时保守处理：视为未支付，让后续节点决策
            return true;
        }
    }
}
