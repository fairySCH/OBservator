package com.happy.observator.model;

import com.happy.observator.service.EmailService;
import com.happy.observator.service.UserService;
import com.happy.observator.service.UserAssetService; // UserAsset 관련 서비스
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@RestController
@RequestMapping("/email")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final UserService userService;
    private final UserAssetService userAssetService;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;

    public EmailController(EmailService emailService, UserService userService, UserAssetService userAssetService) {
        this.emailService = emailService;
        this.userService = userService;
        this.userAssetService = userAssetService;
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendTestEmail(Principal principal) {
        try {
            String username = principal.getName();
            Optional<User> userOptional = userService.findByUsername(username);

            if (userOptional.isEmpty() || userOptional.get().getEmail() == null) {
                logger.warn("Email not found for user: {}", username);
                return ResponseEntity.badRequest().body("User email not found.");
            }

            User user = userOptional.get();

            List<UserAsset> assetData = userAssetService.getUserAssets(user.getId());

            String htmlContent = generateHtmlContent(user, assetData);

            emailService.sendEmailWithHtml(user.getEmail(), "[OBservator] 업비트 자산 정보", htmlContent);

            return ResponseEntity.ok("이메일 전송 성공!");
        } catch (Exception e) {
            logger.error("Error while sending email: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    private String generateHtmlContent(User user, List<UserAsset> assetData) throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        model.put("username", user.getUsername());
        model.put("balances", assetData);

        Template template = freemarkerConfig.getConfiguration().getTemplate("email-content.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(model, stringWriter);

        return stringWriter.toString();
    }
}
