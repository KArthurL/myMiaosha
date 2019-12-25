package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.SequenceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SequenceDOMapper {


    @Select("select * from sequence_info where name = #{name} for update")
    public SequenceDO getByName(@Param("name") String name);


    @Update("update sequence_info set current_value=#{current_value},step=#{step} where name=#{name} ")
    public int update(SequenceDO sequenceDO);
}
