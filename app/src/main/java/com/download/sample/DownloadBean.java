package com.download.sample;

import android.content.Context;

import com.download.library.DownloadListenerAdapter;
import com.download.library.DownloadTask;

public class DownloadBean extends DownloadTask{

        public String title;
        public String imageUrl;

        public DownloadBean(String title, String imageUrl, String url) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.mUrl = url;
        }

        @Override
        public DownloadTask addHeader(String key, String value) {
            return super.addHeader(key, value);
        }

        @Override
        protected DownloadBean setDownloadListenerAdapter(DownloadListenerAdapter downloadListenerAdapter) {
            return (DownloadBean) super.setDownloadListenerAdapter(downloadListenerAdapter);
        }

        @Override
        public DownloadTask setTargetCompareMD5(String targetCompareMD5) {
            return super.setTargetCompareMD5(targetCompareMD5);
        }

        @Override
        public DownloadBean setUrl(String url) {
            return (DownloadBean) super.setUrl(url);
        }

        @Override
        public DownloadBean setContext(Context context) {
            return (DownloadBean) super.setContext(context);
        }

        @Override
        protected DownloadTask setForceDownload(boolean force) {
            return super.setForceDownload(force);
        }

        @Override
        public void setCalculateMD5(boolean calculateMD5) {
            super.setCalculateMD5(calculateMD5);
        }

        @Override
        public DownloadBean setEnableIndicator(boolean enableIndicator) {
            return (DownloadBean) super.setEnableIndicator(enableIndicator);
        }

        @Override
        public DownloadBean setRetry(int retry) {
            return (DownloadBean) super.setRetry(retry);
        }

        @Override
        public DownloadBean setQuickProgress(boolean quickProgress) {
            return (DownloadBean) super.setQuickProgress(quickProgress);
        }

        @Override
        public DownloadBean autoOpenIgnoreMD5() {
            return (DownloadBean) super.autoOpenIgnoreMD5();
        }
}
