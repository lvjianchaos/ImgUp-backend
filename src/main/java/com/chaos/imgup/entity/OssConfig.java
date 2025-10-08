package com.chaos.imgup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("oss_config")
public class OssConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String configName;
    private String ossType;
    private String configDetail;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
