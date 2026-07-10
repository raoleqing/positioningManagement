package com.zns.positioning.positioningmanagement.entity.operator;

import lombok.Data;

@Data
public class CardPackage {
//    {"price":0.80,"cycleCategory":1,"packageId":2974,"packageName":"停机保号套餐 （1个月）","cycle":30,"flow":0}
    private Double price;
    private Integer cycleCategory;
    private Integer packageId;
    private String packageName;
    private Integer cycle;
    private Integer flow;
}
