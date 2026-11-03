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

    /** Названия звуков; последний пункт — «Без звука». */
    public static final String[] SOUND_NAMES = {
            "Щелчок затвора", "Мягкий клик", "Бип камеры", "Поп", "Капля воды", "Колокольчик", "Без звука"
    };
    public static final int SOUND_OFF = SOUND_NAMES.length - 1;
    static final int[] SOUND_RES = {
            R.raw.snd1, R.raw.snd2, R.raw.snd3, R.raw.snd4, R.raw.snd5, R.raw.snd6
    };
    public static final int VIB_DEFAULT_MS = 150;

    private static App app;
    private SoundPool pool;
    private final int[] soundIds = new int[SOUND_RES.length];
    private final boolean[] ready = new boolean[SOUND_RES.length];
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
            for (int i = 0; i < soundIds.length; i++) if (soundIds[i] == id) ready[i] = status == 0;
        });
        for (int i = 0; i < SOUND_RES.length; i++) soundIds[i] = pool.load(this, SOUND_RES[i], 1);
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

    // ---------- звук ----------
    public static int sound(Context c) {
        int s = c.getSharedPreferences("oro", MODE_PRIVATE).getInt("sound", 0);
        return (s < 0 || s > SOUND_OFF) ? 0 : s;
    }

    public static void setSound(Context c, int s) {
        c.getSharedPreferences("oro", MODE_PRIVATE).edit().putInt("sound", s).apply();
    }

    /** Проиграть конкретный звук (для кнопки ▶ и для снимка). */
    public static void playSound(int idx) {
        MAIN.post(() -> {
            if (app == null || idx < 0 || idx >= SOUND_RES.length) return;
            if (app.ready[idx]) app.pool.play(app.soundIds[idx], 1f, 1f, 1, 0, 1f);
        });
    }

    // ---------- вибрация ----------
    public static int vibMs(Context c) {
        return c.getSharedPreferences("oro", MODE_PRIVATE).getInt("vib", VIB_DEFAULT_MS);
    }

    public static void setVibMs(Context c, int ms) {
        c.getSharedPreferences("oro", MODE_PRIVATE).edit().putInt("vib", ms).apply();
    }

    public static void vibrate(int ms) {
        MAIN.post(() -> {
            if (app == null || ms <= 0) return;
            try {
                Vibrator v = (Vibrator) app.getSystemService(VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    // USAGE_ALARM: системные настройки вибрации касаний его не приглушают
                    AudioAttributes va = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM).build();
                    v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE), va);
                }
            } catch (Exception ignored) { }
        });
    }

    /** Звук и вибрация при снимке — по настройкам. */
    public static void feedback() {
        if (app == null) return;
        int s = sound(app);
        if (s != SOUND_OFF) playSound(s);
        vibrate(vibMs(app));
    }

    public static void toast(String s) {
        MAIN.post(() -> { if (app != null) Toast.makeText(app, s, Toast.LENGTH_LONG).show(); });
    }

    public static Context ctx() { return app; }
}
