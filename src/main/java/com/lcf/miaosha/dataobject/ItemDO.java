package com.lcf.miaosha.dataobject;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

public class ItemDO {
    @Id
    private  Integer id;
    private String title;
    private BigDecimal price;
    private String description;
    private Integer sales;
    private String imgUrl;

    private ItemStockDO itemStockDO;

    public ItemStockDO getItemStockDO() {
        return itemStockDO;
    }

    public void setItemStockDO(ItemStockDO itemStockDO) {
        this.itemStockDO = itemStockDO;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return "ItemDO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", sales=" + sales +
                ", imgUrl=" + imgUrl +
                '}';
    }
}
