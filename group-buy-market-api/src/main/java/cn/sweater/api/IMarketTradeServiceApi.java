package cn.sweater.api;

import cn.sweater.api.dto.*;
import cn.sweater.api.response.Response;

public interface IMarketTradeServiceApi {
    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(LockMarketPayOrderRequestDTO request);
    /**
     * 营销结算
     *
     * @param requestDTO 结算商品信息
     * @return 结算结果信息
     */
    Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(SettlementMarketPayOrderRequestDTO requestDTO) throws Exception;
    /**
     * 营销退单
     *
     * @param requestDTO 退单订单信息
     * @return 吞蛋结果信息
     */
    Response<RefundMarketPayOrderResponseDTO> refundMarketPayOrder(RefundMarketPayOrderRequestDTO requestDTO);
}
