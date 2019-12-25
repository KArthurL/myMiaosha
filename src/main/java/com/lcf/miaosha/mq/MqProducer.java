package com.lcf.miaosha.mq;

import com.alibaba.fastjson.JSON;
import com.lcf.miaosha.dao.StockLogMapper;
import com.lcf.miaosha.dataobject.StockLogDO;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.service.OrderService;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqProducer {

    private DefaultMQProducer producer;


    private TransactionMQProducer transactionMQProducer;

    @Autowired
    private OrderService orderService;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;
    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private StockLogMapper stockLogMapper;

    @PostConstruct
    public void init() throws MQClientException {
        producer=new DefaultMQProducer("producer_group");

        transactionMQProducer=new TransactionMQProducer("producer_group_transaction");
        producer.setNamesrvAddr(nameAddr);
        producer.start();


        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.start();
        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {

                //创建订单

                Map<String,Object> map=(Map)o;
                Integer itemId=(Integer)map.get("itemId");
                Integer amount=(Integer)map.get("amount");
                Integer userId=(Integer)map.get("userId");
                Integer promoId=(Integer)map.get("promoId");
                String stockLogId=(String)map.get("stockLogId");
                try {
                    orderService.createOrder(userId,itemId,promoId,amount,stockLogId);
                } catch (BusinessExeption businessExeption) {
                    businessExeption.printStackTrace();

                    stockLogMapper.updateById(stockLogId,3);

                    return  LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                //根据是否扣减库存成功，来判断要返回commit，rollback还是unknown
                String json=new String(messageExt.getBody());
                Map<String,Object> map= JSON.parseObject(json,Map.class);
                Integer itemId=(Integer) map.get("itemId");
                Integer amount=(Integer) map.get("amount");
                String stockLogId=(String)map.get("stockLogId");
                 StockLogDO stockLogDO=stockLogMapper.getById(stockLogId);
                 if(stockLogDO==null){
                     return LocalTransactionState.ROLLBACK_MESSAGE;
                 }else if(stockLogDO.getStatus()==2){
                     return LocalTransactionState.COMMIT_MESSAGE;
                 }else if(stockLogDO.getStatus()==1){
                     return LocalTransactionState.UNKNOW;
                 }else{

                     return LocalTransactionState.ROLLBACK_MESSAGE;
                 }
            }
        });
    }


    //事务型同步库存扣减消息
    public boolean transactionAsyncReduceStock(Integer userId,Integer itemId,Integer promoId,Integer amount,String stockLogId){
        Map<String,Object> bodyMap=new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);
        bodyMap.put("userId",userId);
        bodyMap.put("promoId",promoId);
        bodyMap.put("stockLogId",stockLogId);

        Message message=new Message(topicName,"increase", JSON.toJSONString(bodyMap).getBytes());
        TransactionSendResult transactionSendResult=null;
        try {
            transactionSendResult = transactionMQProducer.sendMessageInTransaction(message,bodyMap);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if(transactionSendResult.getLocalTransactionState()==LocalTransactionState.ROLLBACK_MESSAGE){
            return false;
        }else if(transactionSendResult.getLocalTransactionState()==LocalTransactionState.COMMIT_MESSAGE) {
            return true;
        }
        return false;
    }



    //同步库存扣减消息

    public boolean asyncReduceStock(Integer itemId,Integer amount)  {

        Map<String,Object> bodyMap=new HashMap<>();
        bodyMap.put("itemId",itemId);
        bodyMap.put("amount",amount);


        Message message=new Message(topicName,"increase", JSON.toJSONString(bodyMap).getBytes());

        try {
            SendResult sendResult=producer.send(message);
            if(sendResult.getSendStatus()==SendStatus.SEND_OK){
                return true;
            }else{
                return false;
            }
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

    }


}
