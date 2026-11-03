package oro.screenshot;

import android.content.Intent;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;

/** Заглушка: Android требует её от любого «помощника». Распознавания речи нет. */
public class OroRecognitionService extends RecognitionService {
    @Override protected void onStartListening(Intent i, Callback cb) {
        try { cb.error(SpeechRecognizer.ERROR_CLIENT); } catch (Exception ignored) { }
    }
    @Override protected void onCancel(Callback cb) { }
    @Override protected void onStopListening(Callback cb) { }
}
