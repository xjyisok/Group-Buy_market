package cn.sweater.api;

import cn.sweater.api.dto.LockMarketPayOrderRequestDTO;
import cn.sweater.api.dto.LockMarketPayOrderResponseDTO;
import cn.sweater.api.response.Response;

public interface IMarketTradeServiceApi {
    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(LockMarketPayOrderRequestDTO request);
}
