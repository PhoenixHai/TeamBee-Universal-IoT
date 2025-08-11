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

package cn.universal.core.config;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OSS云存储 配置属性 @Author Lion Li
 */
@Data
@Component
@ConfigurationProperties(prefix = "third-platform")
public class PlatformProperties {

  private LvZhouProperties lvzhou;

  private CtwingProperties ctwing;

  /**
   * + Lora配置属性
   */
  @Data
  @NoArgsConstructor
  public static class LvZhouProperties {

    /**
     * 数据库字段
     */
    private String dbId;

    /**
     * 英文编码
     */
    private String code;

    /**
     * 协议，支持多个，使用英文逗号（,）分割
     */
    private String protocol;

    /**
     * 平台地址
     */
    private String url;

    /**
     * 鉴权
     */
    private String apiKey;

    /**
     * 自定义请求头
     */
    private Map<String, Object> header;
  }

  /**
   * + 电信物联配置属性
   */
  @Data
  @NoArgsConstructor
  public static class CtwingProperties {

  }
}
