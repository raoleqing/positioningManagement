package com.zns.positioning.positioningmanagement.entity.operator;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardPackage {

//     "sellingPrice" -> {BigDecimal@10007} "7.00"
//            "price" -> {BigDecimal@10009} "7.00"
//            "cycleCategory" -> {Integer@10011} 1
//            "packageId" -> {Integer@10013} 14958
//            "packageName" -> "500M年"
//            "cycle" -> {Integer@10017} 360
//            "flow" -> {Integer@10019} 500


    //    {"price":0.80,"cycleCategory":1,"packageId":2974,"packageName":"停机保号套餐 （1个月）","cycle":30,"flow":0}
    private BigDecimal sellingPrice;
    private BigDecimal price;
    private Integer cycleCategory;
    private Integer packageId;
    private String packageName;
    private Integer cycle;
    private Integer flow;
}
