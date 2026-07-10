package com.zns.positioning.positioningmanagement.entity.operator;

import lombok.Data;

import java.util.List;

@Data
public class CardPackageResp {
    private List<CardPackage> basicsePackage;
    private List<CardPackage> speedPackage;
    private List<CardPackage> protectPackage;
}
