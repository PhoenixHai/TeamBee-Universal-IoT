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

package cn.universal.web.config;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.core.base.R;
import cn.universal.core.iot.exception.BizException;
import cn.universal.core.iot.util.DingTalkUtil;
import cn.universal.core.iot.util.ErrorUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @Author 刘利海
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 未知异常处理
   */
  @ExceptionHandler(value = Exception.class)
  @ResponseBody
  public R exceptionHandler(Exception e) {
    // 把错误信息输入到日志中
    log.error("未知异常", e);
    return R.error("未知错误");
  }

  /**
   * spring-validate处理
   *
   * @param exception 错误
   * @return 返回信息
   */
  @ExceptionHandler(value = {BindException.class, MethodArgumentNotValidException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Object bindExceptionHandle(Exception exception) {
    List<Map<String, String>> validationResultList = new ArrayList<>();
    Iterator iterator;
    if (exception instanceof BindException) {
      iterator = ((BindException) exception).getBindingResult().getFieldErrors().iterator();
    } else {
      iterator =
          ((MethodArgumentNotValidException) exception)
              .getBindingResult()
              .getFieldErrors()
              .iterator();
    }

    while (iterator.hasNext()) {
      FieldError error = (FieldError) iterator.next();
      Map<String, String> validationResult = new HashMap();
      validationResult.put("message", error.getDefaultMessage());
      validationResult.put("param", error.getField());
      validationResultList.add(validationResult);
    }
    log.error(JSONUtil.toJsonStr(validationResultList));
    return R.error(JSONUtil.toJsonStr(validationResultList));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public R handleNoMethodxception(HttpRequestMethodNotSupportedException e) {
    return R.error(e.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public R handleIllegalArgumentxception(IllegalArgumentException e) {
    return R.error(e.getMessage());
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public R handlerNoFoundException(Exception e) {
    log.error(e.getMessage(), e);
    return R.error(404, "路径不存在，请检查路径是否正确");
  }

  // @ExceptionHandler(DuplicateKeyException.class)
  // public DataRet<String> handleDuplicateKeyException(DuplicateKeyException e) {
  // log.error(e.getMessage(), e);
  // return DataRet.error("数据库中已存在该记录");
  // }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public R handleException(RuntimeException e) {
    log.error("运行异常", e);
    return R.error(e.getMessage());
  }

  @ExceptionHandler(BizException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public R handleException(BizException e) {
    log.warn("biz异常={}", ExceptionUtil.getSimpleMessage(e));
    return R.error(e.getCode(), e.getMessage());
  }

  @ExceptionHandler(value = {DataAccessException.class, CannotGetJdbcConnectionException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public R handleMysql(Exception e) {
    log.error("数据库异常，请及时处理={}", ErrorUtil.errorInfoToString(e));
    DingTalkUtil.send("数据库异常，请及时处理" + ExceptionUtil.getSimpleMessage(e));
    return R.error("数据库异常，请联系管理员");
  }

  /**
   * 处理认证异常
   */
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleAuthenticationException(
      AuthenticationException exception, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    log.warn("OAuth2 认证失败: {}", exception.getMessage());
    // 设置响应头
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // 构建错误响应
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("error", "authentication_failed");
    errorResponse.put("error_description", getErrorMessage(exception));
    errorResponse.put("timestamp", System.currentTimeMillis());
    errorResponse.put("path", request.getRequestURI());

    // 写入响应体
    response.getWriter().write(JSONUtil.toJsonStr(errorResponse));
  }

  /**
   * 处理 OAuth2 认证异常
   */
  @ExceptionHandler(OAuth2AuthenticationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public void handleOAuth2AuthenticationException(
      OAuth2AuthenticationException exception,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {

    log.warn("OAuth2 认证异常: {}", exception.getMessage());

    // 设置响应头
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // 构建错误响应
    Map<String, Object> errorResponse = new HashMap<>();
    OAuth2Error error = exception.getError();
    errorResponse.put("error", error.getErrorCode());
    errorResponse.put(
        "error_description",
        error.getDescription() != null ? error.getDescription() : error.getErrorCode());
    errorResponse.put("timestamp", System.currentTimeMillis());
    errorResponse.put("path", request.getRequestURI());

    // 写入响应体
    response.getWriter().write(JSONUtil.toJsonStr(errorResponse));
  }

  /**
   * 获取错误信息
   */
  private String getErrorMessage(AuthenticationException exception) {
    if (exception instanceof BadCredentialsException) {
      return exception.getMessage();
    }

    // 默认错误信息
    return "认证失败: " + exception.getMessage();
  }
}
