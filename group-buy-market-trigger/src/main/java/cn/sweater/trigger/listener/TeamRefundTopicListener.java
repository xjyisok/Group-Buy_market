package cn.sweater.trigger.listener;

import cn.sweater.domain.trade.model.valobj.TeamRefundSuccess;
import cn.sweater.domain.trade.service.ITradeRefundOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class TeamRefundTopicListener {
    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value ="${spring.rabbitmq.config.producer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}",type = ExchangeTypes.TOPIC),
                    key="${spring.rabbitmq.config.producer.topic_team_refund.routing_key}"
            )
    )
    public void Listener(String message) {
        log.info("接收到退单成功消息，开始进行库存恢复:{}", message);
        //1. 解析退单信息
        TeamRefundSuccess teamRefundSuccess = JSON.parseObject(message, TeamRefundSuccess.class);
        //2. 退单逻辑
        try{
            tradeRefundOrderService.restoreTeamStockLock(teamRefundSuccess);
            log.info("库存恢复成功{}", JSON.toJSONString(teamRefundSuccess));
        }catch (Exception e){
            log.info("库存恢复失败{}", e.getMessage());
        }
    }
}
