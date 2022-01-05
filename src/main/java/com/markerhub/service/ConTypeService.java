package com.markerhub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.markerhub.entity.ConType;

import java.util.List;
import java.util.Map;

public interface ConTypeService extends IService<ConType> {
    Map<String, Object> getTypeValueList(String userId);
}
