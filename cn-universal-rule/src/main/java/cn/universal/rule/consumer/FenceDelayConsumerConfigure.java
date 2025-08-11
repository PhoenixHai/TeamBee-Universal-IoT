/// *
// *
// * Copyright (c) 2025, iot-Universal. All Rights Reserved.
// *
// * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
// * @Author: Aleo
// * @Email: wo8335224@gmail.com
// * @Wechat: outlookFil
// *
// *
// */
//
// package cn.universal.rule.consumer;
//
// import cn.universal.core.config.InstanceIdProvider;
// import cn.universal.core.iot.constant.IotConstant;
// import jakarta.annotation.Resource;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.spring.annotation.MessageModel;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.core.RocketMQListener;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
//
/// **
// * 编解码重载 tcp端口重启监听 tcp客户端消息 @Author Aleo
// *
// * @since 2023/8/29 9:47
// */
// @Slf4j
// @Component
// @RocketMQMessageListener(
//    consumerGroup = "${iot.rocketmq.fence.topic}",
//    topic = "${iot.rocketmq.fence.topic}",
//    messageModel = MessageModel.BROADCASTING)
// public class FenceDelayConsumerConfigure implements RocketMQListener<String> {
//
//  @Autowired private InstanceIdProvider instanceIdProvider;
//
//  @Resource private FenceDelayConsumer fenceDelayConsumer;
//
//  @Override
//  public void onMessage(String s) {
//    try {
//      // 广播模式下，记录消息来源信息
//      if (s != null && s.contains(IotConstant.CURRENT_INSTANCE_ID)) {
//        String sourceId = extractSourceId(s);
//        if (sourceId != null) {
//          log.info(
//              "接收到rocketmq消息：sourceId={}, 当前实例={}, message={}",
//              sourceId,
//              instanceIdProvider.getSimpleInstanceId(),
//              s);
//
//          // 广播模式下可以选择是否处理自己的消息
//          if (instanceIdProvider.isOwnMessage(sourceId)) {
//            log.debug("广播模式：收到自己发出的消息，继续处理");
//          }
//        } else {
//          log.info("接收到rocketmq消息：{}", s);
//        }
//      } else {
//        log.info("接收到rocketmq消息：{}", s);
//      }
//
//      fenceDelayConsumer.consumer(s);
//    } catch (Exception e) {
//      log.error("处理围栏延迟消息失败: message={}, error={}", s, e.getMessage(), e);
//    }
//  }
//
//  /** 从消息中提取sourceId */
//  private String extractSourceId(String message) {
//    try {
//      int startIndex = message.indexOf("\"sourceId\":\"");
//      if (startIndex != -1) {
//        startIndex += 12;
//        int endIndex = message.indexOf("\"", startIndex);
//        if (endIndex != -1) {
//          return message.substring(startIndex, endIndex);
//        }
//      }
//    } catch (Exception e) {
//      log.debug("提取sourceId失败: {}", e.getMessage());
//    }
//    return null;
//  }
// }
