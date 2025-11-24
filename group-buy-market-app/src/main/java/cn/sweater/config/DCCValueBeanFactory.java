package cn.sweater.config;

import cn.sweater.types.annotations.DCCValue;
import cn.sweater.types.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
@Service
public class DCCValueBeanFactory implements BeanPostProcessor {
    private static final String BASE_CONFIG_PATH="group_buy_market_dcc_";
    private final RedissonClient redissonClient;
    private final Map<String,Object> dccObjectMap=new HashMap<String,Object>();
    public DCCValueBeanFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    @Bean("dccTopic")
    public RTopic dccRedisTopicListener(RedissonClient redissonClient) {
        RTopic topic=redissonClient.getTopic("group_buy_market_dcc");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String s) {
                String[]split = s.split(Constants.SPLIT);
                String attribute=split[0];
                String key=BASE_CONFIG_PATH+attribute;
                String value=split[1];
                RBucket<String> rBucket=redissonClient.getBucket(key);
                if(!rBucket.isExists()){
                    return;
                }
                rBucket.set(value);
                Object dccObject=dccObjectMap.get(key);
                if(dccObject==null){
                    return;
                }
                Class<?> objBeanClass=dccObject.getClass();
                if(AopUtils.isAopProxy(objBeanClass)){
                    objBeanClass=AopUtils.getTargetClass(objBeanClass);
                }
                try{
                    Field field=objBeanClass.getDeclaredField(attribute);
                    field.setAccessible(true);
                    field.set(dccObject,value);
                    field.setAccessible(false);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        });
        return topic;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetBeanClass = bean.getClass();
        Object targetBeanObject=bean;
        if(AopUtils.isAopProxy(targetBeanObject)){
            targetBeanClass = AopUtils.getTargetClass(targetBeanObject);
            targetBeanObject=AopProxyUtils.getSingletonTarget(targetBeanObject);
        }
        Field[] fields=targetBeanClass.getDeclaredFields();
        for(Field field:fields) {
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }
            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            String value = dccValue.value();
            if (StringUtils.isBlank(value)) {
                throw new RuntimeException("降级参数设置为空");
            }
            String[] split = value.split(":");
            String key = BASE_CONFIG_PATH.concat(split[0]);
            String defaultValue = split.length == 2 ? split[1] : null;
            String setValue = defaultValue;
            try {
                if (StringUtils.isBlank(defaultValue)) {
                    throw new RuntimeException("降级参数值为空");
                }
                RBucket<String> bucket = redissonClient.getBucket(key);
                if (!bucket.isExists()) {
                    bucket.set(defaultValue);
                } else {
                    setValue = bucket.get();
                }
                field.setAccessible(true);
                field.set(targetBeanObject, setValue);
                field.setAccessible(false);
            }catch (Exception e) {
                e.printStackTrace();
            }
            dccObjectMap.put(key, targetBeanObject);
        }
        return bean;
    }
}
