package com.wolfogre.sqliteindex;

import com.wolfogre.filebase.Index;

import java.io.IOException;
import java.io.InputStream;
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
    private String bucket;
    private Random random;

    public SqliteIndex(String sqlitePath){
        this.sqlitePath = sqlitePath;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getEigenvalue(byte[] bytes) {
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

    public String getRemotePath(String reference) {
        return null;
    }

    public void deleteReference(String reference) {

    }

    public void gc() {

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
