// package cn.universal.rocketmq;
//
// import cn.universal.core.config.InstanceIdProvider;
// import cn.universal.core.iot.constant.IotConstant.ProductFlushType;
// import cn.universal.core.iot.constant.IotConstant.TcpFlushType;
// import cn.universal.core.iot.protocol.jar.ProtocolCodecJar;
// import cn.universal.core.iot.protocol.jscrtipt.ProtocolCodecJscript;
// import cn.universal.core.iot.protocol.magic.ProtocolCodecMagic;
// import manager.cn.tcp.protocol.TcpServerManager;
// import com.alibaba.fastjson.JSONObject;
// import jakarta.annotation.Resource;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.core.RocketMQListener;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
//
/// **
// * 编解码重载 tcp端口重启监听 tcp客户端消息
// *
// * @author yzq
// * @date 2023/8/29 9:47
// */
// @Slf4j
// @Component
// @RocketMQMessageListener(
//    topic = "${iot.rocketmq.product.flush.topic}",
//    consumerGroup = "${rocketmq.producer.group}-reload")
// public class NettyAndCodecConsumer implements RocketMQListener<String> {
//
//  @Autowired private InstanceIdProvider instanceIdProvider;
//  @Resource private TcpServerManager tcpServerManager;
//
//  @Override
//  public void onMessage(String s) {
//    try {
//      // 应用层过滤：检查是否为自己的消息
//      if (s != null && s.contains("sourceId")) {
//        String sourceId = extractSourceId(s);
//        if (sourceId != null && instanceIdProvider.isOwnMessage(sourceId)) {
//          log.debug("跳过自己发出的产品刷新消息: sourceId={}", sourceId);
//          return;
//        }
//      }
//      log.info("接收到rocketmq消息：{}", s);
//      consumer(s);
//    } catch (Exception e) {
//      log.error("处理产品刷新消息失败: message={}, error={}", s, e.getMessage(), e);
//    }
//  }
//
//  /** 从消息中提取sourceId 简单的字符串解析，避免JSON解析异常 */
//  private String extractSourceId(String message) {
//    try {
//      // 查找 IotConstant.CURRENT_INSTANCE_ID:"value" 模式
//      int startIndex = message.indexOf("\"sourceId\":\"");
//      if (startIndex != -1) {
//        startIndex += 12; // IotConstant.CURRENT_INSTANCE_ID:" 的长度
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
//
//  public void consumer(String data) {
//    log.info("收到消息={}", data);
//    JSONObject jsonObject = JSONObject.parseObject(data);
//    try {
//      if (codec(jsonObject)) {
//        return;
//      }
//      tcp(jsonObject);
//    } catch (Exception e) {
//      log.error("处理失败：", e);
//    }
//  }
//
//  private void tcp(JSONObject jsonObject) {
//    if (jsonObject == null
//        || !jsonObject.containsKey("productKey")
//        || !jsonObject.containsKey("type")
//        || !jsonObject.containsKey("customType")) {
//      return;
//    }
//    // tcp
//    if (ProductFlushType.server.name().equals(jsonObject.getString("type"))) {
//      String productKey = jsonObject.getString("productKey");
//      TcpFlushType type = TcpFlushType.valueOf(jsonObject.getString("type"));
//      if (TcpFlushType.start.name().equals(jsonObject.getString("customType"))) {
//        tcpServerManager.startTcpServerByProductKey(productKey);
//      } else if (TcpFlushType.reload.name().equals(jsonObject.getString("customType"))) {
//        tcpServerManager.restartTcpServer(productKey);
//      } else if (TcpFlushType.close.name().equals(jsonObject.getString("customType"))) {
//        tcpServerManager.stopTcpServer(productKey);
//      }
//      return;
//    }
//  }
//
//  private static boolean codec(JSONObject jsonObject) {
//    if (ProductFlushType.script.name().equals(jsonObject.getString("type"))) {
//      String provider = jsonObject.getString("provider");
//      if ("jscript".equals(jsonObject.getString("customType"))) {
//        ProtocolCodecJscript.getInstance().remove(provider);
//      } else if ("magic".equals(jsonObject.getString("customType"))) {
//        ProtocolCodecMagic.getInstance().remove(provider);
//      } else {
//        ProtocolCodecJar.getInstance().remove(provider);
//      }
//      return true;
//    }
//    return false;
//  }
// }
