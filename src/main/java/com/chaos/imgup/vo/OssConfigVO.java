package com.chaos.imgup.vo;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OssConfigVO {
    private Long id;
    private String configName;
    private String ossType;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}