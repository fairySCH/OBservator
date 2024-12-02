package com.happy.observator.model;

import com.happy.observator.Upbit.UpbitBalance;
import com.happy.observator.service.EmailService;
import com.happy.observator.service.UpbitService;
import com.happy.observator.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@RestController
@RequestMapping("/email")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final UserService userService;
    private final UpbitService upbitService;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;

    public EmailController(EmailService emailService, UserService userService, UpbitService upbitService) {
        this.emailService = emailService;
        this.userService = userService;
        this.upbitService = upbitService;
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendTestEmail(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails.getUsername();
            User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

            List<UpbitBalance> balances = upbitService.getBalances(user.getUpbitAccessKey(), user.getUpbitSecretKey());

            String htmlContent = generateHtmlContent(user, balances);

            emailService.sendEmailWithHtml(user.getEmail(), "[OBservator] 업비트 자산 정보", htmlContent);

            return ResponseEntity.ok("이메일 전송 성공!");
        } catch (Exception e) {
            logger.error("Error while sending email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    private String generateHtmlContent(User user, List<UpbitBalance> balances) throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        model.put("username", user.getUsername());
        model.put("balances", balances);

        Template template = freemarkerConfig.getConfiguration().getTemplate("email-content.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(model, stringWriter);

        return stringWriter.toString();
    }
}
