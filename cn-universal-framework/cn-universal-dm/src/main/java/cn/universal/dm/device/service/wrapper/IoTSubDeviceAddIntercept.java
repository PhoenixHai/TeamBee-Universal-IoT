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

package cn.universal.dm.device.service.wrapper;

import cn.hutool.core.util.StrUtil;
import cn.universal.core.base.R;
import cn.universal.core.iot.constant.IotConstant.DeviceNode;
import cn.universal.core.iot.constant.IotConstant.DownCmd;
import cn.universal.core.iot.message.DownRequest;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.IotDownWrapper;
import cn.universal.persistence.entity.IoTProduct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 子设备逻辑校验 */
@Service("ioTSubDeviceAddIntercept")
@Slf4j
public class IoTSubDeviceAddIntercept implements IotDownWrapper {

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Override
  public R beforeDownAction(IoTProduct product, Object data, DownRequest downRequest) {
    /*返回的R不为空，会影响后续流程，此处只做参数的补充*/
    if (product == null) {
      return null;
    }
    if (downRequest.getCmd() == null) {
      return null;
    }
    if (DownCmd.DEV_ADD.equals(downRequest.getCmd())
        || DownCmd.DEV_ADDS.equals(downRequest.getCmd())) {
      return doGwSubDeviceAdd(product, downRequest);
    }

    return null;
  }

  private R doGwSubDeviceAdd(IoTProduct product, DownRequest downRequest) {
    if (!DeviceNode.GATEWAY_SUB_DEVICE.name().equals(product.getDeviceNode())) {
      return null;
    }
    // 如果是网关子设备，则判断是否绑定了网关
    if (StrUtil.isNotBlank(product.getGwProductKey())) {
      downRequest.setGwProductKey(product.getGwProductKey());
    } else {
      return R.error("添加网关子设备,请先绑定网关");
    }
    return null;
  }

  private void doAddAction() {}
}
