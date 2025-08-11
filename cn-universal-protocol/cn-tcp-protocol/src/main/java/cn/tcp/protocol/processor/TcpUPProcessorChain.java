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

package cn.tcp.protocol.processor;

import cn.universal.core.iot.constant.IotConstant;
import cn.universal.dm.device.service.plugin.ProcessorExecutor;
import cn.tcp.protocol.entity.TcpUPRequest;
import cn.tcp.protocol.processor.TcpUPMessageProcessor.ProcessorResult;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TCP 处理器链
 *
 * <p>使用通用的ProcessorExecutor执行处理器逻辑 专注于TCP业务逻辑，通用逻辑由ProcessorExecutor处理
 *
 * @version 2.0 @Author Aleo
 * @since 2025/01/20
 */
@Slf4j(topic = "tcp")
@Component
public class TcpUPProcessorChain {

  @Autowired
  private ProcessorExecutor processorExecutor;

  @Autowired
  private List<TcpUPMessageProcessor> processors;

  /**
   * 处理TCP消息
   *
   * @param request TCP上行请求
   * @return 处理结果
   */
  public boolean process(TcpUPRequest request) {
    if (request == null) {
      log.warn("[TCP_V2_UP] 请求为空，跳过处理");
      return false;
    }
    MDC.put(IotConstant.TRACE_ID, request.getRequestId());
    log.debug(
        "[TCP_V2_UP] 开始处理TCP消息，设备: {}, 请求ID: {}", request.getDeviceId(),
        request.getRequestId());

    // 使用ProcessorExecutor执行处理器链
    boolean success =
        processorExecutor.executeChain(
            processors,
            "TCP_V2_UP",
            processor -> {
              try {
                // 执行前置检查
                if (!processor.preCheck(request)) {
                  log.debug("[TCP_V2_UP] 处理器 {} 预检查失败", processor.getName());
                  return null; // 返回null表示跳过
                }

                // 执行处理器
                ProcessorResult result = processor.process(request);
                // 执行后置处理
                processor.postProcess(request, result);

                return result;
              } catch (Exception e) {
                // 调用处理器的异常处理方法
                processor.onError(request, e);
                log.error("[TCP_V2_UP] 处理器 {} 执行异常: ", processor.getName(), e);
                return null;
              }
            },
            result -> result != null && ProcessorResult.CONTINUE.equals(result), // 成功检查：结果不为null且成功
            processor -> processor.supports(request) // 支持性检查
        );

    log.debug("[TCP_V2_UP] TCP消息处理完成，设备: {}, 成功: {}", request.getDeviceId(), success);
    MDC.clear();
    return success;
  }

  /**
   * 批量处理TCP消息
   *
   * @param requests TCP上行请求列表
   * @return 成功处理的数量
   */
  public int processBatch(List<TcpUPRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      log.warn("[TCP_V2_UP] 批量请求列表为空，跳过处理");
      return 0;
    }

    log.debug("[TCP_V2_UP] 开始批量处理TCP消息，请求数量: {}", requests.size());

    int successCount = 0;
    for (TcpUPRequest request : requests) {
      try {
        if (process(request)) {
          successCount++;
        }
      } catch (Exception e) {
        log.error("[TCP_V2_UP] 批量处理异常，请求ID: {}, 异常: ", request.getRequestId(), e);
      }
    }

    log.debug("[TCP_V2_UP] 批量处理完成，总数: {}, 成功: {}", requests.size(), successCount);
    return successCount;
  }

  /**
   * 获取处理器数量
   */
  public int getProcessorCount() {
    return processors.size();
  }

  /**
   * 获取处理器名称列表（用于调试）
   */
  public List<String> getProcessorNames() {
    return processorExecutor.getProcessorNames(processors);
  }

  /**
   * 检查是否有指定名称的处理器
   */
  public boolean hasProcessor(String name) {
    return processors.stream().anyMatch(p -> p.getName().equals(name));
  }

  /**
   * 获取启用的处理器数量
   */
  public long getEnabledProcessorCount() {
    return processorExecutor.getEnabledProcessorCount(processors);
  }

  /**
   * 检查指定处理器是否启用
   */
  public boolean isProcessorEnabled(String name) {
    return processorExecutor.isProcessorEnabled(processors, name);
  }

  /**
   * 获取处理器链健康状态
   */
  public boolean isHealthy() {
    return processors.stream()
        .filter(TcpUPMessageProcessor::isEnabled)
        .allMatch(TcpUPMessageProcessor::isHealthy);
  }

  /**
   * 获取处理器链统计信息
   */
  public String getStatistics() {
    StringBuilder stats = new StringBuilder();
    stats.append("TCP处理器链统计信息:\n");
    stats.append("总处理器数: ").append(processors.size()).append("\n");
    stats.append("启用处理器数: ").append(getEnabledProcessorCount()).append("\n");

    processors.forEach(
        processor -> {
          stats
              .append("- ")
              .append(processor.getName())
              .append(" (order: ")
              .append(processor.getOrder())
              .append(", enabled: ")
              .append(processor.isEnabled())
              .append(", healthy: ")
              .append(processor.isHealthy())
              .append(")\n");
        });

    return stats.toString();
  }
}
