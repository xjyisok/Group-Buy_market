package cn.sweater.domain.tag.adapter.repository;

import cn.sweater.domain.tag.model.entity.CrowdTagsJobEntity;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Service;

public interface ITagRepository {
    CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId);

    void addCrowdTagsUserId(String tagId, String userId);

    void updateCrowdTagsStatistics(String tagId, int size);

    Boolean getBitSet(String userId,String tagId);
}
