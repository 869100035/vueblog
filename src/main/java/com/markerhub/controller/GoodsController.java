package com.markerhub.controller;

import com.markerhub.common.lang.Result;
import com.markerhub.entity.Goods;
import com.markerhub.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    GoodsService goodsService;

    @GetMapping("/goodsList")
    public Result getGoods(){
        List<Goods> goodsList = goodsService.list();
        return Result.succ(goodsList);
    }
}
