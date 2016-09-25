package com.wolfogre.qiniufilebase;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.wolfogre.filebase.Filebase;
import com.wolfogre.filebase.Index;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wolfogre on 9/23/16.
 */
public class QiniuFilebase extends Filebase {

    private Auth auth;
    private String bucket;
    private String domain;
    private BucketManager bucketManager;
    private UploadManager uploadManager;

    public QiniuFilebase(Index index) {
        super(index);
        bucket = index.getConfig("bucket");
        domain = index.getConfig("domain");
        auth = Auth.create(index.getConfig("accesskey"), index.getConfig("secretkey"));
        bucketManager = new BucketManager(auth);
        uploadManager = new UploadManager();
    }

    public String upload(InputStream input) {

        try {
            byte[] bytes = new byte[input.available()];
            if(input.read(bytes) != bytes.length)
                throw new RuntimeException("Read input stream error");
            String eigenvalue = index.execEigenvalue(bytes);
            if(!index.exists(eigenvalue)) {
                String remotePath = getNewRomotePaht(eigenvalue);
                uploadManager.put(bytes, remotePath, auth.uploadToken(bucket));
                index.addEigenvalue(eigenvalue, remotePath);
            }
            return index.getNewReference(eigenvalue);
        } catch (QiniuException e) {
            Response response = e.response;
            throw new RuntimeException(response.toString(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public URL download(String reference) {
        return download(reference, 5 * 60);
    }

    public URL download(String reference, int timeout) {
        try {
            return new URL(auth.privateDownloadUrl(domain + index.getRemotePath(reference), timeout));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(String reference) {
        index.deleteReference(reference);
    }

    public String getMimeType(String reference) {
        try {
            return bucketManager.stat(bucket, index.getRemotePath(reference)).mimeType;
        } catch (QiniuException e) {
            Response response = e.response;
            throw new RuntimeException(response.toString(), e);
        }
    }

    private String getNewRomotePaht(String eigenvalue) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss");
        return simpleDateFormat.format(new Date()) + "/" + eigenvalue;
    }

    public void gc() {
        // TODO
    }

    public void close() {
        UploadManager uploadManager = new UploadManager();
        String sqlitepath = index.getConfig("sqlitepath");
        index.close();
        try {
            InputStream input = new FileInputStream(sqlitepath);
            byte[] bytes = new byte[input.available()];
            if(input.read(bytes) != bytes.length)
                throw new RuntimeException("Read input stream error");
            Response response = uploadManager.put(bytes, "backup/" + getNewRomotePaht("index_backup.db"), auth.uploadToken(bucket));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
