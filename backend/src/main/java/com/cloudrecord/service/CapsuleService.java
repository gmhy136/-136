package com.cloudrecord.service;

import com.cloudrecord.dto.CreateCapsuleRequest;
import com.cloudrecord.vo.CapsuleDetailVO;
import com.cloudrecord.vo.CapsuleVO;

import java.util.List;

/**
 * 时间胶囊服务：写信 / 列表 / 开启 / 删除（全部按 user_id 归属校验）。
 */
public interface CapsuleService {

    List<CapsuleVO> list(Long userId);

    CapsuleVO create(Long userId, CreateCapsuleRequest request);

    /** 未到期返回锁定态（不含 content）；到期置 is_opened=1 并返回全文。 */
    CapsuleDetailVO getDetail(Long userId, Long id);

    void delete(Long userId, Long id);
}
