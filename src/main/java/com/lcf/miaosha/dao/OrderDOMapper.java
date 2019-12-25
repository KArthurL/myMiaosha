package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.ItemDO;
import com.lcf.miaosha.dataobject.OrderDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface OrderDOMapper {


    @Insert("insert into order_info (id,userid,itemPrice,orderPrice,itemid,orderAmount,promoId)" +
            "values(#{id},#{userid},#{itemPrice},#{orderPrice},#{itemid},#{orderAmount}#{promoId})")
    public int insert(OrderDO orderDO);
}
