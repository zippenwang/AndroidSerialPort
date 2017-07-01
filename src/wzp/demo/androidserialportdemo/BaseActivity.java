package wzp.demo.androidserialportdemo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import wzp.demo.serialport.SerialPort;
import wzp.demo.serialport.SerialPortInitParam;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public abstract class BaseActivity extends Activity {

	protected SerialPort mSerialPort;
	protected FileOutputStream mOutputStream;
	private FileInputStream mInputStream;
	private ReadThread mReadThread;
	
	protected int DATA_LENGTH = 14;
	
	private static final String LOG_TAG = "BaseActivity";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSerialPort = MyApplication.getSerialPort();
		if (mSerialPort != null) {
			try {
				if (!mSerialPort.isOpen()) {
					Log.d(LOG_TAG, "打开串口");
					mSerialPort.open(SerialPortInitParam.TTYSAC3, SerialPortInitParam.B57600);
				}
				
				mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();
				
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause() {
		if (mReadThread != null) {
			mReadThread.isStop = true;
			mReadThread = null;
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {	
		if (mSerialPort != null) {
			try {
				mSerialPort.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		super.onDestroy();
	}
	
	private class ReadThread extends Thread {
		public volatile boolean isStop = false;
		byte[] buffer = new byte[128];
		
		@Override
		public void run() {
			setName("ReadThread");
			while(!isStop) {
				int size;
				try {
					if (mInputStream == null) {
						return;
					}
					size = mInputStream.read(buffer);
					if (size > 0) {
						StringBuilder res = new StringBuilder();
						for (int i = 0; i < size; i++) {
							res.append(String.format("%02x", (byte) buffer[i]));
							res.append(" ");
						}
						Log.i(LOG_TAG, "接收到" + size + "个字节：" + res.toString());
						
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			Log.i(LOG_TAG, "停止读取数据");
		}
	}
	
	protected abstract void onDataReceived(final byte[] buffer, int size);
	
}
