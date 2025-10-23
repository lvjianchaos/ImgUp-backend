package com.chaos.imgup.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ImageVO {
    private Long id;
    private String originalName;
    private String imageUrl;
    private Long imageSize;
    private String ossConfigName;
    private String ossConfigType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
