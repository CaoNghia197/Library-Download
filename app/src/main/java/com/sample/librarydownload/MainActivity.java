package com.sample.librarydownload;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.sample.library_download.DownloadConfig;
import com.sample.library_download.DownloadListener;
import com.sample.library_download.DownloadManager;
import com.sample.library_download.DownloadTask;
import com.sample.librarydownload.databinding.ActivityMainBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DownloadListener, View.OnClickListener {
    private static final int REQUEST_CODE = 1;
    private ActivityMainBinding mBinding;
    private String[] arrayPermission = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String TAG = MainActivity.class.getName();
    private DownloadTask task;
    private boolean isPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        initView();
    }

    private void initView() {
        startCheckPermission();
        mBinding.btDownload.setOnClickListener(this);
        mBinding.btPause.setOnClickListener(this);
    }

    private void startCheckPermission() {
        boolean isPermission = false;
        for (String permission :
                arrayPermission) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermission = true;
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE);
                isPermission = false;
            }
        }
        if (isPermission){
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

            App.getDownloadMgr().init(builder.build());
        }

    }

    private String[] arrayOf(String permission) {
        return new String[]{permission};
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Showing the toast message
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onClick(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_in));
        if (v.getId() == R.id.bt_download) {
            startDownload();
        } else if (v.getId() == R.id.bt_pause) {
            pauseDownload();
        }
    }

    private void pauseDownload() {
        App.getDownloadMgr().pauseDownload(task);
    }

    private void startDownload() {
        if (!isPause) {
            App.getDownloadMgr().addDownloadTask(DataDownload.getDownloadTask(), this);
        } else {
            App.getDownloadMgr().resumeDownload(task);
        }
    }

    private void install(String path) {
        App.getDownloadMgr().install(path, this);
    }

    @Override
    public void onDownloadStart(DownloadTask task) {
        Log.d(TAG, "onDownloadStart: " + task.toString());
    }

    @Override
    public void onDownloadUpdated(DownloadTask task, long finishedSize, long trafficSpeed) {
        this.task = task;
        Log.d(TAG, "onDownloadUpdated : " + task.getDownloadSavePath());
        Log.d(TAG, "onDownloadUpdated : " + task.getId());
        Log.d(TAG, "onDownloadUpdated : " + task.getMimeType());
        Log.d(TAG, "onDownloadUpdated : " + task.getStatus());
        Log.d(TAG, "onDownloadUpdated : " + task.getDownloadTotalSize());
        Log.d(TAG, "onDownloadUpdated : " + task.getDownloadFinishedSize());
        long total = task.getDownloadTotalSize();
        Log.d(TAG, "onDownloadUpdated : " + task.getDownloadFinishedSize() * 100 / total);
//        Log.d(TAG, "onDownloadUpdated : finishedSize: " + finishedSize);
//        Log.d(TAG, "onDownloadUpdated : trafficSpeed: " + trafficSpeed);
        String tvDownloadSize = task.getDownloadFinishedSize() + " kb" + " / " + task.getDownloadTotalSize() + " kb";
        mBinding.tvSizeDownload.setText(tvDownloadSize);
        mBinding.progressbar.setProgress((int) (task.getDownloadFinishedSize() * 100 / total));
        mBinding.tvProgress.setText(task.getDownloadFinishedSize() * 100 / total + " %");
    }

    @Override
    public void onDownloadPaused(DownloadTask task) {
        Log.d(TAG, "onDownloadPaused: " + task.toString());
        isPause = true;
    }

    @Override
    public void onDownloadResumed(DownloadTask task) {
        Log.d(TAG, "onDownloadResumed: " + task.toString());
    }

    @Override
    public void onDownloadSuccessed(DownloadTask task) {
        Log.d(TAG, "onDownloadSuccessed: " + task.toString());
        install(task.getDownloadSavePath());
    }

    @Override
    public void onDownloadCanceled(DownloadTask task) {
        Log.d(TAG, "onDownloadCanceled: " + task.toString());
    }

    @Override
    public void onDownloadFailed(DownloadTask task) {
        Log.d(TAG, "onDownloadFailed: " + task.toString());
    }

    @Override
    public void onDownloadRetry(DownloadTask task) {
        Log.d(TAG, "onDownloadRetry: " + task.toString());
    }

}