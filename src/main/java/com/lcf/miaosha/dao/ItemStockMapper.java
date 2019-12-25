package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.ItemDO;
import com.lcf.miaosha.dataobject.ItemStockDO;
import org.apache.ibatis.annotations.*;

import javax.validation.constraints.Max;

@Mapper
public interface ItemStockMapper {



    @Insert("insert into item_stock (stock,item_id) " +
            "values(#{stock},#{item_id})")
    @Options(useGeneratedKeys = true,keyProperty = "id", keyColumn = "id")
    public int insert(ItemStockDO itemStockDO);

    @Select("select * from item_stock where item_id = #{id}")
    public ItemStockDO getById(@Param("id")int id);


    @Update("update item_stock set stock=stock-#{amount} where item_id=#{itemId} and stock >=#{amount}")
    public int decreaseStock(@Param("itemId") Integer itemId,@Param("amount") Integer amount);

}
