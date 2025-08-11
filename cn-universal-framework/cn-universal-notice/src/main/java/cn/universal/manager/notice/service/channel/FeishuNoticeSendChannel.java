package cn.universal.manager.notice.service.channel;

import com.alibaba.fastjson2.JSONObject;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FeishuNoticeSendChannel extends AbstractNoticeSendChannel {

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public boolean support(String type) {
    return "feishu".equalsIgnoreCase(type);
  }

  @Override
  public NoticeSendResult send(
      String content,
      String receivers,
      cn.universal.manager.notice.model.NoticeChannel config,
      Map<String, Object> params) {
    try {
      JSONObject configObj = parseConfig(config);
      if (!validateConfig(configObj, "webhook")) {
        String errMsg = "配置不完整";
        logSend("Feishu", content, receivers, false, errMsg);
        return NoticeSendResult.builder()
            .success(false)
            .receivers(receivers)
            .content(content)
            .errorMessage(errMsg)
            .build();
      }
      String webhook = configObj.getString("webhook");
      JSONObject message = new JSONObject();
      message.put("msg_type", "text");
      JSONObject text = new JSONObject();
      text.put("text", content);
      message.put("content", text);
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> request = new HttpEntity<>(message.toJSONString(), headers);
      ResponseEntity<String> response = restTemplate.postForEntity(webhook, request, String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        JSONObject responseBody = JSONObject.parseObject(response.getBody());
        if (responseBody.getInteger("code") == 0) {
          logSend("Feishu", content, receivers, true, "发送成功");
          return NoticeSendResult.builder()
              .success(true)
              .receivers(receivers)
              .content(content)
              .errorMessage(null)
              .build();
        } else {
          String errMsg = "飞书返回错误: " + responseBody.getString("msg");
          logSend("Feishu", content, receivers, false, errMsg);
          return NoticeSendResult.builder()
              .success(false)
              .receivers(receivers)
              .content(content)
              .errorMessage(errMsg)
              .build();
        }
      } else {
        String errMsg = "HTTP请求失败: " + response.getStatusCode();
        logSend("Feishu", content, receivers, false, errMsg);
        return NoticeSendResult.builder()
            .success(false)
            .receivers(receivers)
            .content(content)
            .errorMessage(errMsg)
            .build();
      }
    } catch (Exception e) {
      String errMsg = "发送异常: " + e.getMessage();
      logSend("Feishu", content, receivers, false, errMsg);
      logger.error("飞书发送异常", e);
      return NoticeSendResult.builder()
          .success(false)
          .receivers(receivers)
          .content(content)
          .errorMessage(errMsg)
          .build();
    }
  }
}
