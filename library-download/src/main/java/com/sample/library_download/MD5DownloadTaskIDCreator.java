package com.sample.library_download;

import com.sample.library_download.util.MD5;

public class MD5DownloadTaskIDCreator implements DownloadTaskIDCreator {

	@Override
	public String createId(DownloadTask task) {
		return MD5.getMD5(task.getUrl());
	}

}
