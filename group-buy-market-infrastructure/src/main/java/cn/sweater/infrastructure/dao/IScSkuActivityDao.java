package cn.sweater.infrastructure.dao;

import cn.sweater.infrastructure.dao.po.ScSkuActivity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IScSkuActivityDao {
    ScSkuActivity querySCSkuActivityBySCGoodsId(ScSkuActivity scSkuActivity);
}
