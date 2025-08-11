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

package cn.universal.rule.express;

import cn.universal.rule.function.RuleFunctionTemplate;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import com.ql.util.express.Operator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * QLExpress文本执行 @Author Aleo
 *
 * @since 2025/8/25 14:07
 */
@Component
@Slf4j
public class ExpressTemplate {

  private static final ExpressRunner RUNNER = new ExpressRunner();

  @Resource private RuleFunctionTemplate ruleFunctionTemplate;

  @PostConstruct
  public void initFunction() {
    ruleFunctionTemplate
        .getAllFunctions()
        .forEach(
            function ->
                RUNNER.addFunction(
                    function.functionName(),
                    new Operator() {
                      @Override
                      public Object executeInner(Object[] list) {
                        return function.executeFunction(list);
                      }
                    }));
  }

  /**
   * 执行一段文本
   *
   * @param express 程序文本
   * @param context 执行上下文
   * @return Object 执行结果
   */
  public Object execute(String express, Map<String, Object> context) {
    DefaultContext<String, Object> defaultContext = new DefaultContext<>();
    defaultContext.putAll(context);
    try {
      return RUNNER.execute(express, defaultContext, null, false, false);
    } catch (Exception ignore) {

    }
    return null;
  }

  /**
   * 执行一段文本
   *
   * @param express 程序文本
   * @param context 执行上下文
   * @return Boolean 执行结果
   */
  public Boolean executeTest(String express, Map<String, Object> context) {
    return (Boolean) execute(express, context);
  }
}
