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

package cn.tcp.protocol.processor;

import cn.tcp.protocol.entity.TcpDownRequest;
import cn.universal.core.base.R;
import cn.universal.dm.device.service.plugin.BaseMessageProcessor;

/**
 * TCP消息下行处理器接口
 *
 * <p>继承通用的BaseMessageProcessor，定义TCP模块特有的处理方法 各TCP处理器实现此接口，提供具体的处理逻辑
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
public interface TcpDownMessageProcessor extends BaseMessageProcessor {

  /**
   * 处理TCP消息
   *
   * @param request TCP上行请求对象
   * @return 处理结果
   */
  R<?> process(TcpDownRequest request);

  /**
   * 是否支持处理该消息
   *
   * @param request TCP上行请求对象
   * @return true表示支持，false表示不支持
   */
  boolean supports(TcpDownRequest request);

  /**
   * 处理前的预检查（可选）
   */
  default boolean preCheck(TcpDownRequest request) {
    return true;
  }

  /**
   * 处理后的后置操作（可选）
   */
  default void postProcess(TcpDownRequest request, R<?> result) {
    // 默认不做任何操作
  }
}
