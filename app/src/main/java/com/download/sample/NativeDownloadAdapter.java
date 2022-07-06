package com.download.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.download.library.DownloadException;
import com.download.library.DownloadImpl;
import com.download.library.DownloadListenerAdapter;
import com.download.library.DownloadTask;
import com.download.library.Downloader;
import com.download.library.Extra;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.sample.librarydownload.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class NativeDownloadAdapter extends RecyclerView.Adapter<NativeDownloadAdapter.NativeDownloadViewHolder> {
    private static final String TAG = NativeDownloadAdapter.class.getName();
    private Context context;
    private ArrayList<DownloadBean> mDownloadTasks;

    public NativeDownloadAdapter(Context mContext, ArrayList<DownloadBean> mDownloadTasks) {
        this.context = mContext;
        this.mDownloadTasks = mDownloadTasks;
    }

    @NonNull
    @Override
    public NativeDownloadViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item_download, viewGroup, false);
        return new NativeDownloadViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NativeDownloadAdapter.NativeDownloadViewHolder nativeDownloadViewHolder, int position) {
        final DownloadBean downloadBean = mDownloadTasks.get(position);
        Picasso.get().load(downloadBean.imageUrl)
                .resize(100, 100)
                .centerCrop().
                transform(new NativeDownloadViewHolder.RoundTransform(context))
                .into(nativeDownloadViewHolder.mIconIv);
        nativeDownloadViewHolder.mStatusButton.setEnabled(true);
        nativeDownloadViewHolder.mStatusButton.setTag(downloadBean);
        if (downloadBean.getTotalsLength() > 0L) {
            int mProgress = (int) ((downloadBean.getLoaded()) / (float) downloadBean.getTotalsLength() * 100);
            Log.e(TAG, "mProgress:" + mProgress + " position:" + position);
            nativeDownloadViewHolder.mProgressBar.setProgress(mProgress);
            nativeDownloadViewHolder.mCurrentProgress.setText("tiến độ hiện tại" + byte2FitMemorySize(downloadBean.getLoaded()) + "/" + byte2FitMemorySize(downloadBean.getTotalsLength()) + " Thời gian :" + ((downloadBean.getUsedTime()) / 1000) + "s");
        } else {
            nativeDownloadViewHolder.mProgressBar.setProgress(0);
            nativeDownloadViewHolder.mCurrentProgress.setText("Tiến độ :" + byte2FitMemorySize(downloadBean.getLoaded()) + " Thời gian:" + ((downloadBean.getUsedTime()) / 1000) + "s");
        }
        Log.e(TAG, "status:" + downloadBean.getStatus() + " position:" + position);
        if (downloadBean.getStatus() == DownloadTask.STATUS_NEW) {
            nativeDownloadViewHolder.mStatusButton.setText("bắt đầu");
        } else if (downloadBean.getStatus() == DownloadTask.STATUS_PAUSING) {
            nativeDownloadViewHolder.mStatusButton.setText("Đang tạm dừng...");
            nativeDownloadViewHolder.mStatusButton.setEnabled(false);
        } else if (downloadBean.getStatus() == DownloadTask.STATUS_PENDDING) {
            nativeDownloadViewHolder.mStatusButton.setText("Đang chờ đợi...");
            nativeDownloadViewHolder.mStatusButton.setEnabled(false);
        } else if (downloadBean.getStatus() == DownloadTask.STATUS_PAUSED) {
            nativeDownloadViewHolder.mStatusButton.setText("tiếp tục");
        } else if (downloadBean.getStatus() == DownloadTask.STATUS_DOWNLOADING) {
            nativeDownloadViewHolder.mStatusButton.setText("tạm ngừng");
        } else if (downloadBean.getStatus() == DownloadTask.STATUS_CANCELED || downloadBean.getStatus() == DownloadTask.STATUS_ERROR) {
            nativeDownloadViewHolder.mStatusButton.setText("lỗi");
            nativeDownloadViewHolder.mStatusButton.setEnabled(false);
        } else {
            nativeDownloadViewHolder.mStatusButton.setText("hoàn thành");
            nativeDownloadViewHolder.mStatusButton.setEnabled(false);
        }
        nativeDownloadViewHolder.mStatusButton.setOnClickListener(new View.OnClickListener() {
            long lastTime = SystemClock.elapsedRealtime();

            @Override
            public void onClick(View v) {
//                    Runtime.getInstance().log(TAG, "mStatusButton onClick");
                if (SystemClock.elapsedRealtime() - lastTime <= 500) {
                    return;
                }
                lastTime = SystemClock.elapsedRealtime();
                if (downloadBean.getStatus() == DownloadTask.STATUS_NEW) {
                    nativeDownloadViewHolder.mStatusButton.setText("Đang chờ đợi ...");
                    nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                    boolean isStarted = DownloadImpl.getInstance(context).enqueue(downloadBean);
                    if (!isStarted) {
                        bindViewHolder(nativeDownloadViewHolder, position);
                    }
                } else if (downloadBean.getStatus() == DownloadTask.STATUS_PENDDING) {
                } else if (downloadBean.getStatus() == DownloadTask.STATUS_DOWNLOADING) {
                    if (TextUtils.isEmpty(downloadBean.getUrl())) {
                        nativeDownloadViewHolder.mStatusButton.setText("lỗi");
                        nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                        return;
                    }
                    DownloadTask downloadTask = DownloadImpl.getInstance(context).pause(downloadBean.getUrl());
                    if (downloadTask != null) {
                        nativeDownloadViewHolder.mStatusButton.setText("tiếp tục");
                    } else {
                        bindViewHolder(nativeDownloadViewHolder, position);
                    }
                } else if (downloadBean.getStatus() == DownloadTask.STATUS_PAUSED) {
                    boolean isStarted = DownloadImpl.getInstance(context).resume(downloadBean.getUrl());
                    nativeDownloadViewHolder.mStatusButton.setText("Đang chờ đợi...");
                    nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                    if (!isStarted) {
                        bindViewHolder(nativeDownloadViewHolder, position);
                    }
                } else if (downloadBean.getStatus() == DownloadTask.STATUS_CANCELED) {
                } else {
                    nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                    nativeDownloadViewHolder.mStatusButton.setText("hoàn thành");
                }
            }
        });
        downloadBean.setDownloadListenerAdapter(new DownloadListenerAdapter() {
            @Override
            public void onStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength, Extra extra) {
                if (nativeDownloadViewHolder.mStatusButton.getTag() != downloadBean) {
                    Log.e(TAG, "item recycle onStart");
                    return;
                }
//                    nativeDownloadViewHolder.mStatusButton.setText("暂停");
//                    nativeDownloadViewHolder.mStatusButton.setEnabled(true);
                Log.i(TAG, " isRunning:" + DownloadImpl.getInstance(context).isRunning(url));
            }

            @MainThread //回调到主线程，添加该注释
            @Override
            public void onProgress(String url, long downloaded, long length, long usedTime) {
                if (nativeDownloadViewHolder.mStatusButton.getTag() != downloadBean) {
                    Log.e(TAG, "onProgress item recycle");
                    return;
                }
                int mProgress = (int) ((downloaded) / (float) length * 100);
//                    Log.i(TAG, "onProgress:" + mProgress + " downloaded:" + downloaded + " totals:" + length + " url:" + url + " Thread:" + Thread.currentThread().getName());
                nativeDownloadViewHolder.mProgressBar.setProgress(mProgress);
                if (length <= 0) {
                    nativeDownloadViewHolder.mCurrentProgress.setText("tiến độ :" + byte2FitMemorySize(downloaded) + " thời gian:" + ((downloadBean.getUsedTime()) / 1000) + "s");
                } else {
                    nativeDownloadViewHolder.mCurrentProgress.setText( byte2FitMemorySize(downloaded) + "/" + byte2FitMemorySize(length) + " thời gian :" + ((downloadBean.getUsedTime()) / 1000) + "s");

                }
            }

            @Override
            public boolean onResult(Throwable throwable, Uri uri, String url, Extra extra) {
                if (nativeDownloadViewHolder.mStatusButton.getTag() != downloadBean) {
                    Log.e(TAG, "item recycle");
                    return super.onResult(throwable, uri, url, extra);
                }
                Log.i(TAG, "onResult isSuccess:" + (throwable == null) + " url:" + url + " Thread:" + Thread.currentThread().getName() + " uri:" + uri.toString() + " isPaused:" + DownloadImpl.getInstance(context).isPaused(url));
                nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                if (throwable == null) {
                    nativeDownloadViewHolder.mStatusButton.setText("hoàn thành");
                } else if (throwable instanceof DownloadException) {
                    DownloadException downloadException = (DownloadException) throwable;
                    if (downloadException.getCode() == Downloader.ERROR_USER_PAUSE) {
                        nativeDownloadViewHolder.mStatusButton.setText("tiếp tục");
                        nativeDownloadViewHolder.mStatusButton.setEnabled(true);
                    } else {
                        nativeDownloadViewHolder.mStatusButton.setText("lỗi");
                        nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                    }
                    Toast.makeText(context, downloadException.getMsg(), Toast.LENGTH_LONG).show();
                }
                return super.onResult(throwable, uri, url, extra);
            }

            @Override
            public void onDownloadStatusChanged(Extra extra, int status) {
                super.onDownloadStatusChanged(extra, status);
                Log.e(TAG, "onDownloadStatusChanged:" + status);
                if (nativeDownloadViewHolder.mStatusButton.getTag() != downloadBean) {
                    Log.e(TAG, "item recycle onDownloadStatusChanged");
                    return;
                }
                if (status == DownloadTask.STATUS_NEW) {
                    nativeDownloadViewHolder.mStatusButton.setText("bắt đầu");
                    nativeDownloadViewHolder.mStatusButton.setEnabled(true);

                } else if (status == DownloadTask.STATUS_PENDDING) {
                    nativeDownloadViewHolder.mStatusButton.setText("Đang chờ đợi...");
                    nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                } else if (status == DownloadTask.STATUS_PAUSING) {
                    nativeDownloadViewHolder.mStatusButton.setText("Đang tạm dừng...");
                    nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                } else if (downloadBean.getStatus() == DownloadTask.STATUS_DOWNLOADING) {
                    nativeDownloadViewHolder.mStatusButton.setText("tạm ngừng");
                    nativeDownloadViewHolder.mStatusButton.setEnabled(true);
                } else if (downloadBean.getStatus() == DownloadTask.STATUS_PAUSED) {
                    nativeDownloadViewHolder.mStatusButton.setText("tiếp tục");
                    nativeDownloadViewHolder.mStatusButton.setEnabled(true);
                }  /*else if (downloadBean.getStatus() == DownloadTask.STATUS_PAUSED) {
                        nativeDownloadViewHolder.mStatusButton.setText("继续");
                        nativeDownloadViewHolder.mStatusButton.setEnabled(true);
                    } else if (downloadBean.getStatus() == DownloadTask.STATUS_CANCELED) {
                        nativeDownloadViewHolder.mStatusButton.setText("出错");
                        nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                    } else {
                        nativeDownloadViewHolder.mStatusButton.setEnabled(false);
                        nativeDownloadViewHolder.mStatusButton.setText("已完成");
                    }*/
            }
        });

    }

    private static String byte2FitMemorySize(final long byteNum) {
        if (byteNum < 0) {
            return "";
        } else if (byteNum < 1024) {
            return String.format(Locale.getDefault(), "%.1fB", (double) byteNum);
        } else if (byteNum < 1048576) {
            return String.format(Locale.getDefault(), "%.1fKB", (double) byteNum / 1024);
        } else if (byteNum < 1073741824) {
            return String.format(Locale.getDefault(), "%.1fMB", (double) byteNum / 1048576);
        } else {
            return String.format(Locale.getDefault(), "%.1fGB", (double) byteNum / 1073741824);
        }
    }

    private void run2() {
        final long begin = SystemClock.elapsedRealtime();
        DownloadImpl.with(context)
                .url("http://shouji.360tpcdn.com/170918/93d1695d87df5a0c0002058afc0361f1/com.ss.android.article.news_636.apk")
                .enqueue(new DownloadListenerAdapter() {
                    @Override
                    public void onProgress(String url, long downloaded, long length, long usedTime) {
                        super.onProgress(url, downloaded, length, usedTime);
//                        Log.i(TAG, " progress:" + downloaded + " url:" + url);
                    }

                    @Override
                    public boolean onResult(Throwable throwable, Uri path, String url, Extra extra) {
                        Log.i(TAG, " path:" + path + " url:" + url + " length:" + new File(path.getPath()).length());
                        Log.i(TAG, " DownloadImpl time:" + (SystemClock.elapsedRealtime() - begin) + " length:" + new File(path.getPath()).length());
                        run3();
                        return super.onResult(throwable, path, url, extra);
                    }
                });
    }


    private void run3() {
        final long begin1 = SystemClock.elapsedRealtime();
        FileDownloader.getImpl()
                .create("http://shouji.360tpcdn.com/170918/93d1695d87df5a0c0002058afc0361f1/com.ss.android.article.news_636.apk")
                .setPath(context.getCacheDir().getAbsolutePath() + "/" + "test.apk")
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        Log.i(TAG, "FileDownloader progress:" + soFarBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Log.i(TAG, " FileDownloader time:" + (SystemClock.elapsedRealtime() - begin1)
                                + " length:" + new File(task.getPath()).length()
                                + "  path:" + task.getPath());
                        new File(task.getPath()).delete();
//						run2();
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }

    @Override
    public int getItemCount() {
        return mDownloadTasks.size();
    }

    protected static class NativeDownloadViewHolder extends RecyclerView.ViewHolder {
        ProgressBar mProgressBar;
        Button mStatusButton;
        ImageView mIconIv;
        private final TextView mCurrentProgress;

        public NativeDownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            mIconIv = itemView.findViewById(R.id.icon_iv);
            mStatusButton = itemView.findViewById(R.id.start_button);
            mProgressBar = itemView.findViewById(R.id.progressBar);
            mProgressBar.setMax(100);
            mCurrentProgress = itemView.findViewById(R.id.current_progress);
        }

        public static class RoundTransform implements Transformation {

            private final Context mContext;

            public RoundTransform(Context context) {
                mContext = context;
            }

            @Override
            public Bitmap transform(Bitmap source) {

                int widthLight = source.getWidth();
                int heightLight = source.getHeight();
                int radius = dp2px(mContext, 8); // 圆角半径

                Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(output);
                Paint paintColor = new Paint();
                paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);

                RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));

                canvas.drawRoundRect(rectF, radius, radius, paintColor);
//        canvas.drawRoundRect(rectF, widthLight / 5, heightLight / 5, paintColor);

                Paint paintImage = new Paint();
                paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                canvas.drawBitmap(source, 0, 0, paintImage);
                source.recycle();
                return output;
            }

            public static int dp2px(Context context, float dpValue) {
                final float scale = context.getResources().getDisplayMetrics().density;
                return (int) (dpValue * scale + 0.5f);
            }

            @Override
            public String key() {
                return "roundworm";
            }

        }
    }

}
