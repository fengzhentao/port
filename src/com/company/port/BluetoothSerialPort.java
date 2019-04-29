package com.company.port;

import com.company.base.AbstractSerialPortProcessor;

/**
 * @Description 蓝牙串口
 * @Author fengzt
 * @Date 2019/4/23
 * @Version 1.0
 **/
public class BluetoothSerialPort extends AbstractSerialPortProcessor {

    public BluetoothSerialPort(String portName, int baudRate) {
        super(portName, baudRate, "bluetoothLog");
        log.info("开始连接蓝牙串口" );
    }

    @Override
    public void resultMessage(String message) {
//        message = message.replaceAll("\\s*", "").trim();
        log.info("message=" + message);
    }
}
