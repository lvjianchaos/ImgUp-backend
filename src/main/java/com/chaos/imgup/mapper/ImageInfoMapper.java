package com.chaos.imgup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chaos.imgup.entity.ImageInfo;
import com.chaos.imgup.vo.ImageVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ImageInfoMapper extends BaseMapper<ImageInfo> {
    /**
     * 查询特定用户下的图片信息，并联接查询其所属的图床配置名称和类型。
     *
     * @param userId 用户ID
     * @return List<ImageVO> 包含图片和图床配置信息的列表。
     */
    @Select("SELECT " +
            "ii.id, " +
            "ii.original_name, " +
            "ii.image_url, " +
            "ii.image_size, " +
            "ii.oss_config_id, " +
            "oc.config_name AS ossConfigName, " +
            "oc.oss_type AS ossConfigType, " +
            "ii.create_time, " +
            "ii.update_time " +
            "FROM " +
            "imgup_db.image_info ii " +
            "LEFT JOIN " +
            "imgup_db.oss_config oc ON ii.oss_config_id = oc.id " +
            "WHERE ii.user_id = #{userId}")
    List<ImageVO> selectAllImageInfoWithOssConfig(@Param("userId") Long userId);

    /**
     * 根据图片ID重命名图片。
     *
     * @param id      图片的ID。
     * @param newName 新的原始文件名。
     */
    @Update("UPDATE image_info SET original_name = #{newName} WHERE id = #{id}")
    void renameById(@Param("id") Long id, @Param("newName") String newName); // 使用 @Param 注解
}
