package com.brothers.adventure;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView()
            .setSystemUiVisibility(5894);

        WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);

        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }
}
