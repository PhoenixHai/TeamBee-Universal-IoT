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

package cn.ctaiot.protocol.service;

import cn.ctaiot.protocol.config.CTAIoTModuleInfo;
import cn.ctaiot.protocol.entity.CTAIoTDownRequest;
import cn.ctaiot.protocol.handle.CTAIoTDownHandle;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.base.R;
import cn.universal.core.iot.constant.IotConstant.DownCmd;
import cn.universal.core.service.ICodec;
import cn.universal.dm.device.service.AbstractDownService;
import cn.universal.dm.device.service.impl.IoTDeviceShadowService;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 电信下行实现类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 11:19
 */
@Service("ctaIoTDownService")
@Slf4j
public class CTAIoTDownService extends AbstractDownService<CTAIoTDownRequest> implements ICodec {

  @Resource
  private CTAIoTModuleInfo ctaIoTModuleInfo;
  @Resource
  private CTAIoTDownHandle ctAIoTDownHandle;
  @Resource
  private IoTDeviceShadowService ioTDeviceShadowService;
  @Resource
  private IoTDeviceMapper ioTDeviceMapper;

  @Override
  public String name() {
    return ctaIoTModuleInfo.getName();
  }

  @Override
  public String code() {
    return ctaIoTModuleInfo.getCode();
  }

  @Override
  public R down(String msg) {
    log.info("[CT-AIoT下行] 收到下行消息: {}", msg);
    return ctAIoTDownHandle.down(convert(msg));
  }

  @Override
  public R down(JSONObject msg) {
    log.info("[CT-AIoT下行] 收到下行消息: {}", msg);
    return ctAIoTDownHandle.down(convert(msg));
  }

  @Override
  public R downPro(String msg) {
    log.info("[CT-AIoT下行] 收到下行Pro消息: {}", msg);
    CTAIoTDownRequest ctAIoTDownRequest = JSONUtil.toBean(msg, CTAIoTDownRequest.class);
    return ctAIoTDownHandle.down(ctAIoTDownRequest);
  }

  public void down(CTAIoTDownRequest downRequest) {
    ctAIoTDownHandle.down(downRequest);
  }

  private CTAIoTDownRequest doConvert(Object request) {
    CTAIoTDownRequest value = null;
    if (request instanceof JSONObject) {
      value = JSONUtil.toBean((JSONObject) request, CTAIoTDownRequest.class);
    } else if (request instanceof String) {
      value = JSONUtil.toBean((String) request, CTAIoTDownRequest.class);
    } else {
      value = JSONUtil.toBean(JSONUtil.toJsonStr(request), CTAIoTDownRequest.class);
    }
    IoTProduct ioTProduct = getProduct(value.getProductKey());
    value.setIoTProduct(ioTProduct);
    value.getCtwingRequestData().setConfiguration(JSONUtil.parseObj(ioTProduct.getConfiguration()));
    // 功能且function对象不为空，则编解码，并复制编解码后的内容
    if (DownCmd.DEV_FUNCTION.equals(value.getCmd())
        && CollectionUtil.isNotEmpty(value.getFunction())) {
      String function =
          beforeEncode(
              value.getProductKey(), value.getDeviceId(),
              ioTProduct.getConfiguration(), JSONUtil.toJsonStr(value.getFunction()));
      String deResult = spliceDown(value.getProductKey(), function);
      // 如需调试可打开下行payload日志
      // log.info("[CT-AIoT下行][编解码] deviceId={} payload={}", value.getDeviceId(), deResult);
      value.setPayload(deResult);
    }
    return value;
  }

  @Override
  public String spliceDown(String productKey, String payload) {
    return super.spliceDown(productKey, payload);
  }

  @Override
  public String beforeEncode(String productKey, String deviceId, String config, String function) {
    JSONObject jsonObject = JSONUtil.parseObj(config);
    // 下行报文是否需要附加影子
    Boolean requireUpShadow = jsonObject.getBool("requireUpShadow", false);
    if (requireUpShadow) {
      JSONObject shadowObj = ioTDeviceShadowService.getDeviceShadowObj(productKey, deviceId);
      if (ObjectUtil.isNotNull(shadowObj)) {
        shadowObj.remove("metadata");
        Map<String, Object> functionMap = JSONUtil.parseObj(function);
        functionMap.put("shadow", shadowObj);
        return JSONUtil.toJsonStr(functionMap);
      }
    }

    return function;
  }

  @Override
  protected CTAIoTDownRequest convert(String request) {
    return doConvert(request);
  }

  private CTAIoTDownRequest convert(JSONObject request) {
    return doConvert(request);
  }
}
