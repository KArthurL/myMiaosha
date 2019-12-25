package com.lcf.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.lcf.miaosha.controller.viewobject.UserVO;
import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.CommonError;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.response.CommonReturnType;
import com.lcf.miaosha.service.UserService;
import com.lcf.miaosha.service.model.UserModel;
import org.apache.ibatis.annotations.Result;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")//跨域
public class UserController extends BaseController{

    @Autowired
    UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public CommonReturnType getUser(Integer id) throws BusinessExeption {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel=userService.getUserById(id);
        if(userModel==null)
        {
            //用户不存在
           throw new BusinessExeption(EmBusinessError.USE_NOT_EXIST);
        }

        UserVO userVO= convertFromModel(userModel);
        //返回通用对象
        return CommonReturnType.creat(userVO);
    }


    //用户获取otp短信接口
    @RequestMapping(value = "/getotp",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType getOtp(String telphone){
        //需要按照一定的规则生成OTP验证码
        Random random=new Random();
        int randomInt=random.nextInt(99999);
        randomInt+=10000;
        String otpCode=String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机关联,使用httpsession的方式绑定
        httpServletRequest.getSession().setAttribute(telphone,otpCode);


        //将OTP验证码通过短信通道发送给用户
        System.out.println("telphone="+telphone+"&otpcode="+otpCode);
        return CommonReturnType.creat(null);
    }

    //用户注册接口
    @RequestMapping(value = "/register",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType register(String telphone,String optCode,String name,String gender,Integer age,String password) throws BusinessExeption, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的otpcode相符合
          String inSessionOtpCode=(String)this.httpServletRequest.getSession().getAttribute(telphone);
            if(!com.alibaba.druid.util.StringUtils.equals(optCode,inSessionOtpCode)){
                throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码错误");
            }
        //用户的注册流程
            UserModel userModel=new UserModel();
            userModel.setName(name);
            userModel.setGender(gender);
            userModel.setTelphone(telphone);
            userModel.setAge(age);
            userModel.setRegisterMode("byphone");
            userModel.setEncrptPassword(EncodeByMD5(password));
            userService.register(userModel);
            return CommonReturnType.creat(null);
    }

    //用户登陆接口
    @RequestMapping(value = "/login",method = RequestMethod.POST,consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType login(String telphone,String password) throws BusinessExeption, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(StringUtils.isEmpty(telphone)||StringUtils.isEmpty(password))
        {
            throw new BusinessExeption(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登陆服务
       UserModel userModel= userService.validateLogin(telphone,EncodeByMD5(password));

        //将登陆凭证加入到用户成功登陆的session内

        //修改成若用户登录验证成功后将对应的登录信息和登录凭证一起存入redis中
        //生成登录凭证token，UUID
        String uuidToken=UUID.randomUUID().toString();

        //建立token和用户登录态之间的联系
        redisTemplate.opsForValue().set(uuidToken,userModel);
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);
        /*httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);*/
        return CommonReturnType.creat(uuidToken);

    }

    private String EncodeByMD5(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定一个计算方法
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en=new BASE64Encoder();
        String newstr=base64en.encode(md5.digest(s.getBytes("utf-8")));
        return newstr;
    }

    //将核心领域模型用户对西那个转换为可供UI使用的viewobject
    private UserVO convertFromModel(UserModel userModel)
    {
        if(userModel==null){
            return null;
        }
        UserVO userVO=new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;

    }




}
