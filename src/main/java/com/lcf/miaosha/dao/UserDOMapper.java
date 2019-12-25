package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.UserDO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserDOMapper {

    @Select("select * from user_info where id = #{id}")
    public UserDO getById(@Param("id")int id);

    @Insert("insert into user_info (name,gender,age,telphone,register_mode,third_party_id) " +
            "values(#{name},#{gender},#{age},#{telphone},#{register_mode},#{third_party_id})")
    @Options(useGeneratedKeys = true,keyProperty = "id", keyColumn = "id")
    public int insert(UserDO userDO);

    @Select("select * from user_info where telphone = #{telphone}")
    public UserDO getByTelphone(@Param("telphone")String telphone);
}
