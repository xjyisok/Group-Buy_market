package cn.sweater.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TeamRefundTopicListener {
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value ="${spring.rabbitmq.config.producer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}",type = ExchangeTypes.TOPIC),
                    key="${spring.rabbitmq.config.producer.topic_team_refund.routing_key}"
            )
    )
    public void Listener(String message) {
        log.info("接收到退款message退单成功将发起退款:{}", message);
    }
}
