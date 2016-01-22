package org.stevej.android.propertyfinder.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapCache extends LruCache<String, Bitmap> implements ImageCache {

	public BitmapCache(int size) {
		super(size);
	}
	public BitmapCache() {
		super(getDefaultLruCacheSize());
	}

	public static int getDefaultLruCacheSize() {
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		return cacheSize;
	}

	public Bitmap getBitmap(String url) {
		return get(url);
	}

	public void putBitmap(String url, Bitmap bitmap) {
		put(url, bitmap);
	}
}
