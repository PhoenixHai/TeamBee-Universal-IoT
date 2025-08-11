// package cn.universal.protocol.tcp.processor.up;
//
// import cn.hutool.core.util.RandomUtil;
// import core.cn.universal.protocol.tcp.DeviceIdentityExtractor;
// import core.cn.universal.protocol.tcp.DeviceIdentityExtractorRegistry;
// import entity.cn.universal.protocol.tcp.TcpUPRequest;
// import manager.cn.universal.protocol.tcp.TcpConnectionManager;
// import processor.cn.universal.protocol.tcp.TcpUPMessageProcessor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
//
/// **
// * 设备标识提取处理器 - 从报文中提取设备ProductKey和DeviceId
// * <p>
// * 处理顺序: 20 职责: 1. 根据产品配置选择合适的设备标识提取器 2. 从解码后的数据中提取ProductKey和DeviceId 3. 验证提取结果的有效性
// * <p>
// * 这是解决原有TCP模块问题的核心处理器，实现了设备标识提取与协议适配的解耦
// *
// * @Author Aleo
// * @version 2.0
// * @since 2025/1/20
// */
// @Slf4j(topic = "tcp")
// @Component
// public class DeviceExtractionProcessor implements TcpUPMessageProcessor {
//
//  private final DeviceIdentityExtractorRegistry extractorRegistry;
//  private final TcpConnectionManager tcpConnectionManager;
//
//  @Autowired
//  public DeviceExtractionProcessor(DeviceIdentityExtractorRegistry extractorRegistry,
//      TcpConnectionManager tcpConnectionManager) {
//    this.extractorRegistry = extractorRegistry;
//    this.tcpConnectionManager = tcpConnectionManager;
//  }
//
//
//  @Override
//  public String getName() {
//    return "DeviceExtractionProcessor";
//  }
//
//  @Override
//  public int getOrder() {
//    return 12;
//  }
//
//  @Override
//  public ProcessorResult process(TcpUPRequest message) {
/// /    // 检查是否已提取设备标识 /    if (message.isDeviceIdentified()) { /      log.debug("[{}]
/// 设备标识已提取，跳过处理: messageId={}, deviceId={}", getName(), message.getRequestId(), /
/// message.getDeviceId()); /      return ProcessorResult.CONTINUE; /    } / /    // 检查解码数据 /    if
/// (message.getDecodedData() == null) { /      log.warn("[{}] 解码数据为空，无法提取设备标识: messageId={}",
/// getName(), message.getRequestId()); /      message.setError("解码数据为空，无法提取设备标识"); /      return
/// ProcessorResult.ERROR; /    } / 只在设备首次识别时注册 deviceId->Channel
//
//    if (message.getChannelContext() != null && message.getDeviceId() != null &&
// message.getProductKey() != null) {
//      String deviceId = message.getDeviceId();
//      String productKey = message.getProductKey();
//      String uniqueId = productKey + ":" + deviceId;
//      if (!tcpConnectionManager.isDeviceRegistered(uniqueId)) {
//        tcpConnectionManager.registerDevice(productKey, deviceId,
// message.getChannelContext().channel());
//        log.debug("[{}] 设备首次识别，注册连接: productKey={}, deviceId={}, channel={}", getName(),
// productKey,
//            deviceId, message.getChannelContext().channel().id());
//      } else {
//        log.debug("[{}] 设备已注册，跳过重复注册: productKey={}, deviceId={}", getName(), productKey,
// deviceId);
//      }
//    }
//    try {
//      // 获取设备标识提取器
//      DeviceIdentityExtractor extractor = getDeviceExtractor(message);
//      if (extractor == null) {
//        log.warn("[{}] 未找到合适的设备标识提取器: messageId={}", getName(), message.getRequestId());
//        message.setError("未找到合适的设备标识提取器");
//        return ProcessorResult.ERROR;
//      }
//
//      log.debug("[{}] 开始提取设备标识: messageId={}, extractor={}, decodedData={}", getName(),
// message.getRequestId(),
//          extractor.getType(), message.getDecodedData());
//
//      // 执行设备标识提取
//      DeviceIdentityExtractor.DeviceIdentity identity =
// extractor.extract(message.getDecodedData(), message);
//
//      if (identity == null || !identity.isValid()) {
//        log.warn("[{}] 设备标识提取失败: messageId={}, identity={}", getName(), message.getRequestId(),
// identity);
//        message.setError("设备标识提取失败");
//        return ProcessorResult.ERROR;
//      }
//
//      // 设置提取结果
//      message.setProductKey(identity.getProductKey());
//      message.setDeviceId(identity.getDeviceId());
//      message.setExtDeviceId(identity.getExtDeviceId());
//      message.setIotId(identity.getIotId());
//      message.setStage(TcpUPRequest.ProcessingStage.DEVICE_IDENTIFIED);
//
//      // 缓存提取器信息到上下文
//      message.setContextValue("extractorType", extractor.getType());
//      message.setContextValue("extractorName", extractor.getName());
//
//
//
//      log.info("[{}] 设备标识提取成功: messageId={}, productKey={}, deviceId={}, extDeviceId={}",
// getName(),
//          message.getRequestId(), identity.getProductKey(), identity.getDeviceId(),
// identity.getExtDeviceId());
//
//      return ProcessorResult.CONTINUE;
//
//    } catch (Exception e) {
//      log.error("[{}] 设备标识提取异常: messageId={}, error={}", getName(), message.getRequestId(),
// e.getMessage(), e);
//      message.setError("设备标识提取异常: " + e.getMessage());
//      return ProcessorResult.ERROR;
//    }
//  }
//
//  @Override
//  public boolean supports(TcpUPRequest message) {
//    // 支持所有已解码但未提取设备标识的消息
/// /    return message.getDecodedData() != null && !message.isDeviceIdentified();
//    return true;
//  }
//
//  /**
//   * 获取设备标识提取器
//   */
//  private DeviceIdentityExtractor getDeviceExtractor(TcpUPRequest message) {
//    // 1. 优先从产品配置获取指定的提取器类型
//    if (message.getProductConfig() != null && message.getProductConfig().getDeviceExtractorType()
// != null) {
//      String extractorType = message.getProductConfig().getDeviceExtractorType();
//      DeviceIdentityExtractor extractor = extractorRegistry.getExtractor(extractorType);
//      if (extractor != null) {
//        log.debug("[{}] 使用产品配置的提取器: {}", getName(), extractorType);
//        return extractor;
//      }
//    }
//
//    // 2. 从上下文获取提取器类型
//    String contextExtractorType = message.getContextValue("deviceExtractorType");
//    if (contextExtractorType != null) {
//      DeviceIdentityExtractor extractor = extractorRegistry.getExtractor(contextExtractorType);
//      if (extractor != null) {
//        log.debug("[{}] 使用上下文指定的提取器: {}", getName(), contextExtractorType);
//        return extractor;
//      }
//    }
//
//    // 3. 尝试自动选择合适的提取器
//    DeviceIdentityExtractor autoExtractor =
// extractorRegistry.findBestExtractor(message.getDecodedData());
//    if (autoExtractor != null) {
//      log.debug("[{}] 自动选择提取器: {}", getName(), autoExtractor.getType());
//      return autoExtractor;
//    }
//
//    // 4. 使用默认提取器
//    DeviceIdentityExtractor defaultExtractor = extractorRegistry.getDefaultExtractor();
//    if (defaultExtractor != null) {
//      log.debug("[{}] 使用默认提取器: {}", getName(), defaultExtractor.getType());
//      return defaultExtractor;
//    }
//
//    return null;
//  }
/// / /  @Override /  public void afterProcess(TcpUPRequest message, ProcessorResult result) { /
/// // 记录提取统计信息 /    if (result == ProcessorResult.CONTINUE && message.isDeviceIdentified()) { /
///  message.setContextValue("deviceExtractionSuccess", true); /
/// message.setContextValue("deviceUniqueId", message.getDeviceUniqueId()); /    } else { /
/// message.setContextValue("deviceExtractionSuccess", false); /    } /  }
// }
