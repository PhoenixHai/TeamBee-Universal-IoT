package cn.universal.plugins.protocolapi.core.web;

import cn.universal.plugins.protocolapi.core.exception.InvalidArgumentException;
import cn.universal.plugins.protocolapi.core.model.JsonBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * magic-api接口异常处理器
 *
 * @author mxd
 */
public interface MagicExceptionHandler {

  Logger logger = LoggerFactory.getLogger(MagicExceptionHandler.class);

  /**
   * magic-api中的接口异常处理
   *
   * @param e 异常对象
   * @return 返回json对象
   */
  @ExceptionHandler(Exception.class)
  @ResponseBody
  default Object exceptionHandler(Exception e) {
    logger.error("magic-api调用接口出错", e);
    return new JsonBean<>(-1, e.getMessage());
  }

  /**
   * magic-api中的接口异常处理
   *
   * @param e 异常对象
   * @return 返回json对象
   */
  @ExceptionHandler(InvalidArgumentException.class)
  @ResponseBody
  default Object exceptionHandler(InvalidArgumentException e) {
    return new JsonBean<>(e.getCode(), e.getMessage());
  }
}
