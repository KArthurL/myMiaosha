package com.lcf.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.google.common.util.concurrent.RateLimiter;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.mq.MqProducer;
import com.lcf.miaosha.response.CommonReturnType;
import com.lcf.miaosha.service.ItemsService;
import com.lcf.miaosha.service.OrderService;
import com.lcf.miaosha.service.PromoService;
import com.lcf.miaosha.service.model.OrderModel;
import com.lcf.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@RestController("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")//跨域
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    MqProducer mqProducer;

    @Autowired
    private ItemsService itemsService;

    @Autowired
    private PromoService promoService;


    private RateLimiter rateLimiter;

    @PostConstruct
    public void init(){
        RateLimiter.create(300);
    }




    //封装下单请求

    @RequestMapping(value = "/createorder",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType createOrder(Integer itemId,Integer amount,
                                        @RequestParam(name="promoId",required = false) Integer promoId,
                                        @RequestParam(name="generateToken",required = false) String generateToken) throws BusinessExeption {



        if(!rateLimiter.tryAcquire()){
            throw new BusinessExeption(EmBusinessError.RATELIMIT);
        }


        //获取用户登录信息
        String token=httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token))
        {
            throw new BusinessExeption(EmBusinessError.USE_NOT_LOGIN,"用户未登录");
        }
        UserModel userModel=(UserModel) redisTemplate.opsForValue().get(token);
      /*  Boolean isLogin=(Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");*/
        if(userModel==null){
            throw new BusinessExeption(EmBusinessError.USE_NOT_LOGIN,"用户未登录");
        }

        //UserModel userModel=(UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");

        if(promoId!=null){
            String inRedisPromoToken =(String)redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid-"+userModel.getId()+"_itemid_"+itemId);

            if(inRedisPromoToken==null){
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌错误");
            }

            if(!StringUtils.equals(inRedisPromoToken,generateToken))
            {
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌错误");
            }

        }

        //判断售罄标识

        if(redisTemplate.hasKey("promo_item_stock__invalid_"+itemId)){

            throw new BusinessExeption(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //加入库存流水init状态

       String stockLogId= itemsService.initStockLog(itemId,amount);

        //再去完成事务消息
        //OrderModel orderModel=orderService.createOrder(userModel.getId(),itemId,amount,promoId);

        if(!mqProducer.transactionAsyncReduceStock(userModel.getId(),itemId,amount,promoId,stockLogId)){
            throw new BusinessExeption(EmBusinessError.UNKNOWN_ERROR,"下单失败");
        }

        return CommonReturnType.creat(null);


    }
    @RequestMapping(value = "/generatetoken",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType generateToken(Integer itemId, Integer promoId) throws BusinessExeption {

        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessExeption(EmBusinessError.USE_NOT_LOGIN, "用户未登录");
        }
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        /*  Boolean isLogin=(Boolean)httpServletRequest.getSession().getAttribute("IS_LOGIN");*/
        if (userModel == null) {
            throw new BusinessExeption(EmBusinessError.USE_NOT_LOGIN, "用户未登录");
        }

        String generateToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if (generateToken == null) {
            throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR, "生成令牌失败");
        }

        return CommonReturnType.creat(generateToken);
    }

    }
