package oro.screenshot;

import android.os.Bundle;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;

/** Цифровой помощник. Система вызывает его удержанием кнопки «Домой». */
public class OroVoiceService extends VoiceInteractionService {

    static volatile OroVoiceService instance;

    @Override public void onReady() { super.onReady(); instance = this; }
    @Override public void onShutdown() { instance = null; super.onShutdown(); }

    /** Кнопка «Проверить» в приложении: то же самое, что удержание «Домой». */
    static boolean test() {
        OroVoiceService s = instance;
        if (s == null) return false;
        try {
            s.showSession(new Bundle(), VoiceInteractionSession.SHOW_WITH_ASSIST | VoiceInteractionSession.SHOW_WITH_SCREENSHOT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
