package com.company.base;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEventListener;

import java.util.List;

/**
 * @Description 串口处理器
 * @Author fengzt
 * @Date 2019/4/23
 * @Version 1.0
 **/
public interface SerialPortProcessor {

    public List<CommPortIdentifier> findPort();

    public void openPort();

    public void clonePort(SerialPort serialPort);

    public void sendToPort(byte[] order);

    public byte[] readFromPort();

}
