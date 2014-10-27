package me.mariotti.adk;

import android.app.Activity;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;


/**
 * Created by simone on 21/10/14.
 */
public class Manager {
    private UsbManager mUsbManager;
    private ParcelFileDescriptor mParcelFileDescriptor;
    private FileOutputStream mFileOutputStream;
    private FileInputStream mFileInputStream;
    private TextView logView;
    private Activity mActivity;

    public Manager(UsbManager usbManager, final TextView logView, final Activity mActivity) {
        this.mUsbManager = usbManager; //USB_Service passed from activity
        this.logView = logView;
        this.mActivity = mActivity;
        openAccessory();  //aggiunto

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logView.append("START: Listener thread\n");
                            }
                        });
                        int ret = 0;
                        final byte[] buffer = new byte[4098];
                        while ( mFileInputStream != null) {
                            try {
                                while(ret >= 0){
                                // never terminates
                                ret = mFileInputStream.read(buffer);
                                if (ret > 0) {
                                    final int finalRet = ret;
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String message = new String(buffer, "UTF-8");
                                                logView.append("RECEIVING: " + finalRet+ " byte: " + message + "\n");
                                                Arrays.fill(buffer, (byte) 0);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            } }catch (Exception e) {
                                Log.e("TAG", "openAccessory(): Could not read inputStream: " + e);
                                logView.append(e.toString());
                            }
                        }
                    }
                }
        ).start();
    }

    public void openAccessory() {
        logView.append("CALLED: openAccessory()\n");
        UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
        if (accessoryList != null && accessoryList.length > 0) {
            try {
                if (!logView.getText().toString().contains("mariotti.me")) {
                    logView.append("Manufacturer: " + accessoryList[0].getManufacturer() + "\n");
                    logView.append("Model: " + accessoryList[0].getModel() + "\n");
                    logView.append("Description: " + accessoryList[0].getDescription() + "\n");
                    logView.append("Version: " + accessoryList[0].getVersion() + "\n");
                    logView.append("URL: " + accessoryList[0].getUri() + "\n");
                    logView.append("Serial: " + accessoryList[0].getSerial() + "\n");
                }
                mParcelFileDescriptor = mUsbManager.openAccessory(accessoryList[0]);
                FileDescriptor file = mParcelFileDescriptor.getFileDescriptor();
                mFileOutputStream = new FileOutputStream(file);
                mFileInputStream = new FileInputStream(file);

            } catch (Exception e) {
                logView.append("FAILED: openAccessory()\n");
            }
        }

    }

    public void closeAccessory() {
        if (mParcelFileDescriptor != null) {
            try {
                logView.append("CALLED: closeAccessory()\n");
                mParcelFileDescriptor.close();
            } catch (IOException e) {
                // Exception logging
            }
        }
        // mParcelFileDescriptor = null;
    }

    public void writeSerial(int value) {
        try {
            logView.append("SENDING: " + value + "\n");
            if (mFileInputStream == null) {
                logView.append("WARNING: called writeSerial() while no stream is open. Trying to acquire a new stream\n");
                resetAccessoryConnection();
            }

            byte[] bufferredString =String.valueOf(value).getBytes(Charset.forName("UTF-8"));
            mFileOutputStream.write(bufferredString);
        } catch (Exception e) {
            logView.append("EXCEPTION writeSerial(): " + e.toString() + "\n");

            resetAccessoryConnection();
            try {
                mFileOutputStream.write(value);
            } catch (IOException e1) {
                logView.append("ERROR: Failed to acquire a new stream\n");
                logView.append("EXCEPTION writeSerial(): " + e1.toString() + "\n");
            }

        }
    }

    public String readSerial() {
        // if (mFileInputStream != null)
        String response;
        try {
            logView.append("leggo " + mFileInputStream.toString());
            byte[] buffer = new byte[8];
            int by = mFileInputStream.read(buffer);
            logView.append(" byte letti " + by);
            logView.append(response = new String(buffer, "UTF-8"));

        } catch (Exception e) {
            logView.append(e.toString());
            response = "Errore";
        }
        return response + "\n";


    }

    private void resetAccessoryConnection() {
        closeAccessory();
        openAccessory();
    }
}