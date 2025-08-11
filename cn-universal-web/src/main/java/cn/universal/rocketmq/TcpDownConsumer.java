// package cn.universal.rocketmq;
//
// import cn.hutool.core.util.StrUtil;
// import cn.hutool.json.JSONUtil;
// import cn.universal.core.config.InstanceIdProvider;
// import entity.cn.tcp.protocol.TcpDownRequest;
// import down.processor.cn.tcp.protocol.TcpDownFunction;
// import jakarta.annotation.Resource;
// import java.nio.charset.Charset;
// import java.util.List;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.acl.common.AclClientRPCHook;
// import org.apache.rocketmq.acl.common.SessionCredentials;
// import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
// import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
// import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
// import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
// import org.apache.rocketmq.client.exception.MQClientException;
// import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
// import org.apache.rocketmq.common.message.MessageExt;
// import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.stereotype.Component;
//
/// ** 集群下的监听 */
// @Component
// @Slf4j
// public class TcpDownConsumer implements MessageListenerConcurrently {
//
//  // ==================== 配置参数 ====================
//  @Value("${rocketmq.producer.group}")
//  private String groupName;
//
//  @Value("${rocketmq.name-server}")
//  private String namesrvAddr;
//
//  @Value("${iot.rocketmq.tcp.topic}")
//  private String topic;
//
//  @Value("${rocketmq.consumer.access-key}")
//  private String accessKey;
//
//  @Value("${rocketmq.consumer.secret-key}")
//  private String secretKey;
//
//  // ==================== 依赖注入 ====================
//  @Resource private InstanceIdProvider instanceIdProvider;
//
//  @Resource private TcpDownFunction tcpDownFunction;
//
//  // ==================== 消费者Bean ====================
//  @Bean(destroyMethod = "shutdown", name = "tcpLogConsumer")
//  public DefaultMQPushConsumer defaultMQPushConsumer() throws MQClientException {
//    log.info("开始创建TCP消息消费者...");
//
//    // 创建认证凭据
//    SessionCredentials credentials = new SessionCredentials(accessKey, secretKey);
//    AclClientRPCHook rpcHook = new AclClientRPCHook(credentials);
//
//    // 创建消费者
//    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(rpcHook);
//    consumer.setNamesrvAddr(namesrvAddr);
//    consumer.setConsumerGroup(groupName);
//    consumer.registerMessageListener(this); // 注册自己作为监听器
//    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
//    consumer.setMessageModel(MessageModel.BROADCASTING);
//
//    // 使用实例ID作为Tag进行过滤
//    String instanceTag = instanceIdProvider.getInstanceId();
//    consumer.subscribe(topic, instanceTag);
//
//    // 启动消费者
//    consumer.start();
//
//    log.info(
//        "TCP消息消费者创建成功 - topic: {}, tag: {}, consumerGroup: {}, instanceId: {}",
//        topic,
//        instanceTag,
//        groupName,
//        instanceIdProvider.getSimpleInstanceId());
//
//    return consumer;
//  }
//
//  // ==================== 消息处理逻辑 ====================
//  @Override
//  public ConsumeConcurrentlyStatus consumeMessage(
//      List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//    try {
//      MessageExt messageExt = msgs.get(0);
//      String messageBody = StrUtil.str(messageExt.getBody(), Charset.defaultCharset());
//
//      log.info(
//          "接收到TCP下行指令消息 - topic: {}, tag: {}, message: {}",
//          messageExt.getTopic(),
//          messageExt.getTags(),
//          messageBody);
//
//      // 解析消息
//      TcpDownRequest downRequest = JSONUtil.toBean(messageBody, TcpDownRequest.class);
//
//      // 处理消息
//      tcpDownFunction.process(downRequest);
//
//      log.debug(
//          "TCP下行指令处理完成 - deviceId: {}, productKey: {}",
//          downRequest.getDeviceId(),
//          downRequest.getProductKey());
//
//      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//
//    } catch (Exception e) {
//      log.error("处理TCP下行指令失败 - message: {}, error: {}", msgs.get(0).getBody(), e.getMessage(), e);
//      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//    }
//  }
//
//  // ==================== 工具方法 ====================
//
//  /** 获取当前配置信息 */
//  public String getConfigInfo() {
//    return String.format(
//        "topic=%s, consumerGroup=%s, instanceId=%s",
//        topic, groupName, instanceIdProvider.getSimpleInstanceId());
//  }
//
//  /** 检查消费者状态 */
//  public boolean isConsumerHealthy() {
//    try {
//      // 这里可以添加健康检查逻辑
//      return true;
//    } catch (Exception e) {
//      log.error("消费者健康检查失败", e);
//      return false;
//    }
//  }
// }
