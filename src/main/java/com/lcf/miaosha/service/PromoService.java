package com.lcf.miaosha.service;

import com.lcf.miaosha.service.model.PromoModel;

public interface PromoService {

    PromoModel getPromoById(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId);


    //生成秒杀用的令牌
    String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId);
}
