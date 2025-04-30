package com.swolo.lpy.pysx;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;


public class PrinterActivity extends AppCompatActivity {


    private String TAG = PrinterActivity.class.getName();
    private UsbManager usbManager;
    private EditText usbDevicesTv;
    /**
     * 满足的设备
     */
    private UsbDevice myUsbDevice;
    /**
     * usb接口
     */
    private UsbInterface usbInterface;
    /**
     * 块输出端点
     */
    private UsbEndpoint epBulkOut;
    private UsbEndpoint epBulkIn;
    /**
     * 控制端点
     */
    private UsbEndpoint epControl;
    /**
     * 中断端点
     */
    private UsbEndpoint epIntEndpointOut;
    private UsbEndpoint epIntEndpointIn;
    /**
     * 连接
     */
    private UsbDeviceConnection myDeviceConnection;
    Button connect,send,print;
    private PendingIntent mPermissionIntent;
    //  private static final String ACTION_USB_PERMISSION =
    // 		   "com.Android.example.USB_PERMISSION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.swolo.lpy.pysx.R.layout.activity_printer);

        usbDevicesTv = (EditText) findViewById(com.swolo.lpy.pysx.R.id.usb_device_tv);
        send=(Button) findViewById(com.swolo.lpy.pysx.R.id.send);
        connect=(Button) findViewById(com.swolo.lpy.pysx.R.id.connect);
        print=(Button) findViewById(com.swolo.lpy.pysx.R.id.print);


        //1)创建usbManager
        usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        //2)获取到所有设备 选择出满足的设备
        enumeraterDevices();
        //3)查找设备接口
        getDeviceInterface();
        //4)获取设备endpoint
        assignEndpoint();
        //5)打开conn连接通道
        openDevice();


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//连接usb
                //1)创建usbManager
                usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
                //2)获取到所有设备 选择出满足的设备
                enumeraterDevices();
                //3)查找设备接口
                getDeviceInterface();
                //4)获取设备endpoint
                assignEndpoint();
                //5)打开conn连接通道
                openDevice();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//6.发送数据 String xx = "0D";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            String message="你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国你好中国"+"\n";
                            String ms2="absfsngngndnndmhfmfmjfmjmfjj";
                            sendMessageToPoint(message.getBytes("gbk"));
                        }catch (Exception e){
                        }
                    }
                }).start();
            }
        });
    }



    /**
     * 枚举设备
     */
    @SuppressLint("NewApi")
    public void enumeraterDevices(){
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        StringBuilder sb = new StringBuilder();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            sb.append(devicesString(device));
//                    sb.append(device.toString());
//                    sb.append("\n\n");
//            if (device.getVendorId() !=3034&&device.getVendorId() !=2965&&device.getVendorId() !=26728&&device.getVendorId() !=33923 ) {
//                myUsbDevice = device; // 获取USBDevice，此处为筛选去掉我当前设备已连接的USB设备，不通设备筛选的不同。
//            }
            if (device.getVendorId() == 26728 && device.getProductId() == 1280) {
                myUsbDevice = device; // 获取USBDevice
            }
        }
        usbDevicesTv.setText(sb);

    }

    /**
     * usb设备的信息
     * @param device
     * @return
     */
    @SuppressLint("NewApi")
    public String devicesString(UsbDevice device){
        StringBuilder builder = new StringBuilder("UsbDevice\nName=" + device.getDeviceName() +
                "\nVendorId=" + device.getVendorId() + "\nProductId=" + device.getProductId() +
                "\nmClass=" + device.getClass() + "\nmSubclass=" + device.getDeviceSubclass() +
                "\nmProtocol=" + device.getDeviceProtocol() + "\nmManufacturerName=" +"\nmSerialNumber=" +
                "\n\n");
        return builder.toString();
    }

    /**
     * 获取设备的接口
     */
    @SuppressLint("NewApi")
    private void getDeviceInterface() {
        if(myUsbDevice!=null){
            Log.i(TAG,"interfaceCounts : "+myUsbDevice.getInterfaceCount());
            usbInterface = myUsbDevice.getInterface(0);
            System.out.println("成功获得设备接口:" + usbInterface.getId());

            Log.e("USB:", "成功获得设备接口:" + usbInterface.getId());
        }
    }

    /**
     * 分配端点，IN | OUT，即输入输出；可以通过判断
     */
    @SuppressLint("NewApi")
    private void assignEndpoint() {
        if(usbInterface!=null){
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint ep = usbInterface.getEndpoint(i);
                switch (ep.getType()){
                    case UsbConstants.USB_ENDPOINT_XFER_BULK://块
                        if(UsbConstants.USB_DIR_OUT==ep.getDirection()){//输出
                            epBulkOut = ep;
                            System.out.println("Find the BulkEndpointOut," + "index:" + i + "," + "使用端点号："+ epBulkOut.getEndpointNumber());
                        }else{
                            epBulkIn = ep;
                            System.out .println("Find the BulkEndpointIn:" + "index:" + i+ "," + "使用端点号："+ epBulkIn.getEndpointNumber());
                        }
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_CONTROL://控制
                        epControl = ep;
                        System.out.println("find the ControlEndPoint:" + "index:" + i+ "," + epControl.getEndpointNumber());
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_INT://中断
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {//输出
                            epIntEndpointOut = ep;
                            System.out.println("find the InterruptEndpointOut:" + "index:" + i + ","  + epIntEndpointOut.getEndpointNumber());
                        }
                        if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
                            epIntEndpointIn = ep;
                            System.out.println("find the InterruptEndpointIn:" + "index:" + i + ","+ epIntEndpointIn.getEndpointNumber());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 连接设备
     */
    @SuppressLint("NewApi")
    public void openDevice() {
        if(usbInterface!=null){//接口是否为null
            // 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限
            UsbDeviceConnection conn = null;
            if(usbManager.hasPermission(myUsbDevice)){
                //有权限，那么打开
                conn = usbManager.openDevice(myUsbDevice);
            }else {//没有权限，申请权限
                mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED), 0);
                usbManager.requestPermission(myUsbDevice, mPermissionIntent);
                if(usbManager.hasPermission(myUsbDevice)){
                    //有权限，那么打开
                    conn = usbManager.openDevice(myUsbDevice);
                }
            }
            if(null==conn){
                Toast.makeText(this,"不能连接到设备", Toast.LENGTH_SHORT).show();
                return;
            }
            //打开设备
            if(conn.claimInterface(usbInterface,true)){
                myDeviceConnection = conn;
                if (myDeviceConnection != null)// 到此你的android设备已经连上zigbee设备
                    System.out.println("open设备成功！");
                final String mySerial = myDeviceConnection.getSerial();
                System.out.println("设备serial number：" + mySerial);
            } else {
                System.out.println("无法打开连接通道。");
                Toast.makeText(this,"无法打开连接通道。",Toast.LENGTH_SHORT).show();
                conn.close();
            }
        }
    }

    /**
     * 发送数据
     * @param buffer
     */
    @SuppressLint("NewApi")
    public void sendMessageToPoint(byte[] buffer) {
        if(myDeviceConnection.bulkTransfer(epBulkOut,buffer,buffer.length,0) >= 0){
            //0 或者正数表示成功
            Toast.makeText(this,"发送成功",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this,"发送失败的",Toast.LENGTH_SHORT).show();
        }
    }











}
