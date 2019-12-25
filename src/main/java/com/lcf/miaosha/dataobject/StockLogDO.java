package com.lcf.miaosha.dataobject;

public class StockLogDO {

    private String stock_log_id;
    private Integer item_id;
    private Integer amount;
    private Integer status;
    public String getStock_log_id() {
        return stock_log_id;
    }

    public void setStock_log_id(String stock_log_id) {
        this.stock_log_id = stock_log_id;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
