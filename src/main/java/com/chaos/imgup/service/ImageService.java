package com.chaos.imgup.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chaos.imgup.dto.PageDTO;
import com.chaos.imgup.entity.ImageInfo;
import com.chaos.imgup.vo.UploadResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    UploadResultVO uploadImage(MultipartFile file, Long ossConfigId);

    // 批量上传接口
    List<UploadResultVO> uploadImages(MultipartFile[] files, Long ossConfigId);

    IPage<ImageInfo> listImages(PageDTO pageDTO);

    void deleteImage(Long id);

    // 批量删除接口
    void deleteImages(List<Long> ids);
}

