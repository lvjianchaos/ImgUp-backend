package com.chaos.imgup.service;


import com.chaos.imgup.dto.OssConfigDTO;
import com.chaos.imgup.vo.OssConfigVO;

import java.util.List;
import java.util.Map;

public interface OssConfigService {
    void addConfig(OssConfigDTO ossConfigDTO);
    List<OssConfigVO> listConfigs();
    List<OssConfigVO> listConfigsByType(String type);
    Map<String, String> getConfigDetail(Long id); // 获取解密后的配置详情
    void updateConfig(Long id, OssConfigDTO ossConfigDTO);
    Boolean deleteConfig(Long id);
    void setDefaultConfig(Long id);
}