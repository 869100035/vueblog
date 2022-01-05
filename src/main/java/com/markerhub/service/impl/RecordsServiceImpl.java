package com.markerhub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markerhub.entity.Goods;
import com.markerhub.entity.Records;
import com.markerhub.mapper.GoodsMapper;
import com.markerhub.mapper.RecordsMapper;
import com.markerhub.service.GoodsService;
import com.markerhub.service.RecordsService;
import org.springframework.stereotype.Service;

@Service
public class RecordsServiceImpl extends ServiceImpl<RecordsMapper, Records> implements RecordsService {

}
