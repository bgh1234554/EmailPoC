package com.github.baek.emailpoc.service;

import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrevoSdkEmailService implements EmailService{
    private final TemplateEngine templateEngine;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${mail.from}")
    private String from;

    /*
     * 1. Thymeleaf 템플릿을 HTML 문자열로 렌더링
     * 2. Brevo SDK에서 요구하는 SendSmtpEmail 객체 생성
     * 3. sender, to, subject, htmlContent 설정
     * 4. TransactionalEmailsApi를 통해 Brevo API 호출
     */
    @Override
    public void sendActivationEmail(String to, String activationLink, int expireMinutes) {
        String html = renderTemplate(activationLink,expireMinutes);

        //Brevo SDK에서 트랜잭션 이메일 발송 요청을 표현하는 객체.
        SendSmtpEmail email = new SendSmtpEmail();

        //발신자 정보 설정
        email.setSender(
                /*
                 * name("Dartoo"):
                 * - 수신자 메일함에 표시될 발신자 이름
                 * - 예: Dartoo <bgh1234554@gmail.com>
                 */
                new SendSmtpEmailSender().email(from).name("Dartoo")
        );

        //수신자 정보 설정
        email.setTo(
                List.of(new SendSmtpEmailTo().email(to))
        );

        //이메일 제목 설정
        email.setSubject("[Dartoo] 계정 활성화 이메일");

        //HTML 본문 설정
        email.setHtmlContent(html);
        try{
            //Brevo SDK에 있는 트랜잭션 이메일 API를 호출하기 위한 객체
            TransactionalEmailsApi api = new TransactionalEmailsApi();

            //Brevo API 인증 설정
            api.getApiClient().setApiKey(apiKey);

            //이메일 발송 Api 호출
            api.sendTransacEmail(email);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage(),e);
        }
    }

    private String renderTemplate(String activationLink, int expireMinutes) {
        Context context = new Context();
        context.setVariable("activationLink", activationLink);
        context.setVariable("expireMinutes", expireMinutes);

        return templateEngine.process("email/activation", context);
    }
}
