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

package cn.universal.persistence.base;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/6/28
 */
public interface IotUPRocketMQAdapter {

  /**
   * 判断是否存在rocketmq消费者
   */
  final String IOT_ROCKETMQ_EXIST_CONSUMER = "existIotConsumer";

  default Object send(String topic, String message) {
    return send(topic, message, null);
  }

  default Object send(String topic, String message, String tag) {
    return null;
  }

  default void sendAsync(String topic, String message) {
  }

  default void sendAsync(String topic, String message, String tag) {
  }

  default void sendFifo(String topic, String orderKey, String message, String tag) {
  }

  default void sendFifo(String topic, String orderKey, String message) {
  }

  default void sendTransaction(String topic, String message, String tag) {
  }

  default void sendTransaction(String topic, String message) {
  }

  default void sendDelaySeconds(String topic, Object message, long seconds) {
  }
}
