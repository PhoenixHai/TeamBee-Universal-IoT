package cn.universal.plugins.protocolapi.modules.db.dialect;

import cn.universal.plugins.protocolapi.modules.db.BoundSql;

/**
 * PostgreSQL 方言
 *
 * @author mxd
 */
public class PostgreSQLDialect implements Dialect {

  @Override
  public boolean match(String jdbcUrl) {
    return jdbcUrl.contains(":postgresql:") || jdbcUrl.contains(":greenplum:");
  }

  @Override
  public String getPageSql(String sql, BoundSql boundSql, long offset, long limit) {
    boundSql.addParameter(limit);
    boundSql.addParameter(offset);
    return sql + "\n limit ? offset ?";
  }
}
