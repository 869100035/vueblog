package com.markerhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markerhub.entity.ConType;
import com.markerhub.mapper.ConTypeMapper;
import com.markerhub.service.ConTypeService;
import com.markerhub.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
public class ConTypeServiceImpl extends ServiceImpl<ConTypeMapper, ConType> implements ConTypeService {
    @Autowired
    RedisUtil redisUtil;

    @Override
    public Map<String, Object> getTypeValueList(String userId) {
        Map<String, Object> hmget = redisUtil.hmget(userId);
        if (CollectionUtils.isEmpty(hmget)){
            List<ConType> list = list();
            list.forEach(conType -> {
                hmget.put(conType.getTypeCd(),conType.getTypeName());
            });
            redisUtil.hmset(userId,hmget,3600*24*30);
        }
        return hmget;
    }
}
