package com.cloudrecord.service;

import com.cloudrecord.dto.UpdateProfileRequest;
import com.cloudrecord.vo.UserVO;

public interface UserService {

    UserVO me(Long userId);

    UserVO updateProfile(Long userId, UpdateProfileRequest request);
}
