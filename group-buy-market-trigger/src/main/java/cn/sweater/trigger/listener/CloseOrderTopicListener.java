package cn.sweater.trigger.listener;

import cn.sweater.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.sweater.domain.trade.service.ITradeRefundOrderService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class CloseOrderTopicListener {

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @RabbitListener(queues = "${spring.rabbitmq.config.close-order.close-queue}")
    public void listener(String message) {
        log.info("死信队列触发超时关单 message:{}", message);
        try {
            JSONObject obj = JSON.parseObject(message);
            TradeRefundCommandEntity cmd = new TradeRefundCommandEntity();
            cmd.setOutTradeNo(obj.getString("outTradeNo"));
            cmd.setUserId(obj.getString("userId"));
            cmd.setSource(obj.getString("source"));
            cmd.setChannel(obj.getString("channel"));
            tradeRefundOrderService.refundOrder(cmd);
            log.info("死信关单成功 outTradeNo:{}", cmd.getOutTradeNo());
        } catch (Exception e) {
            // 不重抛，防止消息无限重投，由定时任务兜底
            log.error("死信关单处理异常，由定时任务兜底 message:{}", message, e);
        }
    }
}
