package cn.sweater.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQDelayConfig {

    @Bean
    public DirectExchange groupBuyDlxExchange(
            @Value("${spring.rabbitmq.config.close-order.dlx-exchange}") String name) {
        return new DirectExchange(name, true, false);
    }

    @Bean
    public Queue groupBuyDelayQueue(
            @Value("${spring.rabbitmq.config.close-order.delay-queue}") String name,
            @Value("${spring.rabbitmq.config.close-order.dlx-exchange}") String dlx) {
        return QueueBuilder.durable(name)
                .withArgument("x-message-ttl", 1800000)
                .withArgument("x-dead-letter-exchange", dlx)
                .withArgument("x-dead-letter-routing-key", "close.order")
                .build();
    }

    @Bean
    public Queue groupBuyCloseQueue(
            @Value("${spring.rabbitmq.config.close-order.close-queue}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    public Binding groupBuyCloseBinding(Queue groupBuyCloseQueue,
                                        DirectExchange groupBuyDlxExchange) {
        return BindingBuilder.bind(groupBuyCloseQueue)
                .to(groupBuyDlxExchange).with("close.order");
    }
}
