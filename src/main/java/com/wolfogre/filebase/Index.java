package com.wolfogre.filebase;

import java.io.InputStream;

/**
 * Created by wolfogre on 9/23/16.
 */
public interface Index {
    String getEigenvalue(byte[] bytes);

    String getRemotePath(String reference);

    void deleteReference(String reference);

    void gc();
}
