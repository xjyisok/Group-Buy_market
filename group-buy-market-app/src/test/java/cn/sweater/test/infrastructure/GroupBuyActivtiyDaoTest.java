package cn.sweater.test.infrastructure;

import cn.sweater.infrastructure.dao.IGroupBuyActivityDao;
import cn.sweater.infrastructure.dao.po.GroupBuyActivity;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GroupBuyActivtiyDaoTest {
    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Test
    public void test_queryGroupBuyActivtiyList() {
        List<GroupBuyActivity>activtiylist=groupBuyActivityDao.queryGroupBuyActivityList();
        log.info(JSON.toJSONString(activtiylist));
    }
}
