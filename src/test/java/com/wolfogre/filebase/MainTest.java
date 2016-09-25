package com.wolfogre.filebase;

import com.wolfogre.qiniufilebase.QiniuFilebase;
import com.wolfogre.sqliteindex.SqliteIndex;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by wolfogre on 9/23/16.
 */
public class MainTest {
    @Test
    public void mainTest() throws IOException {
        // 初始化
        Index index = new SqliteIndex("/home/wolfogre/IdeaProjects/Filebase/src/main/resources/filebase.db");
        Filebase filebase = new QiniuFilebase(index);

        // 用一个 HashMap 模拟数据库
        HashMap<Integer, String> database = new HashMap<Integer, String>();

        // 上传一个本地文件
        {
            InputStream inputStream = new FileInputStream("/home/wolfogre/Desktop/BingWallpaper");
            String reference = filebase.upload(inputStream);
            inputStream.close();
            // reference 是资源的句柄，不要弄丢，将其存入数据库
            database.put(1,reference);
        }

        // 上传一个已经上传过的本地文件
        {
            InputStream inputStream = new FileInputStream("/home/wolfogre/Desktop/BingWallpaper");
            String reference = filebase.upload(inputStream);
            inputStream.close();
            // 虽然上传的是同样的文件，但这个时候 reference 的值与上文得到的值是不一样的，
            // 为了节省资源和时间，云上只保存了一份资源，作为优化手段，
            // 作为用户只需要知道：
            //      上传重复的文件并不会影响性能；
            //      从观察者视角来看，一个 reference 对应一份独立的资源。
            database.put(2,reference);
        }

        // 上传一个 URL 指向的资源
        {
            InputStream inputStream = new URL("http://blog.wolfogre.com").openStream();
            String reference = filebase.upload(inputStream);
            inputStream.close();
            // reference 是资源的句柄，不要弄丢，将其存入数据库
            database.put(3,reference);
        }

        // 获得一个 reference 对应的资源的 Mime Type
        {
            String reference = database.get(1);
            String mimeType = filebase.getMimeType(reference);
            System.out.println(mimeType);
        }

        // 获得某一个 reference 对应的资源的下载地址，下载地址有效时间默认为5分钟
        {
            String reference = database.get(1);
            URL url = filebase.download(reference);
            System.out.println(url.toString());
        }

        // 获得某一个 reference 对应的资源的下载地址，将下载地址有效时间设置为 60 秒
        {
            String reference = database.get(2);
            URL url = filebase.download(reference, 60);
            System.out.println(url.toString());
        }

        // 下载某一个 reference 对应的资源存到本地文件
        {
            String reference = database.get(1);
            URL url = filebase.download(reference);
            InputStream inputStream = url.openStream();

            // 这样获得扩展名的方式并不总是正确的，如果实际需求对扩展名非常敏感，不容出错，则建议自行保存
            String expandedName = "." + filebase.getMimeType(reference).split("/")[1];

            FileOutputStream fileOutputStream = new FileOutputStream("/home/wolfogre/Desktop/output" + expandedName);
            byte[] bytes = new byte[inputStream.available()];
            if(inputStream.read(bytes) != bytes.length)
                throw new RuntimeException("Read input stream error");
            fileOutputStream.write(bytes);
            inputStream.close();
            fileOutputStream.close();
        }

        // 删除某一个 reference
        {
            String reference = database.get(1);
            database.remove(reference);
            filebase.remove(reference);
            // 这个时候如果执行 filebase.download(reference) 会返回 null
        }

        // 垃圾回收
        // TODO:考虑删除操作目前没有需求，所以垃圾回收目前还没实现，所以下列代码gc函数实际不起作用
        {
            filebase.gc();
            // 这一句事实上不起作用，因为上文里虽然删除了一个 reference
            // 但这个 reference 指向的资源仍被其他的 reference 指向

            String reference = database.get(2);
            database.remove(reference);
            filebase.remove(reference);

            filebase.gc();
            // 一个不被任何 reference 指向的资源已被删除
        }

        // 结束使用
        // QiniuFilebase 有一个安全策略，close 的时候会把 Sqlite 文件备份到云上
        // 这个策略是否合理待考验，如果 Sqlite 文件过大而 close 又过于频繁会影响性能
        filebase.close();
    }
}
