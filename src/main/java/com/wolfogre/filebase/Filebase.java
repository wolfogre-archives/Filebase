package com.wolfogre.filebase;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by wolfogre on 9/23/16.
 */
public abstract class Filebase {
    protected Index index;

    public Filebase(Index index) {
        this.index = index;
    }

    public abstract String upload(InputStream input);

    public abstract URL download(String reference);

    public abstract URL download(String reference, int timeout);

    public abstract void remove(String reference);

    @Deprecated
    public abstract String getMimeType(String reference);

    public abstract void gc();

    public abstract void close();
}
