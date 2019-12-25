package com.lcf.miaosha.controller;

import com.lcf.miaosha.error.BusinessExeption;
import com.lcf.miaosha.error.EmBusinessError;
import com.lcf.miaosha.response.CommonReturnType;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public CommonReturnType doError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,Exception ex){
            ex.printStackTrace();
        Map<String,Object> responseData=new HashMap<>();
        if(ex instanceof BusinessExeption){
            BusinessExeption businessExeption=(BusinessExeption)ex;
            responseData.put("errCode",businessExeption.getErrCode());
            responseData.put("errMsg",businessExeption.getErrMsg());

        }else if(ex instanceof ServletRequestBindingException){
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg","url绑定路由问题");
        }else if(ex instanceof NoHandlerFoundException){
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg","没有找到对应的访问路径");
        }else{
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.creat(responseData,"fail");
    }
}
