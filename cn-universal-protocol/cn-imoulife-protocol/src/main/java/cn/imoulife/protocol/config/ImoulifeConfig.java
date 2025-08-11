/*
 *
 * Copyright (c) 2025, iot-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.imoulife.protocol.config;

import cn.hutool.json.JSONObject;
import java.text.MessageFormat;
import java.util.UUID;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

/**
 * 乐橙配置
 */
@Component
@Data
public class ImoulifeConfig {

  @Value("${imoulife.appid}")
  private String appId;

  @Value("${imoulife.appsecret}")
  private String secret;

  @Value("${imoulife.ver}")
  private String ver;

  @Value("${imoulife.host}")
  private String host;

  @Value("${imoulife.port}")
  private String port;

  private String time;
  private String nonce;

  public JSONObject refreshParams() {
    time = System.currentTimeMillis() / 1000 + "";
    nonce = UUID.randomUUID().toString();
    JSONObject param = new JSONObject();
    JSONObject system = new JSONObject();
    system.set("ver", ver);
    system.set("time", time);
    system.set("nonce", nonce);
    system.set("appId", appId);
    system.set("sign", digestSign());
    param.set("id", UUID.randomUUID().toString());
    param.set("system", system);
    param.set("params", new JSONObject());
    return param;
  }

  private String digestSign() {
    String signString =
        MessageFormat.format(
            "time:{0},nonce:{1},appSecret:{2}", this.time, this.nonce, this.secret);
    return DigestUtils.md5DigestAsHex(signString.getBytes());
  }
}
