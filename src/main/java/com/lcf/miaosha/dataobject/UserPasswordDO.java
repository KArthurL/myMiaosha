package com.lcf.miaosha.dataobject;

public class UserPasswordDO {

    /**
     * 数据库id
     */
    private Integer id;
    /**
     * 用户id
     */
    private Integer user_id;
    /**
     * 用户密码
     */
    private String encrpt_password;

    public String getEncrpt_password() {
        return encrpt_password;
    }

    public void setEncrpt_password(String encrpt_password) {
        this.encrpt_password = encrpt_password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }
}
