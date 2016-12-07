package com.fairlink.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.fairlink.common.R;

public class ShutDownHint extends Activity {
    public static ShutDownHint instance;
    private TextView      mText;
    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;



    
    protected void onCreate(Bundle savedInstanceState) {
        
        Intent controlHomeButton = new Intent();
        controlHomeButton.setAction("customer.control.homeButton");
        controlHomeButton.putExtra("lock", true);
        sendBroadcast(controlHomeButton);

        super.onCreate(savedInstanceState);
        instance = this;



        setContentView(R.layout.shut_down_hint);

        mText = (TextView) findViewById(R.id.shut_down_inform_txt);

        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);        
        
    }
    
    
    public void setTextContent(String str) {
        mText.setText(str);
    }
    
    protected void onDestroy() {
        Intent controlHomeButton = new Intent();
        controlHomeButton .setAction("customer.control.homeButton");
        controlHomeButton .putExtra("lock", false);
        sendBroadcast(controlHomeButton);
        
        super.onDestroy();
    };
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU){
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
