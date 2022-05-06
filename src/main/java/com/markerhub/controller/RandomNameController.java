package com.markerhub.controller;

import com.markerhub.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/stu")
public class RandomNameController {

    @Autowired
    private RedisUtil redisUtil;

    private final static String rdmNmKey0 = "random:name:key0";
    private final static String rdmNmKey1 = "random:name:key1";
    private final static String rdmNmKey2 = "random:name:key2";

    @PostMapping("/addStu")
    @ResponseBody
    public String addStudent(String stuNm,String classNum){
        if (StringUtils.isNotBlank(stuNm)){
            redisUtil.sadd(switchClass(classNum),stuNm);
            redisUtil.expire(switchClass(classNum),-1);
        }
        return "1";
    }

    @PostMapping("/delStu")
    @ResponseBody
    public String delStudent(String name,String classNum){
        redisUtil.sdel(switchClass(classNum),name);
        redisUtil.expire(switchClass(classNum),-1);
        return "1";
    }

    @PostMapping("/rdmStu")
    @ResponseBody
    public List rdmStudent(String rdmCount,String classNum){
        if (StringUtils.isBlank(rdmCount)){
            rdmCount = "1";
        }
        List list = redisUtil.sRandomMembers(switchClass(classNum), Long.valueOf(rdmCount));
        return list;
    }

    @GetMapping("/indexStu")
    public String listStudent(Map<String,Object> map,String classNum){
        Set set = redisUtil.sMembers(switchClass(classNum));
        ArrayList list = new ArrayList<>(set);
        map.put("stuList",list);
        return "student";
    }

    @PostMapping("/selClass")
    @ResponseBody
    public List listStudent(String classNum,Map<String,Object> map){
        Set set = redisUtil.sMembers(switchClass(classNum));
        ArrayList list = new ArrayList<>(set);
        return list;
    }

    private String switchClass(String classNum){
        classNum = StringUtils.isBlank(classNum)?"":classNum;
        String key = rdmNmKey0;
        switch (classNum){
            case "1":
                key = rdmNmKey1;
                break;
            case "2":
                key = rdmNmKey2;
                break;
        }
        return key;
    }
}
