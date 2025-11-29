package cn.sweater.api;

import cn.sweater.api.dto.GoodsMarketRequestDTO;
import cn.sweater.api.dto.GoodsMarketResponseDTO;
import cn.sweater.api.response.Response;
public interface IMarketIndexService {

    /**
     * 查询拼团营销配置
     *
     * @param goodsMarketRequestDTO 营销商品信息
     * @return 营销配置信息
     */
    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO goodsMarketRequestDTO);

}
