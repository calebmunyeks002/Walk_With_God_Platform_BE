package org.walkwithgod.notification;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mail;
    private final String fromAddress;
    private final String frontendUrl;

    public EmailService(
            JavaMailSender mail,
            @Value("${spring.mail.username:inukatrust.demo@gmail.com}") String fromAddress,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.mail = mail;
        this.fromAddress = fromAddress;
        this.frontendUrl = frontendUrl;
    }

    /** Async — never blocks the request thread. */
    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mail.send(msg);
            log.info("Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {} — {}", to, e.getMessage());
        }
    }

    /** Welcome email sent on registration. */
    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        String subject = "Welcome to WalkWithGod, " + firstName + " ✨";
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <title>Welcome to WalkWithGod</title>
                </head>
                <body style="margin:0;padding:0;font-family:Arial,Helvetica,sans-serif;background:#f6f2fb;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f6f2fb;padding:40px 20px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 8px 30px rgba(63,27,86,.08);">
                          <tr>
                            <td style="background:linear-gradient(135deg,#6d22a4,#4e177e);padding:40px 40px 30px;text-align:center;color:#fff;">
                              <div style="font-size:28px;font-weight:800;letter-spacing:.5px;">
                                Walk<span style="color:#c68bf0;">With</span>God
                              </div>
                              <div style="font-size:11px;letter-spacing:3px;color:#d5c4e3;margin-top:8px;">
                                FAITH • PURPOSE • GROWTH
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:40px 40px 30px;color:#2a1740;">
                              <h1 style="margin:0 0 16px;font-size:24px;color:#2a1740;">Welcome, %s! ✨</h1>
                              <p style="font-size:15px;line-height:1.6;color:#5b3a70;margin:0 0 18px;">
                                We're so glad you've joined our community of believers walking together in faith.
                                Here's what you can do right now:
                              </p>
                              <ul style="font-size:14px;line-height:1.8;color:#5b3a70;padding-left:20px;margin:0 0 26px;">
                                <li>Read the <strong>Bible</strong> and take notes</li>
                                <li>Follow <strong>daily devotions</strong> for inspiration</li>
                                <li>Connect with a verified <strong>mentor</strong> for guidance</li>
                                <li>Share and encourage others in the <strong>community</strong></li>
                              </ul>
                              <table role="presentation" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="border-radius:12px;background:linear-gradient(90deg,#8a31cf,#4e177e);">
                                    <a href="%s/dashboard" style="display:inline-block;padding:14px 28px;color:#ffffff;font-weight:800;font-size:14px;text-decoration:none;letter-spacing:.3px;">
                                      Go to Your Dashboard →
                                    </a>
                                  </td>
                                </tr>
                              </table>
                              <p style="font-size:14px;line-height:1.6;color:#5b3a70;margin:30px 0 0;font-style:italic;border-left:3px solid #c68bf0;padding-left:16px;">
                                “But those who hope in the LORD will renew their strength. They will soar on wings like eagles…”<br>
                                <strong style="color:#6d22a4;font-style:normal;">— Isaiah 40:31</strong>
                              </p>
                            </td>
                          </tr>
                          <tr>
                            <td style="background:#faf8fc;padding:22px 40px;text-align:center;font-size:11px;color:#8c8295;">
                              You're receiving this because you signed up at WalkWithGod.<br>
                              Questions? Reply to this email — we'd love to hear from you.
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """
                .formatted(firstName, frontendUrl);

        sendHtml(to, subject, html);
    }

    /** Generic notification email. */
    @Async
    public void sendNotificationEmail(String to, String title, String body, String link) {
        String subject = title;
        String linkHtml = (link == null || link.isBlank())
                ? ""
                : """
                          <table role="presentation" cellpadding="0" cellspacing="0" style="margin-top:22px;">
                            <tr>
                              <td style="border-radius:12px;background:linear-gradient(90deg,#8a31cf,#4e177e);">
                                <a href="%s%s" style="display:inline-block;padding:12px 24px;color:#fff;font-weight:800;font-size:14px;text-decoration:none;">
                                  View in WalkWithGod →
                                </a>
                              </td>
                            </tr>
                          </table>
                        """
                        .formatted(frontendUrl, link);

        String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><title>%s</title></head>
                <body style="margin:0;padding:0;font-family:Arial,Helvetica,sans-serif;background:#f6f2fb;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f6f2fb;padding:40px 20px;">
                    <tr><td align="center">
                      <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 8px 30px rgba(63,27,86,.08);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#6d22a4,#4e177e);padding:32px 40px;text-align:center;color:#fff;">
                            <div style="font-size:22px;font-weight:800;">
                              Walk<span style="color:#c68bf0;">With</span>God
                            </div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:36px 40px;color:#2a1740;">
                            <h2 style="margin:0 0 14px;font-size:20px;color:#2a1740;">%s</h2>
                            <p style="font-size:15px;line-height:1.6;color:#5b3a70;margin:0;">%s</p>
                            %s
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#faf8fc;padding:18px 40px;text-align:center;font-size:11px;color:#8c8295;">
                            You're receiving this from WalkWithGod.
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .formatted(subject, title, body, linkHtml);

        sendHtml(to, subject, html);
    }
}