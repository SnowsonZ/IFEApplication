package com.fairlink.common;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoManager {

	private static final int MAX_SCREEN_HEIGHT = 1280;
	private static final int MAX_SCREEN_WIDTH = 1280;
	private Logger logger = new Logger(this, "");

	public interface PhotoDownloadCallback {
		public void onPhotoDownload(String url, String path);

		public void onPhotoDownloadError(String url, String path);
	}

	public static interface PhotoDecodedCallback {
		public void onPhotoDecoded(String path, Bitmap bitmap);
	}

	private static final int MAX_CACHE_SIZE = 500 * 1024 * 1024;
	private static final int FREE_SPACE_SIZE = 20 * 1024 * 1024;
	private static PhotoManager sInstance;
	private File mPhotoDir;
	private Map<String, DownloadTask> urlAndDownloadTaskMap = new HashMap<String, DownloadTask>();
	private ExecutorService mPool = Executors.newFixedThreadPool(4);
	private HashMap<String, String> mPhotoCache = new HashMap<String, String>();
	private HashMap<String, SoftReference<Bitmap>> mPhotoBitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	private PhotoDBManager manager;
	private int mCurrentCacheSize = 0;
	private static final Handler mHandler = new Handler();

	private PhotoManager() {
	}

	private class DownloadTask implements Runnable {
		public DownloadTask(String url, PhotoDownloadCallback callback) {
			this.callbackList.add(callback);
			this.downloadurl = url;
		}

		public void addCallBack(PhotoDownloadCallback callback) {
			this.callbackList.add(callback);
		}

		String downloadurl;
		String path;
		List<PhotoDownloadCallback> callbackList = new ArrayList<PhotoDownloadCallback>();

		@SuppressLint("NewApi")
		@Override
		public void run() {
			URL url;
			InputStream is = null;
			FileOutputStream out = null;
			HttpURLConnection connection = null;
			String fileName = downloadurl.hashCode() + ".png";
			File image = new File(mPhotoDir, fileName);

			try {
				url = new URL(downloadurl);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(50000);
				connection.setReadTimeout(50000);
				connection.setDoInput(true);
				connection.setDoOutput(true);
				int code = connection.getResponseCode();
				if (code != HttpURLConnection.HTTP_OK) {
					logger.error("get error response code " + code + " when download image at " + url);
					notifyError();
				}

				path = image.getAbsolutePath();

				if (image.exists())
					image.delete();

				image.createNewFile();
				is = connection.getInputStream();
				out = new FileOutputStream(image);

				byte[] bytes = new byte[1024 * 1024];
				int size;

				logger.debug("start download image at " + url);
				while ((size = is.read(bytes)) != -1) {
					out.write(bytes, 0, size);
				}

				if (mCurrentCacheSize + image.length() > MAX_CACHE_SIZE || mPhotoDir.getFreeSpace() <= FREE_SPACE_SIZE) {
					cleanSomeCache();
				}

				saveCacheFile(downloadurl, path);

				logger.debug("download image at " + url + " end. current size " + mCurrentCacheSize);
				notifySuccess();

			} catch (MalformedURLException e) {
				logger.error("download image at [" + downloadurl + "] failed with MalformedURLException: "
						+ e.getMessage());
				if (image.exists())
					image.delete();
				notifyError();

				return;
			} catch (IOException e) {
				logger.error("download image at [" + downloadurl + "] failed with IOException: " + e.getMessage());
				if (image.exists())
					image.delete();
				notifyError();

				return;
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						logger.error("close FileOutputStream failed with error: " + e.getMessage());
					}
				}

				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						logger.error("close InputStream failed with error: " + e.getMessage());
					}
				}

				if (connection != null) {
					connection.disconnect();
				}

				urlAndDownloadTaskMap.remove(downloadurl);
			}
		}

		private void notifySuccess() {
			if (!callbackList.isEmpty()) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						for (PhotoDownloadCallback callback : callbackList) {
							callback.onPhotoDownload(downloadurl, path);
						}
					}
				});
			}
		}

		private void notifyError() {
			if (!callbackList.isEmpty()) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						for (PhotoDownloadCallback callback : callbackList) {
							callback.onPhotoDownloadError(downloadurl, path);
						}
					}
				});
			}
		}
	}

	public void init() {
		mPhotoDir = new File(Environment.getExternalStorageDirectory(), "IFE/photocache");
		if (!mPhotoDir.exists()) {
			mPhotoDir.mkdirs();
		}

		manager = new PhotoDBManager(GlobalStorage.getInstance().getBaseContext());

		loadCachePhoto();

		logger.info("init finished. " + mPhotoCache.size() + " cache images. " + mCurrentCacheSize + " size");
	}

	private synchronized void saveCacheFile(String key, String value) {
		mPhotoCache.put(key, value);
		manager.addPictureItem(key, value);

	}

	private synchronized void cleanSomeCache() {
		Set<Entry<String, String>> entrySet = mPhotoCache.entrySet();
		Iterator<Entry<String, String>> it = entrySet.iterator();
		int size = entrySet.size();
		int i = 0;
		String path;
		File file;
		Entry<String, String> entry;
		while (it.hasNext() && i < size / 2) {
			entry = it.next();
			path = entry.getValue();
			it.remove();
			manager.deletePictureItem(entry.getKey());
			file = new File(path);
			mCurrentCacheSize -= file.length();
			file.delete();
			i++;
		}
	}

	private void loadCachePhoto() {
		HashMap<String, String> cachefile = manager.queryCacheFile();
		Set<Entry<String, String>> entrySet = cachefile.entrySet();
		Iterator<Entry<String, String>> it = entrySet.iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			File file = new File(entry.getValue());
			if (file.exists()) {
				mCurrentCacheSize += file.length();
				mPhotoCache.put(entry.getKey(), entry.getValue());
			} else {
				logger.error("delete invalid image [" + entry.getKey() + "] in db");
				manager.deletePictureItem(entry.getKey());
			}
		}
	}

	public static synchronized PhotoManager getInstance() {
		if (sInstance == null) {
			sInstance = new PhotoManager();
		}
		return sInstance;
	}

	public synchronized void downloadImage(String url, PhotoDownloadCallback callback) {

		DownloadTask downloadTask = urlAndDownloadTaskMap.get(url);
		if (downloadTask == null) {
			urlAndDownloadTaskMap.put(url, new DownloadTask(url, callback));
			mPool.submit(urlAndDownloadTaskMap.get(url));
		} else {
			logger.debug("find duplicate download request for " + url);
			downloadTask.addCallBack(callback);
		}
	}

	public String getImageFile(String url) {

		String cacheImagePath = mPhotoCache.get(url);
		if (cacheImagePath == null)
			return null;

		File image = new File(cacheImagePath);
		if (image.exists()) {
			logger.debug("find cache image for " + url);
			return cacheImagePath;
		} else {
			logger.error("cache image file [" + cacheImagePath + "] from url [" + url
					+ "] not exist, remove it from cache list");
			mPhotoCache.remove(cacheImagePath);
			manager.deletePictureItem(cacheImagePath);
			return null;
		}
	}

	public static void decodePhotoAsync(String imagePath, int reqwidth, int reqheight, PhotoDecodedCallback callback) {
		SoftReference<Bitmap> ref = sInstance.mPhotoBitmapCache.get(imagePath);
		if (ref != null && ref.get() != null) {
		    callback.onPhotoDecoded(imagePath, ref.get());
		} else {
		    new DecodeTask(imagePath, reqwidth, reqheight, callback).execute((String) null);
		}
	}

	private static Bitmap decodePhotoFromFile(String imagePath, int reqwidth, int reqheight) {
		if (reqwidth > MAX_SCREEN_WIDTH)
			reqwidth = MAX_SCREEN_WIDTH;

		if (reqheight > MAX_SCREEN_HEIGHT)
			reqheight = MAX_SCREEN_HEIGHT;

		Bitmap bitmap;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);
		options.inSampleSize = calculateInSampleSize(options, reqwidth, reqheight);

		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		bitmap = BitmapFactory.decodeFile(imagePath, options);
		return bitmap;
	}

	public Bitmap decodePhoto(String imagePath, int reqwidth, int reqheight) {
		Bitmap bitmap;
		SoftReference<Bitmap> ref = mPhotoBitmapCache.get(imagePath);
		if (ref != null) {
			bitmap = ref.get();
			if (bitmap != null) {
				return bitmap;
			}
		}

		bitmap = decodePhotoFromFile(imagePath, reqwidth, reqheight);
		if (bitmap != null) {
			ref = new SoftReference<Bitmap>(bitmap);
			sInstance.mPhotoBitmapCache.put(imagePath, ref);
		}
		return bitmap;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			int widthRatio = Math.round((float) width / (float) reqWidth);
			int heightRatio = Math.round((float) height / (float) reqHeight);
			if (widthRatio > heightRatio) {
				inSampleSize = widthRatio;
			} else {
				inSampleSize = heightRatio;
			}
			if (reqWidth != -1 && reqHeight != -1) {
				final float totalPixels = width * height;

				final float totalReqPixelsCap = reqWidth * reqHeight * 2;

				while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
					inSampleSize++;
				}
			}
		}
		return inSampleSize;
	}

	static class DecodeTask extends AsyncTask<String, Integer, Bitmap> {

		String path;
		PhotoDecodedCallback callback;
		int width;
		int height;

		public DecodeTask(String path, int width, int height, PhotoDecodedCallback callback) {
			this.path = path;
			this.callback = callback;
			this.width = width;
			this.height = height;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			return decodePhotoFromFile(path, width, height);
		}

		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				SoftReference<Bitmap> ref = new SoftReference<Bitmap>(bitmap);
				sInstance.mPhotoBitmapCache.put(path, ref);
			}
			if (callback != null) {
				callback.onPhotoDecoded(path, bitmap);
			}
		}
	}
}
