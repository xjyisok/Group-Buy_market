package cn.sweater.test.domain.tag;

import cn.sweater.domain.tag.service.ITagService;
import cn.sweater.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBitSet;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITagServiceTest {
    @Resource
    ITagService tagService;
    @Resource
    IRedisService redisService;
    @Test
    public void test_tagService() {
        tagService.exexTagBatchJob("RQ_KJHKL98UU78H66554GFDV","10001");
    }
    @Test
    public void test_tag_getBit() {
        RBitSet bitSet = redisService.getBitSet("RQ_KJHKL98UU78H66554GFDV");
        log.info("测试结果：{}",bitSet.get(redisService.getIndexFromUserId("xjy")));
        log.info("测试结果：{}",bitSet.get(redisService.getIndexFromUserId("sq")));
        log.info("测试结果：{}",bitSet.get(redisService.getIndexFromUserId("dsh")));
        log.info("测试结果：{}",bitSet.get(redisService.getIndexFromUserId("zy")));
        log.info("测试结果：{}",bitSet.get(redisService.getIndexFromUserId("yby")));
        log.info("测试结果：{}",bitSet.get(redisService.getIndexFromUserId("gt")));
    }
}
