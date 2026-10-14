package com.cloudrecord;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.dto.CreateRecordRequest;
import com.cloudrecord.dto.RegisterRequest;
import com.cloudrecord.dto.SendCodeRequest;
import com.cloudrecord.dto.UpdateRecordRequest;
import com.cloudrecord.entity.EmailCode;
import com.cloudrecord.entity.Mood;
import com.cloudrecord.mapper.EmailCodeMapper;
import com.cloudrecord.mapper.MoodMapper;
import com.cloudrecord.mapper.UserMapper;
import com.cloudrecord.vo.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 记录卡全流程集成测试（连接本机 cloudrecord_test 库）：
 * 注册 → 上传图片 → 创建 → 列表 → 详情 → 更新 → 删除 → 删除后 404。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecordFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailCodeMapper emailCodeMapper;

    @Autowired
    private MoodMapper moodMapper;

    private String email;
    private Long userId;

    @BeforeEach
    void setUp() {
        email = "rec_" + System.currentTimeMillis() + "@test.com";
    }

    @AfterEach
    void tearDown() {
        if (userId != null) {
            userMapper.deleteById(userId);
        }
        emailCodeMapper.delete(new LambdaQueryWrapper<EmailCode>().eq(EmailCode::getEmail, email));
    }

    @Test
    void fullFlow() throws Exception {
        String token = registerAndLogin();

        // 上传图片
        MockMultipartFile file = new MockMultipartFile("file", "pic.png", "image/png", "hello".getBytes());
        String uploadBody = mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.url").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String imageUrl = objectMapper.readTree(uploadBody).get("data").get("url").asText();

        Integer moodId = moodMapper.selectList(null).get(0).getId();

        // 创建记录卡
        CreateRecordRequest create = new CreateRecordRequest();
        create.setContent("今天看到了很美的晚霞");
        create.setMoodId(moodId);
        create.setStyleKey("warm-sunny");
        create.setAiType("原创");
        create.setImageUrls(List.of(imageUrl));
        String createBody = mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.moodName").isNotEmpty())
                .andExpect(jsonPath("$.data.imageUrls[0]").value(imageUrl))
                .andReturn().getResponse().getContentAsString();
        Long recordId = objectMapper.readTree(createBody).get("data").get("id").asLong();

        // 列表
        mockMvc.perform(get("/api/v1/records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1));

        // 详情
        mockMvc.perform(get("/api/v1/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content").value("今天看到了很美的晚霞"));

        // 更新（清空图片）
        UpdateRecordRequest update = new UpdateRecordRequest();
        update.setContent("改：晚霞好美");
        update.setMoodId(moodId);
        update.setImageUrls(List.of());
        mockMvc.perform(put("/api/v1/records/" + recordId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.content").value("改：晚霞好美"));

        // 删除
        mockMvc.perform(delete("/api/v1/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 删除后再查 → 404
        mockMvc.perform(get("/api/v1/records/" + recordId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.code").value(404));
    }

    private String registerAndLogin() throws Exception {
        SendCodeRequest send = new SendCodeRequest();
        send.setEmail(email);
        mockMvc.perform(post("/api/v1/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(send)))
                .andExpect(jsonPath("$.code").value(0));

        String code = emailCodeMapper.selectList(new LambdaQueryWrapper<EmailCode>()
                        .eq(EmailCode::getEmail, email)
                        .orderByDesc(EmailCode::getCreatedAt))
                .get(0).getCode();

        RegisterRequest reg = new RegisterRequest();
        reg.setEmail(email);
        reg.setCode(code);
        reg.setNickname("记录测试");
        reg.setPassword("secret1");
        String regBody = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();

        AuthResponse auth = objectMapper.readValue(
                objectMapper.readTree(regBody).get("data").toString(), AuthResponse.class);
        userId = auth.getUser().getId();
        return auth.getAccessToken();
    }
}
