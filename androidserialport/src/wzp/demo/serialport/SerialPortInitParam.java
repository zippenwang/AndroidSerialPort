package wzp.demo.serialport;

public interface SerialPortInitParam {
	
	/*
	 * 串口编号
	 */
	int TTYSAC0 = 0;
	int TTYSAC1 = 1;
	int TTYSAC2 = 2;
	int TTYSAC3 = 3;
	int TTYUSB0 = 4;
	int TTYUSB1 = 5;
	

	/*
	 * 波特率
	 */
	int B2400 = 2400;
	int B4800 = 4800;
	int B9600 = 9600;
	int B19200 = 19200;
	int B38400 = 38400;
	int B57600 = 57600;
	int B115200 = 115200;
	
	/*
	 * 奇偶校验
	 */
	char ODD_CHECK = 'O';
	char EVEN_CHECK = 'E';
	char NONE_CHECK = 'N';
	
	/*
	 * 数据位个数
	 */
	int EIGHT_DATA_BIT = 8;
	int SEVEN_DATA_BIT = 7;
	
	/*
	 * 停止位个数
	 */
	int ONE_STOP_BIT = 1;
	int TWO_STOP_BIT = 2;
}
