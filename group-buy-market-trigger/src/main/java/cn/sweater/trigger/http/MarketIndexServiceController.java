package cn.sweater.trigger.http;

import cn.sweater.api.dto.GoodsMarketRequestDTO;
import cn.sweater.api.dto.GoodsMarketResponseDTO;
import cn.sweater.api.IMarketIndexService;
import cn.sweater.api.dto.SettlementMarketPayOrderResponseDTO;
import cn.sweater.api.response.Response;
import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.TeamStatisticVO;
import cn.sweater.domain.activity.service.trial.IIndexGroupBuyMarketService;
import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/index/")
public class MarketIndexServiceController implements IMarketIndexService {
    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;
    //NOTE商品以及相关拼团信息查询接口
    @RequestMapping(value = "query_group_buy_market_config",method = RequestMethod.POST)
    @Override
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequestDTO goodsMarketRequestDTO) {
        try{
            log.info("查询拼团营销信息查询开始:{} goodsId:{} ", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId());
            if (StringUtils.isBlank(goodsMarketRequestDTO.getGoodsId()) || StringUtils.isBlank(goodsMarketRequestDTO.getChannel())
                    || StringUtils.isBlank(goodsMarketRequestDTO.getSource())
                    || StringUtils.isBlank(goodsMarketRequestDTO.getUserId())) {
                return Response.<GoodsMarketResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }//NOTE进行优惠试算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                            .userId(goodsMarketRequestDTO.getUserId())
                            .channel(goodsMarketRequestDTO.getChannel())
                            .source(goodsMarketRequestDTO.getSource())
                            .goodsId(goodsMarketRequestDTO.getGoodsId())
                    .build());
            GroupBuyActivityDiscountVO groupBuyActivityDiscountVO=trialBalanceEntity.getGroupBuyActivityDiscountVO();
            Long activityId = groupBuyActivityDiscountVO.getActivityId();
            //NOTE查询当前商品下所有的拼团对应页面的多个拼团
            List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities=indexGroupBuyMarketService
                    .queryInProgressUserGroupBuyOrderDetailList(activityId,goodsMarketRequestDTO.getUserId(),1,2);
            //System.out.println(JSON.toJSONString(userGroupBuyOrderDetailEntities));
            //NOTE查询当前商品的统计对象
            TeamStatisticVO teamStatisticVO=indexGroupBuyMarketService.quertTeamStatisticByActivtiyId(activityId);
            //NOTE组合成最终的Response
            GoodsMarketResponseDTO goodsMarketResponseDTO=new GoodsMarketResponseDTO();
            goodsMarketResponseDTO.setGoods(GoodsMarketResponseDTO.Goods.builder()
                            .deductionPrice(trialBalanceEntity.getDeductionPrice())
                            .goodsId(trialBalanceEntity.getGoodsId())
                            .originalPrice(trialBalanceEntity.getOriginalPrice())
                            .payPrice(trialBalanceEntity.getPayPrice())
                    .build());
            List<GoodsMarketResponseDTO.Team>teamList=new ArrayList<GoodsMarketResponseDTO.Team>();
            for(UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity:userGroupBuyOrderDetailEntities){
                GoodsMarketResponseDTO.Team team=GoodsMarketResponseDTO.Team.builder()
                        .userId(userGroupBuyOrderDetailEntity.getUserId())
                        .teamId(userGroupBuyOrderDetailEntity.getTeamId())
                        .activityId(activityId)
                        .targetCount(userGroupBuyOrderDetailEntity.getTargetCount())
                        .lockCount(userGroupBuyOrderDetailEntity.getLockCount())
                        .completeCount(userGroupBuyOrderDetailEntity.getCompleteCount())
                        .validEndTime(userGroupBuyOrderDetailEntity.getValidEndTime())
                        .validStartTime(userGroupBuyOrderDetailEntity.getValidStartTime())
                        .validTimeCountdown(GoodsMarketResponseDTO.Team.differenceDateTime2Str(
                                new Date(), userGroupBuyOrderDetailEntity.getValidEndTime()
                        ))
                        .outTradeNo(userGroupBuyOrderDetailEntity.getOutTradeNo())
                        .build();
                teamList.add(team);
            }
            goodsMarketResponseDTO.setTeamList(teamList);
            GoodsMarketResponseDTO.TeamStatistic teamStatistic = GoodsMarketResponseDTO.TeamStatistic.builder()
                    .allTeamCount(teamStatisticVO.getAllTeamCount())
                    .allTeamCompleteCount(teamStatisticVO.getAllTeamCompleteCount())
                    .allTeamUserCount(teamStatisticVO.getAllTeamUserCount())
                    .build();
            goodsMarketResponseDTO.setTeamStatistic(teamStatistic);
            Response<GoodsMarketResponseDTO>response=Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(goodsMarketResponseDTO)
                    .build();

            log.info("查询拼团营销配置完成:{} goodsId:{} response:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), JSON.toJSONString(response));
            return response;
        } catch (AppException e) {
            log.info("查询拼团营销信息查询异常:{} goodsId:{} ", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId());
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch(Exception e) {
            log.info("查询拼团营销信息查询失败:{} goodsId:{} ", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId());
            e.printStackTrace();
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
