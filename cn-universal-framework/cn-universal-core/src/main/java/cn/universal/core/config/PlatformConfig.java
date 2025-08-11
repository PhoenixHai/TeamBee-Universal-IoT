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

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 11:10
 */
@Data
public class PlatformConfig implements Serializable {

  /** 数据库字段 */
  private String dbId;

  /** 英文编码 */
  private String code;

  /** 协议，支持多个，使用英文逗号（,）分割 */
  private String protocol;

  /** 平台地址 */
  private String url;

  /** 自定义请求头 */
  private Map<String, Object> header;
}
