package com.example.ensurify.service;

import com.example.ensurify.common.apiPayload.exception.GeneralException;
import com.example.ensurify.common.jwt.TokenProvider;
import com.example.ensurify.domain.User;
import com.example.ensurify.dto.request.LoginRequest;
import com.example.ensurify.dto.response.GetUserInfoResponse;
import com.example.ensurify.dto.response.LoginResponse;
import com.example.ensurify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;

import static com.example.ensurify.common.apiPayload.code.status.ErrorStatus.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(30);

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new GeneralException(USER_NOT_FOUND));

        log.info("로그인: userId={}", user.getId());

        if (!Objects.equals(request.getPassword(), user.getPassword())) {
            throw new GeneralException(INVALID_PASSWORD);
        }

        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        log.info("access_token: " + accessToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    /**
     * 행원 정보 조회
     */
    public GetUserInfoResponse getUserInfo(Long userId) {

        User user = findById(userId);

        return GetUserInfoResponse.builder()
                .name(user.getName())
                .imageUrl(user.getImageUrl())
                .build();
    }

    // id로 행원 검색
    public User findById(Long memberId) {
        return userRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(USER_NOT_FOUND));
    }
}
