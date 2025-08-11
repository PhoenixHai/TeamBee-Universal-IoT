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

import cn.universal.core.base.R;
import cn.universal.dm.device.service.plugin.ProcessorExecutor;
import cn.tcp.protocol.entity.TcpDownRequest;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HTTP下行消息处理器链
 *
 * <p>使用通用的ProcessorExecutor执行处理器逻辑 专注于HTTP下行业务逻辑，通用逻辑由ProcessorExecutor处理
 *
 * @version 2.0 @Author Aleo
 * @since 2025/01/20
 */
@Slf4j(topic = "tcp")
@Component
public class TcpDownProcessorChain {

  @Autowired(required = false)
  private ProcessorExecutor processorExecutor;

  @Autowired(required = false)
  private List<TcpDownMessageProcessor> processors;

  /**
   * 处理HTTP下行消息
   *
   * @param request HTTP下行请求
   * @return 处理结果
   */
  public R<?> process(TcpDownRequest request) {
    if (request == null) {
      log.warn("[TCP_DOWN] 请求为空，跳过处理");
      return R.error("请求为空");
    }

    log.debug("[TCP_DOWN] 开始处理下行消息，请求ID: {}", request.getRequestId());

    R<?> result =
        processorExecutor.executeChainWithResult(
            processors,
            "TCP_DOWN",
            processor -> {
              if (!processor.preCheck(request)) {
                log.debug("[TCP_DOWN] 处理器 {} 预检查失败", processor.getName());
                return null;
              }
              var r = processor.process(request);
              processor.postProcess(request, r);
              return r;
            },
            Objects::nonNull, // 成功检查
            processor -> processor.supports(request));

    log.debug("[TCP_DOWN] 下行消息处理完成，结果: {}", result);
    return result != null ? result : R.error("无处理器成功处理该请求");
  }

  /** 获取处理器数量 */
  public int getProcessorCount() {
    return processors.size();
  }

  /** 获取处理器名称列表（用于调试） */
  public List<String> getProcessorNames() {
    return processorExecutor.getProcessorNames(processors);
  }

  /** 检查是否有指定名称的处理器 */
  public boolean hasProcessor(String name) {
    return processors.stream().anyMatch(p -> p.getName().equals(name));
  }
}
