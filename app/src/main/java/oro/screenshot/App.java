package oro.screenshot;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

/** Общие вещи приложения: выбранный способ, звук, вибрация, сообщения. */
public class App extends Application {

    private static App app;
    private SoundPool pool;
    private int soundId;
    private boolean loaded, playWhenLoaded;
    public static final Handler MAIN = new Handler(Looper.getMainLooper());

    @Override public void onCreate() {
        super.onCreate();
        app = this;
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA) // канал мультимедиа: громкость кнопками
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(aa).build();
        pool.setOnLoadCompleteListener((p, id, status) -> {
            loaded = status == 0;
            if (loaded && playWhenLoaded) { playWhenLoaded = false; p.play(soundId, 1f, 1f, 1, 0, 1f); }
        });
        soundId = pool.load(this, R.raw.shutter, 1);
    }

    // ---------- способ съёмки ----------
    public static int method(Context c) {
        SharedPreferences sp = c.getSharedPreferences("oro", MODE_PRIVATE);
        int def = Build.VERSION.SDK_INT >= 30 ? 2 : 1;
        int m = sp.getInt("method", def);
        if (m == 2 && Build.VERSION.SDK_INT < 30) m = 1;
        return m;
    }

    public static void setMethod(Context c, int m) {
        c.getSharedPreferences("oro", MODE_PRIVATE).edit().putInt("method", m).apply();
    }

    // ---------- звук + вибрация 50 мс ----------
    public static void feedback() {
        MAIN.post(() -> {
            if (app == null) return;
            if (app.loaded) app.pool.play(app.soundId, 1f, 1f, 1, 0, 1f);
            else app.playWhenLoaded = true;
            try {
                Vibrator v = (Vibrator) app.getSystemService(VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    AudioAttributes va = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).build();
                    v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE), va);
                }
            } catch (Exception ignored) { }
        });
    }

    public static void toast(String s) {
        MAIN.post(() -> { if (app != null) Toast.makeText(app, s, Toast.LENGTH_LONG).show(); });
    }

    public static Context ctx() { return app; }
}
