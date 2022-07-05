package com.sample.librarydownload;

import android.app.Application;
import android.os.Environment;

import com.sample.library_download.DownloadConfig;
import com.sample.library_download.DownloadManager;
import com.sample.library_download.DownloadTask;
import com.sample.library_download.DownloadTaskIDCreator;
import com.sample.library_download.MD5DownloadTaskIDCreator;

import java.io.File;

public class App extends Application {

    private static DownloadManager downloadMgr;
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadMgr = DownloadManager.getInstance();
        instance = new App();
    }

    public static DownloadManager getDownloadMgr() {
        if (downloadMgr == null) {
            downloadMgr = DownloadManager.getInstance();
        }
        return downloadMgr;
    }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public void init() {
        // use default configuration
        //downloadMgr.init();

        // custom configuration
        DownloadConfig.Builder builder = new DownloadConfig.Builder(this);
        String downloadPath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "download";
        } else {
            downloadPath = Environment.getDataDirectory().getAbsolutePath() + File.separator + "data" + File.separator + getPackageName() + File.separator + "download";
        }
        File downloadFile = new File(downloadPath);
        if (!downloadFile.isDirectory() && !downloadFile.mkdirs()) {
            throw new IllegalAccessError(" cannot create download folder");
        }
        builder.setDownloadSavePath(downloadPath);
        builder.setMaxDownloadThread(3);
        builder.setDownloadTaskIDCreator(new IDCreator());

        downloadMgr.init(builder.build());
    }
}
