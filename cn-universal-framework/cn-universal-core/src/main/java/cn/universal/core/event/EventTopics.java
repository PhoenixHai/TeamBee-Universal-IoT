package cn.universal.core.event;

/**
 * 事件主题管理类 集中管理所有Redis事件主题
 *
 * <p>使用示例：
 *
 * <p>// 发布事件 eventPublisher.publishEvent(EventTopics.PROTOCOL_UPDATED, eventData);
 * eventPublisher.publishEvent(EventTopics.getTcpCommandTopic(instanceId), command);
 *
 * <p>// 订阅事件（在RedisEventSubscriber中） container.addMessageListener( new
 * MessageListenerAdapter(this,
 * "handleProtocolUpdated"), new ChannelTopic(EventTopics.PROTOCOL_UPDATED));
 *
 * <p>// 模式匹配订阅 container.addMessageListener( new MessageListenerAdapter(this, "handleTcpCommand"),
 * new PatternTopic(EventTopics.TCP_COMMAND));
 *
 * @author Aleo
 * @version 1.0
 * @since 2025/1/9
 */
public class EventTopics {

  // ==================== 协议相关事件 ====================

  /**
   * 协议更新事件 用于通知集群内其他实例协议配置已更新
   */
  public static final String PROTOCOL_UPDATED = "protocol:updated";

  // ==================== 电子围栏相关事件 ====================

  /**
   * 电子围栏事件 用于处理电子围栏触发事件
   */
  public static final String FENCE_EVENT = "fence:event";

  // ==================== TCP相关事件 ====================

  /**
   * TCP下行指令事件（模式匹配） 用于处理TCP下行指令
   */
  public static final String TCP_DOWN = "tcp:down:*";

  // ==================== 产品配置相关事件 ====================

  /**
   * 产品配置更新事件 用于通知产品配置变更
   */
  public static final String PRODUCT_CONFIG_UPDATED = "product:config:updated";

  // ==================== 测试相关事件 ====================

  /**
   * 测试TCP事件 用于测试TCP相关功能
   */
  public static final String TEST_TCP_REL = "test:tcp:rel";

  // ==================== 工具方法 ====================

  /**
   * 获取TCP下行指令主题（带实例ID）
   *
   * @param instanceId 实例ID
   * @return 完整的TCP下行指令主题
   */
  public static String getTcpDownTopic(String instanceId) {
    return "tcp:down:" + instanceId;
  }
}
