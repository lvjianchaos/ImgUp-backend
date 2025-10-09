package com.chaos.imgup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chaos.imgup.entity.ImageInfo;
import com.chaos.imgup.entity.OssConfig;
import com.chaos.imgup.entity.User;
import com.chaos.imgup.mapper.ImageInfoMapper;
import com.chaos.imgup.mapper.OssConfigMapper;
import com.chaos.imgup.service.ImageService;
import com.chaos.imgup.service.OssConfigService;
import com.chaos.imgup.uploader.Uploader;
import com.chaos.imgup.uploader.UploaderFactory;
import com.chaos.imgup.util.AuthUtil;
import com.chaos.imgup.vo.UploadResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private OssConfigMapper ossConfigMapper;

    @Autowired
    private OssConfigService ossConfigService;

    @Autowired
    private ImageInfoMapper imageInfoMapper;

    @Autowired
    private UploaderFactory uploaderFactory;

    @Override
    @Transactional
    public UploadResultVO uploadImage(MultipartFile file, Long ossConfigId) {
        User currentUser = AuthUtil.getCurrentUser();

        // 1. 确定使用哪个OSS配置
        OssConfig configToUse;
        if (ossConfigId != null) {
            configToUse = ossConfigMapper.selectOne(new QueryWrapper<OssConfig>()
                    .eq("id", ossConfigId)
                    .eq("user_id", currentUser.getId()));
            if (configToUse == null) {
                throw new RuntimeException("指定的配置不存在或无权使用");
            }
        } else {
            configToUse = ossConfigMapper.selectOne(new QueryWrapper<OssConfig>()
                    .eq("user_id", currentUser.getId())
                    .eq("is_default", true));
            if (configToUse == null) {
                throw new RuntimeException("未找到默认图床配置，请先配置");
            }
        }

        // 2. 获取解密的配置详情
        Map<String, String> decryptedDetails = ossConfigService.getConfigDetail(configToUse.getId());

        // 3. 使用工厂获取对应的Uploader
        Uploader uploader = uploaderFactory.getUploader(configToUse.getOssType());

        // 4. 执行上传
        String fileUrl;
        try {
            fileUrl = uploader.upload(file, decryptedDetails);
        } catch (Exception e) {
            // 实际项目中应记录详细日志
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        // 5. 将图片信息存入数据库
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setUserId(currentUser.getId());
        imageInfo.setOssConfigId(configToUse.getId());
        imageInfo.setOriginalName(file.getOriginalFilename());
        imageInfo.setImageUrl(fileUrl);
        imageInfo.setImageSize(file.getSize());
        imageInfo.setImageType(file.getContentType());
        imageInfoMapper.insert(imageInfo);

        // 6. 返回结果
        return UploadResultVO.builder()
                .url(fileUrl)
                .originalName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }
}