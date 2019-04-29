package com.company.base;

import gnu.io.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Description 抽象串口处理器
 * @Author fengzt
 * @Date 2019/4/23
 * @Version 1.0
 **/
public abstract class AbstractSerialPortProcessor implements SerialPortProcessor, SerialPortEventListener, Runnable{

    private String portName;
    private int baudRate;
    private CommPortIdentifier portIdentifier;// 串口通信管理类
    private SerialPort serialPort;
    private BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(); // 堵塞队列用来存放读到的数据
    public Log log;

    public AbstractSerialPortProcessor(String portName, int baudRate, String logDir) {
        this.portName = portName;
        this.baudRate = baudRate;
        if (log == null) {
            log = new Log(1,3, logDir);
        }
        openPort();
    }

    /**
     * 查找所有可用端口
     *
     * @return 可用端口名称列表
    */
    @Override
    public List<CommPortIdentifier> findPort() {
        // 获得当前所有可用串口
        Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
        List<CommPortIdentifier> portNameList = new ArrayList<CommPortIdentifier>();
        // 将可用串口添加到List并返回该List
        while (portList.hasMoreElements()) {
            portNameList.add(portList.nextElement());
        }
        return portNameList;
    }

    /**
     * 打开串口
     *
     * @return 串口对象
     */
    @Override
    public void openPort() {
        try {
            // 通过端口名识别端口
            portIdentifier = CommPortIdentifier
                    .getPortIdentifier(portName);
            // 打开端口，设置端口名与timeout（打开操作的超时时间）
            CommPort commPort = portIdentifier.open(portName, baudRate);
            // 判断是不是串口
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                try {
                    // 给串口添加监听器
                    serialPort.addEventListener(this);
                    // 设置当有数据到达时唤醒监听接收线程
                    serialPort.notifyOnDataAvailable(true);
                    // 设置当通信中断时唤醒中断线程
                    serialPort.notifyOnBreakInterrupt(true);
                    // 设置串口的波特率等参数
                    serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                } catch (UnsupportedCommOperationException e) {
                    throw new RuntimeException("设置串口参数失败");
                } catch (TooManyListenersException e) {
                    throw new RuntimeException("监听类对象过多");
                }
            } else {
                throw new RuntimeException("端口指向设备不是串口类型");
            }
        } catch (NoSuchPortException e1) {
            throw new RuntimeException("没有该端口对应的串口设备");
        } catch (PortInUseException e2) {
            throw new RuntimeException("端口已被占用");
        }
        log.info("串口打开成功");
    }

    /**
     * 关闭串口
     */
    @Override
    public void clonePort(SerialPort serialPort) {
        if (serialPort != null) {
            serialPort.close();
        }
    }

    /**
     * 向串口发送数据
     *
     * @param order 待发送数据
     */
    @Override
    public void sendToPort(byte[] order) {
        OutputStream out = null;
        try {
            out = serialPort.getOutputStream();
            out.write(order);
            out.flush();
        } catch (IOException e) {
            System.out.println("向串口发送数据失败");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                System.out.println("关闭串口对象的输出流出错");
            }
        }
    }

    /**
     * 从串口读取数据
     *
     * @return 读取到的数据
     */
    @Override
    public byte[] readFromPort() {
        InputStream inputStream = null;
        byte[] bytes = new byte[1024];
        try {
            // 获取buffer里的数据长度
            inputStream = serialPort.getInputStream();
            int buffLength = inputStream.available();
            while (buffLength != 0) {
                // 初始化byte数组为buffer中数据的长度
                inputStream.read(bytes);
                buffLength = inputStream.available();
            }
        } catch (IOException e) {
            System.out.println("从串口读取数据时出错");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                System.out.println("关闭串口对象输入流出错");
            }
        }
        return bytes;
    }

    public static void sleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        switch (serialPortEvent.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:// 当有可用数据时读取数据
                sleep(2000);
                byte[] readBuffer = readFromPort();
                String needData = new String(readBuffer);
                msgQueue.add(needData);
            default:
                break;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (msgQueue.size()>0) {
                try {
                    resultMessage(msgQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public abstract void resultMessage(String message);
}
