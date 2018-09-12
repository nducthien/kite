package hu.itware.kite.service.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import hu.itware.kite.service.R;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		WebView content = (WebView) findViewById(R.id.web_help_content);
		//---Disable selection in webview
		disableWebViewSelection(content);

		WebSettings settings = content.getSettings();
		content.setBackgroundColor(0x00000000);
		settings.setSupportZoom(false);
		settings.setSupportMultipleWindows(false);
		settings.setLoadWithOverviewMode(true);
		settings.setJavaScriptEnabled(false);

		content.loadUrl("file:///android_asset/szerviz_leiras.html");
	}

	protected void disableWebViewSelection(WebView wv) {

		if (wv != null) {
			wv.setLongClickable(false);
			wv.setOnLongClickListener(new View.OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					return true;
				}
			});
		}
	}
}