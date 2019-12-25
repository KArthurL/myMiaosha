package com.lcf.miaosha.service;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.service.model.ItemModel;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ItemsService {

    //创建商品
    ItemModel createItem(ItemModel itemModel) throws BusinessExeption;

    //商品列表浏览
    List<ItemModel> listItem();

    //商品详情浏览
    ItemModel getItemById(int id);

    //库存减扣
    boolean decreaseStock(Integer id,Integer amount);

    //商品销量增加
    void increaseSales(Integer itemId,Integer amount);

    //验证Item以及promo model 缓存模型
    ItemModel getItemByIdInCache(Integer id);


    //初始化库存流水
    String initStockLog(Integer itemId,Integer amount);

    boolean asyncDecreaseStock(Integer id,Integer amount);
}
