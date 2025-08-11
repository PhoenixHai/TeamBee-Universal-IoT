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

import cn.universal.dm.device.service.plugin.BaseMessageProcessor;
import cn.tcp.protocol.entity.TcpUPRequest;

/**
 * TCP消息处理器接口
 *
 * <p>继承通用的BaseMessageProcessor，定义TCP模块特有的处理方法 各TCP处理器实现此接口，提供具体的处理逻辑
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
public interface TcpUPMessageProcessor extends BaseMessageProcessor {

  /**
   * 处理TCP消息
   *
   * @param request TCP上行请求对象
   * @return 处理结果
   */
  ProcessorResult process(TcpUPRequest request);

  /**
   * 是否支持处理该消息
   *
   * @param request TCP上行请求对象
   * @return true表示支持，false表示不支持
   */
  boolean supports(TcpUPRequest request);

  /** 处理器描述（可选） */
  default String getDescription() {
    return getName();
  }

  /** 处理器优先级（可选，用于细粒度排序） */
  default int getPriority() {
    return 0;
  }

  /** 是否必须执行（可选） */
  default boolean isRequired() {
    return false;
  }

  /** 处理前的预检查（可选） */
  default boolean preCheck(TcpUPRequest request) {
    return true;
  }

  /** 处理后的后置操作（可选） */
  default void postProcess(TcpUPRequest request, ProcessorResult result) {
    // 默认不做任何操作
  }

  /** 异常处理（可选） */
  default void onError(TcpUPRequest request, Exception e) {
    request.setError("处理器 [" + getName() + "] 异常: " + e.getMessage());
  }

  /** 处理结果枚举 */
  enum ProcessorResult {
    /** 继续处理 - 传递给下一个处理器 */
    CONTINUE,

    /** 停止处理 - 成功完成，不再传递给后续处理器 */
    STOP,

    /** 跳过当前消息 - 忽略该消息，不进行后续处理 */
    SKIP,

    /** 处理失败 - 发生错误，停止处理链 */
    ERROR
  }
}
