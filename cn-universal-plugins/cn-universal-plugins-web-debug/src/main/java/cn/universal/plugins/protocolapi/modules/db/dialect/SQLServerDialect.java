package cn.universal.plugins.protocolapi.modules.db.dialect;

import cn.universal.plugins.protocolapi.modules.db.BoundSql;

/**
 * SQL Server 方言
 *
 * @author mxd
 */
public class SQLServerDialect implements Dialect {

  @Override
  public boolean match(String jdbcUrl) {
    return jdbcUrl.contains(":sqlserver2012:");
  }

  @Override
  public String getPageSql(String sql, BoundSql boundSql, long offset, long limit) {
    boundSql.addParameter(offset);
    boundSql.addParameter(limit);
    return sql + "\n OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
  }
}
