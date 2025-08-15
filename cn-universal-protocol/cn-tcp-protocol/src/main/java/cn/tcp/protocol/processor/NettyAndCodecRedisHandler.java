package cn.tcp.protocol.processor;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.tcp.protocol.manager.TcpServerManager;
import cn.universal.core.config.InstanceIdProvider;
import cn.universal.core.event.EventMessage;
import cn.universal.core.event.processer.ProductConfigProcessor;
import cn.universal.core.iot.constant.IotConstant.ProductFlushType;
import cn.universal.core.iot.protocol.jar.ProtocolCodecJar;
import cn.universal.core.iot.protocol.jscrtipt.ProtocolCodecJscript;
import cn.universal.core.iot.protocol.magic.ProtocolCodecMagic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Netty和编解码Redis事件处理器 替代NettyAndCodecConsumer
 */
@Slf4j
@Component
public class NettyAndCodecRedisHandler implements ProductConfigProcessor {

  @Autowired
  private InstanceIdProvider instanceIdProvider;

  @Autowired
  private TcpServerManager tcpServerManager;

  /**
   * 处理产品刷新和编解码重载事件
   */
  @Override
  public void handleProductConfigUpdated(EventMessage message) {
    try {
      // 检查是否为自己的消息
      if (isOwnMessage(message)) {
        log.debug("跳过自己发出的产品刷新消息");
        return;
      }

      log.info("收到产品刷新事件: {}", message);
      processProductFlush(message);

    } catch (Exception e) {
      log.error("处理产品刷新事件失败: message={}, error={}", message, e.getMessage(), e);
    }
  }

  @Override
  public void handleProtocolUpdated(EventMessage message) {
    try {
      JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(message.getData()));
      if (handleCodecReload(jsonObject)) {
        return;
      }
    } catch (Exception e) {
      log.error("处理产品刷新失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 处理产品刷新逻辑
   */
  private void processProductFlush(EventMessage data) {
    log.info("处理产品刷新消息: {}", data);
    JSONObject jsonObject = JSONUtil.parseObj(JSONUtil.toJsonStr(data.getData()));

    try {
      if (handleCodecReload(jsonObject)) {
        return;
      }
      handleTcpServerReload(jsonObject);
    } catch (Exception e) {
      log.error("处理产品刷新失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 处理TCP服务器重载
   */
  private void handleTcpServerReload(JSONObject jsonObject) {
    if (jsonObject == null
        || !jsonObject.containsKey("productKey")
        || !jsonObject.containsKey("type")
        || !jsonObject.containsKey("customType")) {
      return;
    }

    // TCP服务器重载
    if (ProductFlushType.server.name().equals(jsonObject.getStr("type"))) {
      String productKey = jsonObject.getStr("productKey");
      String customType = jsonObject.getStr("customType");

      log.info("处理TCP服务器重载: productKey={}, customType={}", productKey, customType);

      switch (customType) {
        case "start" -> tcpServerManager.startTcpServerByProductKey(productKey);
        case "reload" -> tcpServerManager.restartTcpServer(productKey);
        case "close" -> tcpServerManager.stopTcpServer(productKey);
        default -> log.warn("未知的TCP服务器操作类型: {}", customType);
      }
    }
  }

  /**
   * 处理编解码重载
   */
  private boolean handleCodecReload(JSONObject jsonObject) {
    if (ProductFlushType.script.name().equals(jsonObject.getStr("type"))) {
      String provider = jsonObject.getStr("provider");
      String customType = jsonObject.getStr("customType");

      log.info("处理编解码重载: provider={}, customType={}", provider, customType);

      switch (customType) {
        case "jscript" -> ProtocolCodecJscript.getInstance().remove(provider);
        case "magic" -> ProtocolCodecMagic.getInstance().remove(provider);
        default -> ProtocolCodecJar.getInstance().remove(provider);
      }
      return true;
    }
    return false;
  }

  /**
   * 判断是否是自己发送的消息
   */
  private boolean isOwnMessage(EventMessage message) {
    try {
      String nodeId = message.getNodeId();
      return instanceIdProvider.getInstanceId().equals(nodeId);
    } catch (Exception e) {
      return false;
    }
  }
}
