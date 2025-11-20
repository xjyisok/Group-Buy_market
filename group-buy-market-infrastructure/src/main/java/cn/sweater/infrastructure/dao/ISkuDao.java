package cn.sweater.infrastructure.dao;

import cn.sweater.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ISkuDao {
    Sku querySkuByGoodsId(String googdsId);
}
