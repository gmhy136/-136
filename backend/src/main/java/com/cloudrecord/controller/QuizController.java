package com.cloudrecord.controller;

import com.cloudrecord.common.Result;
import com.cloudrecord.dto.CreateQuizRequest;
import com.cloudrecord.dto.SubmitAnswerRequest;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.service.QuizService;
import com.cloudrecord.vo.AnswerResultVO;
import com.cloudrecord.vo.FeedQuizVO;
import com.cloudrecord.vo.PlayQuizVO;
import com.cloudrecord.vo.QuizDetailVO;
import com.cloudrecord.vo.QuizVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 问答游戏接口。
 */
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public Result<QuizDetailVO> create(@Valid @RequestBody CreateQuizRequest req) {
        return Result.ok(quizService.create(UserContext.requireUserId(), req));
    }

    @GetMapping
    public Result<List<QuizVO>> list() {
        return Result.ok(quizService.list(UserContext.requireUserId()));
    }

    @GetMapping("/feed")
    public Result<List<FeedQuizVO>> feed() {
        return Result.ok(quizService.feed(UserContext.requireUserId()));
    }

    @GetMapping("/{id}")
    public Result<QuizDetailVO> get(@PathVariable Long id) {
        return Result.ok(quizService.getDetail(UserContext.requireUserId(), id));
    }

    @GetMapping("/{id}/play")
    public Result<PlayQuizVO> play(@PathVariable Long id) {
        return Result.ok(quizService.play(UserContext.requireUserId(), id));
    }

    @PutMapping("/{id}")
    public Result<QuizDetailVO> update(@PathVariable Long id, @Valid @RequestBody CreateQuizRequest req) {
        return Result.ok(quizService.update(UserContext.requireUserId(), id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        quizService.delete(UserContext.requireUserId(), id);
        return Result.ok();
    }

    @PostMapping("/{id}/publish")
    public Result<QuizDetailVO> publish(@PathVariable Long id) {
        return Result.ok(quizService.publish(UserContext.requireUserId(), id));
    }

    @PostMapping("/{id}/answers")
    public Result<AnswerResultVO> submit(@PathVariable Long id, @RequestBody SubmitAnswerRequest req) {
        return Result.ok(quizService.submit(UserContext.requireUserId(), id, req));
    }

    @GetMapping("/{id}/my-answer")
    public Result<AnswerResultVO> myAnswer(@PathVariable Long id) {
        return Result.ok(quizService.myAnswer(UserContext.requireUserId(), id));
    }
}
