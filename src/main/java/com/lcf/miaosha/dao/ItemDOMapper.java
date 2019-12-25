package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.ItemDO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ItemDOMapper {

    @Select("select * from item where id = #{id}")
    @Results({
            //不加这个，id就莫的了
            @Result(property = "id",column = "id"),
            @Result(property = "itemStockDO",column = "id",
                    one=@One(select="com.lcf.miaosha.dao.ItemStockMapper.getById"))
    })
    public ItemDO getById(@Param("id") int id);

    @Insert("insert into item (title,price,description,sales,imgUrl)" +
            "values(#{title},#{price},#{description},#{sales},#{imgUrl})")
    @Options(useGeneratedKeys = true,keyProperty = "id", keyColumn = "id")
    public int insert(ItemDO itemDO);


    @Select("select * from item order by id")
    @Results({
            //不加这个，id就莫的了
            @Result(property = "id",column = "id"),
            @Result(property = "itemStockDO",column = "id",
            one=@One(select="com.lcf.miaosha.dao.ItemStockMapper.getById"))
    })
    public List<ItemDO> listItem();


    @Update("update item set sales=sales+#{amount} where id=#{id}")
    public int increaseSales(@Param("id") Integer id,@Param("amount") Integer amount);


    @Select("select id from item")
    public List<Integer> getIds();
}
