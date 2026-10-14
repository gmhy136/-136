package com.cloudrecord.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudrecord.entity.RecordCard;
import com.cloudrecord.vo.MoodDayRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordCardMapper extends BaseMapper<RecordCard> {

    /** 记录天数：去重日期计数（不含逻辑删除）。 */
    @Select("SELECT COUNT(DISTINCT DATE(created_at)) FROM record_card WHERE user_id = #{userId} AND deleted = 0")
    int countDistinctDays(@Param("userId") Long userId);

    /** 某月每天的最后一条心情（有心情的记录才计入），返回「第几天 → 心情ID」。 */
    @Select("""
            SELECT DAY(created_at) AS day, mood_id AS moodId
            FROM (
              SELECT created_at, mood_id,
                     ROW_NUMBER() OVER (PARTITION BY DATE(created_at) ORDER BY created_at DESC, id DESC) AS rn
              FROM record_card
              WHERE user_id = #{userId} AND deleted = 0 AND mood_id IS NOT NULL
                AND created_at >= #{start} AND created_at < #{end}
            ) t
            WHERE rn = 1
            ORDER BY day
            """)
    List<MoodDayRow> selectMoodDayRows(@Param("userId") Long userId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    /** 最近若干条记录的心情ID（供今日一句参考用户心情，去重由调用方处理）。 */
    @Select("SELECT mood_id FROM record_card WHERE user_id = #{userId} AND deleted = 0 AND mood_id IS NOT NULL ORDER BY created_at DESC LIMIT #{limit}")
    List<Integer> selectRecentMoodIds(@Param("userId") Long userId, @Param("limit") int limit);
}
