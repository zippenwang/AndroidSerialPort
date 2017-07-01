package wzp.demo.androidserialportdemo;

import java.io.IOException;

import wzp.demo.serialport.SerialPort;
import wzp.demo.serialport.SerialPortInitParam;
import android.app.Application;

public class MyApplication extends Application {
	
	private static SerialPort mSerialPort = null;

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			mSerialPort = new SerialPort(SerialPortInitParam.TTYSAC3, SerialPortInitParam.B57600);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static SerialPort getSerialPort() {
		return mSerialPort;
	}
	
}
