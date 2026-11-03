package oro.screenshot;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Build;

/** Способы 1 и 2 — через спецвозможность. */
public class Shot {

    /** Задержка, чтобы окно помощника успело исчезнуть до снимка. */
    static final long DELAY_MS = 400;

    public static void viaAccessibility(Context c, int method) {
        ShotAccessibilityService s = ShotAccessibilityService.instance;
        if (s == null) {
            App.toast("Screenshot ORO: включите спецвозможность (откройте приложение — там кнопка «Включить»)");
            return;
        }
        if (method == 2 && Build.VERSION.SDK_INT < 30) method = 1;
        final int m = method;
        App.MAIN.postDelayed(() -> {
            ShotAccessibilityService svc = ShotAccessibilityService.instance;
            if (svc == null) { App.toast("Screenshot ORO: спецвозможность отключена"); return; }
            if (m == 1) {
                boolean ok = svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT);
                if (ok) App.feedback();
                else App.toast("Screenshot ORO: система отказала в снимке (способ 1)");
            } else {
                svc.capture();
            }
        }, DELAY_MS);
    }
}
