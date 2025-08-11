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

package cn.imoulife.protocol.handle;

import cn.universal.dm.device.service.IoTUPPushAdapter;
import cn.imoulife.protocol.entity.ImoulifeUPRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 乐橙上行消息实际处理类
 *
 * @version 1.0
 * @since 2025/8/24 12:13
 */
@Slf4j
@Component
public class ImoulifeUPHandle extends IoTUPPushAdapter<ImoulifeUPRequest> {

  public String up(List<ImoulifeUPRequest> downRequests) {
    return doUp(downRequests);
  }
}
