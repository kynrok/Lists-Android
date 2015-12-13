package pnixx.lists.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * User: P.Nixx
 * Date: 02.11.12
 * Time: 13:47
 */
public class DiskCache {
	private File cacheDir;
	private int DISK_CACHE_SIZE = 1024 * 1024 * 10; //10Mb

	public DiskCache(Context context) {
		cacheDir = context.getCacheDir();
	}

	//Получаем изображение
	public Bitmap getBitmap(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);

		//Get bitmap file
		if( f.exists() ) {
			try {
				return BitmapFactory.decodeFile(f.getPath(), Cache.getOpts());
			} catch( OutOfMemoryError e ) {
				Log.e("DiskCache", "OurOfMemoryError: DiscCache.getBitmap");
			}
		}
		return null;
	}

	//Запись файла в кэш
	public void writeFile(String url, Bitmap bitmap) {

		//Получаем hash файла
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);

		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
		} catch( Exception e ) {
			e.printStackTrace();
		} finally {
			try {
				if( out != null ) {
					out.close();
				}
			} catch( Exception ex ) {
			}
		}
	}

	//Проверяет существование файла
	public boolean exists(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		return f.exists();
	}

	//Remove file from cache
	public void removeFile(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		if( f.exists() ) {
			f.delete();
		}
	}

	//clear memory cache
	public void clearCache() {
		long size = 0;

		//clear SD cache
		File[] files = cacheDir.listFiles();
		for (File f:files) {
			size = size+f.length();
			f.delete();
		}
	}
}
