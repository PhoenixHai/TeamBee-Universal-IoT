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

package cn.onenet.protocol.onenet.handle;

import cn.universal.dm.device.service.IoTUPPushAdapter;
import cn.onenet.protocol.onenet.entity.OnenetUPRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 移动上行消息实际处理类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/6 16:26
 */
@Slf4j
@Component
public class OnenetUPHandle extends IoTUPPushAdapter<OnenetUPRequest> {

  public String up(List<OnenetUPRequest> downRequests) {
    return doUp(downRequests);
  }
}
