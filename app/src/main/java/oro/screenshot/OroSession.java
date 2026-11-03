package oro.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;

/** Сеанс помощника: экрана не показывает, только делает снимок выбранным способом. */
public class OroSession extends VoiceInteractionSession {

    private boolean handled;
    private int method;
    private final Runnable timeout = () -> {
        if (!handled) {
            handled = true;
            App.toast("Screenshot ORO: система не передала снимок. Включите «Использовать скриншот» в настройках помощника или выберите способ 1 / 2");
            hide();
        }
    };

    public OroSession(Context c) { super(c); }

    @Override public void onPrepareShow(Bundle args, int flags) {
        super.onPrepareShow(args, flags);
        handled = false;
        method = App.method(getContext());
    }

    @Override public void onShow(Bundle args, int flags) {
        super.onShow(args, flags);
        if (method == 3) {
            if (!handled) App.MAIN.postDelayed(timeout, 2500);
        } else {
            handled = true;
            hide();
            Shot.viaAccessibility(getContext(), method);
        }
    }

    /** Способ 3: снимок экрана передаёт сама система. */
    @Override public void onHandleScreenshot(Bitmap shot) {
        if (method != 3 || handled) return;
        handled = true;
        App.MAIN.removeCallbacks(timeout);
        if (shot == null) {
            App.toast("Screenshot ORO: система не передала снимок. Включите «Использовать скриншот» в настройках помощника или выберите способ 1 / 2");
        } else {
            Bitmap copy = shot.copy(Bitmap.Config.ARGB_8888, false);
            if (copy != null) {
                App.feedback();
                Saver.saveAsync(getContext(), copy);
            } else {
                App.toast("Screenshot ORO: не удалось обработать снимок");
            }
        }
        hide();
    }

    @Override public void onHide() {
        App.MAIN.removeCallbacks(timeout);
        super.onHide();
    }
}
