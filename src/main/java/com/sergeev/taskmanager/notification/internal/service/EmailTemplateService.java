package com.sergeev.taskmanager.notification.internal.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailTemplateService {

    public String buildResetPasswordEmail(String resetLink) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", new Locale("ru")));

        return """
        <!DOCTYPE html>
        <html lang="ru">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Сброс пароля</title>
        </head>
        <body style="margin:0;padding:0;background:#f4f4f0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 16px;">
            <tr><td align="center">
              <table width="540" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;border:1px solid #e8e8e4;">

                <!-- Header -->
                <tr>
                  <td style="background:#1a1a2e;padding:28px 40px;">
                    <span style="font-size:18px;font-weight:500;color:#ffffff;">Сознательный Гражданин</span>
                  </td>
                </tr>

                <!-- Body -->
                <tr>
                  <td style="padding:40px 40px 32px;">
                    <p style="margin:0 0 8px;font-size:22px;font-weight:500;color:#1a1a1a;line-height:1.3;">Сброс пароля</p>
                    <p style="margin:0 0 24px;font-size:14px;color:#888;">Запрос получен · %s</p>

                    <p style="margin:0 0 20px;font-size:15px;color:#333;line-height:1.7;">
                      Мы получили запрос на сброс пароля для вашей учётной записи.
                      Нажмите кнопку ниже, чтобы задать новый пароль.
                    </p>

                    <!-- CTA Button -->
                    <table width="100%%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td align="center" style="padding:32px 0;">
                          <a href="%s"
                             style="display:inline-block;background:#1a1a2e;color:#ffffff;text-decoration:none;
                                    font-size:15px;font-weight:500;padding:14px 36px;border-radius:8px;">
                            Сменить пароль
                          </a>
                        </td>
                      </tr>
                    </table>

                    <!-- Warning block -->
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#f7f7f4;border-radius:8px;">
                      <tr>
                        <td style="padding:16px 20px;">
                          <p style="margin:0 0 6px;font-size:13px;font-weight:600;color:#1a1a1a;">Важно</p>
                          <ul style="margin:0;padding-left:18px;font-size:13px;color:#666;line-height:1.8;">
                            <li>Ссылка действительна <strong style="color:#1a1a1a;">30 минут</strong></li>
                            <li>Ссылку можно использовать только один раз</li>
                            <li>Если вы не запрашивали сброс — просто проигнорируйте это письмо</li>
                          </ul>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <!-- Fallback link -->
                <tr>
                  <td style="padding:20px 40px;border-top:1px solid #e8e8e4;">
                    <p style="margin:0 0 6px;font-size:12px;color:#888;">
                      Если кнопка не работает, скопируйте ссылку в браузер:
                    </p>
                    <p style="margin:0;font-size:12px;color:#aaa;word-break:break-all;font-family:monospace;">
                      %s
                    </p>
                  </td>
                </tr>

                <!-- Footer -->
                <tr>
                  <td style="background:#f7f7f4;padding:16px 40px;text-align:center;">
                    <p style="margin:0;font-size:12px;color:#aaa;">
                      © 2026 Сознательный Гражданин · Это автоматическое письмо, не отвечайте на него
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(timestamp, resetLink, resetLink);
    }

}
