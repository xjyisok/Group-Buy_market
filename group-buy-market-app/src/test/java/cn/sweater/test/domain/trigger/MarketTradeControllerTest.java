package cn.sweater.test.domain.trigger;

import cn.sweater.api.IMarketTradeServiceApi;
import cn.sweater.api.dto.LockMarketPayOrderRequestDTO;
import cn.sweater.api.dto.LockMarketPayOrderResponseDTO;
import cn.sweater.api.response.Response;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 营销交易服务
 * @create 2025-01-11 14:20
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MarketTradeControllerTest {

    @Resource
    private IMarketTradeServiceApi marketTradeService;

    @Test
    public void test_lockMarketPayOrder() {
        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
        lockMarketPayOrderRequestDTO.setUserId("xjy");
        lockMarketPayOrderRequestDTO.setTeamId(null);
        lockMarketPayOrderRequestDTO.setActivityId(100123L);
        lockMarketPayOrderRequestDTO.setGoodsId("9890001");
        lockMarketPayOrderRequestDTO.setSource("s01");
        lockMarketPayOrderRequestDTO.setChannel("c01");
        lockMarketPayOrderRequestDTO.setOutTradeNo(RandomStringUtils.randomNumeric(12));

        Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = marketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);

        log.info("测试结果 req:{} res:{}", JSON.toJSONString(lockMarketPayOrderRequestDTO), JSON.toJSONString(lockMarketPayOrderResponseDTOResponse));
    }

    @Test
    public void test_lockMarketPayOrder_teamId_not_null() {
        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
        lockMarketPayOrderRequestDTO.setUserId("dsh");
        lockMarketPayOrderRequestDTO.setTeamId("mylkUS0B");
        lockMarketPayOrderRequestDTO.setActivityId(100123L);
        lockMarketPayOrderRequestDTO.setGoodsId("9890001");
        lockMarketPayOrderRequestDTO.setSource("s01");
        lockMarketPayOrderRequestDTO.setChannel("c01");
        lockMarketPayOrderRequestDTO.setOutTradeNo(RandomStringUtils.randomNumeric(12));

        Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = marketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);

        log.info("测试结果 req:{} res:{}", JSON.toJSONString(lockMarketPayOrderRequestDTO), JSON.toJSONString(lockMarketPayOrderResponseDTOResponse));
    }

}
