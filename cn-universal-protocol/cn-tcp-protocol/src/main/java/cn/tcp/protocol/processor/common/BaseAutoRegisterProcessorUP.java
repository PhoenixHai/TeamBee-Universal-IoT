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
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.tcp.protocol.entity.TcpUPRequest;
import cn.tcp.protocol.processor.TcpUPMessageProcessor;
import cn.universal.core.base.R;
import cn.universal.core.iot.constant.IotConstant;
import cn.universal.core.service.IotServiceImplFactory;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * 公共自动注册处理器基类
 *
 * <p>步骤TWO：根据request.getIoTProduct().getConfiguration()的配置，当设备不在数据库，是否自动注册， 之后再是否把IoTDeviceDTO再回填
 *
 * <p>三种主题类型的公共处理逻辑： - 检查设备是否存在 - 根据产品配置决定是否自动注册 - 自动注册新设备 - 重新回填设备信息
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j(topic = "tcp")
public abstract class BaseAutoRegisterProcessorUP extends AbstratIoTService
    implements TcpUPMessageProcessor {

  @Value("${iot.register.auto.unionId:defaultUnionId}")
  private String unionId;

  @Value("${iot.register.auto.latitude:0.0}")
  private String latitude;

  @Value("${iot.register.auto.longitude:0.0}")
  private String longitude;

  @Override
  public String getName() {
    return "自动注册处理器-" + getTopicType();
  }

  @Override
  public String getDescription() {
    return "处理" + getTopicType() + "主题的设备自动注册";
  }

  @Override
  public int getOrder() {
    return 200; // 自动注册处理是第二步
  }

  @Override
  public ProcessorResult process(TcpUPRequest request) {
    try {
      log.debug(
          "[{}] 开始处理自动注册，设备: {}, 产品: {}",
          getName(),
          request.getDeviceId(),
          request.getProductKey());

      // 1. 检查设备是否存在
      if (request.getIoTDeviceDTO() != null) {
        log.debug("[{}] 设备已存在，跳过自动注册: {}", getName(), request.getDeviceId());
        return ProcessorResult.CONTINUE;
      }

      // 2. 检查产品配置是否支持自动注册
      if (!isAutoRegisterEnabled(request)) {
        log.info("[{}] 产品未开启自动注册功能，停止处理: {}", getName(), request.getProductKey());
        return ProcessorResult.STOP;
      }

      // 3. 执行自动注册
      if (!performAutoRegister(request)) {
        log.error("[{}] 自动注册失败: {}", getName(), request.getDeviceId());
        return ProcessorResult.ERROR;
      }

      // 4. 重新查询和回填设备信息
      if (!refillDeviceInfo(request)) {
        log.error("[{}] 设备信息重新回填失败: {}", getName(), request.getDeviceId());
        return ProcessorResult.ERROR;
      }

      // 5. 主题类型特定的自动注册处理
      if (!processTopicSpecificAutoRegister(request)) {
        log.error("[{}] 主题特定自动注册处理失败", getName());
        return ProcessorResult.ERROR;
      }

      log.info("[{}] 设备自动注册成功: {}", getName(), request.getDeviceId());
      return ProcessorResult.CONTINUE;

    } catch (Exception e) {
      log.error("[{}] 自动注册处理异常，设备: {}, 异常: ", getName(), request.getDeviceId(), e);
      return ProcessorResult.ERROR;
    }
  }

  @Override
  public boolean supports(TcpUPRequest request) {
    return false;
  }

  /**
   * 获取主题类型名称
   */
  protected abstract String getTopicType();

  /**
   * 增强注册请求（添加主题类型特定的数据）
   */
  protected abstract void enhanceRegisterRequest(TcpUPRequest request, JSONObject deviceData);

  /**
   * 处理主题类型特定的自动注册逻辑
   */
  protected abstract boolean processTopicSpecificAutoRegister(TcpUPRequest request);

  /**
   * 检查是否启用自动注册
   */
  protected boolean isAutoRegisterEnabled(TcpUPRequest request) {
    try {
      IoTProduct ioTProduct = request.getIoTProduct();
      if (ioTProduct == null) {
        log.warn("[{}] 产品信息为空，无法检查自动注册配置", getName());
        return false;
      }

      String configuration = ioTProduct.getConfiguration();
      if (StrUtil.isBlank(configuration)) {
        log.debug("[{}] 产品配置为空，默认不启用自动注册", getName());
        return false;
      }

      JSONObject config = JSONUtil.parseObj(configuration);
      boolean allowInsert = config.getBool(IotConstant.ALLOW_INSERT, false);

      log.debug("[{}] 产品自动注册配置: {}", getName(), allowInsert);
      return allowInsert;

    } catch (Exception e) {
      log.error("[{}] 检查自动注册配置异常: ", getName(), e);
      return false;
    }
  }

  /**
   * 执行自动注册
   */
  protected boolean performAutoRegister(TcpUPRequest request) {
    try {
      String deviceId = request.getDeviceId();
      IoTProduct ioTProduct = request.getIoTProduct();

      log.info("[{}] 开始自动注册设备: {}, 产品: {}", getName(), deviceId,
          ioTProduct.getProductKey());

      // 构建注册请求
      JSONObject downRequest = buildAutoRegisterRequest(request, deviceId, ioTProduct);

      // 调用第三方平台进行注册
      R result = IotServiceImplFactory.getIDown(ioTProduct.getThirdPlatform()).down(downRequest);

      if (result.isSuccess()) {
        log.info("[{}] 设备自动注册成功: {}", getName(), deviceId);

        // 记录注册信息到上下文
        request.setContextValue("autoRegistered", true);
        request.setContextValue("registerTime", System.currentTimeMillis());
        request.setContextValue("registerRequest", downRequest);

        return true;
      } else {
        log.error("[{}] 设备自动注册失败: {}, 错误: {}", getName(), deviceId, result.getMsg());
        request.setContextValue("registerError", result.getMsg());
        return false;
      }

    } catch (Exception e) {
      log.error("[{}] 设备自动注册异常: ", getName(), e);
      return false;
    }
  }

  /**
   * 构建自动注册请求
   */
  protected JSONObject buildAutoRegisterRequest(
      TcpUPRequest request, String deviceId, IoTProduct ioTProduct) {
    JSONObject downRequest = new JSONObject();
    downRequest.set("appUnionId", unionId);
    downRequest.set("productKey", ioTProduct.getProductKey());
    downRequest.set("deviceId", deviceId);
    downRequest.set("cmd", "dev_add");

    JSONObject deviceData = new JSONObject();
    deviceData.set("deviceName", deviceId);
    deviceData.set("imei", deviceId);
    deviceData.set("latitude", latitude);
    deviceData.set("longitude", longitude);

    // 主题类型特定的注册数据
    enhanceRegisterRequest(request, deviceData);

    downRequest.set("data", deviceData);

    log.debug("[{}] 自动注册请求构建完成: {}", getName(), downRequest);
    return downRequest;
  }

  /**
   * 重新回填设备信息
   */
  protected boolean refillDeviceInfo(TcpUPRequest request) {
    try {
      // 重新查询设备信息
      IoTDeviceDTO ioTDeviceDTO =
          lifeCycleDevInstance(
              IoTDeviceQuery.builder()
                  .deviceId(request.getDeviceId())
                  .productKey(request.getProductKey())
                  .build());

      if (ioTDeviceDTO == null) {
        log.warn("[{}] 自动注册后设备信息仍然为空: {}", getName(), request.getDeviceId());
        return false;
      }

      // 更新请求中的设备信息
      request.setIoTDeviceDTO(ioTDeviceDTO);
      request.setContextValue("deviceInfo", ioTDeviceDTO);

      log.debug("[{}] 设备信息重新回填成功: {}", getName(), request.getDeviceId());
      return true;

    } catch (Exception e) {
      log.error("[{}] 设备信息重新回填异常: ", getName(), e);
      return false;
    }
  }
}
