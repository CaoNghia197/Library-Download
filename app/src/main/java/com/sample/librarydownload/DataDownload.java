package com.sample.librarydownload;

import android.os.Environment;

import com.sample.library_download.DownloadTask;

public class DataDownload {
    private static DownloadTask downloadTask;
    private static String URL_DOWNLOAD = "https://dl.vgplay.vn/app/57-thanhvankiem3d-20220215.apk";
    private static String APP_NAME = "Thanh Vân Kiếm";
    private String APP_NAME_APK = "thanhvankiem.apk";
    private String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";

    public static DownloadTask getDownloadTask() {
        if (downloadTask == null) {
            downloadTask = new DownloadTask();
            downloadTask.setName(APP_NAME);
            downloadTask.setUrl(URL_DOWNLOAD);
        }
        return downloadTask;
    }
}
