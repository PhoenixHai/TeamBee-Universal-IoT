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

package cn.imoulife.protocol.entity;

/**
 * 可用方法 @Author 🐤 zhongxin
 *
 * @email ✉ asimooc@foxmail.com
 * @since ⏰ 2018/11/21
 */
public enum UriMethodEnum {
  /**
   * 获取管理员token
   */
  ACCESS_TOKEN("accessToken"),
  /**
   * 设备列表获取
   */
  DOWN_TRANSFER("downTransfer");

  private String method;

  UriMethodEnum(String method) {
    this.method = method;
  }

  @Override
  public String toString() {
    return this.method;
  }
}
