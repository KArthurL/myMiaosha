package com.lcf.miaosha.dao;

import com.lcf.miaosha.dataobject.UserDO;
import com.lcf.miaosha.dataobject.UserPasswordDO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

@Mapper
public interface UserPasswoedDOMapper {


    @Select("select * from user_password where user_id=#{uid}")
    public UserPasswordDO getByUserId(@Param("uid") Integer uid);

    @Insert("insert into user_password (encrpt_password,user_id) " +
            "values(#{encrpt_password},#{user_id})")
    @Options(useGeneratedKeys = true,keyProperty = "id", keyColumn = "id")
    public int insert(UserPasswordDO userPasswordDO);


}
