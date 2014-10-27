package me.mariotti.playground;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import me.mariotti.adk.Manager;


public class Playground extends Activity {
    private Manager mManager;
    private boolean switchLed13 = false;
    private boolean switchLed12 = false;
    private boolean blinkLed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_lamp);
        TextView t = (TextView) findViewById(R.id.textView);
        mManager = new Manager((UsbManager) getSystemService(Context.USB_SERVICE), t, this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);   //Prevent the auto-pop of keyboard
    }

    @Override
    protected void onStop() {
        mManager.closeAccessory();     //aggiunto
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.led_lamp, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void switchLight13(View v) {
        if (blinkLed) {
            ((Switch) findViewById(R.id.BlinkLed)).toggle();
            blinkLight(v);
        }
        switchLed13 = !switchLed13;
        int command = switchLed13 ? 1 : 0;
        mManager.writeSerial(command);
    }

    public void switchLight12(View v) {
        if (blinkLed) {
            ((Switch) findViewById(R.id.BlinkLed)).toggle();
            blinkLight(v);
        }
        switchLed12 = !switchLed12;
        int command = switchLed12 ? 3 : 2;
        mManager.writeSerial(command);
    }

    public void blinkLight(View v) {
        if (switchLed12) {
            ((Switch) findViewById(R.id.secondLed)).toggle();
            switchLight12(v);
        }
        if (switchLed13) {
            ((Switch) findViewById(R.id.firstLed)).toggle();
            switchLight13(v);
        }
        blinkLed = !blinkLed;
        int command = blinkLed ? 5 : 4;
        mManager.writeSerial(command);
        ((EditText) findViewById(R.id.editText)).setEnabled(blinkLed);
        ((Button) findViewById(R.id.button)).setEnabled(blinkLed);
    }
    public void setDelay(View v){
        try {
                 String delay2  =((EditText) findViewById(R.id.editText)).getText().toString();
        int delay=Integer.parseInt(((EditText) findViewById(R.id.editText)).getText().toString());
        mManager.writeSerial(6);
        mManager.writeSerial(delay); }catch (NumberFormatException e){
            Log.w("err", e.toString());}
       //mManager.readSerial();
    }
}
