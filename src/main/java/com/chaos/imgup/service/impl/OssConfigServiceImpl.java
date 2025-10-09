package com.chaos.imgup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.chaos.imgup.dto.OssConfigDTO;
import com.chaos.imgup.entity.OssConfig;
import com.chaos.imgup.entity.User;
import com.chaos.imgup.mapper.OssConfigMapper;
import com.chaos.imgup.service.OssConfigService;
import com.chaos.imgup.util.AuthUtil;
import com.chaos.imgup.vo.OssConfigVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OssConfigServiceImpl implements OssConfigService {

    @Autowired
    private OssConfigMapper ossConfigMapper;

    @Autowired
    @Qualifier("jasyptStringEncryptor") // 指定使用我们自定义的Bean
    private StringEncryptor stringEncryptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void addConfig(OssConfigDTO ossConfigDTO) {
        User currentUser = AuthUtil.getCurrentUser();

        // 如果要新增的配置是默认，则先把其他配置置为非默认
        if (Boolean.TRUE.equals(ossConfigDTO.getIsDefault())) {
            unsetOldDefault(currentUser.getId());
        }

        OssConfig ossConfig = new OssConfig();
        BeanUtils.copyProperties(ossConfigDTO, ossConfig);
        ossConfig.setUserId(currentUser.getId());

        try {
            // 加密配置详情
            String jsonConfigDetail = objectMapper.writeValueAsString(ossConfigDTO.getConfigDetail());
            ossConfig.setConfigDetail(stringEncryptor.encrypt(jsonConfigDetail));
        } catch (Exception e) {
            throw new RuntimeException("配置信息序列化或加密失败", e);
        }

        ossConfigMapper.insert(ossConfig);
    }

    @Override
    public List<OssConfigVO> listConfigs() {
        User currentUser = AuthUtil.getCurrentUser();
        List<OssConfig> configs = ossConfigMapper.selectList(
                new QueryWrapper<OssConfig>().eq("user_id", currentUser.getId())
        );
        return configs.stream().map(config -> {
            OssConfigVO vo = new OssConfigVO();
            BeanUtils.copyProperties(config, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> getConfigDetail(Long id) {
        User currentUser = AuthUtil.getCurrentUser();
        OssConfig config = getConfigForUser(id, currentUser.getId());

        try {
            String decryptedDetails = stringEncryptor.decrypt(config.getConfigDetail());
            return objectMapper.readValue(decryptedDetails, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("配置信息解密或反序列化失败", e);
        }
    }

    @Override
    @Transactional
    public void updateConfig(Long id, OssConfigDTO ossConfigDTO) {
        User currentUser = AuthUtil.getCurrentUser();
        OssConfig existingConfig = getConfigForUser(id, currentUser.getId());

        if (Boolean.TRUE.equals(ossConfigDTO.getIsDefault())) {
            unsetOldDefault(currentUser.getId());
        }

        BeanUtils.copyProperties(ossConfigDTO, existingConfig);
        try {
            String jsonConfigDetail = objectMapper.writeValueAsString(ossConfigDTO.getConfigDetail());
            existingConfig.setConfigDetail(stringEncryptor.encrypt(jsonConfigDetail));
        } catch (Exception e) {
            throw new RuntimeException("配置信息序列化或加密失败", e);
        }
        ossConfigMapper.updateById(existingConfig);
    }

    @Override
    public void deleteConfig(Long id) {
        User currentUser = AuthUtil.getCurrentUser();
        OssConfig config = getConfigForUser(id, currentUser.getId());
        ossConfigMapper.deleteById(config.getId());
    }

    @Override
    @Transactional
    public void setDefaultConfig(Long id) {
        User currentUser = AuthUtil.getCurrentUser();
        getConfigForUser(id, currentUser.getId()); // 验证权限
        unsetOldDefault(currentUser.getId());

        OssConfig newDefault = new OssConfig();
        newDefault.setId(id);
        newDefault.setIsDefault(true);
        ossConfigMapper.updateById(newDefault);
    }

    // 辅助方法：将用户所有旧的默认配置取消
    private void unsetOldDefault(Long userId) {
        ossConfigMapper.update(
                null,
                new UpdateWrapper<OssConfig>()
                        .eq("user_id", userId)
                        .eq("is_default", true)
                        .set("is_default", false)
        );
    }

    // 辅助方法：获取属于指定用户的配置，并进行权限校验
    private OssConfig getConfigForUser(Long configId, Long userId) {
        OssConfig config = ossConfigMapper.selectOne(
                new QueryWrapper<OssConfig>().eq("id", configId).eq("user_id", userId)
        );
        if (config == null) {
            throw new RuntimeException("配置不存在或无权访问");
        }
        return config;
    }
}
