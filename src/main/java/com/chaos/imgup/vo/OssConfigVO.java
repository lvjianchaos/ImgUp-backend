package com.chaos.imgup.vo;


import lombok.Data;

@Data
public class OssConfigVO {
    private Long id;
    private String configName;
    private String ossType;
    private Boolean isDefault;
}