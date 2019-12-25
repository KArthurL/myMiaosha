package com.lcf.miaosha.service.impl;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.lcf.miaosha.dao.OrderDOMapper;
import com.lcf.miaosha.dao.SequenceDOMapper;
import com.lcf.miaosha.dao.StockLogMapper;
import com.lcf.miaosha.dataobject.OrderDO;
import com.lcf.miaosha.dataobject.SequenceDO;
import com.lcf.miaosha.dataobject.StockLogDO;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.service.ItemsService;
import com.lcf.miaosha.service.OrderService;
import com.lcf.miaosha.service.UserService;
import com.lcf.miaosha.service.model.ItemModel;
import com.lcf.miaosha.service.model.OrderModel;
import com.lcf.miaosha.service.model.UserModel;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private ItemsService itemsService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;
    @Autowired
    private StockLogMapper stockLogMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount,Integer promoId,String stockLogId) throws BusinessExeption {
        //校验下单状态，下单商品是否存在，用户是否合法，购买数量是否正确
        //ItemModel itemModel=itemsService.getItemById(itemId);
        ItemModel itemModel=itemsService.getItemByIdInCache(itemId);
        if(itemModel==null){
            throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel=userService.getUserByIdInCache(userId);
        if(itemModel==null){
            throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }
        if(amount<=0||amount>99){
            throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量填写有误");
        }

        //校验活动信息
        if(promoId!=null){
            if(promoId.intValue()!=itemModel.getPromoModel().getId()){
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息不正确");
            }else if(itemModel.getPromoModel().getStatus()!=2){
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动信息还未开始");
            }
        }


        //落单减库存
        boolean flag=itemsService.decreaseStock(itemId,amount);
        if(!flag){
            throw new BusinessExeption(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //订单入库
        OrderModel orderModel=new OrderModel();

        orderModel.setUserid(userId);
        orderModel.setItemid(itemId);
        orderModel.setOrderAmount(amount);
        if(promoId!=null){
        orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        orderModel.setOrderPrice(itemModel.getPromoModel().getPromoItemPrice().multiply(new BigDecimal(amount)));
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
            orderModel.setOrderPrice(itemModel.getPrice().multiply(new BigDecimal(amount)));
        }
        orderModel.setId(orderService.generateOrderNo());
        orderModel.setPromoId(promoId);
        OrderDO orderDO=convertFromOrderModel(orderModel);
        orderDOMapper.insert(orderDO);
        //加上商品销量
        itemsService.increaseSales(itemId,amount);

        //设置库存流水状态为成功
        StockLogDO stockLogDO=stockLogMapper.getById(stockLogId);
        if(stockLogDO==null){
            throw new BusinessExeption(EmBusinessError.UNKNOWN_ERROR);
        }
        //stockLogDO.setStatus(2);

        stockLogMapper.updateById(stockLogId,2);
        //返回前端
        return orderModel;
    }


    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO  orderDO=new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        return orderDO;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateOrderNo(){
        StringBuilder stringBuilder=new StringBuilder();
        //订单号16位
        //前8位时间信息，年月日
        LocalDateTime now =LocalDateTime.now();
        String nowDate=now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);
        //中间6位为自增序列
        int sequence=0;
        SequenceDO sequenceDO=sequenceDOMapper.getByName("order_info");
        sequence= sequenceDO.getCurrent_value();
        sequenceDO.setCurrent_value(sequence+sequenceDO.getStep());
        sequenceDOMapper.update(sequenceDO);
        String sequenceStr=String.valueOf(sequence);
        for(int i=0;i<6-sequenceStr.length();i++)
        {
            stringBuilder.append("0");
        }
        stringBuilder.append(sequenceStr);
        //最后2位为分库分表位,暂时写死
        stringBuilder.append("00");
        return stringBuilder.toString();
    }
}
