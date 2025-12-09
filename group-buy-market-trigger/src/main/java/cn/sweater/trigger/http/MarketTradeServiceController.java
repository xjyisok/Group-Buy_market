package cn.sweater.trigger.http;

import cn.sweater.api.IMarketTradeServiceApi;
import cn.sweater.api.dto.LockMarketPayOrderRequestDTO;
import cn.sweater.api.dto.LockMarketPayOrderResponseDTO;
import cn.sweater.api.dto.SettlementMarketPayOrderRequestDTO;
import cn.sweater.api.dto.SettlementMarketPayOrderResponseDTO;
import cn.sweater.api.response.Response;
import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.service.trial.IIndexGroupBuyMarketService;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.sweater.domain.trade.model.valobj.NotifyConfigVO;
import cn.sweater.domain.trade.model.valobj.NotifyTypeEnumVO;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.domain.trade.service.ITradeOrderService;
import cn.sweater.domain.trade.service.ITradeSettlementOrderService;
import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@Slf4j
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeServiceController implements IMarketTradeServiceApi {
    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeOrderService tradeOrderService;
    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;
    //NOTE用户点击参与拼团发起的请求
    @RequestMapping(value = "lock_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(@RequestBody LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO) {
        try {
            // 参数
            String userId = lockMarketPayOrderRequestDTO.getUserId();
            String source = lockMarketPayOrderRequestDTO.getSource();
            String channel = lockMarketPayOrderRequestDTO.getChannel();
            String goodsId = lockMarketPayOrderRequestDTO.getGoodsId();
            Long activityId = lockMarketPayOrderRequestDTO.getActivityId();
            String outTradeNo = lockMarketPayOrderRequestDTO.getOutTradeNo();
            String teamId = lockMarketPayOrderRequestDTO.getTeamId();
            LockMarketPayOrderRequestDTO.NotifyConfigVO notifyConfigVO = lockMarketPayOrderRequestDTO.getNotifyConfigVO();

            log.info("营销交易锁单:{} LockMarketPayOrderRequestDTO:{}", userId, JSON.toJSONString(lockMarketPayOrderRequestDTO));

            if (StringUtils.isBlank(userId) || StringUtils.isBlank(source) || StringUtils.isBlank(channel) || StringUtils.isBlank(goodsId)
                    || StringUtils.isBlank(goodsId) || null == activityId||("HTTP".equals(notifyConfigVO.getNotifyType()) && StringUtils.isBlank(notifyConfigVO.getNotifyUrl()))
            ) {
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 查询 outTradeNo 是否已经存在交易记录
            MarketPayOrderEntity marketPayOrderEntity = tradeOrderService.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
            if (null != marketPayOrderEntity&& TradeOrderStatusEnumVO.CREATE.equals(marketPayOrderEntity.getTradeOrderStatusEnumVO())) {
                LockMarketPayOrderResponseDTO lockMarketPayOrderResponseDTO = LockMarketPayOrderResponseDTO.builder()
                        .orderId(marketPayOrderEntity.getOrderId())
                        .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                        .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                        .originalPrice(marketPayOrderEntity.getOriginalPrice())
                        .payPrice(marketPayOrderEntity.getPayPrice())
                        .build();

                log.info("交易锁单记录(存在):{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info(ResponseCode.SUCCESS.getInfo())
                        .data(lockMarketPayOrderResponseDTO)
                        .build();
            }

            // 判断拼团锁单是否完成了目标
            if (null != teamId) {
                GroupBuyProgressVO groupBuyProgressVO = tradeOrderService.queryGroupBuyProgress(teamId);
                if (null != groupBuyProgressVO && Objects.equals(groupBuyProgressVO.getTargetCount(), groupBuyProgressVO.getLockCount())) {
                    log.info("交易锁单拦截-拼单目标已达成:{} {}", userId, teamId);
                    return Response.<LockMarketPayOrderResponseDTO>builder()
                            .code(ResponseCode.E0006.getCode())
                            .info(ResponseCode.E0006.getInfo())
                            .build();
                }
            }

            // 营销优惠试算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                    .userId(userId)
                    .source(source)
                    .channel(channel)
                    .goodsId(goodsId)
                    .activityId(activityId)
                    .build());
            //人群限定
            if(!trialBalanceEntity.getIsEnable()||!trialBalanceEntity.getIsEnable()){
                return Response.<LockMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.E0007.getCode())
                        .info(ResponseCode.E0007.getInfo())
                        .build();
            }
            GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();

            // 锁单
            marketPayOrderEntity = tradeOrderService.lockMarketPayOrder(
                    UserEntity.builder().userId(userId).build(),
                    PayActivityEntity.builder()
                            .teamId(teamId)
                            .activityId(activityId)
                            .activityName(groupBuyActivityDiscountVO.getActivityName())
                            .startTime(groupBuyActivityDiscountVO.getStartTime())
                            .endTime(groupBuyActivityDiscountVO.getEndTime())
                            .targetCount(groupBuyActivityDiscountVO.getTarget())
                            .validTime(groupBuyActivityDiscountVO.getValidTime())
                            .build(),
                    PayDiscountEntity.builder()
                            .source(source)
                            .channel(channel)
                            .goodsId(goodsId)
                            .goodsName(trialBalanceEntity.getGoodsName())
                            .originalPrice(trialBalanceEntity.getOriginalPrice())
                            .payPrice(trialBalanceEntity.getPayPrice())
                            .deductionPrice(trialBalanceEntity.getDeductionPrice())
                            .outTradeNo(outTradeNo)
                            .notifyConfigVO(NotifyConfigVO.builder()
                                    .notifyType(NotifyTypeEnumVO.valueOf(notifyConfigVO.getNotifyType()))
                                    .notifyMQ(notifyConfigVO.getNotifyMQ())
                                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                                    .build())
                            .build());

            log.info("交易锁单记录(新):{} marketPayOrderEntity:{}", userId, JSON.toJSONString(marketPayOrderEntity));

            // 返回结果
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(LockMarketPayOrderResponseDTO.builder()
                            .orderId(marketPayOrderEntity.getOrderId())
                            .originalPrice(marketPayOrderEntity.getOriginalPrice())
                            .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                            .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                            .payPrice(marketPayOrderEntity.getPayPrice())
                            .originalPrice(marketPayOrderEntity.getOriginalPrice())
                            .build())
                    .build();
        } catch (AppException e) {
            log.error("营销交易锁单业务异常:{} LockMarketPayOrderRequestDTO:{}", lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("营销交易锁单服务失败:{} LockMarketPayOrderRequestDTO:{}", lockMarketPayOrderRequestDTO.getUserId(), JSON.toJSONString(lockMarketPayOrderRequestDTO), e);
            return Response.<LockMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
    //NOTE用户点击支付发起的请求
    @RequestMapping(value = "settlement_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(@RequestBody SettlementMarketPayOrderRequestDTO requestDTO) throws Exception {
        try {
            log.info("营销交易结算开始:{} requestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO));
            if (StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getChannel()) || StringUtils.isBlank(requestDTO.getSource())
                    || StringUtils.isBlank(requestDTO.getOutTradeNo()) || null == requestDTO.getOutTradeTime()) {
                return Response.<SettlementMarketPayOrderResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            TradePaySuccessEntity tradePaySuccessEntity = TradePaySuccessEntity.builder()
                    .userId(requestDTO.getUserId())
                    .source(requestDTO.getSource())
                    .channel(requestDTO.getChannel())
                    .outTradeNo(requestDTO.getOutTradeNo())
                    .outTradeTime(requestDTO.getOutTradeTime())
                    .build();
            TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementOrderService.settlementMarketPayOrder(tradePaySuccessEntity);
            SettlementMarketPayOrderResponseDTO settlementMarketPayOrderResponseDTO = new SettlementMarketPayOrderResponseDTO();
            settlementMarketPayOrderResponseDTO.setActivityId(tradePaySettlementEntity.getActivityId());
            settlementMarketPayOrderResponseDTO.setOutTradeNo(tradePaySettlementEntity.getOutTradeNo());
            settlementMarketPayOrderResponseDTO.setTeamId(tradePaySettlementEntity.getTeamId());
            settlementMarketPayOrderResponseDTO.setUserId(tradePaySettlementEntity.getUserId());
            log.info("营销交易结算结束:{} requestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO));
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(settlementMarketPayOrderResponseDTO)
                    .build();
        }catch (AppException e) {
            log.info("营销交易结算异常:{} requestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO));
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        }catch (Exception e) {
            log.info("营销交易结算失败:{} requestDTO:{}", requestDTO.getUserId(), JSON.toJSONString(requestDTO));
            return Response.<SettlementMarketPayOrderResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
