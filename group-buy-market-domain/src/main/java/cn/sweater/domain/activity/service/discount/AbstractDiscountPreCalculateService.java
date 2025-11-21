package cn.sweater.domain.activity.service.discount;

import cn.sweater.domain.activity.model.entity.DiscountTypeEnum;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.tag.adapter.repository.ITagRepository;
import org.checkerframework.checker.units.qual.A;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.math.BigDecimal;

public abstract class AbstractDiscountPreCalculateService implements IDiscountPreCalculateService{
    @Resource
    private ITagRepository tagRepository;
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
        return tagRepository.getBitSet(userId, tagId);
    }
}
