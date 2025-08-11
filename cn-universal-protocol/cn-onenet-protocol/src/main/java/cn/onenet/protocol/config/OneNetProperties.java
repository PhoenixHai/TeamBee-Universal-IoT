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

package cn.onenet.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OneNet 协议配置属性
 * 
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@ConfigurationProperties(prefix = "onenet.protocol")
@Data
public class OneNetProperties {
  
  /** 是否启用OneNet协议模块 */
  private boolean enabled = false;
  
  /** API配置 */
  private Api api = new Api();
  
  /** MQTT配置 */
  private Mqtt mqtt = new Mqtt();
  
  @Data
  public static class Api {
    /** API Key */
    private String key;
    /** API URL */
    private String url = "https://api.heclouds.com";
    /** 超时时间(毫秒) */
    private int timeout = 30000;
  }
  
  @Data
  public static class Mqtt {
    /** MQTT服务器地址 */
    private String host = "183.230.40.39";
    /** MQTT端口 */
    private int port = 6002;
    /** 用户名 */
    private String username;
    /** 密码 */
    private String password;
  }
}
