package oro.screenshot;

import android.app.Activity;
import android.os.Bundle;

/** Запасной вход помощника, если прошивка не приняла полноценного помощника. */
public class AssistActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        int m = App.method(this);
        finish();
        overridePendingTransition(0, 0);
        if (m == 3) App.toast("Screenshot ORO: на этом устройстве способ 3 недоступен — выберите способ 1 или 2");
        else Shot.viaAccessibility(this, m);
    }
}
