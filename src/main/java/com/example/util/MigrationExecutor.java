package com.example.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/** Scripts internes de DDL H2 : commandes simples separees par un point-virgule. */
public class MigrationExecutor {
    private final String url, user, password;
    public MigrationExecutor(String url, String user, String password) {
        this.url=url; this.user=user; this.password=password;
    }
    public void migrate() throws Exception {
        executeMigration("db/migration/v1_0_initial.sql");
        executeMigration("db/migration/v2_0_add_description.sql");
    }
    public void executeMigration(String path) throws Exception {
        try (InputStream in=getClass().getClassLoader().getResourceAsStream(path)) {
            if (in==null) throw new FileNotFoundException(path);
            String script=new String(in.readAllBytes(), StandardCharsets.UTF_8);
            try(Connection c=DriverManager.getConnection(url,user,password); Statement s=c.createStatement()) {
                s.execute("CREATE TABLE IF NOT EXISTS schema_history (script VARCHAR(255) PRIMARY KEY, applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM schema_history WHERE script=?")) {
                    p.setString(1,path);
                    try(ResultSet r=p.executeQuery()) { r.next(); if(r.getInt(1)>0) return; }
                }
                // H2 valide implicitement les DDL : pas de promesse de rollback atomique.
                for(String sql:script.replaceAll("(?m)--.*$", "").split(";")) {
                    if(!sql.trim().isEmpty()) s.execute(sql.trim());
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO schema_history(script) VALUES (?)")) {
                    p.setString(1,path); p.executeUpdate();
                }
            }
        }
    }
}
