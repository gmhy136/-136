package com.cloudrecord.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudrecord.entity.RecordCard;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RecordCardMapper extends BaseMapper<RecordCard> {

    /** 记录天数：去重日期计数（不含逻辑删除）。 */
    @Select("SELECT COUNT(DISTINCT DATE(created_at)) FROM record_card WHERE user_id = #{userId} AND deleted = 0")
    int countDistinctDays(@Param("userId") Long userId);
}
