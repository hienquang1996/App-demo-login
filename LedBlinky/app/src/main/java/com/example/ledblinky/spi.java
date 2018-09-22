package com.example.ledblinky;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;

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
public class spi extends Activity implements View.OnClickListener{


    private Button btn;
    private Button spi;
    private Button back;
    private TextView adc;
    private TextView raw;
    private TextView temp;
    private Button out;

    //SPI Configuration Parameters
    private static final String SPI_DEVICE_NAME = "SPI0.1";
    private SpiDevice mSPIDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spi);
        // final TextView txtConsole = findViewById(R.id.txtConsole);   //con trỏ hằng
        btn = findViewById(R.id.btn);
        spi = findViewById(R.id.spi);
        back = findViewById(R.id.back);
        out = findViewById(R.id.out);



        raw = findViewById(R.id.raw);
        adc = findViewById(R.id.adc);
        temp = findViewById(R.id.temp);
        raw.setText(" ");
        adc.setText(" ");
        temp.setText(" ");


        initSPI();
        // initGPIO();
        //  setupBlinkyTimer();
        btn.setOnClickListener(this);
        spi.setOnClickListener(this);
        back.setOnClickListener(this);
        out.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            closeSPI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSPI()
    {
        PeripheralManager manager = PeripheralManager.getInstance();
        List<String> deviceList = manager.getSpiBusList();
        if(deviceList.isEmpty())
        {
            Log.d(TAG,"No SPI bus is not available");
        }
        else
        {
            Log.d(TAG,"SPI bus available: " + deviceList);
            //check if SPI_DEVICE_NAME is in list
            try {
                mSPIDevice = manager.openSpiDevice(SPI_DEVICE_NAME);

                mSPIDevice.setMode(SpiDevice.MODE1);
                mSPIDevice.setFrequency(1000000);
                mSPIDevice.setBitsPerWord(8);
                mSPIDevice.setBitJustification(SpiDevice.BIT_JUSTIFICATION_MSB_FIRST);


                Log.d(TAG,"SPI: OK... ");


            }catch (IOException e)
            {
                Log.d(TAG,"Open SPI bus fail... ");
            }
        }
    }

    private void closeSPI() throws IOException {
        if(mSPIDevice != null)
        {
            try {
                mSPIDevice.close();
            }finally {
                mSPIDevice = null;
            }

        }
    }

    private void sendCommand(SpiDevice device, byte[] buffer) throws  IOException{

        //send data to slave
        device.write(buffer, buffer.length);


        //read the response
        byte[] response = new byte[2];
        device.read(response, response.length);


        for(int i = 0; i< 2; i++) {

            Log.d(TAG, "Response byte " + Integer.toString(i) + " is: " + response[i]);
        }



        double raw_value = (double)(response[0] * 256 + response[1]);
        double adc_value = raw_value * 6.144/32768; //Check in datasheet for this value and note in your report
        double temp_value = (double)(((response[0]<<8) + response[1])>>2);
        System.out.println("Temp: " + temp_value*0.03125);
        System.out.println("RAW value:" + raw_value);
        System.out.println("ADC value:" + adc_value);
        raw.setText(String.valueOf(raw_value));
        adc.setText(String.valueOf(adc_value));
        temp.setText(String.valueOf(temp_value*0.03125));
    }


    //An example to send 2-bytes command and waiting for 2 bytes response
    @Override
    public void onClick(View view) {

        if(view == btn)
        {
            try {
                byte[] test_data = new byte[4]; //Totally, 4 bytes are sent by the master
                for(int i = 0; i<4;i++)
                {
                    test_data[i] = (byte)(0x00);
                }
                test_data[0] = (byte)(0x70); //ADS111 channel 3 = 70, 2 = 600, 1 = 50, 0 = 40
                test_data[1] = (byte)(0x8b); //Check in datasheet for this value and note in your report
                sendCommand(mSPIDevice, test_data);
            }catch(IOException e)
            {

            }
        }

        if(view == back){
            Intent intent = new Intent(spi.this,menu.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }

        if(view == out){
            Intent intent = new Intent(spi.this,login.class);
            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_LONG).show();
            startActivity(intent);
        }

        if (view == spi){
            try {
                closeSPI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
