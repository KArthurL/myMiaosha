package com.lcf.miaosha.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.lcf.miaosha.dao.UserDOMapper;
import com.lcf.miaosha.dao.UserPasswoedDOMapper;
import com.lcf.miaosha.dataobject.UserDO;
import com.lcf.miaosha.dataobject.UserPasswordDO;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.service.UserService;
import com.lcf.miaosha.service.model.UserModel;
import com.lcf.miaosha.validator.ValidationResult;
import com.lcf.miaosha.validator.ValidatorImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswoedDOMapper userPasswoedDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public UserModel getUserById(Integer id) {
        //调用dao层获取对应用户的dataobject
        UserDO userDO=userDOMapper.getById(id);
        if(userDO==null){
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDO userPasswordDO=userPasswoedDOMapper.getByUserId(userDO.getId());
        return convertFromDataObject(userDO,userPasswordDO);
    }


    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessExeption {
            if(userModel==null){
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR);
            }
          ValidationResult result= validator.validate(userModel);
            if(result.isHasErrors()){
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrorMsg());
            }
            UserDO userDO=convertFromModel(userModel);
            try{
            //自增主键复制给userDO
            userDOMapper.insert(userDO);}
            catch (DuplicateKeyException ex){
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机已被注册");
            }
            userModel.setId(userDO.getId());
            UserPasswordDO userPasswordDO=convertPasswordFromModel(userModel);

            userPasswoedDOMapper.insert(userPasswordDO);
    }


    @Override
    public UserModel validateLogin(String telphone, String password) throws BusinessExeption {
        //通过用户的手机获取用户信息
        UserDO userDO=userDOMapper.getByTelphone(telphone);
        if(userDO==null){
            throw new BusinessExeption(EmBusinessError.USE_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO=userPasswoedDOMapper.getByUserId(userDO.getId());
        UserModel userModel=convertFromDataObject(userDO,userPasswordDO);


        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if(!StringUtils.equals(password,userModel.getEncrptPassword())){
            throw new BusinessExeption(EmBusinessError.USE_LOGIN_FAIL);
        }

        return userModel;
    }

    @Override
    public UserModel getUserByIdInCache(Integer id) {
        UserModel userModel=(UserModel)redisTemplate.opsForValue().get("user_validate_"+id);
        if(userModel==null){
            userModel=this.getUserById(id);
            redisTemplate.opsForValue().set("user_validate_"+id,userModel);
            redisTemplate.expire("user_validate_"+id,10, TimeUnit.SECONDS);
        }


        return userModel;
    }

    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO)
    {
        if(userDO==null) {
            return null;
        }
        UserModel userModel=new UserModel();
        userModel.setAge(userDO.getAge());
        userModel.setId(userDO.getId());
        userModel.setGender(userDO.getGender());
        userModel.setName(userDO.getName());
        userModel.setRegisterMode(userDO.getRegister_mode());
        userModel.setTelphone(userDO.getTelphone());
        userModel.setThirdPartyId(userDO.getThird_party_id());
        if(userPasswordDO!=null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrpt_password());
        }
        return userModel;
    }
    private UserDO convertFromModel(UserModel userModel){
        if(userModel==null) {
            return null;
        }
        UserDO userDO=new UserDO();
        userDO.setThird_party_id(userModel.getThirdPartyId());
        userDO.setTelphone(userModel.getTelphone());
        userDO.setRegister_mode(userModel.getRegisterMode());
        userDO.setName(userModel.getName());
        userDO.setGender(userModel.getGender());
        userDO.setAge(userModel.getAge());
        return userDO;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel==null){
            return null;
        }
        UserPasswordDO userPasswordDO=new UserPasswordDO();
        userPasswordDO.setEncrpt_password(userModel.getEncrptPassword());
        userPasswordDO.setUser_id(userModel.getId());
        return userPasswordDO;

    }

}
