package com.chaos.imgup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("image_info")
public class ImageInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long ossConfigId;
    private String originalName;
    private String storageName;
    private String imageUrl;
    private Long imageSize;
    private String imageType;
    private LocalDateTime createTime;
}
