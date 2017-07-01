#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <stdint.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <android/log.h>

#include "wzp_demo_serialport_SerialPort.h"

static const char *TAG="serial_port";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

/*
 * 获取波特率
 */
static speed_t getBaudrate(jint baudrate) {
	switch(baudrate) {
		case 0: return B0;
		case 50: return B50;
		case 75: return B75;
		case 110: return B110;
		case 134: return B134;
		case 150: return B150;
		case 200: return B200;
		case 300: return B300;
		case 600: return B600;
		case 1200: return B1200;
		case 1800: return B1800;
		case 2400: return B2400;
		case 4800: return B4800;
		case 9600: return B9600;
		case 19200: return B19200;
		case 38400: return B38400;
		case 57600: return B57600;
		case 115200: return B115200;
		case 230400: return B230400;
		case 460800: return B460800;
		case 500000: return B500000;
		case 576000: return B576000;
		case 921600: return B921600;
		case 1000000: return B1000000;
		case 1152000: return B1152000;
		case 1500000: return B1500000;
		case 2000000: return B2000000;
		case 2500000: return B2500000;
		case 3000000: return B3000000;
		case 3500000: return B3500000;
		case 4000000: return B4000000;
		default: return -1;
	}
}

/*
 * Class:     wzp_demo_serialport_SerialPort
 * Method:    open
 * Signature: (IICII)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_wzp_demo_serialport_SerialPort_jOpen
  (JNIEnv *env, jobject thiz, jint whichPort, jint dataBitNum,
		  jchar oddEvenCheck, jint stopBitNum, jint baudRate) {
	int fd;

	// 和O_RDWR进行或操作的值为0，正常情况下应该是O_RDWR|O_NOCTTY|O_NDELAY，但此次只能为0，
	// 否则在一个循环中连续发送大量数据，会非常容易出现异常。
	if (0 == whichPort) {
		LOGI("open fd /dev/ttySAC0");
		fd = open("/dev/ttySAC0", O_RDWR);
	} else if (1 == whichPort) {
		LOGI("open fd /dev/ttySAC1");
		fd = open("/dev/ttySAC1", O_RDWR);
	} else if(2 == whichPort) {
		LOGI("open fd /dev/ttySAC2");
		fd = open("/dev/ttySAC2", O_RDWR);
	} else if(3 == whichPort) {
		LOGI("open fd /dev/ttySAC3");
		fd = open("/dev/ttySAC3", O_RDWR|0);
	} else if(4 == whichPort) {
		LOGI("open fd /dev/ttyUSB0");
		fd = open("/dev/ttyUSB0", O_RDWR);
	} else if(5 == whichPort) {
		LOGI("open fd /dev/ttyUSB1");
		fd = open("/dev/ttyUSB1", O_RDWR);
	} else {
		LOGE("Parameter Error! Serial port not found");
		fd = 0;
		return NULL;
	}

	if (fd > 0) {
		LOGI("serial port open success! fd=%d", fd);

		struct termios ios;
		speed_t speed;

		if (tcgetattr(fd, &ios) < 0) {
			LOGE("tcgetattr() failed! get serial port info failed!");
			close(fd);
			return NULL;
		}

		bzero(&ios, sizeof(ios));
		ios.c_cflag |=  CLOCAL | CREAD;
		ios.c_cflag &= ~CSIZE;

		/*
		 * 设置波特率
		 */
		speed = getBaudrate(baudRate);
		if (speed == -1) {
			LOGE("Invalid baudrate");
			close(fd);
			return NULL;
		}
		cfsetispeed(&ios, speed);
		cfsetospeed(&ios, speed);

		/*
		 * 设置数据位个数
		 */
		switch(dataBitNum) {
			case 7:
				ios.c_cflag |= CS7;
				break;
			case 8:
				ios.c_cflag |= CS8;
				break;
			default :
				LOGE("data bit num can only be 7 or 8");
				close(fd);
				return NULL;
		}

		/*
		 * 设置奇偶校验
		 */
		switch(oddEvenCheck) {
			// 奇校验
			case 'O':
				ios.c_cflag |= PARENB;
				ios.c_cflag |= PARODD;
				ios.c_iflag |= (INPCK | ISTRIP);
				break;
			// 偶校验
			case 'E':
				ios.c_iflag |= (INPCK | ISTRIP);
				ios.c_cflag |= PARENB;
				ios.c_cflag &= ~PARODD;
				break;
			// 无校验
			case 'N':
				ios.c_cflag &= ~PARENB;
				break;
			default :
				LOGE("oddEvenCheck bit error!");
				close(fd);
				return NULL;
		}

		/*
		 * 设置停止位
		 */
		if(stopBitNum == 1) {
			ios.c_cflag &=  ~CSTOPB;
		} else if (stopBitNum == 2) {
			ios.c_cflag |=  CSTOPB;
		} else {
			LOGE("stopBitNum can only be 1 or 2");
			close(fd);
			return NULL;
		}

		// 本地模式标志设为0（默认的初始值）
		ios.c_lflag = 0;

		/*
		 * 每次从串口读取数据，无论串口的缓冲区中是否有数据，都会立马返回，即读操作不会阻塞
		 */
		ios.c_cc[VTIME]  = 0;
		ios.c_cc[VMIN] = 0;

		/*
		 * 用于清空串口在接收和发送数据时，缓存在串口寄存器中的数据
		 * TCIFLUSH 清除正接收到的数据，且不会读出来；
		 * TCOFLUSH 清除正写入的数据，且不会发送至终端；
		 * TCIOFLUSH 清除所有正在发生的I/O操作；
		 */
		tcflush(fd, TCIFLUSH);

		if (tcsetattr(fd, TCSANOW, &ios) < 0) {
			LOGE("tcsetattr() failed! serial port initial failed!");
			close(fd);
			return NULL;
		}
	} else {
		LOGE("serial port open failed! fd=%d", fd);
		return NULL;
	}

	/*
	 * Create a corresponding file descriptor
	 */
	jobject mFileDescriptor;
	jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
	jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
	jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
	mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
	(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);

	return mFileDescriptor;
}

/*
 * Class:     wzp_demo_serialport_SerialPort
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT jint JNICALL Java_wzp_demo_serialport_SerialPort_jClose
  (JNIEnv *env, jobject thiz) {
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);

	if (NULL == mFd) {
		LOGE("Serial port close failed! Serial port didn't open!");
		return -1;
	}

	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	LOGI("close(fd = %d)", descriptor);

	if (close(descriptor) < 0) {
		LOGE("serial port close failed! unexcepted exception!");
		return -1;
	}

	return 0;
}

/*
 * Class:     wzp_demo_serialport_SerialPort
 * Method:    isOpen
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_wzp_demo_serialport_SerialPort_isOpen
  (JNIEnv *env, jobject thiz) {
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);

	if (NULL == mFd) {
		return 0;
	} else {
		return 1;
	}
}

/*
 * Class:     wzp_demo_serialport_SerialPort
 * Method:    flushBuffer
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_wzp_demo_serialport_SerialPort_flushBuffer
  (JNIEnv *env, jobject thiz, jint mode) {
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);

	if (NULL == mFd) {
		LOGE("FLUSH ERROR! Serial port didn't open!");
		return;
	}

	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	LOGI("Flush: fd = %d", descriptor);
	switch(mode) {
		case 0:
			tcflush(descriptor, TCIFLUSH);
			break;

		case 1:
			tcflush(descriptor, TCOFLUSH);
			break;

		case 2:
			tcflush(descriptor, TCIOFLUSH);
			break;

		default:
			break;
	}
}

