package cn.sweater.domain.activity.service.discount;

import cn.sweater.domain.activity.model.entity.DiscountTypeEnum;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

public abstract class AbstractDiscountPreCalculateService implements IDiscountPreCalculateService{
    @Override
    public BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVO) {
        if(DiscountTypeEnum.TAG.getCode().equals(groupBuyDiscountVO.getDiscountType())){
            if(!filteredTag(userId,groupBuyDiscountVO.getTagId())){
                return originalPrice;
            }
        }
        return doCalculate(originalPrice,groupBuyDiscountVO);
    }
    public abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVO);
    protected Boolean filteredTag(String userId,String tagId){
        //TODO
        return true;
    }
}
