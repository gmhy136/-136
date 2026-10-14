package com.cloudrecord.controller;

import com.cloudrecord.ai.AiService;
import com.cloudrecord.ai.ExpandResult;
import com.cloudrecord.ai.SplitIdealResult;
import com.cloudrecord.common.Result;
import com.cloudrecord.dto.ExpandRequest;
import com.cloudrecord.dto.SplitIdealRequest;
import com.cloudrecord.dto.StyleRequest;
import com.cloudrecord.security.UserContext;
import com.cloudrecord.vo.AiRemainingVO;
import com.cloudrecord.vo.StyleRecommendationVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * AI 能力接口：扩写走 SSE，风格推荐走 JSON。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/expand", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter expand(@Valid @RequestBody ExpandRequest request) {
        Long userId = UserContext.requireUserId();
        ExpandResult result = aiService.expand(userId, request.getContent(), request.getMoodId());

        SseEmitter emitter = new SseEmitter(0L);
        send(emitter, Map.of("type", "start", "remaining", result.remaining()));
        result.deltas()
                .doOnNext(delta -> send(emitter, Map.of("type", "delta", "content", delta)))
                .doOnComplete(() -> {
                    send(emitter, Map.of("type", "done"));
                    emitter.complete();
                })
                .doOnError(e -> {
                    log.error("AI 扩写流异常", e);
                    send(emitter, Map.of("type", "error", "message", "生成中断，可重试"));
                    emitter.complete();
                })
                .subscribe();
        return emitter;
    }

    @PostMapping("/style")
    public Result<StyleRecommendationVO> style(@Valid @RequestBody StyleRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.ok(aiService.recommendStyle(userId, request.getContent(), request.getMoodId()));
    }

    @PostMapping("/split-ideal")
    public Result<SplitIdealResult> splitIdeal(@Valid @RequestBody SplitIdealRequest request) {
        Long userId = UserContext.requireUserId();
        return Result.ok(aiService.splitIdeal(userId, request.getTitle(), request.getDescription()));
    }

    @GetMapping("/remaining")
    public Result<AiRemainingVO> remaining() {
        return Result.ok(aiService.remaining(UserContext.requireUserId()));
    }

    private void send(SseEmitter emitter, Map<String, ?> payload) {
        try {
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(payload)));
        } catch (Exception ignored) {
            // 客户端已断开等情况，忽略
        }
    }
}
