package cn.sweater.infrastructure.event;

import cn.sweater.types.event.BaseEvent;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 消息发送
 * @create 2024-03-30 12:40
 */
@Slf4j
@Component
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.config.producer.exchange}")
    private String exchangeName;

    @Value("${spring.rabbitmq.config.close-order.delay-queue}")
    private String closeOrderDelayQueue;

    public void publishCloseOrderDelay(String message) {
        try {
            rabbitTemplate.convertAndSend(closeOrderDelayQueue, message, m -> {
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });
            log.info("投递超时关单延迟消息成功 message:{}", message);
        } catch (Exception e) {
            log.error("投递超时关单延迟消息失败 message:{}", message, e);
            // 不抛出，不影响主流程，由定时任务兜底
        }
    }

    public void publish(String routingKey, String message) {
        try {
            //System.out.println("发送的消息topic" + routingKey + "消息" + message);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message, m -> {
                // 持久化消息配置
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });
        } catch (Exception e) {
            log.error("发送MQ消息失败 team_success message:{}", message, e);
            throw e;
        }
    }

}
