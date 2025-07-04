package com.swolo.lpy.pysx.main.gp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.printer.command.EscCommand;
import com.printer.command.LabelCommand;
import com.swolo.lpy.pysx.R;
import com.swolo.lpy.pysx.main.DeviceConnFactoryManager;
import com.swolo.lpy.pysx.main.StockOutActivity;
import com.swolo.lpy.pysx.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED;
import static android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED;
import static com.swolo.lpy.pysx.main.gp.Constant.ACTION_USB_PERMISSION;
import static com.swolo.lpy.pysx.main.gp.Constant.MESSAGE_UPDATE_PARAMETER;
import static com.swolo.lpy.pysx.main.DeviceConnFactoryManager.ACTION_CONN_STATE;
import static com.swolo.lpy.pysx.main.DeviceConnFactoryManager.ACTION_QUERY_PRINTER_STATE;
import static com.swolo.lpy.pysx.main.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class GpActivity extends AppCompatActivity {
    private static final String	TAG	= "MainActivity";
    ArrayList<String> per	= new ArrayList<>();
    private UsbManager usbManager;
    private int			counts;
    private static final int	REQUEST_CODE = 0x004;

    /**
     * 连接状态断开
     */
    private static final int CONN_STATE_DISCONN = 0x007;


    /**
     * 使用打印机指令错误
     */
    private static final int PRINTER_COMMAND_ERROR = 0x008;


    /**
     * ESC查询打印机实时状态指令
     */
    private byte[] esc = { 0x10, 0x04, 0x02 };


    /**
     * CPCL查询打印机实时状态指令
     */
    private byte[] cpcl = { 0x1b, 0x68 };


    /**
     * TSC查询打印机状态指令
     */
    private byte[] tsc = { 0x1b, '!', '?' };

    private static final int	CONN_MOST_DEVICES	= 0x11;
    private static final int	CONN_PRINTER		= 0x12;
    private PendingIntent mPermissionIntent;
    private				String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH
    };
    private String			usbName;
    private TextView tvConnState;
    private ThreadPool		threadPool;


    /**
     * 判断打印机所使用指令是否是ESC指令
     */
    private int		id = 0;
//    private EditText etPrintCounts;
//    private Spinner mode_sp;
    //    private byte[]		tscmode		= { 0x1f, 0x1b, 0x1f, (byte) 0xfc, 0x01, 0x02, 0x03, 0x33 };
//    private byte[]		cpclmode	= { 0x1f, 0x1b, 0x1f, (byte) 0xfc, 0x01, 0x02, 0x03, 0x44 };
//    private byte[]		escmode		= { 0x1f, 0x1b, 0x1f, (byte) 0xfc, 0x01, 0x02, 0x03, 0x55 };
//    private byte[]		selftest	= { 0x1f, 0x1b, 0x1f, (byte) 0x93, 0x10, 0x11, 0x12, 0x15, 0x16, 0x17, 0x10, 0x00 };
    private int		printcount	= 0;
    private boolean		continuityprint = false;
    /* private KeepConn keepConn; */

    private Button toMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gp);

        usbManager = (UsbManager) getSystemService( Context.USB_SERVICE );
        checkPermission();
        requestPermission();
        tvConnState	= (TextView) findViewById( R.id.tv_connState );

        toMain = (Button) findViewById(R.id.to_main);
        toMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GpActivity.this, StockOutActivity.class));

            }
        });

//        etPrintCounts	= (EditText) findViewById( R.id.et_print_counts );

        initsp();
    }






    private void initsp()
    {
        List<String> list = new ArrayList<String>();
        list.add( getString( R.string.str_escmode ) );
        list.add( getString( R.string.str_tscmode ) );
        list.add( getString( R.string.str_cpclmode ) );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( this,
                android.R.layout.simple_spinner_item, list );
        adapter.setDropDownViewResource( android.R.layout.simple_list_item_single_choice );

//        mode_sp = (Spinner) findViewById( R.id.mode_sp );
//        mode_sp.setAdapter( adapter );

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        IntentFilter filter = new IntentFilter( ACTION_USB_PERMISSION );
        filter.addAction( ACTION_USB_DEVICE_DETACHED );
        filter.addAction( ACTION_QUERY_PRINTER_STATE );
        filter.addAction( ACTION_CONN_STATE );
        filter.addAction( ACTION_USB_DEVICE_ATTACHED );

        registerReceiver( receiver, filter );
    }


    private void checkPermission()
    {
        for ( String permission : permissions )
        {
            if ( PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission( this, permission ) )
            {
                per.add( permission );
            }
        }
    }


    private void requestPermission()
    {
        if ( per.size() > 0 )
        {
            String[] p = new String[per.size()];
            ActivityCompat.requestPermissions( this, per.toArray( p ), REQUEST_CODE );
        }
    }


    /**
     * USB连接
     *
     * @param view
     */
    public void btnUsbConn( View view )
    {
        startActivityForResult( new Intent( GpActivity.this, UsbListAcitivity.class ), Constant.USB_REQUEST_CODE );
    }


    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            String action = intent.getAction();
            switch ( action )
            {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra( UsbManager.EXTRA_DEVICE );
                        if ( intent.getBooleanExtra( UsbManager.EXTRA_PERMISSION_GRANTED, false ) )
                        {
                            if ( device != null )
                            {
                                System.out.println( "permission ok for device " + device );
                                usbConn( device );
                            }
                        } else {
                            System.out.println( "permission denied for device " + device );
                        }
                    }
                    break;
                /* Usb连接断开、蓝牙连接断开广播 */
                case ACTION_USB_DEVICE_DETACHED:
                    mHandler.obtainMessage( CONN_STATE_DISCONN ).sendToTarget();
                    break;
                case ACTION_CONN_STATE:
                    int state = intent.getIntExtra( DeviceConnFactoryManager.STATE, -1 );
                    int deviceId = intent.getIntExtra( DeviceConnFactoryManager.DEVICE_ID, -1 );
                    switch ( state )
                    {
                        case DeviceConnFactoryManager.CONN_STATE_DISCONNECT:
                            if ( id == deviceId )
                            {
                                tvConnState.setText( getString( R.string.str_conn_state_disconnect ) );
                            }
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                            tvConnState.setText( getString( R.string.str_conn_state_connecting ) );
                            break;
                        case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                            tvConnState.setText( getString( R.string.str_conn_state_connected ) + "\n" + getConnDeviceInfo() );
                            /*
                             *                            if(DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].connMethod== DeviceConnFactoryManager.CONN_METHOD.WIFI){
                             *                                wificonn=true;
                             *                                if(keepConn==null) {
                             *                                    keepConn = new KeepConn();
                             *                                    keepConn.start();
                             *                                }
                             *                            }
                             */
                            break;
                        case CONN_STATE_FAILED:
                            Utils.toast( GpActivity.this, getString( R.string.str_conn_fail ) );
                            /* wificonn=false; */
                            tvConnState.setText( getString( R.string.str_conn_state_disconnect ) );
                            break;
                        default:
                            break;
                    }
                    break;
                case ACTION_QUERY_PRINTER_STATE:
                    if ( counts >= 0 )
                    {
                        if ( continuityprint )
                        {
                            printcount++;
                            Utils.toast( GpActivity.this, getString( R.string.str_continuityprinter ) + " " + printcount );
                        }
                        if ( counts != 0 )
                        {
//                            sendContinuityPrint();
                        }else {
                            continuityprint = false;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage( Message msg )
        {
            switch ( msg.what )
            {
                case CONN_STATE_DISCONN:
                    if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null || !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState() )
                    {
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].closePort( id );
                        Utils.toast( GpActivity.this, getString( R.string.str_disconnect_success ) );
                    }
                    break;
                case PRINTER_COMMAND_ERROR:
                    Utils.toast( GpActivity.this, getString( R.string.str_choice_printer_command ) );
                    break;
                case CONN_PRINTER:
                    Utils.toast( GpActivity.this, getString( R.string.str_cann_printer ) );
                    break;
                case MESSAGE_UPDATE_PARAMETER:
                    String strIp = msg.getData().getString( "Ip" );
                    String strPort = msg.getData().getString( "Port" );
                    /* 初始化端口信息 */
                    new DeviceConnFactoryManager.Build()
                            /* 设置端口连接方式 */
                            .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.WIFI )
                            /* 设置端口IP地址 */
                            .setIp( strIp )
                            /* 设置端口ID（主要用于连接多设备） */
                            .setId( id )
                            /* 设置连接的热点端口号 */
                            .setPort( Integer.parseInt( strPort ) )
                            .build();
                    threadPool = ThreadPool.getInstantiation();
                    threadPool.addTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    } );
                    break;
                default:
                    new DeviceConnFactoryManager.Build()
                            /* 设置端口连接方式 */
                            .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.WIFI )
                            /* 设置端口IP地址 */
                            .setIp( "192.168.2.227" )
                            /* 设置端口ID（主要用于连接多设备） */
                            .setId( id )
                            /* 设置连接的热点端口号 */
                            .setPort( 9100 )
                            .build();
                    threadPool.addTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    } );
                    break;
            }
        }
    };

    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver( receiver );
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.e( TAG, "onDestroy()" );
        DeviceConnFactoryManager.closeAllPort();
        if ( threadPool != null )
        {
            threadPool.stopThreadPool();
            threadPool = null;
        }
    }


    private String getConnDeviceInfo()
    {
        String				str				= "";
        DeviceConnFactoryManager	deviceConnFactoryManager	= DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id];
        if ( deviceConnFactoryManager != null
                && deviceConnFactoryManager.getConnState() )
        {
            if ( "USB".equals( deviceConnFactoryManager.getConnMethod().toString() ) )
            {
                str	+= "USB\n";
                str	+= "USB Name: " + deviceConnFactoryManager.usbDevice().getDeviceName();
            } else if ( "WIFI".equals( deviceConnFactoryManager.getConnMethod().toString() ) )
            {
                str	+= "WIFI\n";
                str	+= "IP: " + deviceConnFactoryManager.getIp() + "\t";
                str	+= "Port: " + deviceConnFactoryManager.getPort();
            } else if ( "BLUETOOTH".equals( deviceConnFactoryManager.getConnMethod().toString() ) )
            {
                str	+= "BLUETOOTH\n";
                str	+= "MacAddress: " + deviceConnFactoryManager.getMacAddress();
            } else if ( "SERIAL_PORT".equals( deviceConnFactoryManager.getConnMethod().toString() ) )
            {
                str	+= "SERIAL_PORT\n";
                str	+= "Path: " + deviceConnFactoryManager.getSerialPortPath() + "\t";
                str	+= "Baudrate: " + deviceConnFactoryManager.getBaudrate();
            }
        }
        return(str);
    }


    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        if ( resultCode == RESULT_OK )
        {
            switch ( requestCode )
            {
                /*蓝牙连接*/
                case Constant.BLUETOOTH_REQUEST_CODE: {
                    closeport();
                    /*获取蓝牙mac地址*/
//                    String macAddress = data.getStringExtra( BluetoothDeviceList.EXTRA_DEVICE_ADDRESS );
                    /* 初始化话DeviceConnFactoryManager */
                    new DeviceConnFactoryManager.Build()
                            .setId( id )
                            /* 设置连接方式 */
                            .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH )
                            /* 设置连接的蓝牙mac地址 */
//                            .setMacAddress( macAddress )
                            .build();
                    /* 打开端口 */
                    Log.d(TAG, "onActivityResult: 连接蓝牙"+id);
                    threadPool = ThreadPool.getInstantiation();
                    threadPool.addTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    } );

                    break;
                }
                /*USB连接*/
                case Constant.USB_REQUEST_CODE: {
                    closeport();
                    /* 获取USB设备名 */
//                    usbName = data.getStringExtra( UsbListAcitivity.USB_NAME );
                    usbName = data.getStringExtra("usb_name");

                    /* 通过USB设备名找到USB设备 */
                    UsbDevice usbDevice = Utils.getUsbDeviceFromName( GpActivity.this, usbName );
                    /* 判断USB设备是否有权限 */
                    if ( usbManager.hasPermission( usbDevice ) )
                    {
                        usbConn( usbDevice );
                    } else {        /* 请求权限 */
                        mPermissionIntent = PendingIntent.getBroadcast( this, 0, new Intent( ACTION_USB_PERMISSION ), 0 );
                        usbManager.requestPermission( usbDevice, mPermissionIntent );
                    }
                    break;
                }
                /*串口连接*/
                case Constant.SERIALPORT_REQUEST_CODE:
                    closeport();
                    /* 获取波特率 */
                    int baudrate = data.getIntExtra( Constant.SERIALPORTBAUDRATE, 0 );
                    /* 获取串口号 */
                    String path = data.getStringExtra( Constant.SERIALPORTPATH );

                    if ( baudrate != 0 && !TextUtils.isEmpty( path ) )
                    {
                        /* 初始化DeviceConnFactoryManager */
                        new DeviceConnFactoryManager.Build()
                                /* 设置连接方式 */
                                .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.SERIAL_PORT )
                                .setId( id )
                                /* 设置波特率 */
                                .setBaudrate( baudrate )
                                /* 设置串口号 */
                                .setSerialPort( path )
                                .build();
                        /* 打开端口 */
                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                    }
                    break;
                case CONN_MOST_DEVICES:
                    id = data.getIntExtra( "id", -1 );
                    if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null &&
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState() )
                    {
                        tvConnState.setText( getString( R.string.str_conn_state_connected ) + "\n" + getConnDeviceInfo() );
                    } else {
                        tvConnState.setText( getString( R.string.str_conn_state_disconnect ) );
                    }
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 重新连接回收上次连接的对象，避免内存泄漏
     */
    private void closeport()
    {
        if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null &&DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort != null )
        {
//            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader.cancel();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort.closePort();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort = null;
        }
    }

    /**
     * usb连接
     *
     * @param usbDevice
     */
    private void usbConn( UsbDevice usbDevice )
    {
        new DeviceConnFactoryManager.Build()
                .setId( id )
                .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.USB )
                .setUsbDevice( usbDevice )
                .setContext( this )
                .build();
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
    }

    /**
     * 打印标签
     * @param view
     */
    public void btnLabelPrint(View view )
    {
        threadPool = ThreadPool.getInstantiation();
        threadPool.addTask( new Runnable()
        {
            @Override
            public void run()
            {
                if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                        !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState() )
                {
                    mHandler.obtainMessage( CONN_PRINTER ).sendToTarget();
                    return;
                }
                if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.TSC )
                {
                    sendLabel();
                } else {
                    mHandler.obtainMessage( PRINTER_COMMAND_ERROR ).sendToTarget();
                }
            }
        } );
    }


    /**
     * 发送标签
     */
    void sendLabel()
    {
        LabelCommand tsc = new LabelCommand();
        /* 撕纸模式开启 */
        tsc.addTear( EscCommand.ENABLE.ON );
        /* 设置标签尺寸，按照实际尺寸设置 */
        tsc.addSize( 40, 60 );
        /* 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0 */
        tsc.addGap( 10 );
        /* 设置打印方向 */
        tsc.addDirection( LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL );
        /* 开启带Response的打印，用于连续打印 */
        tsc.addQueryPrinterStatus( LabelCommand.RESPONSE_MODE.ON );
        /* 设置原点坐标 */
        tsc.addReference( 0, 0 );
        /* 清除打印缓冲区 */
        tsc.addCls();
        /* 绘制简体中文 */
        String name = "金针菇";
        tsc.addText( 50, 400, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2,
                name);
        String weight = "30斤";
        tsc.addText( 50, 140, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_2, LabelCommand.FONTMUL.MUL_2,
                weight );
        /* 绘制图片 */
        Bitmap b = BitmapFactory.decodeResource( getResources(), R.drawable.printer);
//        tsc.addBitmap( 20, 120, LabelCommand.BITMAP_MODE.OVERWRITE, 250, b );

//        tsc.addQRCode( 10, 330, LabelCommand.EEC.LEVEL_L, 5, LabelCommand.ROTATION.ROTATION_0, "Printer" );
        /* 绘制一维条码 */
//        tsc.add1DBarcode( 10, 350, LabelCommand.BARCODETYPE.CODE128, 100, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, "SMARNET" );

        tsc.addText(100, 300, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_270, LabelCommand.FONTMUL.MUL_4, LabelCommand.FONTMUL.MUL_4,
                "35001" );

//        tsc.addText(100, 420, LabelCommand.FONTTYPE.TRADITIONAL_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
//                "繁體字" );

//        tsc.addText(190, 450, LabelCommand.FONTTYPE.KOREAN, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
//                "한국어" );

        /* 打印标签 */
        tsc.addPrint( 1, 1 );
        /* 打印标签后 蜂鸣器响 */

        tsc.addSound( 2, 100 );
        tsc.addCashdrwer( LabelCommand.FOOT.F5, 255, 255 );
        Vector<Byte> datas = tsc.getCommand();
        /* 发送数据 */
        if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null )
        {
            Log.d(TAG, "sendLabel: 打印机为空");
            return;
        }
        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately( datas );
    }






}
