package com.cloudrecord.dto;

import lombok.Data;

import java.util.List;

/**
 * 提交答题请求。
 */
@Data
public class SubmitAnswerRequest {

    private Boolean anonymous;

    private List<AnswerInput> answers;
}
