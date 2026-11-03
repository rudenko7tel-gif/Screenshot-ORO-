package oro.screenshot;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.hardware.HardwareBuffer;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;

/** Спецвозможность: способ 1 (системный скриншот) и способ 2 (тихий снимок). */
public class ShotAccessibilityService extends AccessibilityService {

    static volatile ShotAccessibilityService instance;

    @Override protected void onServiceConnected() { instance = this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent e) { }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
    @Override public boolean onUnbind(android.content.Intent i) { instance = null; return super.onUnbind(i); }

    /** Способ 2 (Android 11+): снимаем экран сами и сохраняем PNG. */
    void capture() {
        if (android.os.Build.VERSION.SDK_INT < 30) return;
        takeScreenshot(Display.DEFAULT_DISPLAY, getMainExecutor(), new TakeScreenshotCallback() {
            @Override public void onSuccess(ScreenshotResult r) {
                Bitmap sw = null;
                try {
                    HardwareBuffer hb = r.getHardwareBuffer();
                    Bitmap hw = Bitmap.wrapHardwareBuffer(hb, r.getColorSpace());
                    if (hw != null) sw = hw.copy(Bitmap.Config.ARGB_8888, false);
                    hb.close();
                } catch (Exception ignored) { }
                if (sw == null) { App.toast("Screenshot ORO: не удалось получить снимок"); return; }
                App.feedback();
                Saver.saveAsync(ShotAccessibilityService.this, sw);
            }
            @Override public void onFailure(int code) {
                App.toast("Screenshot ORO: система отказала в снимке (код " + code + ")");
            }
        });
    }
}
