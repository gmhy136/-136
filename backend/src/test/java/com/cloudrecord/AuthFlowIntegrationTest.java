package com.cloudrecord;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudrecord.dto.LoginRequest;
import com.cloudrecord.dto.RegisterRequest;
import com.cloudrecord.dto.SendCodeRequest;
import com.cloudrecord.dto.UpdateProfileRequest;
import com.cloudrecord.entity.EmailCode;
import com.cloudrecord.entity.User;
import com.cloudrecord.mapper.EmailCodeMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户系统全流程集成测试（连接本机 cloudrecord_test 库）：
 * 发验证码 → 注册 → 登录 → 刷新 → GET/PUT /me → 未登录 401。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailCodeMapper emailCodeMapper;

    private String email;

    @BeforeEach
    void setUp() {
        email = "it_" + System.currentTimeMillis() + "@test.com";
    }

    @AfterEach
    void tearDown() {
        userMapper.delete(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        emailCodeMapper.delete(new LambdaQueryWrapper<EmailCode>().eq(EmailCode::getEmail, email));
    }

    @Test
    void fullFlow() throws Exception {
        // 未登录访问受保护接口 → 401
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());

        // 发验证码
        SendCodeRequest send = new SendCodeRequest();
        send.setEmail(email);
        mockMvc.perform(post("/api/v1/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(send)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 从库中读出刚生成的验证码
        String code = emailCodeMapper.selectList(new LambdaQueryWrapper<EmailCode>()
                        .eq(EmailCode::getEmail, email)
                        .orderByDesc(EmailCode::getCreatedAt))
                .get(0).getCode();

        // 注册
        RegisterRequest reg = new RegisterRequest();
        reg.setEmail(email);
        reg.setCode(code);
        reg.setNickname("集成测试");
        reg.setPassword("secret1");
        String regBody = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        AuthResponse registered = parseAuth(regBody);

        // 重复注册 → 400
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(jsonPath("$.code").value(400));

        // 登录
        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("secret1");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // 刷新
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + registered.getRefreshToken() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // 携带 access_token 访问 /me
        mockMvc.perform(get("/api/v1/user/me")
                        .header("Authorization", "Bearer " + registered.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.nickname").value("集成测试"));

        // 更新资料
        UpdateProfileRequest upd = new UpdateProfileRequest();
        upd.setBio("你好，这是简介");
        mockMvc.perform(put("/api/v1/user/me")
                        .header("Authorization", "Bearer " + registered.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(upd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.bio").value("你好，这是简介"));
    }

    private AuthResponse parseAuth(String body) throws Exception {
        return objectMapper.readValue(objectMapper.readTree(body).get("data").toString(), AuthResponse.class);
    }
}
