package com.lcf.miaosha.mq;

import com.alibaba.fastjson.JSON;
import com.lcf.miaosha.dao.DuplicateMapper;
import com.lcf.miaosha.dao.ItemStockMapper;
import com.lcf.miaosha.dataobject.ItemStockDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class MqConsumer {


    private DefaultMQPushConsumer consumer;


    @Autowired
    private ItemStockMapper itemStockMapper;

    @Autowired
    private DuplicateMapper duplicateMapper;

    @Value("mq.nameserver.addr")
    private String nameAddr;
    @Value("mq.topicname")
    private String topicName;

    @PostConstruct
    public void init() throws MQClientException {
        consumer=new DefaultMQPushConsumer("stock_consumer_group");

        consumer.setNamesrvAddr(nameAddr);
        consumer.subscribe(topicName,"*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

                Message msg=list.get(0);
                String json=new String(msg.getBody());
                Map<String,Object> map= JSON.parseObject(json,Map.class);
                Integer itemId=(Integer) map.get("itemId");
                Integer amount=(Integer) map.get("amount");
                String stockLogId=(String) map.get("stockLogId");

                try {
                    duplicateMapper.insert(stockLogId);
                    itemStockMapper.decreaseStock(itemId, amount);
                }catch (DuplicateKeyException e){
                    log.error("发生重复消费");

                }finally {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

                }
            }
        });

        consumer.start();
    }
}
