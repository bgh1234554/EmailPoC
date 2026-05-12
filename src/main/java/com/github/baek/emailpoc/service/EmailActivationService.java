package com.github.baek.emailpoc.service;

import com.github.baek.emailpoc.domain.EmailActivation;
import com.github.baek.emailpoc.repository.EmailActivationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailActivationService {
    private final EmailActivationRepository activationRepository;
    private final SmtpEmailService smtpEmailService;
    private final BrevoSdkEmailService brevoSdkEmailService;

    @Value("${mail.activation.expire-minutes}")
    private int expireMinutes;

    @Value("${app.base-url}")
    private String appBaseUrl;

    public void sendBySmtp(String email) {
        send(email, smtpEmailService);
    }

    public void sendByBrevoSdk(String email) {
        send(email, brevoSdkEmailService);
    }

    public String activate(String token){
        EmailActivation activation = activationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 활성화 링크입니다."));
        if (activation.isTimeExpired()) {
            activation.expire();
            throw new IllegalStateException("활성화 링크가 만료되었습니다.");
        }
        if (activation.isActivated()) {
            return "이미 활성화된 이메일입니다.";
        }
        activation.activate();

        return "계정 활성화가 완료되었습니다.";
    }

    /*
     * 사용자가 이메일의 활성화 링크를 클릭했을 때 호출되는 검증 로직.
     *
     * Controller의 GET /api/poc/email/activate?token=... 요청에서 호출된다.
     *
     * 검증 흐름:
     *
     * 1. token으로 EmailActivation 조회
     * 2. token이 없으면 유효하지 않은 링크로 처리
     * 3. expiredAt이 지났거나 expired=true이면 만료 처리
     * 4. 이미 activated=true이면 이미 활성화된 상태로 응답
     * 5. 정상 token이면 activated=true로 변경
     *
     * 반환값은 Controller에서 HTML 화면에 보여줄 간단한 메시지다.
     * PoC라서 String을 반환하지만, 실제 프로젝트에서는 void, DTO, enum 등으로 바꿀 수 있다.
     */
    private void send(String email, EmailService emailService) {
        expireActiveTokens(email);
        
        String token = generateToken();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(expireMinutes);
        EmailActivation activation = EmailActivation.builder()
                .email(email)
                .token(token)
                .expiredAt(expiredAt)
                .build();
        activationRepository.save(activation);
        
        String activationLink = createActivationLink(token);
        emailService.sendActivationEmail(email, activationLink, expireMinutes);
    }

    private String createActivationLink(String token) {
        return appBaseUrl + "/api/poc/email/activate?token=" + token;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        //보안에 적합한 난수 생성기
        new SecureRandom().nextBytes(bytes);

        //랜덤 바이트는 그대로 URL에 넣을 수 없으므로 문자열로 인코딩한다.
        //withoutPadding()은 끝의 '=' 패딩 문자를 제거해 URL을 더 깔끔하게 만든다.
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private void expireActiveTokens(String email) {
        List<EmailActivation> activeTokens =
                activationRepository.findAllByEmailAndActivatedFalseAndExpiredFalse(email);

        activeTokens.forEach(EmailActivation::expire);
    }
}
