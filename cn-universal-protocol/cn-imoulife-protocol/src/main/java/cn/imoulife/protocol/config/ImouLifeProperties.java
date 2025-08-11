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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ImouLife 协议配置属性
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@ConfigurationProperties(prefix = "imoulife.protocol")
@Data
public class ImouLifeProperties {

  /**
   * 是否启用ImouLife协议模块
   */
  private boolean enabled = false;

  /**
   * 乐橙云API配置
   */
  private Api api = new Api();

  /**
   * 认证配置
   */
  private Auth auth = new Auth();

  @Data
  public static class Api {

    /**
     * API基础URL
     */
    private String baseUrl = "https://openapi.lechange.cn";
    /**
     * API版本
     */
    private String version = "v1";
    /**
     * 超时时间(毫秒)
     */
    private int timeout = 30000;
  }

  @Data
  public static class Auth {

    /**
     * 应用ID
     */
    private String appId;
    /**
     * 应用密钥
     */
    private String appSecret;
    /**
     * 访问令牌
     */
    private String accessToken;
    /**
     * 令牌过期时间(秒)
     */
    private long tokenExpireTime = 7200;
  }
}
