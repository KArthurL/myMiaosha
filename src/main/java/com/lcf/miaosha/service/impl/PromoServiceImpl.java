package com.lcf.miaosha.service.impl;

import com.lcf.miaosha.dao.PromoDOMapper;
import com.lcf.miaosha.dataobject.PromoDO;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.service.ItemsService;
import com.lcf.miaosha.service.PromoService;
import com.lcf.miaosha.service.UserService;
import com.lcf.miaosha.service.model.ItemModel;
import com.lcf.miaosha.service.model.PromoModel;
import com.lcf.miaosha.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    PromoDOMapper promoDOMapper;

    @Autowired
    ItemsService itemsService;

    @Autowired
    UserService userService;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoById(Integer itemId) {
        //获取对应商品的秒杀活动信息
        PromoDO promoDO=promoDOMapper.getByItemId(itemId);
        PromoModel promoModel=convertFromDO(promoDO);
        if(promoModel==null){
            return null;
        }

        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now=new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    @Override
    public void publishPromo(Integer promoId) {
        //通过活动id获取活动
        PromoDO promoDO=promoDOMapper.getByItemId(promoId);
        if(promoDO.getItemId()==null||promoDO.getItemId().intValue()==0){
            return ;
        }
        ItemModel itemModel=itemsService.getItemByIdInCache(promoDO.getId());

        redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(),itemModel.getStock());

        //将大闸的限制数字设到redis内
        redisTemplate.opsForValue().set("promo_door_count_"+promoId,itemModel.getStock()*5);
    }

    @Override
    public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) {


        //判断售罄标识

        if(redisTemplate.hasKey("promo_item_stock__invalid_"+itemId)){

            return null;
        }
        PromoDO promoDO=promoDOMapper.getByPromoId(promoId);
        PromoModel promoModel=convertFromDO(promoDO);
        if(promoModel==null){
            return null;
        }

        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now=new DateTime();
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }


        if(promoModel.getStatus()!=2){
            return null;
        }
        ItemModel itemModel=itemsService.getItemByIdInCache(itemId);
        if(itemModel==null){
            return null;
        }
        UserModel userModel=userService.getUserByIdInCache(userId);
        if(userModel==null){

            return null;
        }

        //获取秒杀大闸的数量
        long result=redisTemplate.opsForValue().increment("promo_door_count_"+promoId,-1);
        if(result<0){
            return null;
        }


        String token= UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userid-"+userId+"_itemid_"+itemId,token);
        redisTemplate.expire("promo_token_"+promoId,5, TimeUnit.MINUTES);
        return token;
    }

    private PromoModel convertFromDO(PromoDO promoDO){
        if(promoDO==null){
            return null;
        }
        PromoModel promoModel=new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }

}
