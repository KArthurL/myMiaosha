package com.lcf.miaosha.service.impl;

import com.lcf.miaosha.dao.ItemDOMapper;
import com.lcf.miaosha.dao.ItemStockMapper;
import com.lcf.miaosha.dao.StockLogMapper;
import com.lcf.miaosha.dataobject.ItemDO;
import com.lcf.miaosha.dataobject.ItemStockDO;
import com.lcf.miaosha.dataobject.StockLogDO;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.mq.MqProducer;
import com.lcf.miaosha.service.ItemsService;
import com.lcf.miaosha.service.PromoService;
import com.lcf.miaosha.service.model.ItemModel;
import com.lcf.miaosha.service.model.PromoModel;
import com.lcf.miaosha.validator.ValidationResult;
import com.lcf.miaosha.validator.ValidatorImpl;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemsService {

    @Autowired
    private ValidatorImpl validator;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer producer;


    @Autowired
    private StockLogMapper stockLogMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessExeption {
        ValidationResult validationResult=validator.validate(itemModel);
        //校验入参
        if(validationResult.isHasErrors()){
            throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,validationResult.getErrorMsg());
        }
        //转化model为DO
        ItemDO itemDO=convertFromModel(itemModel);
        itemDOMapper.insert(itemDO);
        itemModel.setId(itemDO.getId());
        ItemStockDO itemStockDO=convertStockFromModel(itemModel);
        itemStockMapper.insert(itemStockDO);

        return getItemById(itemModel.getId());
    }

    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList=itemDOMapper.listItem();
        List<ItemModel> itemModels= itemDOList.stream().map(itemDO -> {
        ItemStockDO itemStockDO=itemDO.getItemStockDO();
        ItemModel itemModel=convertFromDO(itemDO,itemStockDO);
        return itemModel;
        }).collect(Collectors.toList());
        return itemModels;
    }

    @Override
    public ItemModel getItemById(int id) {
        ItemDO itemDO=itemDOMapper.getById(id);
        if(itemDO==null){
            return null;
        }
        //操作获得库存数量

        ItemModel itemModel=convertFromDO(itemDO,itemDO.getItemStockDO());
        PromoModel promoModel= promoService.getPromoById(itemModel.getId());
        if(promoModel!=null&&promoModel.getStatus().intValue()!=3){

            itemModel.setPromoModel(promoModel);

        }
        return itemModel;
    }

    @Override

    public boolean decreaseStock(Integer id, Integer amount) {
        //int affectedRow=itemStockMapper.decreaseStock(id,amount);
        long result=redisTemplate.opsForValue().increment("promo_item_stock_"+id,amount.intValue()*-1);
        if(result>0){
                return true;
        }else if(result==0){

            //打上库存已售罄的标识
            redisTemplate.opsForValue().set("promo_item_stock__invalid_"+id,"true");


            return true;
        }
        else {
            redisTemplate.opsForValue().increment("promo_item_stock_"+id,amount.intValue());
            return false;
        }
    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) {
        itemDOMapper.increaseSales(itemId,amount);
    }

    private ItemDO convertFromModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemDO itemDO=new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        return itemDO;
    }


    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel=(ItemModel) redisTemplate.opsForValue().get("item_validate_"+id);

        if(itemModel==null){
            itemModel=this.getItemById(id);
            redisTemplate.opsForValue().set("item_validate_"+id,itemModel);
            redisTemplate.expire("item_validate_"+id,10, TimeUnit.SECONDS);
        }

        return itemModel;
    }


    @Override
    public boolean asyncDecreaseStock(Integer id, Integer amount) {
        return false;
    }

    @Override
    @Transactional
    public String initStockLog(Integer itemId, Integer amount) {

        StockLogDO stockLogDO=new StockLogDO();
        stockLogDO.setItem_id(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStock_log_id(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setStatus(1);
        stockLogMapper.insert(stockLogDO);
        return stockLogDO.getStock_log_id();

    }

    private ItemStockDO convertStockFromModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemStockDO itemStockDO=new ItemStockDO();
        itemStockDO.setItem_id(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }
    private ItemModel convertFromDO(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel=new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
    
}
