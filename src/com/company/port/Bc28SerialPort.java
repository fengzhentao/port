package com.company.port;

import com.company.base.AbstractSerialPortProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description BC28串口
 * @Author fengzt
 * @Date 2019/4/23
 * @Version 1.0
 **/
public class Bc28SerialPort extends AbstractSerialPortProcessor{

    //重试次数
    private int i = 0;

    public Bc28SerialPort(String portName, int baudRate) {
        super(portName, baudRate, "bc28log");
        log.info("开始连接bc28串口");
        restart();
        bc28Command();
    }

    @Override
    public void resultMessage(String message) {
//                    System.out.println(message);
        message = message.replaceAll("\\s*", "").trim();
        log.info(" message="+message);
        if (message.startsWith("ERROR")) {
            i++;
            log.info("重试次数="+i);
            bcMqtt();
        }
        if (message.contains("CSQ")) {
            log.info("信号量="+message);
        }
        if ("+QMTOPEN:0,0".equals(message) || "OK+QMTOPEN:0,0".equals(message)) {
            log.info("打开成功");
            conn();
        }
        if ("+QMTOPEN:0,-1".equals(message) || "OK+QMTOPEN:0,-1".equals(message)) {
            log.error("打开失败");
            bcMqtt();
        }
        if ("+QMTCONN:0,0,0".equals(message) || "OK+QMTCONN:0,0,0".equals(message)) {
            log.info("连接成功");
            topic();
        }
        if ("+QMTSTAT:0,1+QMTCONN:0,2".equals(message) || "OK+QMTCONN:0,0,0".equals(message)
                || "+QMTCONN:0,2".equals(message) || "+QMTSTAT:0,1".equals(message)
                || "+QMTCONN:0,2+QMTSTAT:0,3".equals(message) || "+QMTCONN:0,2+QMTSTAT:0,1".equals(message)
                || "+QMTCONN:0,2+QMTSTAT:0,3+QMTSTAT:0,1".equals(message)) {
            log.error("连接失败");
            csq();
            topic();
            i = 0;
        }
        if ("+QMTSUB:0,5556,0,0".equals(message) || "OK+QMTSUB:0,5556,0,0".equals(message)) {
            log.info("订阅成功");
            i = 0;
        }
        if ("+QMTCLOSE:0,0".equals(message) || "OK+QMTCLOSE:0,0".equals(message)) {
            log.error("断开连接");
            i = 0;
            bcMqtt();
        }
    }

    public void bc28Command() {
        List<String> commandList = new ArrayList<>();
        commandList.add("AT\n");
        commandList.add("ATI\n");
        commandList.add("AT+CGSN\n");
        commandList.add("AT+CFUN?\n");
        commandList.add("AT+CIMI\n");
        commandList.add("AT+CEREG?\n");
        commandList.add("AT+CGATT?\n");
        commandList.add("AT+CGPADDR\n");
        commandList.add("AT+CSQ\n");
        commandList.stream().forEach(c -> {
            sleep(3000);
            log.info("command="+c);
            sendToPort(c.getBytes());
        });
        version();
        open();
    }

    public void bcMqtt() {
        restart();
        csq();
        version();
        open();
    }

    public void csq() {
        sleep(2000);
        log.info(" command="+"csq");
        sendToPort("AT+CSQ\n".getBytes());
    }
    public void version() {
        sleep(3000);
        log.info(" command="+"version");
        sendToPort("AT+QMTCFG=\"version\",0,4\n".getBytes());
    }
    public void open() {
        sleep(3000);
        log.info("command="+"open");
        sendToPort("AT+QMTOPEN=0,\"47.101.191.32\",1883\n".getBytes());
    }
    public void conn() {
        sleep(6000);
        log.info("command="+"conn");
        sendToPort("AT+QMTCONN=0,\"0000000E65FD5060000003E8\",\"0000000E65FD50605CC11A77\",\"123456\"\n".getBytes());
    }
    public void topic() {
        sleep(10000);
        log.info("command="+"topic");
        sendToPort("AT+QMTSUB=0,5556,\"/iot/0000000E65FD5060/c2d\",0\n".getBytes());
    }
    public void unConn() {
        sleep(20000);
        log.info("command="+"unConn");
        sendToPort("AT+QMTCLOSE=0\n".getBytes());
    }
    public void restart() {
        log.info("command="+"restart");
        sleep(2000);
        sendToPort("AT+NRB\n".getBytes());
        sleep(20000);
    }
}
