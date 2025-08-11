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

package cn.tcp.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TCP 协议配置属性
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/2
 */
@ConfigurationProperties(prefix = "tcp.protocol")
@Data
public class TcpProperties {

  /**
   * 是否启用TCP协议模块
   */
  private boolean enabled = true;

  /**
   * 标准TCP配置
   */
  private Standard standard = new Standard();

  @Data
  public static class Standard {

    /**
     * 是否启用标准TCP
     */
    private boolean enabled = true;
  }
}
