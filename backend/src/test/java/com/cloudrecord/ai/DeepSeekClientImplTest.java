package com.cloudrecord.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeepSeekClientImplTest {

    private ChatResponse resp(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    @Test
    void stream_mapsChatResponseToText() {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(resp("今"), resp("天")));
        DeepSeekClient client = new DeepSeekClientImpl(chatModel);

        List<String> deltas = client.stream("sys", "usr", AiParams.expand()).collectList().block();

        assertThat(deltas).containsExactly("今", "天");
    }

    @Test
    void call_returnsText() {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(resp("结果"));
        DeepSeekClient client = new DeepSeekClientImpl(chatModel);

        assertThat(client.call("sys", "usr", AiParams.style())).isEqualTo("结果");
    }
}
