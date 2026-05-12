package com.github.baek.emailpoc.web;

import com.github.baek.emailpoc.dto.EmailActivationRequest;
import com.github.baek.emailpoc.dto.EmailActivationResponse;
import com.github.baek.emailpoc.service.EmailActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/poc/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailActivationService emailActivationService;

    @PostMapping("/smtp")
    public EmailActivationResponse sendActivationBySmtp(
            @RequestBody EmailActivationRequest request
    ) {
        emailActivationService.sendBySmtp(request.email());

        return new EmailActivationResponse("SMTP 방식으로 활성화 이메일 발송 요청이 완료되었습니다.");
    }

    @PostMapping("/brevo-sdk")
    public EmailActivationResponse sendActivationByBrevoSdk(
            @RequestBody EmailActivationRequest request
    ) {
        emailActivationService.sendByBrevoSdk(request.email());

        return new EmailActivationResponse("Brevo SDK 방식으로 활성화 이메일 발송 요청이 완료되었습니다.");
    }

    @GetMapping(value = "/activate", produces = MediaType.TEXT_HTML_VALUE)
    public String activate(@RequestParam String token) {
        String result = emailActivationService.activate(token);

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <title>계정 활성화 결과</title>
                </head>
                <body>
                    <h1>%s</h1>
                    <p>PoC 계정 활성화 링크 검증이 완료되었습니다.</p>
                </body>
                </html>
                """.formatted(result);
    }
}
/*
컨트롤러 endpoint는 3개다.

POST /api/poc/email/smtp
POST /api/poc/email/brevo-sdk
GET  /api/poc/email/activate?token=...

발송 endpoint는 2개지만, 활성화 검증 endpoint는 1개만 둔다.

SMTP로 보냈든 Brevo SDK로 보냈든, 사용자가 링크를 클릭한 뒤의 검증 로직은 동일하기 때문이다.
*/