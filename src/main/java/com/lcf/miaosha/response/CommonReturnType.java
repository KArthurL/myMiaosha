package com.lcf.miaosha.response;

import org.omg.CORBA.OBJ_ADAPTER;

public class CommonReturnType {
    //表明返回处理结果，success或者fail
    private String status;
    //若status=success,则data内返回json数据
    //若status=fail，则data内使用通用的错误码格式
    private Object data;


    public static CommonReturnType creat(Object result){
        return CommonReturnType.creat(result,"success");
    }
    public static CommonReturnType creat(Object result,String status){
        CommonReturnType commonReturnType=new CommonReturnType();
        commonReturnType.setData(result);
        commonReturnType.setStatus(status);
        return commonReturnType;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
