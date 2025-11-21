package cn.sweater.infrastructure.adapter.repository;

import cn.sweater.domain.tag.adapter.repository.ITagRepository;
import cn.sweater.domain.tag.model.entity.CrowdTagsJobEntity;
import cn.sweater.infrastructure.dao.ICrowdTagsDao;
import cn.sweater.infrastructure.dao.ICrowdTagsDetailDao;
import cn.sweater.infrastructure.dao.ICrowdTagsJobDao;
import cn.sweater.infrastructure.dao.po.CrowdTags;
import cn.sweater.infrastructure.dao.po.CrowdTagsDetail;
import cn.sweater.infrastructure.dao.po.CrowdTagsJob;
import cn.sweater.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.BitSet;

@Service
public class ITagRepositoryImpl implements ITagRepository {
    @Resource
    private ICrowdTagsDao crowdTagsDao;
    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;
    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao;
    @Resource
    private IRedisService redisService;
    @Override
    public CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId) {
        CrowdTagsJob  crowdTagsJob=crowdTagsJobDao.queryCrowdTagsJob(CrowdTagsJob.builder()
                        .batchId(batchId)
                        .tagId(tagId)
                .build());
        CrowdTagsJobEntity crowdTagsJobEntity=new CrowdTagsJobEntity();
        crowdTagsJobEntity.setTagType(crowdTagsJob.getTagType());
        crowdTagsJobEntity.setTagRule(crowdTagsJob.getTagRule());
        crowdTagsJobEntity.setStatEndTime(crowdTagsJob.getStatEndTime());
        crowdTagsJobEntity.setStatStartTime(crowdTagsJob.getStatStartTime());
        return crowdTagsJobEntity;
    }

    @Override
    public void addCrowdTagsUserId(String tagId, String userId) {
        CrowdTagsDetail crowdTagsDetailReq = new CrowdTagsDetail();
        crowdTagsDetailReq.setTagId(tagId);
        crowdTagsDetailReq.setUserId(userId);

        try {
            crowdTagsDetailDao.addCrowdTagsUserId(crowdTagsDetailReq);

            // 获取BitSet
            RBitSet bitSet = redisService.getBitSet(tagId);
            bitSet.set(redisService.getIndexFromUserId(userId), true);
        } catch (DuplicateKeyException ignore) {
            // 忽略唯一索引冲突
        }

    }

    @Override
    public void updateCrowdTagsStatistics(String tagId, int size) {
        CrowdTags crowdTagsreq = new CrowdTags();
        crowdTagsreq.setTagId(tagId);
        crowdTagsreq.setStatistics(size);
        crowdTagsDao.updateCrowdTagsStatistics(crowdTagsreq);
    }

    @Override
    public Boolean getBitSet(String userId,String tagId) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }
}
