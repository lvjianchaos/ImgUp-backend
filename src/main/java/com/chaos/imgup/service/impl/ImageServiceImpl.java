package com.chaos.imgup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chaos.imgup.dto.PageDTO;
import com.chaos.imgup.entity.ImageInfo;
import com.chaos.imgup.entity.OssConfig;
import com.chaos.imgup.entity.User;
import com.chaos.imgup.mapper.ImageInfoMapper;
import com.chaos.imgup.mapper.OssConfigMapper;
import com.chaos.imgup.service.ImageService;
import com.chaos.imgup.service.OssConfigService;
import com.chaos.imgup.uploader.UploadResponse;
import com.chaos.imgup.uploader.Uploader;
import com.chaos.imgup.uploader.UploaderFactory;
import com.chaos.imgup.util.AuthUtil;
import com.chaos.imgup.vo.UploadResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
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
                    .eq("id", ossConfigId).eq("user_id", currentUser.getId()));
            if (configToUse == null) throw new RuntimeException("指定的配置不存在或无权使用");
        } else {
            configToUse = ossConfigMapper.selectOne(new QueryWrapper<OssConfig>()
                    .eq("user_id", currentUser.getId()).eq("is_default", true));
            if (configToUse == null) throw new RuntimeException("未找到默认图床配置，请先配置");
        }

        // 2. 获取解密的配置详情
        Map<String, String> decryptedDetails = ossConfigService.getConfigDetail(configToUse.getId());
        // 3. 使用工厂获取对应的Uploader
        Uploader uploader = uploaderFactory.getUploader(configToUse.getOssType());

        // 4. 执行上传
        UploadResponse uploadResponse;
        try {
            uploadResponse = uploader.upload(file, decryptedDetails);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        // 5. 将图片信息存入数据库 (使用新字段)
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setUserId(currentUser.getId());
        imageInfo.setOssConfigId(configToUse.getId());
        imageInfo.setOriginalName(file.getOriginalFilename());
        imageInfo.setImageUrl(uploadResponse.url()); // 保存完整URL
        imageInfo.setStorageName(uploadResponse.storageName()); // 保存Storage Name
        imageInfo.setImageSize(file.getSize());
        imageInfo.setImageType(file.getContentType());
        imageInfoMapper.insert(imageInfo);

        // 6. 返回结果
        return UploadResultVO.builder()
                .url(uploadResponse.url())
                .originalName(file.getOriginalFilename())
                .size(file.getSize())
                .build();
    }

    @Override
    @Transactional // 整个批量上传是一个事务
    public List<UploadResultVO> uploadImages(MultipartFile[] files, Long ossConfigId) {
        return Arrays.stream(files)
                .parallel() // 使用并行流，在文件较多时可以提升处理速度
                .map(file -> uploadImage(file, ossConfigId))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<ImageInfo> listImages(PageDTO pageDTO) {
        User currentUser = AuthUtil.getCurrentUser();

        Page<ImageInfo> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());

        QueryWrapper<ImageInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUser.getId());
        // 按创建时间倒序排列
        queryWrapper.orderByDesc("create_time");

        return imageInfoMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        User currentUser = AuthUtil.getCurrentUser();

        // 1. 查询图片信息，同时校验权限
        ImageInfo imageInfo = imageInfoMapper.selectOne(new QueryWrapper<ImageInfo>()
                .eq("id", id)
                .eq("user_id", currentUser.getId()));

        if (imageInfo == null) {
            throw new RuntimeException("图片不存在或无权删除");
        }

        // 2. 查询图片关联的OSS配置
        OssConfig ossConfig = ossConfigMapper.selectById(imageInfo.getOssConfigId());
        if (ossConfig == null) {
            // 即使配置被删了，也应该允许删除图片记录，但要记录一个警告
             log.warn("图片 {} 关联的OSS配置 {} 不存在，将仅删除数据库记录", id, imageInfo.getOssConfigId());
        } else {
            // 3. 调用Uploader删除云端文件
            try {
                Map<String, String> decryptedDetails = ossConfigService.getConfigDetail(ossConfig.getId());
                Uploader uploader = uploaderFactory.getUploader(ossConfig.getOssType());
                uploader.delete(imageInfo.getStorageName(), decryptedDetails);
            } catch (Exception e) {
                // TODO: 应记录详细日志
                e.printStackTrace();
                // 可以根据业务决定是否继续删除数据库记录，通常建议抛出异常回滚
                throw new RuntimeException("删除云端文件失败: " + e.getMessage());
            }
        }

        // 4. 删除数据库记录
        imageInfoMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteImages(List<Long> ids) {
        User currentUser = AuthUtil.getCurrentUser();

        // 1. 一次性查询出所有待删除的图片信息，并校验权限
        List<ImageInfo> imagesToDelete = imageInfoMapper.selectList(new QueryWrapper<ImageInfo>()
                .in("id", ids)
                .eq("user_id", currentUser.getId()));

        if (imagesToDelete.size() != ids.size()) {
            // 如果查询出的数量和传入ID数量不符，说明部分图片不存在或不属于该用户
            throw new RuntimeException("部分图片不存在或无权删除");
        }
        if (imagesToDelete.isEmpty()) {
            return; // 如果列表为空，直接返回
        }

        // 2. 按 ossConfigId 分组，减少 OSSClient 的创建次数
        Map<Long, List<ImageInfo>> groupedByConfig = imagesToDelete.stream()
                .collect(Collectors.groupingBy(ImageInfo::getOssConfigId));

        // 3. 遍历分组，执行删除
        groupedByConfig.forEach((configId, images) -> {
            try {
                OssConfig ossConfig = ossConfigMapper.selectById(configId);
                if (ossConfig == null) throw new RuntimeException("OSS配置不存在: " + configId);

                Map<String, String> decryptedDetails = ossConfigService.getConfigDetail(ossConfig.getId());
                Uploader uploader = uploaderFactory.getUploader(ossConfig.getOssType());

                // 对该分组下的所有图片执行删除
                for (ImageInfo image : images) {
                    uploader.delete(image.getStorageName(), decryptedDetails);
                }
            } catch (Exception e) {
                throw new RuntimeException("从云端删除文件时出错: " + e.getMessage(), e);
            }
        });

        // 4. 所有云端文件都删除成功后，一次性删除所有数据库记录
        imageInfoMapper.deleteByIds(ids);
    }
}
