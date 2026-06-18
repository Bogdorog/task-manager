package com.sergeev.taskmanager.notification.internal.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
// TO DO
/*
Исправить заголовки
Сменить цветовую палитру
Добавить имя в письмо
Исправить письмо об удалении аккаунта
 */
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
          <title>Восстановление доступа</title>
        </head>
        <body style="margin:0;padding:0;background:#f8fafc;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
          <!-- ВСЕГДА используйте 100%% внутри .formatted() -->
          <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 16px;">
            <tr><td align="center">
              <table width="540" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e2e8f0;box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">
        
                <tr>
                  <td style="background:#7FBF90;padding:24px 40px;vertical-align:middle;">
                    <table width="100%%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td>
                          <span style="font-size:20px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;">Task Manager</span>
                          <span style="font-size:12px;font-weight:400;color:#ffffff;margin-left:8px;vertical-align:middle;background:rgba(255,255,255,0.1);padding:2px 8px;border-radius:4px;">Workspace</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
        
                <tr>
                  <td style="padding:40px 40px 32px;">
                    <p style="margin:0 0 6px;font-size:24px;font-weight:700;color:#0f172a;line-height:1.2;letter-spacing:-0.5px;">Восстановление пароля</p>
                    <p style="margin:0 0 24px;font-size:13px;color:#64748b;">Уведомление системы безопасности · %s</p>
        
                    <p style="margin:0 0 24px;font-size:15px;color:#334155;line-height:1.6;">
                      Приветствуем! Мы получили запрос на сброс пароля от вашей учётной записи в системе управления задачами. Чтобы задать новый пароль и вернуться к вашим доскам, нажмите кнопку ниже:
                    </p>
        
                    <table width="100%%" cellpadding="0" cellspacing="0">
                      <tr>
                        <td align="center" style="padding:16px 0 32px;">
                          <a href="%s"
                             style="display:inline-block;background:#7FBF90;color:#ffffff;text-decoration:none;
                                    font-size:15px;font-weight:600;padding:14px 40px;border-radius:8px;
                                    box-shadow: 0 2px 4px rgba(25,118,210,0.2);transition: background-color 0.2s;">
                            Установить новый пароль
                          </a>
                        </td>
                      </tr>
                    </table>
        
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#f8fafc;border-radius:12px;border:1px solid #f1f5f9;">
                      <tr>
                        <td style="padding:20px;">
                          <p style="margin:0 0 8px;font-size:14px;font-weight:700;color:#0f172a;">Безопасность учетной записи</p>
                          <ul style="margin:0;padding-left:20px;font-size:13px;color:#475569;line-height:1.8;">
                            <li>Ссылка действительна в течение <strong style="color:#0f172a;">30 минут</strong></li>
                            <li>Действие ссылки аннулируется после первого использования</li>
                            <li>Если вы не отправляли этот запрос, ваш аккаунт в безопасности — просто проигнорируйте письмо</li>
                          </ul>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
        
                <tr>
                  <td style="padding:24px 40px;border-top:1px solid #f1f5f9;background:#fafafa;">
                    <p style="margin:0 0 8px;font-size:12px;font-weight:500;color:#64748b;">
                      Если кнопка не нажимается, скопируйте эту прямую ссылку в адресную строку браузера:
                    </p>
                    <p style="margin:0;font-size:12px;color:#1976d2;word-break:break-all;font-family: SFMono-Regular, Consolas, Monaco, monospace;background:#ffffff;padding:10px;border-radius:6px;border:1px solid #e2e8f0;">
                      %s
                    </p>
                  </td>
                </tr>
        
                <tr>
                  <td style="background:#CFE8D5;padding:20px 40px;text-align:center;border-top:1px solid #e2e8f0;">
                    <p style="margin:0;font-size:12px;color:#66BB6A;line-height:1.5;">
                      © 2026 Task Space · Автоматическая рассылка сервиса уведомлений.<br>Пожалуйста, не отвечайте на это письмо.
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

    public String buildAccountDeletionEmail(String deletionLink) {

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", new Locale("ru")));

        return """
<!DOCTYPE html>
<html lang="ru">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Удаление аккаунта</title>
</head>
<body style="margin:0;padding:0;background:#f8fafc;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
  <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 16px;">
    <tr><td align="center">
      <table width="540" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e2e8f0;box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);">

        <tr>
          <td style="background:#7FBF90;padding:24px 40px;vertical-align:middle;">
            <table width="100%%" cellpadding="0" cellspacing="0">
              <tr>
                <td>
                  <span style="font-size:20px;font-weight:700;color:#ffffff;letter-spacing:-0.5px;">Task Manager</span>
                  <span style="font-size:12px;font-weight:400;color:#ffffff;margin-left:8px;vertical-align:middle;background:rgba(255,255,255,0.1);padding:2px 8px;border-radius:4px;">Workspace</span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <tr>
          <td style="padding:40px 40px 32px;">
            <p style="margin:0 0 6px;font-size:24px;font-weight:700;color:#0f172a;line-height:1.2;letter-spacing:-0.5px;">Удаление аккаунта</p>
            <p style="margin:0 0 24px;font-size:13px;color:#64748b;">Запрос на удаление · %s</p>

            <p style="margin:0 0 24px;font-size:15px;color:#334155;line-height:1.6;">
              Приветствуем! Мы получили запрос на полное удаление вашей учётной записи из системы управления задачами. Обращаем ваше внимание, что это действие <strong style="color:#0f172a;">необратимо</strong> — все ваши данные, задачи и вложения будут удалены без возможности восстановления.
            </p>

            <p style="margin:0 0 24px;font-size:15px;color:#334155;line-height:1.6;">
              Если вы действительно хотите продолжить, нажмите кнопку ниже для подтверждения:
            </p>

            <table width="100%%" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center" style="padding:16px 0 32px;">
                  <a href="%s"
                     style="display:inline-block;background:#dc2626;color:#ffffff;text-decoration:none;
                            font-size:15px;font-weight:600;padding:14px 40px;border-radius:8px;
                            box-shadow: 0 2px 4px rgba(220,38,38,0.2);transition: background-color 0.2s;">
                    Удалить аккаунт навсегда
                  </a>
                </td>
              </tr>
            </table>

            <table width="100%%" cellpadding="0" cellspacing="0"
                   style="background:#fef2f2;border-radius:12px;border:1px solid #fee2e2;">
              <tr>
                <td style="padding:20px;">
                  <p style="margin:0 0 8px;font-size:14px;font-weight:700;color:#991b1b;">Важная информация</p>
                  <ul style="margin:0;padding-left:20px;font-size:13px;color:#7f1d1d;line-height:1.8;">
                    <li>Аккаунт и все связанные данные будут <strong style="color:#991b1b;">безвозвратно удалены</strong></li>
                    <li>Отменить это действие после подтверждения невозможно</li>
                    <li>Ссылка действительна в течение <strong style="color:#991b1b;">30 минут</strong></li>
                    <li>Если вы не отправляли этот запрос — немедленно смените пароль</li>
                  </ul>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <tr>
          <td style="padding:24px 40px;border-top:1px solid #f1f5f9;background:#fafafa;">
            <p style="margin:0 0 8px;font-size:12px;font-weight:500;color:#64748b;">
              Если кнопка не нажимается, скопируйте эту прямую ссылку в адресную строку браузера:
            </p>
            <p style="margin:0;font-size:12px;color:#1976d2;word-break:break-all;font-family: SFMono-Regular, Consolas, Monaco, monospace;background:#ffffff;padding:10px;border-radius:6px;border:1px solid #e2e8f0;">
              %s
            </p>
          </td>
        </tr>

        <tr>
          <td style="background:#CFE8D5;padding:20px 40px;text-align:center;border-top:1px solid #e2e8f0;">
            <p style="margin:0;font-size:12px;color:#66BB6A;line-height:1.5;">
              © 2026 Task Space · Автоматическая рассылка сервиса уведомлений.<br>Пожалуйста, не отвечайте на это письмо.
            </p>
          </td>
        </tr>

      </table>
    </td></tr>
  </table>
</body>
</html>
""".formatted(timestamp, deletionLink, deletionLink);
    }
}
