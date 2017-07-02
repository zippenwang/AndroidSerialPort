package wzp.demo.serialport;

public interface FlushMode {

	int TCIFLUSH = 0;					// 清空串口输入(read)的数据
	int TCOFLUSH = 1;					// 清空串口输出(write)的数据
	int TCIOFLUSH = 2;					// 清空串口输入(read)、输出(write)的数据
}
