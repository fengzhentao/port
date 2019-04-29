package com.company.port;

import com.company.base.AbstractSerialPortProcessor;

/**
 * @Description 蓝牙串口
 * @Author fengzt
 * @Date 2019/4/23
 * @Version 1.0
 **/
public class N2SerialPort extends AbstractSerialPortProcessor {

    public N2SerialPort(String portName, int baudRate) {
        super(portName, baudRate, "n2log");
        log.info("开始连接n2串口");
    }

    @Override
    public void resultMessage(String message) {
        log.info("message=" + message);
    }
}
