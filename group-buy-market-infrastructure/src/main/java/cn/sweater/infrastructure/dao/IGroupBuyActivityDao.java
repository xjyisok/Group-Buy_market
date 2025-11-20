package cn.sweater.infrastructure.dao;

import cn.sweater.infrastructure.dao.po.GroupBuyActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @description 拼团活动Dao
 * @create 2024-12-07 10:10
 */
@Mapper
public interface IGroupBuyActivityDao {

    List<GroupBuyActivity> queryGroupBuyActivityList();
    GroupBuyActivity queryValidGroupBuyActivity(GroupBuyActivity groupBuyActivityReq);

}
