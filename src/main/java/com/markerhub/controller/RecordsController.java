package com.markerhub.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.Blog;
import com.markerhub.entity.Records;
import com.markerhub.service.ConTypeService;
import com.markerhub.service.RecordsService;
import com.markerhub.shiro.AccountProfile;
import com.markerhub.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;


import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class RecordsController {
    @Autowired
    RecordsService recordsService;

    @Autowired
    ConTypeService conTypeService;
    @Autowired
    EsUtil esUtil;
    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("/recordsByPage")
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage) {

        Page page = new Page(currentPage, 5);
        IPage pageData = recordsService.page(page, new QueryWrapper<Records>().orderByDesc("crtTime"));

        return Result.succ(pageData);
    }

    @GetMapping("/record/{id}")
    public Result recDetail(@PathVariable(name = "id") Long id) {
        Records rec = recordsService.getById(id);
        Assert.notNull(rec, "该记录已被删除");
        return Result.succ(rec);
    }

    @RequiresAuthentication
    @PostMapping("/record/edit")
    public Result edit(@Validated @RequestBody Records records) throws IOException {
        if(StringUtils.isNotBlank( records.getId())) {
            records.setUpdTime(TimeUtil.getTodayTimeForMat());
        } else {
            records.setUserId(String.valueOf(ShiroUtil.getProfile().getId()));
            records.setCrtTime(TimeUtil.getTodayTimeForMat());
            records.setUpdTime(TimeUtil.getTodayTimeForMat());
//            BeanUtil.copyProperties(records, temp, "conType", "title", "conMoney", "conTime","remark");
        }

//        blogService.saveOrUpdate(temp);
        boolean b = recordsService.saveOrUpdate(records);
        Map<String, Object> recMap = EntityUtils.entityToMap(records);
        esUtil.saveDoc(recMap);
        return Result.succ(null);
    }
    @RequiresAuthentication
    @GetMapping("/recordsList")
    public Result getRecordsListGroupByConType(){
        long days = 6;
        Map<String, Object> resMap = new HashMap<>();
        Map<String, Object> recordsMap = new HashMap<>();
        QueryWrapper<Records> queryWrapper = new QueryWrapper<>();
        String userId = String.valueOf(ShiroUtil.getProfile().getId());
        String beforeDay = TimeUtil.getTheDayBeforeSomeDays(days -1 )+"";
        String afterDay = TimeUtil.getTheDayAfterSomeDays(1)+"";
        queryWrapper.select("conType","conMoney","conTime").eq("userId",userId)
                    .between("conTime",beforeDay,afterDay);

        List<Records> recordsList = recordsService.list(queryWrapper);
        Map<String, Object> typeMap = conTypeService.getTypeValueList(userId);
        Map<String, List<Records>> collect = recordsList.stream().collect(Collectors.groupingBy(Records::getConType));
        collect.forEach((k,v) -> {
            Map<String, List<Records>> groupByCrtTime = v.stream()
                    .sorted(Comparator.comparing(Records::getConTime,Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.groupingBy(Records::getConTime, LinkedHashMap::new,Collectors.toList()));
            String typeName = String.valueOf(typeMap.get(k));
            recordsMap.put(typeName,checkRecords(groupByCrtTime,days));
        });
        resMap.put("recordsMap",recordsMap);
        resMap.put("daysList",getDays(days));
        return Result.succ(resMap);
    }
    @RequiresAuthentication
    @GetMapping("/typeList")
    public Result getTypeListGroupByConType(){
        long days = 6;
        Map<String, String> resMap = new HashMap<>();
        String userId = String.valueOf(ShiroUtil.getProfile().getId());
        Map<String, Object> typeMap = conTypeService.getTypeValueList(userId);
        QueryWrapper<Records> queryWrapper = new QueryWrapper<>();
        String beforeDay = TimeUtil.getTheDayBeforeSomeDays(days-1)+"";
        String afterDay = TimeUtil.getTheDayAfterSomeDays(1)+" ";
        queryWrapper.eq("userId",userId)
                .between("crtTime",beforeDay,afterDay);
        List<Records> recordsList = recordsService.list(queryWrapper);
        Map<String, List<Records>> collect = recordsList.stream().collect(Collectors.groupingBy(Records::getConType));
        collect.forEach((k,v) -> {
            BigDecimal sum = v.stream().map(Records::getConMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
            String typeName = String.valueOf(typeMap.get(k));
            resMap.put(typeName,String.valueOf(sum));
        });
        return Result.succ(resMap);
    }
    @RequiresAuthentication
    @GetMapping("/recList")
    public Result getRecordsList(){
        Map<String, String> resMap = new HashMap<>();
        String userId = String.valueOf(ShiroUtil.getProfile().getId());
        QueryWrapper<Records> queryWrapper = new QueryWrapper<>();
        String beforeDay = TimeUtil.getTheDayBeforeSomeDays(2)+" 00:00:00";
        String afterDay = TimeUtil.getTheDayAfterSomeDays(1)+" 00:00:00";
        queryWrapper.eq("userId",userId)
                .between("crtTime",beforeDay,afterDay)
                .orderByDesc("crtTIme");
        List<Records> recordsList = recordsService.list(queryWrapper);
//        Map<String, List<Records>> collect = recordsList.stream().collect(Collectors.groupingBy(Records::getConType));
//        collect.forEach((k,v) -> {
//            BigDecimal sum = v.stream().map(Records::getConMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
//            resMap.put(k,String.valueOf(sum));
//        });
        return Result.succ(recordsList);
    }
    @RequiresAuthentication
    @PostMapping("/record/search")
    public Result searchRecByWord(@RequestBody Map<String,Object> map) throws IOException {
        String searchWord= String.valueOf(map.get("searchWord"));
        String userId = String.valueOf(ShiroUtil.getProfile().getId());
        List<Map<String, Object>> mapList = esUtil.searchRecDoc(searchWord,userId);
        return Result.succ(mapList);
    }

    private List<String> checkRecords(Map<String, List<Records>> map,long days){
        List<String> list = new ArrayList<>();
        for (long i = days; i > 0; i--) {
            String recordsSum = "0";
            LocalDate theDayBeforeSomeDays = TimeUtil.getTheDayBeforeSomeDays(i - 1);
            List<Records> recordsList = map.get(theDayBeforeSomeDays.toString());
            if (!CollectionUtils.isEmpty(recordsList)){
                BigDecimal sum = recordsList.stream().map(Records::getConMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
                recordsSum = sum.toString();
            }
            list.add(recordsSum);
        }
        return list;
    }

    private List<String> getDays(long days){
        List<String> list = new ArrayList<>();
        for (long i = days; i > 0; i--) {
            String recordsSum = "0";
            LocalDate theDayBeforeSomeDays = TimeUtil.getTheDayBeforeSomeDays(i - 1);
            list.add(theDayBeforeSomeDays.toString().substring(5));
        }
        return list;
    }

    private String getRecordsSum(List<Records> list){
        BigDecimal res = new BigDecimal("0");
        list.forEach(rec -> {
            res.add(rec.getConMoney());
        });
        return res.toString();
    }
}
