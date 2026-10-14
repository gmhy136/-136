package com.cloudrecord.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Spring AI 原生 DeepSeek 接入实现。
 */
@Service
@RequiredArgsConstructor
public class DeepSeekClientImpl implements DeepSeekClient {

    private final ChatModel chatModel;

    @Override
    public Flux<String> stream(String system, String user, AiParams params) {
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(system), new UserMessage(user)),
                toOptions(params));
        return chatModel.stream(prompt).mapNotNull(this::extract);
    }

    @Override
    public String call(String system, String user, AiParams params) {
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(system), new UserMessage(user)),
                toOptions(params));
        return extract(chatModel.call(prompt));
    }

    private String extract(ChatResponse response) {
        if (response == null) {
            return null;
        }
        Generation generation = response.getResult();
        if (generation == null || generation.getOutput() == null) {
            return null;
        }
        return generation.getOutput().getText();
    }

    private DeepSeekChatOptions toOptions(AiParams params) {
        return DeepSeekChatOptions.builder()
                .temperature(params.temperature())
                .maxTokens(params.maxTokens())
                .build();
    }
}
