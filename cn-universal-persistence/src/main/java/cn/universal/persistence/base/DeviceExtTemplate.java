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

package cn.universal.persistence.base;

import cn.universal.core.iot.message.DownRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DeviceExtTemplate {

  private final Map<String, IDeviceExt> deviceExtMap;

  public DeviceExtTemplate(List<IDeviceExt> deviceExtList) {
    deviceExtMap =
        deviceExtList.stream()
            .collect(Collectors.toMap(IDeviceExt::productKey, Function.identity()));
  }

  public void downExt(DownRequest downRequest) {
    IDeviceExt iDeviceExt = deviceExtMap.get(downRequest.getProductKey());
    if (Objects.nonNull(iDeviceExt)) {
      iDeviceExt.downExt(downRequest);
    }
  }

  public void upExt(BaseUPRequest upRequest) {
    IDeviceExt iDeviceExt = deviceExtMap.get(upRequest.getProductKey());
    if (Objects.nonNull(iDeviceExt)) {
      iDeviceExt.upExt(upRequest);
    }
  }
}
