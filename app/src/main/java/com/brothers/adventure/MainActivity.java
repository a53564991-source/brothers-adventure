package com.brothers.adventure;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.widget.TextView;
import android.view.Gravity;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final String GAME = "https://game.brothers.invalid/index.html";
    private static final int LIMIT = 8 * 1024 * 1024;
    private byte[] game;
    private byte[] read(InputStream input) throws IOException {
        try (InputStream in = input; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int n;
            while ((n = in.read(buffer)) != -1) {
                if (out.size() + n > LIMIT) throw new IOException("File too large");
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        }
    }
    private boolean valid(byte[] bytes) {
        String html = new String(bytes, StandardCharsets.UTF_8);
        return html.toLowerCase(java.util.Locale.ROOT).contains("<!doctype html") && html.contains("</script>") && html.contains("형제의 모험");
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        TextView loading = new TextView(this);
        loading.setGravity(Gravity.CENTER); loading.setText("최신 게임 확인 중…");
        setContentView(loading);
        new Thread(() -> {
            File cache = new File(getFilesDir(), "game.html");
            byte[] selected = null;
            try {
                String repo = new String(read(getAssets().open("repository.txt")), StandardCharsets.UTF_8).trim();
                if (!repo.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")) throw new IOException("Invalid repository");
                URL url = new URL("https://raw.githubusercontent.com/" + repo + "/main/index.html?t=" + System.currentTimeMillis());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3500); connection.setReadTimeout(3500);
                connection.setUseCaches(false); connection.setInstanceFollowRedirects(false);
                try {
                    if (connection.getResponseCode() == 200) {
                        byte[] candidate = read(connection.getInputStream());
                        if (valid(candidate)) {
                            selected = candidate;
                            File temp = new File(getFilesDir(), "game.tmp");
                            try (FileOutputStream out = new FileOutputStream(temp)) { out.write(candidate); }
                            temp.renameTo(cache);
                        }
                    }
                } finally { connection.disconnect(); }
            } catch (Exception ignored) {}
            if (selected == null) try { byte[] cached = read(new FileInputStream(cache)); if (valid(cached)) selected = cached; } catch (Exception ignored) {}
            if (selected == null) try { selected = read(getAssets().open("index.html")); } catch (Exception ignored) {}
            final byte[] result = selected;
            runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) showGame(result); });
        }).start();
    }
    private void showGame(byte[] bytes) {
        if (bytes == null) { TextView error = new TextView(this); error.setText("게임 파일을 열 수 없습니다."); setContentView(error); return; }
        game = bytes;
        WebView web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return !GAME.equals(request.getUrl().toString()); }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (GAME.equals(request.getUrl().toString())) return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(game));
                return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
            }
        });
        setContentView(web); web.loadUrl(GAME);
    }
}
