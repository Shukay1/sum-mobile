package app.menly.preview;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.graphics.Color;
import android.view.WindowManager;
public class MainActivity extends Activity {
  WebView view;
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    getWindow().setStatusBarColor(Color.rgb(9,9,12));
    getWindow().setNavigationBarColor(Color.rgb(9,9,12));
    view=new WebView(this);
    view.setBackgroundColor(Color.rgb(9,9,12));
    view.getSettings().setJavaScriptEnabled(true);
    view.getSettings().setDomStorageEnabled(true);
    view.setWebViewClient(new WebViewClient());
    view.setWebChromeClient(new WebChromeClient());
    setContentView(view);
    view.loadUrl("file:///android_asset/index.html");
  }
  @Override public void onBackPressed() { if(view.canGoBack())view.goBack();else super.onBackPressed(); }
}
