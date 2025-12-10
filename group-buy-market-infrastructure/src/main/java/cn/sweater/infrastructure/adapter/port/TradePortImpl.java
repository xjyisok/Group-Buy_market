package cn.sweater.infrastructure.adapter.port;

import cn.sweater.domain.trade.adapter.port.ITradePort;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.model.valobj.NotifyTypeEnumVO;
import cn.sweater.infrastructure.event.EventPublisher;
import cn.sweater.infrastructure.gateway.GroupBuyNotifyService;
import cn.sweater.infrastructure.redis.IRedisService;
import cn.sweater.types.enums.NotifyTaskHttpEnumVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Repository
public class TradePortImpl implements ITradePort {
    @Resource
    private GroupBuyNotifyService groupBuyNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private EventPublisher publisher;
    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
        RLock lock=redisService.getLock(notifyTask.lockKey());
        try{
            if(lock.tryLock(3,0, TimeUnit.SECONDS)){
                try{
                    if(notifyTask.getNotifyType().equals(NotifyTypeEnumVO.HTTP.getCode())) {
                        if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || notifyTask.getNotifyUrl().equals("暂无")) {
                            return NotifyTaskHttpEnumVO.SUCCESS.getCode();
                        }
                        return groupBuyNotifyService.GroupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                    }
                    else if(notifyTask.getNotifyType().equals(NotifyTypeEnumVO.MQ.getCode())) {
                        //("发送退单MQ消息"+notifyTask.getNotifyMQ()+notifyTask.getParameterJson());
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
}
