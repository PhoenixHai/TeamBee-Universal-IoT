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

package cn.imoulife.protocol.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.imoulife.protocol.config.ImouLifeModuleInfo;
import cn.imoulife.protocol.entity.ImoulifeDownRequest;
import cn.imoulife.protocol.handle.ImoulifeDownHandle;
import cn.universal.core.base.R;
import cn.universal.dm.device.service.AbstractDownService;
import cn.universal.persistence.entity.IoTProduct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 萤石云下行实现类
 *
 * @version 1.0
 * @since 2025/8/24 11:32
 */
@Service("lechenDownService")
@Slf4j
public class ImoulifeDownService extends AbstractDownService<ImoulifeDownRequest> {

  @Resource
  private ImouLifeModuleInfo imouLifeModuleInfo;
  @Resource
  private ImoulifeDownHandle imoulifeDownHandle;

  @Override
  public String code() {
    return imouLifeModuleInfo.getCode();
  }

  @Override
  public String name() {
    return imouLifeModuleInfo.getName();
  }

  @Override
  public R down(String msg) {
    log.info("imoulife down msg={}", msg);
    return imoulifeDownHandle.down(convert(msg));
  }

  @Override
  public R down(JSONObject msg) {
    log.info("imoulife down msg={}", JSONUtil.toJsonStr(msg));
    return imoulifeDownHandle.down(convert(msg));
  }

  @Override
  public R downPro(String msg) {
    return null;
  }

  private ImoulifeDownRequest doConvert(Object request) {
    ImoulifeDownRequest value = null;
    if (request instanceof JSONObject) {
      value = JSONUtil.toBean((JSONObject) request, ImoulifeDownRequest.class);
    } else if (request instanceof String) {
      value = JSONUtil.toBean((String) request, ImoulifeDownRequest.class);
    } else {
      value = JSONUtil.toBean(JSONUtil.toJsonStr(request), ImoulifeDownRequest.class);
    }
    IoTProduct ioTProduct = getProduct(value.getProductKey());
    value.setIoTProduct(ioTProduct);
    value
        .getImoulifeRequestData()
        .setConfiguration(JSONUtil.parseObj(ioTProduct.getConfiguration()));
    return value;
  }

  @Override
  protected ImoulifeDownRequest convert(String request) {
    return doConvert(request);
  }

  private ImoulifeDownRequest convert(JSONObject request) {
    return doConvert(request);
  }
}
