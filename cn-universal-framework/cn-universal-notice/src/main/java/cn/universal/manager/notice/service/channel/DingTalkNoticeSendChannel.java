package cn.universal.manager.notice.service.channel;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DingTalkNoticeSendChannel extends AbstractNoticeSendChannel {

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public boolean support(String type) {
    return "dingTalk".equalsIgnoreCase(type);
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
        return buildAndLogResult("DingTalk", content, receivers, false, errMsg);
      }
      String webhook = configObj.getString("webhook");
      String secret = configObj.getString("secret");
      JSONObject message = new JSONObject();
      message.put("msgtype", "text");
      JSONObject text = new JSONObject();
      text.put("content", content);
      message.put("text", text);
      if (StrUtil.isNotBlank(receivers)) {
        text.put("atMobiles", StrUtil.split(receivers, ',', true, true));
        message.put("at", text);
      }
      if (secret != null && !secret.trim().isEmpty()) {
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        String sign = calculateHmacSHA256(stringToSign, secret);
        String signedUrl = webhook + "&timestamp=" + timestamp + "&sign=" + sign;
        webhook = signedUrl;
      }
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> request = new HttpEntity<>(message.toJSONString(), headers);
      ResponseEntity<String> response = restTemplate.postForEntity(webhook, request, String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        JSONObject responseBody = JSONObject.parseObject(response.getBody());
        if (responseBody.getInteger("errcode") == 0) {
          return buildAndLogResult("DingTalk", content, receivers, true, null);
        } else {
          String errMsg = "钉钉返回错误: " + responseBody.getString("errmsg");
          return buildAndLogResult("DingTalk", content, receivers, false, errMsg);
        }
      } else {
        String errMsg = "HTTP请求失败: " + response.getStatusCode();
        return buildAndLogResult("DingTalk", content, receivers, false, errMsg);
      }
    } catch (Exception e) {
      String errMsg = "发送异常: " + e.getMessage();
      logger.error("钉钉发送异常", e);
      return buildAndLogResult("DingTalk", content, receivers, false, errMsg);
    }
  }

  /**
   * 计算HMAC-SHA256签名
   */
  private String calculateHmacSHA256(String data, String key) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKeySpec =
        new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    mac.init(secretKeySpec);
    byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(hash);
  }
}
