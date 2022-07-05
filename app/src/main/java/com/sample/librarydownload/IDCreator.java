package com.sample.librarydownload;

import com.sample.library_download.DownloadTask;
import com.sample.library_download.DownloadTaskIDCreator;

public class IDCreator implements DownloadTaskIDCreator {

	@Override
	public String createId(DownloadTask task) {
		return task.getUrl();
	}

}
