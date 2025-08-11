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

package cn.universal.core.service;

import cn.universal.core.iot.message.UPRequest;

/**
 * 编解码接口
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 18:36
 */
public interface ICodec {

  String VERSION = "1";

  default String version() {
    return "1";
  }

  /**
   * 预编码
   *
   * @param message 消息原始串
   * @return 上行消息
   */
  default UPRequest preDecode(String productKey, String message) {
    return null;
  }

  /**
   * 进编码前特殊处理 附加影子
   *
   * @return 下行消息
   */
  default String beforeEncode(String productKey, String deviceId, String config, String function) {
    return function;
  }

  /**
   * 下行消息编码
   *
   * @param productKey 产品key
   * @param payload    消息的原串
   */
  default String spliceDown(String productKey, String payload) {
    return null;
  }
}
