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

package cn.ctaiot.protocol.config;

import cn.universal.core.base.monitor.NetMonitor;
import cn.universal.core.service.IUP;
import com.ctiot.aep.mqmsgpush.sdk.IMsgConsumer;
import com.ctiot.aep.mqmsgpush.sdk.IMsgListener;
import com.ctiot.aep.mqmsgpush.sdk.MqMsgConsumer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/5/26
 */
@Configuration
@Slf4j
public class CTAIoTMQMessage {

  @Autowired(required = false)
  @Qualifier("ctaIoTUPService")
  private IUP upService;

  @Autowired
  private CTAIoTProperties properties;

  @PostConstruct
  public void init() {
    if (properties.isEnable() && upService != null) {
      System.out.println("mqtt启动了");
      String certFilePath = ""; // 直接填空字符串，CA证书，JDK已经内置相关根证书，无需指定
      // 创建消息接收Listener
      IMsgListener msgListener =
          new IMsgListener() {

            @Override
            public void onMessage(String msg) {
              // 接收消息
              upService.asyncUP(msg);
              // 消息处理...
              // 为了提高效率，建议对消息进行异步处理（使用其它线程、发送到Kafka等）
            }
          };

      // 创建消息接收类
      IMsgConsumer consumer = new MqMsgConsumer();
      try {
        // 初始化
        /**
         * @param server 消息服务server地址
         * @param tenantId 租户Id
         * @param token 用户认证token
         * @param certFilePath 证书文件路径
         * @param topicNames 主题名列表，如果该列表为空或null，则自动消费该租户所有主题消息
         * @param msgListener 消息接收者
         * @return 是否初始化成功
         */
        consumer.init(
            properties.getServer(),
            properties.getTenantId(),
            properties.getToken(),
            certFilePath,
            null,
            msgListener);
        // 开始接收消息
        consumer.start();
        NetMonitor.freshConnectionStatus(properties.getServer(), Boolean.TRUE);
        // 程序退出时，停止接收、销毁
        // rocketmq.stop();
        // rocketmq.destroy();
      } catch (Exception e) {
        log.error("电信ctwing mq 异常={}", e);
      }
    }
  }
}
