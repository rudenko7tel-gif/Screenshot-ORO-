package oro.screenshot;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Сохранение PNG в стандартную папку скриншотов устройства. */
public class Saver {

    /** Samsung хранит скриншоты в DCIM/Screenshots, остальные — в Pictures/Screenshots. */
    static String folder() {
        return "samsung".equalsIgnoreCase(Build.MANUFACTURER) ? "DCIM/Screenshots" : "Pictures/Screenshots";
    }

    static void saveAsync(Context c, Bitmap b) {
        final Context ac = c.getApplicationContext();
        new Thread(() -> {
            String err = save(ac, b);
            b.recycle();
            if (err != null) App.toast("Screenshot ORO: не удалось сохранить — " + err);
        }).start();
    }

    /** @return null при успехе, иначе текст ошибки */
    static String save(Context c, Bitmap b) {
        String name = "Screenshot_" + new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date()) + "_ORO.png";
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentResolver cr = c.getContentResolver();
                ContentValues v = new ContentValues();
                v.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                v.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                v.put(MediaStore.Images.Media.RELATIVE_PATH, folder());
                v.put(MediaStore.Images.Media.IS_PENDING, 1);
                Uri uri = cr.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), v);
                if (uri == null) return "нет доступа к галерее";
                try (OutputStream os = cr.openOutputStream(uri)) {
                    if (os == null || !b.compress(Bitmap.CompressFormat.PNG, 100, os)) {
                        cr.delete(uri, null, null);
                        return "ошибка записи";
                    }
                }
                v.clear();
                v.put(MediaStore.Images.Media.IS_PENDING, 0);
                cr.update(uri, v, null, null);
            } else {
                File dir = new File(Environment.getExternalStorageDirectory(), folder());
                if (!dir.exists() && !dir.mkdirs()) return "нет доступа к памяти (разрешите доступ в приложении)";
                File f = new File(dir, name);
                try (FileOutputStream fo = new FileOutputStream(f)) {
                    if (!b.compress(Bitmap.CompressFormat.PNG, 100, fo)) return "ошибка записи";
                }
                MediaScannerConnection.scanFile(c, new String[]{f.getAbsolutePath()}, new String[]{"image/png"}, null);
            }
            return null;
        } catch (SecurityException se) {
            return "нет доступа к памяти (разрешите доступ в приложении)";
        } catch (Exception e) {
            return e.getClass().getSimpleName();
        }
    }
}
