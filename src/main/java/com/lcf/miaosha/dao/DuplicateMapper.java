package com.lcf.miaosha.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DuplicateMapper {



    @Insert("insert into duplicate (id) values(#{id})")
    void insert(@Param("id") String s);

}
