package com.wolfogre.qiniufilebase;

import com.wolfogre.filebase.Filebase;
import com.wolfogre.filebase.Index;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Created by wolfogre on 9/23/16.
 */
public class QiniuFilebase extends Filebase {

    public QiniuFilebase(Index index) {
        super(index);
    }

    public String upload(InputStream input) {
        return null;
    }

    public URL download(String reference) {
        return null;
    }

    public URL download(String reference, int timeout) {
        return null;
    }

    public void remove(String reference) {

    }

    public String getMimeType(String reference) {
        return null;
    }

    public void gc() {

    }

    public void close() {

    }

}
