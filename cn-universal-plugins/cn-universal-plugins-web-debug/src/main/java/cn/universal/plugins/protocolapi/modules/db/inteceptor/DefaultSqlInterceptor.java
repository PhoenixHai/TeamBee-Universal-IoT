package cn.universal.plugins.protocolapi.modules.db.inteceptor;

import cn.universal.plugins.protocolapi.core.context.RequestEntity;
import cn.universal.plugins.protocolapi.modules.db.BoundSql;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认打印SQL实现
 *
 * @author mxd
 */
public class DefaultSqlInterceptor implements SQLInterceptor {

  public void handleLog(BoundSql boundSql, RequestEntity requestEntity) {
    Logger logger =
        LoggerFactory.getLogger(
            requestEntity == null
                ? "Unknown"
                : requestEntity.getMagicScriptContext().getScriptName());
    String parameters =
        Arrays.stream(boundSql.getParameters())
            .map(
                it -> {
                  if (it == null) {
                    return "null";
                  }
                  if (it instanceof Object[]) {
                    return "["
                        + Stream.of((Object[]) it)
                            .map(
                                x ->
                                    x == null
                                        ? "null"
                                        : (x + "(" + x.getClass().getSimpleName() + ")"))
                            .collect(Collectors.joining(", "))
                        + "]";
                  }
                  return it + "(" + it.getClass().getSimpleName() + ")";
                })
            .collect(Collectors.joining(", "));
    String dataSourceName = boundSql.getSqlModule().getDataSourceName();
    logger.info("执行SQL：{}", boundSql.getSql().trim());
    if (dataSourceName != null) {
      logger.info("数据源：{}", dataSourceName);
    }
    if (parameters.length() > 0) {
      logger.info("SQL参数：{}", parameters);
    }
  }

  @Override
  public Object postHandle(BoundSql boundSql, Object result, RequestEntity requestEntity) {
    handleLog(boundSql, requestEntity);
    return result;
  }

  @Override
  public void handleException(BoundSql boundSql, Throwable throwable, RequestEntity requestEntity) {
    handleLog(boundSql, requestEntity);
  }
}
