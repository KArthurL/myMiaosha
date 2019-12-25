package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.PromoDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PromoDOMapper {


    @Select("select * from promo where itemId=#{itemId}")
    public PromoDO getByItemId(@Param("itemId")int id);

    @Select("select * from promo where id=#{id}")
    PromoDO getByPromoId(@Param("id")int id);
}
