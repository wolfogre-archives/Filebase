package com.wolfogre.filebase;

/**
 * Created by wolfogre on 9/23/16.
 */
public interface Index {

    String getConfig(String option);

    String execEigenvalue(byte[] bytes);

    boolean exists(String eigenvalue);

    void addEigenvalue(String eigenvalue, String remotePath);

    String getNewReference(String eigenvalue);

    String getRemotePath(String reference);

    void deleteReference(String reference);

    void gc();

    void close();
}
