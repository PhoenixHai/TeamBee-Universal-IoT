package cn.universal.core.event.processer;

import cn.universal.core.event.EventMessage;

/**
 * 产品配置处理器接口
 *
 * @author Aleo
 * @version 1.0
 * @since 2025/1/9
 */
public interface ProductConfigProcessor {

  /**
   * 处理产品配置更新事件
   *
   * @param message 事件消息字符串
   */
  void handleProductConfigUpdated(EventMessage message);

  void handleProtocolUpdated(EventMessage message);
}
