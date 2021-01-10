package cool.arisu.shake_asobi_light;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import orz.kassy.shakegesture.ShakeGestureManager;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ShakeGestureManager.GestureListener {

	private ShakeGestureManager gestureManager = null;
	private WebView webView = null;
	private boolean isImmersiveMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		webView = findViewById(R.id.webview);
		webView.loadUrl("file:///android_asset/index.html");
	}

	protected void onResume() {
		gestureManager = new ShakeGestureManager(this, this);
		gestureManager.startSensing();
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onResume();
	}

	protected void onPause() {
		gestureManager.stopSensing();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result == null) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		} else if (result.getContents() == null) {
			Toast.makeText(this, "エラー: QRコードの読み取りがキャンセルされました", Toast.LENGTH_LONG).show();
			return;
		}
		Log.d("readQR", result.getContents());
		String qrCodeText = result.getContents().toString();
		if (!qrCodeText.matches("https://vc.asobistore.jp/.*")) {
			Toast.makeText(this, "エラー: QRコードにアソビライトのURLが含まれていません", Toast.LENGTH_LONG).show();
			return;
		}


		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.loadUrl(qrCodeText);

		Toast.makeText(this, "「接続」をタップしてください。", Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_switch_immersive_mode) {
			// 全画面表示 (没入モード) を切り替え
			switchImmersiveMode();
			return true;
		} else if (id == R.id.action_scan_qr_code) {
			// QRコードのスキャンを実行
			Toast.makeText(MainActivity.this, "「スマホでアソビライト連携」のQRコードを撮影してください", Toast.LENGTH_LONG).show();
			new IntentIntegrator(MainActivity.this).initiateScan();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onGestureDetected(int gestureType, int gestureCount) {

		Log.i("MainActivity", "Gesture - " + gestureType + ", " + gestureCount);
		shakeVirtualPenLight();

	}

	public void shakeVirtualPenLight() {

		if (webView == null) return;

		webView.loadUrl("javascript:document.querySelector(\"[alt='タップ']\").click();");
	}

	public void switchImmersiveMode() {

		if (isImmersiveMode) {
			// 元に戻す
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			isImmersiveMode = false;
			Toast.makeText(this, "全画面表示を解除しました", Toast.LENGTH_SHORT).show();
			return;
		}

		// 没入モードへ
		getWindow().getDecorView().setSystemUiVisibility(
			// 通知バーを非表示
			View.SYSTEM_UI_FLAG_FULLSCREEN |
			// ナビゲーションバーを非表示
			View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		);
		Toast.makeText(this, "全画面表示へ切り替えました", Toast.LENGTH_SHORT).show();
		isImmersiveMode = true;

	}

	@Override
	public void onMessage(String s) {

	}
}
