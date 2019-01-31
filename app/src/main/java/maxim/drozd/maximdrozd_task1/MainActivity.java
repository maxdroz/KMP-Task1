package maxim.drozd.maximdrozd_task1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        TextView link = findViewById(R.id.textView2);
        String href = getResources().getString(R.string.Link);
        String text = "<a href=\"" + href + "\">" + "Github link to Kamerton12" + "</a>";
        link.setText(Html.fromHtml(text));
        link.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
