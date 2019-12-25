package com.lcf.miaosha.service;

import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.service.model.UserModel;

public interface UserService {

    //通过用户id获取用户对象
    UserModel getUserById(Integer id);

    UserModel getUserByIdInCache(Integer id);
    void register(UserModel userModel) throws BusinessExeption;


    /**
     *
     * @param telphone 用户注册手机
     * @param password 用户加密后密码
     * @throws BusinessExeption
     */
    UserModel validateLogin(String telphone,String password) throws BusinessExeption;
}
