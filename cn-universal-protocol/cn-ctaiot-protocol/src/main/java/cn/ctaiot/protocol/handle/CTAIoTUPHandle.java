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

package cn.ctaiot.protocol.handle;

import cn.ctaiot.protocol.entity.CTAIoTUPRequest;
import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.dm.device.service.IoTUPPushAdapter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 上行消息实际处理类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/6 16:26
 */
@Component
@Slf4j
public class CTAIoTUPHandle extends IoTUPPushAdapter<CTAIoTUPRequest> {

  public String up(List<CTAIoTUPRequest> downRequests) {
    return doUp(downRequests);
  }

  /**
   * 推送前扩展：消息转换、规则引擎等处理
   *
   * @param upRequests 上行请求列表
   */
  @Override
  protected void onBeforePush(List<CTAIoTUPRequest> upRequests) {
    log.info("[CT-AIoT上行][推送前处理] 开始处理 {} 条消息", upRequests.size());

    // 示例1：消息转换 - 统一格式转换
    upRequests.forEach(
        request -> {
          // 可以在这里进行消息格式转换
          // 比如添加平台标识、时间戳格式化等
          log.debug("[CT-AIoT上行][消息转换] 处理消息: {}", request.getIotId());
        });

    // 示例2：规则引擎 - 消息过滤
    upRequests.removeIf(
        request -> {
          // 过滤掉不符合规则的消息
          boolean shouldFilter =
              request.getIoTDeviceDTO() == null || request.getIoTDeviceDTO().isAppDisable();
          if (shouldFilter) {
            log.info("[CT-AIoT上行][规则过滤] 过滤消息: {}", request.getIotId());
          }
          return shouldFilter;
        });

    log.info("[CT-AIoT上行][推送前处理] 处理完成，剩余 {} 条消息", upRequests.size());
  }

  /**
   * 推送后扩展：结果处理、日志记录等
   *
   * @param upRequests  上行请求列表
   * @param pushResults 推送结果列表
   */
  @Override
  protected void onAfterPush(List<CTAIoTUPRequest> upRequests, List<IoTPushResult> pushResults) {
    log.info(
        "[CT-AIoT上行][推送后处理] 推送完成，消息数量: {}, 结果数量: {}",
        upRequests.size(),
        pushResults != null ? pushResults.size() : 0);

    // 示例1：推送结果统计
    if (pushResults != null) {
      pushResults.forEach(
          result -> {
            log.debug(
                "[CT-AIoT上行][推送统计] 设备 {} 渠道 {} 推送结果: {}",
                result.getDeviceId(),
                result.getChannel(),
                result.isOk() ? "成功" : "失败");
          });
    }

    // 示例2：异步处理 - 可以在这里触发其他业务逻辑
    // 比如数据同步、事件通知等
    if (pushResults != null) {
      long successCount = pushResults.stream().filter(IoTPushResult::isOk).count();
      long totalCount = pushResults.size();
      double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
      log.info("[CT-AIoT上行][性能统计] 推送成功率: {}/{} ({:.2f}%)", successCount, totalCount,
          successRate);
    }
  }
}
