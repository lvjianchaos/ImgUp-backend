package com.chaos.imgup.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chaos.imgup.common.Result;
import com.chaos.imgup.dto.PageDTO;
import com.chaos.imgup.entity.ImageInfo;
import com.chaos.imgup.service.ImageService;
import com.chaos.imgup.vo.ImageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @GetMapping
    public Result<IPage<ImageVO>> list(PageDTO pageDTO) {
        IPage<ImageInfo> page = imageService.listImages(pageDTO);

        // 将 IPage<ImageInfo> 转换为 IPage<ImageVO>
        IPage<ImageVO> pageVO = page.convert(imageInfo -> {
            ImageVO vo = new ImageVO();
            BeanUtils.copyProperties(imageInfo, vo);
            // 确保 imageUrl 字段也被复制
            vo.setImageUrl(imageInfo.getImageUrl());
            return vo;
        });

        return Result.success(pageVO);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        imageService.deleteImage(id);
        return Result.success("删除成功");
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要删除的图片");
        }
        imageService.deleteImages(ids);
        return Result.success("批量删除成功");
    }
}
