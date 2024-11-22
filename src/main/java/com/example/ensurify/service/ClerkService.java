package com.example.ensurify.service;

import com.example.ensurify.common.apiPayload.exception.GeneralException;
import com.example.ensurify.common.jwt.TokenProvider;
import com.example.ensurify.domain.Clerk;
import com.example.ensurify.dto.request.LoginRequest;
import com.example.ensurify.dto.response.GetClerkInfoResponse;
import com.example.ensurify.dto.response.LoginResponse;
import com.example.ensurify.repository.ClerkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;

import static com.example.ensurify.common.apiPayload.code.status.ErrorStatus.CLERK_NOT_FOUND;
import static com.example.ensurify.common.apiPayload.code.status.ErrorStatus.INVALID_PASSWORD;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClerkService {

    private final ClerkRepository clerkRepository;
    private final TokenProvider tokenProvider;

    private static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(30);

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {

        Clerk clerk = clerkRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new GeneralException(CLERK_NOT_FOUND));

        log.info("로그인: clerkId={}", clerk.getId());

        if (!Objects.equals(request.getPassword(), clerk.getPassword())) {
            throw new GeneralException(INVALID_PASSWORD);
        }

        String accessToken = tokenProvider.generateToken(clerk, ACCESS_TOKEN_DURATION);
        log.info("access_token: " + accessToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    /**
     * 행원 정보 조회
     */
    public GetClerkInfoResponse getClerkInfo(Long clerkId) {

        Clerk clerk = findById(clerkId);

        return GetClerkInfoResponse.builder()
                .name(clerk.getName())
                .imageUrl(clerk.getImageUrl())
                .build();
    }

    // id로 행원 검색
    public Clerk findById(Long memberId) {
        return clerkRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(CLERK_NOT_FOUND));
    }
}
