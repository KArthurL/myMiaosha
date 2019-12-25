package com.lcf.miaosha.controller;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.lcf.miaosha.controller.viewobject.ItemVO;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.response.CommonReturnType;
import com.lcf.miaosha.service.BloomFilterService;
import com.lcf.miaosha.service.CacheService;
import com.lcf.miaosha.service.ItemsService;
import com.lcf.miaosha.service.PromoService;
import com.lcf.miaosha.service.model.ItemModel;
import org.checkerframework.checker.units.qual.A;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sun.awt.EmbeddedFrame;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController("item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")//跨域
public class ItemController extends BaseController {

    @Autowired
    ItemsService itemsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private PromoService promoService;

    @Autowired
    private BloomFilterService bloomFilterService;


    @RequestMapping(value = "/publishpromo",method = RequestMethod.GET,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType publishPromo(Integer id){
        promoService.publishPromo(id);
        return CommonReturnType.creat(null);

    }


    //创建商品的contoroller
    @RequestMapping(value = "/create",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createItem(String title, String description, BigDecimal price,Integer stock,String imgUrl) throws BusinessExeption {
        //封装service请求用来创建商品
        ItemModel itemModel=new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);
        itemModel.setPrice(price);

        ItemModel itemModelForReturn=itemsService.createItem(itemModel);

        ItemVO itemVO=convertVOFromModel(itemModelForReturn);
        return CommonReturnType.creat(itemVO);
    }


    //商品详情页浏览
    @RequestMapping(value = "/get",method = RequestMethod.GET,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType getItem(Integer id) throws BusinessExeption {

        if(!bloomFilterService.includeByBloomFilter(id)){
            throw new BusinessExeption(EmBusinessError.NONE_ITEM);
        }

        ItemModel itemModel=(ItemModel) cacheService.getFromCommonCache("item_"+id);

        if(itemModel==null) {
            //根据商品的id到redis内获取
            itemModel = (ItemModel) redisTemplate.opsForValue().get("item_" + id);

            //若redis内不存在对应的itemmodel，则访问service
            if (itemModel == null) {

                itemModel = itemsService.getItemById(id);
                //设置itemModel到redis内
                redisTemplate.opsForValue().set("item_" + id, itemModel);
                redisTemplate.expire("item_" + id, 1000+new Random().nextInt(1000), TimeUnit.SECONDS);
            }
            cacheService.setCommonCache("item_" + id, itemModel);
        }

        ItemVO itemVO=convertVOFromModel(itemModel);
        return CommonReturnType.creat(itemVO);
    }

    //商品列表页
    
    @RequestMapping(value = "/list",method = RequestMethod.GET,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType listItem(){
        List<ItemModel> itemModels=itemsService.listItem();
        List<ItemVO> itemVOS=itemModels.stream().map(itemModel -> {
                ItemVO itemVO=convertVOFromModel(itemModel);
                return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.creat(itemVOS);
    }



    private  ItemVO convertVOFromModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemVO itemVO=new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);

        if(itemModel.getPromoModel()!=null){
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());

        }else{
            itemVO.setPromoStatus(0);
        }


        return itemVO;
    }
}
