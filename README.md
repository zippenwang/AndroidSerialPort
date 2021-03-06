# AndroidSerialPort
Android嵌入式设备的串口通信

### 核心类描述
#### SerialPort
串口通信的核心类，底层用JNI封装打开、关闭串口等操作，上层使用File相关的IO流实现串口数据接收与发送（即读写操作）；


#### SerialPortInitParam
串口初始化时的各种参数，包括：串口编号、波特率、奇偶校验、数据位个数、停止位个数；

### 用法
#### 打开串口
##### 方法一：调用带参数的构造器，传入串口初始化参数，创建串口实例的同时打开串口；

如：打开TTYSAC3串口，8位数据位、无奇偶校验、1位停止位、波特率为57600。
```java
SerialPort mSerialPort;

try {
    mSerialPort = new SerialPort(SerialPortInitParam.TTYSAC3, SerialPortInitParam.EIGHT_DATA_BIT, SerialPortInitParam.NONE_CHECK, 
	SerialPortInitParam.ONE_STOP_BIT, SerialPortInitParam.B57600);
} catch (IOException e) {
    e.printStackTrace();
}

```

由于数据位、停止位、奇偶校验一般是固定的，也可调用如下构造器，默认选用8位数据位、无奇偶校验、1位停止位。
```java
SerialPort mSerialPort;

try {
    mSerialPort = new SerialPort(SerialPortInitParam.TTYSAC3, SerialPortInitParam.B57600);
} catch (IOException e) {
    e.printStackTrace();
}

```

##### 方法二：使用无参构造器创建实例，再调用open()方法，传入串口初始化参数，打开串口。
open()方法所需传入的参数和构造器的输入参数相对应，同样包含两种重载形式。

```java
SerialPort mSerialPort = new SerialPort();

try {
    mSerialPort.open(SerialPortInitParam.TTYSAC3, SerialPortInitParam.EIGHT_DATA_BIT, SerialPortInitParam.NONE_CHECK, 
	SerialPortInitParam.ONE_STOP_BIT, SerialPortInitParam.B57600);
} catch (IOException e) {
    e.printStackTrace();
}

```

默认选用8位数据位、无奇偶校验、1位停止位。
```java
SerialPort mSerialPort = new SerialPort();

try {
    mSerialPort.open(SerialPortInitParam.TTYSAC3, SerialPortInitParam.B57600);
} catch (IOException e) {
    e.printStackTrace();
}

```

#### 串口通信
1. 获取IO流

```java
mOutputStream = mSerialPort.getOutputStream();
mInputStream = mSerialPort.getInputStream();
```
2. 利用IO流的read()、write()方法从串口接收、发送数据
 
#### 其他操作
判断串口是否已经打开，避免重复执行打开串口操作（虽然重复执行，并不会造成异常），或者当串口关闭时，能够及时打开。

```java
if (!mSerialPort.isOpen()) {
    Log.d(LOG_TAG, "打开串口");
    mSerialPort.open(SerialPortInitParam.TTYSAC3, SerialPortInitParam.B57600);
}
```

清空串口缓冲区。包含三种模式：
- TCIFLUSH：清空串口输入(read)的数据
- TCOFLUSH：清空串口输出(write)的数据
- TCIOFLUSH：清空串口输入(read)、输出(write)的数据

```java
mSerialPort.flushBuffer(FlushMode.TCIOFLUSH);
```

#### 注意点
IO流的打开与关闭应该和串口的打开关闭保持一致，SerialPort中已经为其做了封装，调用SerialPort的open()、close()方法，会同时打开、关闭IO流。因此，切记，不允许在其他地方对getInputStream()或getOutputStream()方法获得的IO流调用close()方法关闭IO流，因为IO流的关闭操作是不可逆的，会造成串口无法接受或发送数据。