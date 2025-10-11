package com.chaos.imgup.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chaos.imgup.dto.PageDTO;
import com.chaos.imgup.entity.ImageInfo;
import com.chaos.imgup.vo.UploadResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    UploadResultVO uploadImage(MultipartFile file, Long ossConfigId);

    IPage<ImageInfo> listImages(PageDTO pageDTO);

    void deleteImage(Long id);
}

