package com.cloudrecord.service;

import com.cloudrecord.dto.CreateRecordRequest;
import com.cloudrecord.dto.UpdateRecordRequest;
import com.cloudrecord.vo.PageVO;
import com.cloudrecord.vo.RecordVO;

public interface RecordService {

    PageVO<RecordVO> list(Long userId, long page, long size);

    RecordVO get(Long userId, Long id);

    RecordVO create(Long userId, CreateRecordRequest request);

    RecordVO update(Long userId, Long id, UpdateRecordRequest request);

    void delete(Long userId, Long id);
}
