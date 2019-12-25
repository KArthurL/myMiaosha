package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.ItemStockDO;
import com.lcf.miaosha.dataobject.StockLogDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StockLogMapper {



    @Insert("insert into stock_log (stock_log_id,item_id,amount,status) " +
            "values(#{stock_log_id},#{item_id},#{amount},#{status})")
    public void insert(StockLogDO stockLogDO);


    @Select("select * from stock_log where stock_log_id = #{id}")
    public StockLogDO getById(@Param("id") String id);


    @Update("update stock_log set status=#{status} where stock_log_id=#{id}")
    int updateById(@Param("id")String id,@Param("status") Integer status);


}
