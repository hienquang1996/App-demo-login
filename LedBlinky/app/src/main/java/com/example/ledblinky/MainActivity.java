package com.example.ledblinky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ledblinky.MVVM.VM.NPNHomeViewModel;
import com.example.ledblinky.MVVM.View.NPNHomeView;
import com.example.ledblinky.Network.ApiResponseListener;
import com.example.ledblinky.Network.VolleyRemoteApiClient;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class MainActivity extends Activity implements View.OnClickListener, NPNHomeView{

    private static final String TAG = "NPNIoTs";

    //GPIO Configuration Parameters
    private static final String LED_PIN_NAME = "BCM26"; // GPIO port wired to the LED
    private Gpio mLedGpio;

    // UART Configuration Parameters
    private static final int BAUD_RATE = 115200;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private UartDevice mUartDevice;
    private HandlerThread mInputThread;
    private Handler mInputHandler;

    private static final int CHUNK_SIZE = 512;

    Timer mBlinkyTimer;
    private Button btnSetting;
    private Button out;
    private Button back;

    NPNHomeViewModel mHomeViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView txtConsole = findViewById(R.id.txtConsole);   //con trỏ hằng
        btnSetting = findViewById(R.id.btnSetting);
        out = findViewById(R.id.out);
        back = findViewById(R.id.back);
        final EditText txtTemperature = findViewById(R.id.txtTemperature);
        final EditText txtPressure = findViewById(R.id.txtPressure);
        final Context context= this;
        mHomeViewModel = new NPNHomeViewModel();
        mHomeViewModel.attach(this, this);


        initGPIO();
        initUart();
        setupBlinkyTimer();
        btnSetting.setOnClickListener(this);
        back.setOnClickListener(this);
        out.setOnClickListener(this);



    }

    @Override
    public void onSuccessUpdateServer(String message) {
        Log.d(TAG, "Request server is successful");
    }

    @Override
    public void onErrorUpdateServer(String message) {
        Log.d(TAG, "Request server is fail");
    }

    @Override
    public void onClick(View view) {
            if (view == btnSetting) {
                String url = "http://demo1.chipfc.com/Device/UpdateDeviceStatus/?name=" + "Quang"
                        + "&temperature=" + Integer.toString(35)
                        + "&rssi=" + Integer.toString(35)
                        + "&soc=" + Integer.toString(35)
                        + "&Twi=" + Integer.toString(35);
                mHomeViewModel.updateToServer(url);
            }

            if(view == back){
                Intent intent = new Intent(MainActivity.this,menu.class);
                Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
                startActivity(intent);
            }

        if(view == out){
            Intent intent = new Intent(MainActivity.this,login.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }
    }
    private void setupBlinkyTimer()
    {
        mBlinkyTimer = new Timer();
        TimerTask blinkyTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    mLedGpio.setValue(!mLedGpio.getValue());
                }catch(Throwable t)
                {
                    Log.d(TAG, "Error in Blinky LED");
                }
            }
        };
        mBlinkyTimer.schedule(blinkyTask, 2000, 1000);
    }
    private void initGPIO()
    {
        PeripheralManager manager = PeripheralManager.getInstance();
        try {
            mLedGpio = manager.openGpio(LED_PIN_NAME);
            // Step 2. Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        } catch (IOException e) {
            Log.d(TAG, "Error on PeripheralIO API");
        }
    }
    private void initUart()
    {
        try {
            openUart("UART0", BAUD_RATE);
        }catch (IOException e) {
            Log.d(TAG, "Error on UART API");
        }
    }
    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
           //read data from Rx buffer
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int noBytes = -1;
                while ((noBytes = mUartDevice.read(buffer, buffer.length)) > 0) {
                    Log.d(TAG,"Number of bytes: " + Integer.toString(noBytes));
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    private void openUart(String name, int baudRate) throws IOException {
        mUartDevice = PeripheralManager.getInstance().openUartDevice(name);
        // Configure the UART
        mUartDevice.setBaudrate(baudRate);
        mUartDevice.setDataSize(DATA_BITS);
        mUartDevice.setParity(UartDevice.PARITY_NONE);
        mUartDevice.setStopBits(STOP_BITS);

        mUartDevice.registerUartDeviceCallback(mInputHandler, mCallback);
    }

    private void closeUart() throws IOException {
        if (mUartDevice != null) {
            mUartDevice.unregisterUartDeviceCallback(mCallback);
            try {
                mUartDevice.close();
            } finally {
                mUartDevice = null;
            }
        }
    }
}

