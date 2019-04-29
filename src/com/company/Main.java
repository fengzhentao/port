package com.company;

//import com.company.port.Bc28SerialPort;
//import com.company.port.BluetoothSerialPort;
import com.company.port.N2SerialPort;

public class Main {

    public static void main(String[] args) {

//        Bc28SerialPort bc28SerialPort = new Bc28SerialPort("COM6", 9600);
//        new Thread(bc28SerialPort).start();

//        BluetoothSerialPort bluetoothSerialPort = new BluetoothSerialPort("COM6", 115200);
//        new Thread(bluetoothSerialPort).start();

        N2SerialPort n2SerialPort = new N2SerialPort("COM6", 9600);
        new Thread(n2SerialPort).start();
    }
}
