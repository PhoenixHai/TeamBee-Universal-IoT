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
import lombok.extern.slf4j.Slf4j;

/** 电信天翼物联AIoT（ctwing）配置类 */
@Slf4j
@Data
public class CTAIoTConfig {

  private String appid;
  private String appKey;
  private String appSecret;
  private String server;
  private String tenantId;
  private String token;
  private boolean enable;

  // Bean创建方法已移至CTAIoTAutoConfiguration
}
