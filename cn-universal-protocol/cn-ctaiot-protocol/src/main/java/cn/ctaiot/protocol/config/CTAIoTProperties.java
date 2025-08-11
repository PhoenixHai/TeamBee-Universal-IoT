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

package cn.ctaiot.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CT-AIoT 配置属性类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@Data
@ConfigurationProperties(prefix = "ct.aiot.aep")
public class CTAIoTProperties {

  /** 是否启用CT-AIoT模块 */
  private boolean enable = false;

  /** 应用ID */
  private String appid;

  /** 应用Key */
  private String appkey;

  /** 应用Secret */
  private String appsecret;

  /** 服务器地址 */
  private String server;

  /** 租户ID */
  private String tenantId;

  /** 认证Token */
  private String token;
}
