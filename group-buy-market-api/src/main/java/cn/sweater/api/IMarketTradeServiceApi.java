package cn.sweater.api;

import cn.sweater.api.dto.LockMarketPayOrderRequestDTO;
import cn.sweater.api.dto.LockMarketPayOrderResponseDTO;
import cn.sweater.api.dto.SettlementMarketPayOrderRequestDTO;
import cn.sweater.api.dto.SettlementMarketPayOrderResponseDTO;
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


}
