package com.lcf.miaosha.controller;

import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
//所有controller基类
public class BaseController {


    public static final String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";

    //定义exceptionhandler解决未被controller层吸收的exception
    /*@ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Object handlerException(HttpServletRequest request, Exception ex){
        Map<String,Object> responseData=new HashMap<>();

        if(ex instanceof BusinessExeption){

            BusinessExeption businessExeption=(BusinessExeption)ex;
            responseData.put("errCode",businessExeption.getErrCode());
            responseData.put("errMsg",businessExeption.getErrMsg());


        }
        else
        {

            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());

        }
        return CommonReturnType.creat(responseData,"fail");
    }*/
}
