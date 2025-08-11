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

package cn.onenet.protocol.onenet.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.onenet.protocol.config.OneNetModuleInfo;
import cn.onenet.protocol.onenet.entity.OnenetDownRequest;
import cn.onenet.protocol.onenet.handle.OnenetDownHandle;
import cn.universal.core.base.R;
import cn.universal.core.iot.constant.IotConstant.DownCmd;
import cn.universal.core.service.ICodec;
import cn.universal.dm.device.service.AbstractDownService;
import cn.universal.persistence.entity.IoTProduct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 移动下行实现类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 11:19
 */
@Service("onenetDownService")
@Slf4j
public class OnenetDownService extends AbstractDownService<OnenetDownRequest> implements ICodec {

  @Resource
  private OnenetDownHandle onenetDownHandle;
  @Resource
  private OneNetModuleInfo oneNetModuleInfo;

  @Override
  public String code() {
    return oneNetModuleInfo.getCode();
  }

  @Override
  public String name() {
    return oneNetModuleInfo.getName();
  }

  @Override
  public R down(String msg) {
    log.info("onenet down msg={}", msg);
    return onenetDownHandle.down(convert(msg));
  }

  @Override
  public R down(JSONObject msg) {
    log.info("onenet down msg={}", msg);
    return onenetDownHandle.down(convert(msg));
  }

  @Override
  public R downPro(String msg) {
    return null;
  }

  private OnenetDownRequest doConvert(Object request) {
    OnenetDownRequest value = null;
    if (request instanceof JSONObject) {
      value = JSONUtil.toBean((JSONObject) request, OnenetDownRequest.class);
    } else if (request instanceof String) {
      value = JSONUtil.toBean((String) request, OnenetDownRequest.class);
    } else {
      value = JSONUtil.toBean(JSONUtil.toJsonStr(request), OnenetDownRequest.class);
    }
    IoTProduct ioTProduct = getProduct(value.getProductKey());
    value.setIoTProduct(ioTProduct);
    value.getOnenetRequestData().setConfiguration(JSONUtil.parseObj(ioTProduct.getConfiguration()));
    // 功能且function对象不为空，则编解码，并复制编解码后的内容
    if (DownCmd.DEV_FUNCTION.equals(value.getCmd())
        && CollectionUtil.isNotEmpty(value.getFunction())) {
      String deResult = spliceDown(value.getProductKey(), JSONUtil.toJsonStr(value.getFunction()));
      //      log.info("电信设备={} 编解码结果={}", value.getDeviceId(), deResult);
      value.setPayload(deResult);
    }
    return value;
  }

  @Override
  public String spliceDown(String productKey, String payload) {
    return super.spliceDown(productKey, payload);
  }

  @Override
  protected OnenetDownRequest convert(String request) {
    return doConvert(request);
  }

  private OnenetDownRequest convert(JSONObject request) {
    return doConvert(request);
  }
}
