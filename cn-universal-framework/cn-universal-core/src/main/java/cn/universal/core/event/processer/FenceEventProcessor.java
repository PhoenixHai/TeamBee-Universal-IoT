package cn.universal.core.event.processer;

import cn.universal.core.event.EventMessage;

/**
 * 电子围栏事件处理器接口
 *
 * @author Aleo
 * @version 1.0
 * @since 2025/1/9
 */
public interface FenceEventProcessor {

  /**
   * 处理电子围栏事件
   *
   * @param message 事件消息字符串
   */
  void handleFenceEvent(EventMessage message);
}
