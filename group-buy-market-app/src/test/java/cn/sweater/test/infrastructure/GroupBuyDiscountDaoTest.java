package cn.sweater.test.infrastructure;

import cn.sweater.infrastructure.dao.IGroupBuyActivityDao;
import cn.sweater.infrastructure.dao.IGroupBuyDiscountDao;
import cn.sweater.infrastructure.dao.po.GroupBuyActivity;
import cn.sweater.infrastructure.dao.po.GroupBuyDiscount;
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
public class GroupBuyDiscountDaoTest {
    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;
    @Test
    public void test_queryGroupBuyActivtiyList() {
        List<GroupBuyDiscount>discountList=groupBuyDiscountDao.queryGroupBuyDiscountList();
        log.info(JSON.toJSONString(discountList));
    }
}
