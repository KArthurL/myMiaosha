package com.lcf.miaosha.error;

public enum EmBusinessError implements CommonError {
    //通用错误类型10001
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),
    NONE_ITEM(10003,"商品不存在"),

    //20000开头为用户信息相关错误定义
    USE_NOT_EXIST(20001,"用户不存在"),
    USE_LOGIN_FAIL(20002,"用户手机号或密码不正确"),
    USE_NOT_LOGIN(20003,"用户还未登录"),
    //30000开头为交易信息错误定义
    STOCK_NOT_ENOUGH(30001,"库存不足"),
    RATELIMIT(30002,"活动太火爆，请稍后再试")
    ;


    private  EmBusinessError(int errCode,String errMsg){
        this.errCode=errCode;
        this.errMsg=errMsg;
    }

    private int errCode;
    private String errMsg;
    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg=errMsg;
        return this;
    }
}
