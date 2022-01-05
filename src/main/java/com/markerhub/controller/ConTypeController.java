package com.markerhub.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.ConType;
import com.markerhub.entity.Records;
import com.markerhub.service.ConTypeService;
import com.markerhub.service.RecordsService;
import com.markerhub.util.RedisUtil;
import com.markerhub.util.ShiroUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
public class ConTypeController {

    @Autowired
    ConTypeService conTypeService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RecordsService recordsService;


    @RequiresAuthentication
    @GetMapping("/conTypeList")
    public Result list() {
        String userId = String.valueOf(ShiroUtil.getProfile().getId());
        Map<String, Object> typeValueList = conTypeService.getTypeValueList(userId);
        return Result.succ(typeValueList);
    }

    @RequiresAuthentication
    @PostMapping("/updConType")
    public Result updConType(@RequestBody List<Map<String,String>> conTypeList) {
        String userId = String.valueOf(ShiroUtil.getProfile().getId());
        Map<String, Object> delParam = new HashMap<>();
        List<ConType> saveList = new ArrayList<>(conTypeList.size());
        for (int i = 1; i <= conTypeList.size() ; i++) {
            Map<String, String> map = conTypeList.get(i - 1);
            String oldCd = map.get("typeCd");
            String typeCd = StringUtils.isNotBlank(oldCd)?oldCd:UUID.randomUUID().toString().replace("-","");
            ConType params = new ConType();
            params.setUserId(userId);
            params.setTypeCd(typeCd);
            params.setTypeName(map.get("typeName"));
            params.setCrtTime(LocalDateTime.now().toString());
            params.setUpdTime(LocalDateTime.now().toString());
            saveList.add(params);
        }
        delParam.put("userId",userId);
        boolean delRes = conTypeService.removeByMap(delParam);
        boolean saveRes = conTypeService.saveBatch(saveList, saveList.size());
        if ( delRes && saveRes ){
            redisUtil.del(userId);
            delRecsByTypeCd(userId,conTypeList);
            return Result.succ("success");
        }
        return Result.fail("修改失败");
    }

    private void delRecsByTypeCd(String userId,List<Map<String,String>> conTypeList){
        List<String> existTypeCds = new ArrayList<>();
        QueryWrapper<Records> queryWrapper = new QueryWrapper<>();
         conTypeList.forEach(con ->{
             String typeCd = con.get("typeCd");
             if (StringUtils.isNotBlank(typeCd)){
                 existTypeCds.add(typeCd);
             }
         });
        queryWrapper.eq("userId",userId).notIn("conType",existTypeCds);
        boolean remove = recordsService.remove(queryWrapper);
        System.out.println("删除结果"+remove);


    }
}
