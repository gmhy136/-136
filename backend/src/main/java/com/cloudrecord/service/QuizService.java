package com.cloudrecord.service;

import com.cloudrecord.dto.CreateQuizRequest;
import com.cloudrecord.dto.SubmitAnswerRequest;
import com.cloudrecord.vo.AnswerResultVO;
import com.cloudrecord.vo.FeedQuizVO;
import com.cloudrecord.vo.PlayQuizVO;
import com.cloudrecord.vo.QuizDetailVO;
import com.cloudrecord.vo.QuizVO;

import java.util.List;

/**
 * 问答游戏服务。
 */
public interface QuizService {

    QuizDetailVO create(Long userId, CreateQuizRequest req);

    List<QuizVO> list(Long userId);

    /** 朋友圈：好友 + 我的已发布题目。 */
    List<FeedQuizVO> feed(Long userId);

    /** 答题视角（不含答案），仅好友 / 出题人可看。 */
    PlayQuizVO play(Long userId, Long quizId);

    QuizDetailVO getDetail(Long userId, Long quizId);

    QuizDetailVO update(Long userId, Long quizId, CreateQuizRequest req);

    void delete(Long userId, Long quizId);

    QuizDetailVO publish(Long userId, Long quizId);

    AnswerResultVO submit(Long userId, Long quizId, SubmitAnswerRequest req);

    AnswerResultVO myAnswer(Long userId, Long quizId);
}
