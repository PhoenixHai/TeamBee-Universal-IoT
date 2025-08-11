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

package cn.tcp.protocol.processor.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.core.iot.message.UPRequest;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import cn.tcp.protocol.core.DeviceIdentityExtractorRegistry;
import cn.tcp.protocol.entity.TcpUPRequest;
import cn.tcp.protocol.manager.TcpConnectionManager;
import cn.tcp.protocol.processor.TcpUPMessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 公共设备信息处理器基类
 *
 * <p>步骤ONE：实现设备 IoTDeviceDTO 和产品IoTProduct的信息回填，确保上报的消息产品或设备是存在的
 *
 * <p>三种主题类型的公共处理逻辑： - 设备和产品信息查询和回填 - 消息基础信息提取 - 主题解析和设备标识提取
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j(topic = "tcp")
public abstract class BaseDeviceInfoProcessorUP extends AbstratIoTService
    implements TcpUPMessageProcessor {

  @Autowired
  protected DeviceIdentityExtractorRegistry extractorRegistry;

  @Autowired
  protected TcpConnectionManager tcpConnectionManager;

  @Autowired
  private ICodecService codecService;

  @Override
  public String getName() {
    return "设备信息处理器-" + getProcessorType();
  }

  @Override
  public String getDescription() {
    return "处理" + getProcessorType() + "主题的设备和产品信息回填";
  }

  @Override
  public int getOrder() {
    return 10; // 设备信息处理是第一步
  }

  /**
   * 获处理类型名称
   */
  protected abstract String getProcessorType();

  @Override
  public ProcessorResult process(TcpUPRequest message) {
    String productKey = message.getProductKey();
    if (!fillProductInfo(message)) {
      log.warn("产品不存在");
    }
    if (message.getProductConfig().getAlwaysPreDecode() && StrUtil.isBlank(message.getDeviceId())) {
      UPRequest upRequest = codecService.preDecode(productKey, message.getPayload());
      log.info("[{}] 开始deviceId 识别,执行[preDecode],返回={}", getName(),
          JSONUtil.toJsonStr(upRequest));
      if (upRequest == null || StrUtil.isBlank(upRequest.getDeviceId())) {
        log.warn("[{}] preDecode 协议不存在 or 无法识别deviceId,停止运行", getName(), productKey);
        return ProcessorResult.STOP;
      }
      message.setDeviceId(upRequest.getDeviceId());
    }
    if (!fillDeviceInfo(message)) {
      log.warn("设备不存在不存在");
    }
    String deviceId = message.getDeviceId();
    String uniqueId = productKey + ":" + deviceId;
    if (!tcpConnectionManager.isDeviceRegistered(uniqueId)) {
      tcpConnectionManager.registerDevice(
          productKey, deviceId, message.getChannelContext().channel());
      log.debug(
          "[{}] 设备首次识别，注册连接: productKey={}, deviceId={}, channel={}",
          getName(),
          productKey,
          deviceId,
          message.getChannelContext().channel().id());
    } else {
      log.debug("[{}] 设备已注册，跳过重复注册: productKey={}, deviceId={}", getName(), productKey,
          deviceId);
    }
    return ProcessorResult.CONTINUE;
  }

  /**
   * 回填产品信息
   */
  protected boolean fillProductInfo(TcpUPRequest request) {
    try {
      String productKey = request.getProductKey();
      IoTProduct ioTProduct = getProduct(productKey);

      if (ioTProduct == null) {
        log.warn("[{}] 产品不存在: {}", getName(), productKey);
        return false;
      }

      request.setIoTProduct(ioTProduct);
      request.setContextValue("productInfo", ioTProduct);

      log.debug("[{}] 产品信息回填成功: {}", getName(), productKey);
      return true;

    } catch (Exception e) {
      log.error("[{}] 产品信息回填异常: ", getName(), e);
      return false;
    }
  }

  /**
   * 回填设备信息
   */
  protected boolean fillDeviceInfo(TcpUPRequest request) {
    try {
      if (request.getIoTDeviceDTO() != null) {
        return true;
      }

      IoTDeviceDTO ioTDeviceDTO =
          lifeCycleDevInstance(
              IoTDeviceQuery.builder()
                  .deviceId(request.getDeviceId())
                  .productKey(request.getProductKey())
                  .build());

      request.setIoTDeviceDTO(ioTDeviceDTO);
      request.setContextValue("deviceInfo", ioTDeviceDTO);

      if (ioTDeviceDTO != null) {
        log.debug("[{}] 设备信息回填成功: {}", getName(), request.getDeviceId());
      } else {
        log.debug("[{}] 设备不存在，可能需要自动注册: {}", getName(), request.getDeviceId());
      }

      return true;

    } catch (Exception e) {
      log.error("[{}] 设备信息回填异常: ", getName(), e);
      return false;
    }
  }

  // ==================== 生命周期方法 ====================

  @Override
  public boolean preCheck(TcpUPRequest message) {
    // 检查必要的数据
    return message.getChannelContext() != null && message.getProductKey() != null;
  }
}
