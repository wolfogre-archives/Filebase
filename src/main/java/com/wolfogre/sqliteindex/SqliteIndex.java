package com.wolfogre.sqliteindex;

import com.wolfogre.filebase.Index;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 * Created by wolfogre on 9/23/16.
 */
public class SqliteIndex implements Index {
    private String sqlitePath;
    private Connection connection;
    private Statement statement;
    private Random random;


    public SqliteIndex(String sqlitePath){
        this.sqlitePath = sqlitePath;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
            statement = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        random = new Random();
    }

    public String getConfig(String option) {
        if("sqlitepath".equals(option))
            return sqlitePath;
        return executeQuery("SELECT value FROM configuration WHERE option = '" + option + "'");
    }

    public String execEigenvalue(byte[] bytes) {
        MessageDigest mdSHA;
        try {
            mdSHA = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] result = mdSHA.digest(bytes);
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b : result){
            stringBuilder.append(Integer.toHexString((b >> 4) & 15));
            stringBuilder.append(Integer.toHexString(b & 15));
        }
        return stringBuilder.toString();
    }

    public boolean exists(String eigenvalue) {
        return executeExists("SELECT EXISTS (SELECT * FROM eigenvalue WHERE id = '" + eigenvalue + "')");
    }

    public void addEigenvalue(String eigenvalue, String remotePath) {
        executeUpdate("INSERT INTO eigenvalue(id, path) VALUES('" + eigenvalue + "','" + remotePath + "')");
    }

    public String getNewReference(String eigenvalue) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 50; ++i)
            stringBuilder.append((char)('A' + random.nextInt(26)));
        String reference = stringBuilder.toString();
        executeUpdate("INSERT INTO reference (id, eigenvalue_id) VALUES ('" + reference + "','" + eigenvalue + "')");
        return reference;
    }

    public String getRemotePath(String reference) {
        if(!executeExists("SELECT EXISTS (SELECT * FROM reference WHERE id = '" + reference + "')"))
            return null;
        return executeQuery("SELECT path FROM eigenvalue WHERE id = (SELECT eigenvalue_id FROM reference WHERE id = '" + reference +"')");
    }

    public void deleteReference(String reference) {
        if(!executeExists("SELECT EXISTS (SELECT * FROM reference WHERE id = '" + reference + "')"))
            return;
        executeUpdate("DELETE FROM reference WHERE id = '" + reference + "'");
    }

    public void gc() {
        // TODO：垃圾回收暂不实现
    }

    public void close() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int executeUpdate(String sql) {
        try {
            return statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String executeQuery(String sql) {
        try {
            return statement.executeQuery(sql).getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean executeExists(String sql) {
        try {
            return statement.executeQuery(sql).getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
