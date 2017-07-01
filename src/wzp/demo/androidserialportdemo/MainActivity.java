package wzp.demo.androidserialportdemo;

import java.io.IOException;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
	
	private EditText edtSend;
	private Button btnSend;
	private EditText edtReceive;
	private Button btnClear;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initWidget();
	}

	private void initWidget() {
		edtSend = (EditText) findViewById(R.id.edt_send);
		btnSend = (Button) findViewById(R.id.btn_send);
		edtReceive = (EditText) findViewById(R.id.edt_receive);
		btnClear = (Button) findViewById(R.id.btn_clear);
		
		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(edtSend.getText().toString())) {
					Toast.makeText(MainActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if (mOutputStream != null) {
					try {
						mOutputStream.write(edtSend.getText().toString().getBytes());
						edtReceive.append("\n发送成功：：" + edtSend.getText().toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				edtReceive.setText("");
			}
		});
	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				edtReceive.append("\n接收到：" + new String(buffer, 0, size));
			}
		});
	}

}
