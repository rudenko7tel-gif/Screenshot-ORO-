package oro.screenshot;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Главный экран (эскиз 2): способы съёмки карточками, настройка внутри выбранной. */
public class MainActivity extends Activity {

    static final int GREEN = 0xFF3B6D11, RED = 0xFFE24B4A, AMBER = 0xFF854F0B,
            GRAY = 0xFF5F5E5A, LIGHT = 0xFFB4B2A9, PAGE = 0xFFF4F3EF, TEXT = 0xFF2C2C2A;

    LinearLayout root;

    static final String[] TITLES = {
            "1. Системный скриншот",
            "2. Тихий снимок приложением",
            "3. Снимок через помощника"
    };
    static final String[] HOW = {
            "Как работает: приложение нажимает системный «Скриншот» за вас. Снимок сохраняет система, может появиться её панель с превью.",
            "Как работает: приложение само снимает экран и сохраняет PNG в папку скриншотов. Без системной панели.",
            "Как работает: при удержании «Домой» система сама передаёт снимок помощнику, приложение сохраняет его в PNG. Спецвозможность не нужна."
    };
    static final String[] DEVICES = {
            "Для каких устройств: Android 9 и новее, все устройства.",
            "Для каких устройств: Android 11 и новее.",
            "Для каких устройств: Android 9 и новее, но некоторые прошивки снимок не передают — проверьте кнопкой «Проверить»."
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(PAGE);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        root.setPadding(p, p, p, p);
        sv.addView(root);
        setContentView(sv);
    }

    @Override protected void onResume() {
        super.onResume();
        build();
    }

    // ---------------- экран ----------------
    void build() {
        root.removeAllViews();
        int m = App.method(this);

        LinearLayout head = new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        ImageView ic = new ImageView(this);
        ic.setImageResource(R.mipmap.ic_launcher);
        head.addView(ic, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView t = text("Screenshot ORO", 22, TEXT, true);
        t.setPadding(dp(10), 0, 0, 0);
        head.addView(t);
        TextView ver = text("  v0.0003", 13, GRAY, false);
        head.addView(ver, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView q = text("?", 20, Color.WHITE, true);
        q.setGravity(Gravity.CENTER);
        GradientDrawable qg = new GradientDrawable();
        qg.setShape(GradientDrawable.OVAL);
        qg.setColor(GREEN);
        q.setBackground(qg);
        q.setOnClickListener(v -> showHelp());
        head.addView(q, new LinearLayout.LayoutParams(dp(40), dp(40)));
        root.addView(head);

        TextView hint = text("Удерживайте кнопку «Домой» — получится снимок экрана со звуком и вибрацией. Выберите способ, который работает на этом устройстве:", 15, GRAY, false);
        hint.setPadding(0, dp(10), 0, dp(10));
        root.addView(hint);

        for (int i = 1; i <= 3; i++) root.addView(card(i, i == m));

        // звук (вариант 17) и вибрация (вариант 20)
        LinearLayout snd = box(false);
        snd.addView(text("Звук снимка", 16, TEXT, true));
        snd.addView(text("Играет через мультимедиа: громкость — кнопками.", 14, GRAY, false));
        int cur = App.sound(this);
        for (int k = 0; k < App.SOUND_NAMES.length; k++) {
            final int idx = k;
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));
            TextView nm = text((k == cur ? "◉  " : "○  ") + App.SOUND_NAMES[k], 16, k == cur ? GREEN : TEXT, k == cur);
            row.addView(nm, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            if (k != App.SOUND_OFF) {
                TextView pl = text("▶", 18, Color.WHITE, false);
                pl.setGravity(Gravity.CENTER);
                GradientDrawable pg = new GradientDrawable();
                pg.setShape(GradientDrawable.OVAL);
                pg.setColor(GREEN);
                pl.setBackground(pg);
                pl.setOnClickListener(v -> App.playSound(idx));
                row.addView(pl, new LinearLayout.LayoutParams(dp(40), dp(40)));
            }
            row.setOnClickListener(v -> {
                App.setSound(this, idx);
                if (idx != App.SOUND_OFF) App.playSound(idx);
                build();
            });
            snd.addView(row);
        }

        int vib = App.vibMs(this);
        TextView vl = text(vib == 0 ? "Вибрация: выкл" : "Вибрация: " + vib + " мс", 16, TEXT, true);
        vl.setPadding(0, dp(12), 0, 0);
        snd.addView(vl);
        android.widget.SeekBar sb = new android.widget.SeekBar(this);
        sb.setMax(50); // 0..500 мс, шаг 10
        sb.setProgress(vib / 10);
        sb.setProgressTintList(android.content.res.ColorStateList.valueOf(GREEN));
        sb.setThumbTintList(android.content.res.ColorStateList.valueOf(GREEN));
        sb.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar b, int p, boolean user) {
                vl.setText(p == 0 ? "Вибрация: выкл" : "Вибрация: " + (p * 10) + " мс");
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar b) { }
            @Override public void onStopTrackingTouch(android.widget.SeekBar b) {
                App.setVibMs(MainActivity.this, b.getProgress() * 10);
                App.vibrate(b.getProgress() * 10);
            }
        });
        snd.addView(sb);
        root.addView(snd);
    }

    View card(int i, boolean sel) {
        boolean avail = !(i == 2 && Build.VERSION.SDK_INT < 30);
        LinearLayout c = box(sel);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView radio = text(sel ? "◉ " : "○ ", 18, sel ? GREEN : GRAY, false);
        top.addView(radio);
        top.addView(text(TITLES[i - 1], 16, avail ? TEXT : LIGHT, true));
        c.addView(top);

        String badge; int bc;
        if (!avail) { badge = "нужен Android 11 или новее"; bc = LIGHT; }
        else if (i == 3) { badge = "проверьте на этом устройстве"; bc = AMBER; }
        else { badge = "подходит для этого устройства"; bc = GREEN; }
        TextView bt = text(badge, 13, bc, true);
        bt.setPadding(0, dp(2), 0, dp(4));
        c.addView(bt);

        c.addView(text(HOW[i - 1], 14, avail ? GRAY : LIGHT, false));
        TextView dv = text(DEVICES[i - 1], 14, avail ? GRAY : LIGHT, false);
        dv.setPadding(0, dp(4), 0, 0);
        c.addView(dv);

        c.setOnClickListener(v -> {
            if (!avail) { Toast.makeText(this, "Способ 2 работает на Android 11 и новее", Toast.LENGTH_SHORT).show(); return; }
            App.setMethod(this, i);
            build();
        });

        if (sel && avail) setup(c, i);
        return c;
    }

    /** Настройка внутри выбранной карточки. */
    void setup(LinearLayout c, int i) {
        View line = new View(this);
        line.setBackgroundColor(0x22000000);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(0, dp(10), 0, dp(6));
        c.addView(line, lp);

        // шаг: помощник
        boolean asst = isAssistant();
        c.addView(statusRow("Цифровой помощник", asst, "Назначить", v -> openAssistantSettings()));
        if (!asst) c.addView(note("Откроются настройки: «Цифровой помощник» (или «Помощник и голосовой ввод») → выберите Screenshot ORO."));

        // шаг: спецвозможность
        if (i == 1 || i == 2) {
            boolean acc = isAccessibilityOn();
            c.addView(statusRow("Спецвозможность", acc, "Включить", v -> open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
            if (!acc) {
                c.addView(note("В списке найдите Screenshot ORO (на Samsung — в «Установленные приложения») и включите."));
                if (Build.VERSION.SDK_INT >= 33) {
                    c.addView(note("Если переключатель серый: «О приложении» → ⋮ (вверху справа) → «Разрешить ограниченные настройки», затем снова «Включить»."));
                    Button info = button("О приложении");
                    info.setOnClickListener(v -> open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))));
                    c.addView(info, wrap());
                }
            }
        }

        // шаг: память (только Android 9, способ 3 сохраняет файл сам)
        if (i == 3 && Build.VERSION.SDK_INT < 29) {
            boolean st = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            c.addView(statusRow("Доступ к памяти", st, "Разрешить",
                    v -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1)));
        }
        if (i == 3) {
            c.addView(note("Если снимок не появляется: в настройках помощника включите «Использовать скриншот»."));
            Button as = button("Настройки помощника");
            as.setOnClickListener(v -> openAssistantSettings());
            c.addView(as, wrap());
        }

        c.addView(note("Если удержание «Домой» открывает «Обвести и найти» — отключите эту функцию в настройках панели навигации."));

        Button test = button("Проверить");
        test.setOnClickListener(v -> test(i));
        LinearLayout.LayoutParams tp = wrap();
        tp.topMargin = dp(8);
        c.addView(test, tp);
    }

    void test(int i) {
        if (OroVoiceService.test()) return;               // точно как удержание «Домой»
        if (i != 3 && ShotAccessibilityService.instance != null) { Shot.viaAccessibility(this, i); return; }
        Toast.makeText(this, isAssistant()
                ? "Помощник назначен, но ещё не запущен. Удерживайте кнопку «Домой»."
                : "Сначала назначьте Screenshot ORO цифровым помощником", Toast.LENGTH_LONG).show();
    }

    @Override public void onRequestPermissionsResult(int rc, String[] p, int[] r) { build(); }

    // ---------------- справка ----------------
    void showHelp() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Как пользоваться")
                .setMessage("1. Выберите способ и назначьте Screenshot ORO цифровым помощником.\n\n"
                        + "2. Для способов 1 и 2 включите спецвозможность.\n\n"
                        + "3. Удержите кнопку «Домой» — получится снимок со звуком и вибрацией.")
                .setPositiveButton("Понятно", null)
                .show();
    }

    // ---------------- проверки ----------------
    boolean isAssistant() {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                RoleManager rm = getSystemService(RoleManager.class);
                if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_ASSISTANT))
                    return rm.isRoleHeld(RoleManager.ROLE_ASSISTANT);
            } catch (Exception ignored) { }
        }
        String a = Settings.Secure.getString(getContentResolver(), "assistant");
        String v = Settings.Secure.getString(getContentResolver(), "voice_interaction_service");
        String pk = getPackageName() + "/";
        return (a != null && a.startsWith(pk)) || (v != null && v.startsWith(pk));
    }

    boolean isAccessibilityOn() {
        if (ShotAccessibilityService.instance != null) return true;
        String s = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return s != null && s.contains(getPackageName() + "/");
    }

    void openAssistantSettings() {
        if (open(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))) return;
        if (open(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))) return;
        open(new Intent(Settings.ACTION_SETTINGS));
    }

    boolean open(Intent in) {
        try { startActivity(in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return true; }
        catch (Exception e) { return false; }
    }

    // ---------------- элементы ----------------
    View statusRow(String name, boolean ok, String action, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(4), 0, dp(4));
        TextView s = text(ok ? "✓ " : "✗ ", 18, ok ? GREEN : RED, true);
        r.addView(s);
        TextView n = text(name + (ok ? " — готово" : ""), 15, TEXT, false);
        r.addView(n, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        if (!ok) {
            Button b = button(action);
            b.setOnClickListener(l);
            r.addView(b);
        }
        return r;
    }

    TextView note(String s) {
        TextView t = text(s, 13, GRAY, false);
        t.setPadding(dp(22), 0, 0, dp(4));
        return t;
    }

    LinearLayout box(boolean sel) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.WHITE);
        g.setCornerRadius(dp(12));
        g.setStroke(sel ? dp(2) : dp(1), sel ? GREEN : 0xFFD3D1C7);
        l.setBackground(g);
        l.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        l.setLayoutParams(lp);
        return l;
    }

    Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable();
        g.setColor(GREEN);
        g.setCornerRadius(dp(8));
        b.setBackground(g);
        b.setPadding(dp(14), dp(6), dp(14), dp(6));
        b.setMinHeight(dp(40));
        b.setMinimumHeight(dp(40));
        return b;
    }

    LinearLayout.LayoutParams wrap() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        return lp;
    }

    TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
