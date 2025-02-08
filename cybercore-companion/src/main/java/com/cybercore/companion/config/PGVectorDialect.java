package com.cybercore.companion.config;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

public class PGVectorDialect extends PostgreSQL95Dialect {
    public PGVectorDialect() {
        super();
        this.registerColumnType(Types.ARRAY, "vector");
    }
}
