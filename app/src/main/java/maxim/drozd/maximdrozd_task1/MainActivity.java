package maxim.drozd.maximdrozd_task1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.microsoft.appcenter.distribute.Distribute;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        AppCenter.start(getApplication(), "537ba06c-9f74-4109-bfa7-18ca47970105", Analytics.class, Crashes.class, Distribute.class);

        TextView link = findViewById(R.id.textView2);
        String href = getResources().getString(R.string.Link);
        String text = "<a href=\"" + href + "\">" + "Github link to Kamerton12" + "</a>";
        link.setText(Html.fromHtml(text));

        link.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
