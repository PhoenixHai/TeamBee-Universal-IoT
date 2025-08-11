package cn.universal.manager.notice.service.channel;

import com.alibaba.fastjson2.JSONObject;
import java.util.Map;
import java.util.Properties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class EmailNoticeSendChannel extends AbstractNoticeSendChannel {

  @Override
  public boolean support(String type) {
    return "email".equalsIgnoreCase(type);
  }

  @Override
  public NoticeSendResult send(
      String content,
      String receivers,
      cn.universal.manager.notice.model.NoticeChannel config,
      Map<String, Object> params) {
    try {
      JSONObject configObj = parseConfig(config);
      if (!validateConfig(
          configObj, "smtpHost", "smtpPort", "username", "password", "fromEmail", "receivers")) {
        String errMsg = "配置不完整";
        return buildAndLogResult("Email", content, receivers, false, errMsg);
      }
      JavaMailSender mailSender = createMailSender(configObj);
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(configObj.getString("fromEmail"));
      message.setTo(receivers.split(","));
      message.setSubject("系统通知");
      message.setText(content);
      mailSender.send(message);
      return buildAndLogResult("Email", content, receivers, true, null);
    } catch (Exception e) {
      String errMsg = "发送异常: " + e.getMessage();
      logger.error("邮件发送异常", e);
      return buildAndLogResult("Email", content, receivers, false, errMsg);
    }
  }

  /** 创建邮件发送器 */
  private JavaMailSender createMailSender(JSONObject config) {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(config.getString("smtpHost"));
    mailSender.setPort(config.getInteger("smtpPort"));
    mailSender.setUsername(config.getString("username"));
    mailSender.setPassword(config.getString("password"));

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "false");

    return mailSender;
  }
}
