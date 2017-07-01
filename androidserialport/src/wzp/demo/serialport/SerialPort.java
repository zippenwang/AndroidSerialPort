package wzp.demo.serialport;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;


public class SerialPort {

	private static final String TAG = "SerialPort";

	private FileDescriptor mFd;
	
	/*
	 * SerialPort打开，输入、输出流也打开；SerialPort关闭，输入、输出流也关闭；
	 * 不允许在其他地方打开或关闭串口输入、输出流！
	 */
	private FileInputStream mFileInputStream;				// 串口输入流
	private FileOutputStream mFileOutputStream;				// 串口输出流

	
	/*
	 * Constructor
	 */
	public SerialPort() {}
	
	public SerialPort(int whichPort, int baudrate) throws IOException {
		open(whichPort, baudrate);
	}
	
	public SerialPort(int whichPort, int dataBitNum, char oddEvenCheck, 
			int stopBitNum, int baudrate) throws IOException {
		open(whichPort, dataBitNum, oddEvenCheck, stopBitNum, baudrate);
	}
	
	/**
	 * 打开串口
	 * 
	 * @param whichPort
	 * @param baudrate
	 * @throws IOException
	 */
	public void open(int whichPort, int baudrate) throws IOException {
		open(whichPort, SerialPortInitParam.EIGHT_DATA_BIT, SerialPortInitParam.NONE_CHECK, 
				SerialPortInitParam.ONE_STOP_BIT, baudrate);
	}
	
	/**
	 * 打开串口
	 * 
	 * @param whichPort
	 * @param dataBitNum
	 * @param oddEvenCheck
	 * @param stopBitNum
	 * @param baudrate
	 * @throws IOException
	 */
	public void open(int whichPort, int dataBitNum, char oddEvenCheck, 
			int stopBitNum, int baudrate) throws IOException {
		mFd = jOpen(whichPort, dataBitNum, oddEvenCheck, stopBitNum, baudrate);
		if (mFd == null) {
			Log.e(TAG, "native jOpen() returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}
	
	/**
	 * 关闭 串口
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		int res = jClose();
		
		// 关闭异常
		if (res < 0) {
			throw new IOException("串口关闭出现异常！");
		}
		
		/*
		 * 正常关闭
		 */
		mFd = null;
		if (mFileInputStream != null) {
			mFileInputStream.close();
			mFileInputStream = null;			
		}
		if (mFileOutputStream != null) {
			mFileOutputStream.close();
			mFileOutputStream = null;			
		}
	}
	
	
	/*
	 * Getter
	 */
	public FileInputStream getInputStream() {
		return mFileInputStream;
	}

	public FileOutputStream getOutputStream() {
		return mFileOutputStream;
	}

	/*
	 * JNI
	 */
	/**
	 * 打开串口
	 * 
	 * @param whichPort 串口号
	 * @param dataBitNum 数据位个数（8或7）
	 * @param oddEvenCheck 奇偶校验（'O'、'E'、'N'）
	 * @param stopBitNum 停止位个数（1或2）
	 * @param baudrate 波特率
	 * @return 串口对应的FileDescriptor
	 */
	private native FileDescriptor jOpen(int whichPort, int dataBitNum, 
			char oddEvenCheck, int stopBitNum, int baudrate);
	
	/**
	 * 关闭串口
	 * 
	 * @return 0:正常;-1:异常
	 */
	private native int jClose();
	
	/**
	 * 判断串口是否打开
	 * 
	 * @return
	 */
	public native boolean isOpen();
	
	/**
	 * 清空串口缓冲区
	 * 
	 * @param mode 清空模式
	 */
	public native void flushBuffer(int mode);
	
	
	static {
		System.loadLibrary("serial_port");
	}
}
