package com.quick.browseruc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;


public class BaseToastActivity {

	public static final int UNSET = Integer.MIN_VALUE;

    protected Context context;
    //protected Bundle params;
    protected WindowManager windowManager;
    protected View container = null;

    public BaseToastActivity(Context context, Bundle params){
        this.context = context;
        //this.params = params;
		windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

	public Context getContext() {
		return context;
	}
	
	private final BroadcastReceiver homeListenerReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        final String SYSTEM_DIALOG_REASON_RECENT_KEY = "recentapps";
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null && (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) || 
                		reason.equals(SYSTEM_DIALOG_REASON_RECENT_KEY))) {
                    finish();
                }
            }
        }
    };

	public void setContentView(View view){
		setContentView(view, UNSET, UNSET, UNSET, UNSET, UNSET);
	}
	

	public void setContentView(View view, int gravity, int x, int y, int width, int height) {
		container = view;
		
		IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		context.registerReceiver(homeListenerReceiver, homeFilter);
		
		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		
		params.gravity = gravity == UNSET ? Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL : gravity;
		params.height = height == UNSET ? WindowManager.LayoutParams.MATCH_PARENT : height;
		params.width = width == UNSET ? WindowManager.LayoutParams.MATCH_PARENT : width;
		if (x != UNSET) params.x = x;
		if (y != UNSET) params.y = y;
		params.format = PixelFormat.TRANSLUCENT;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        //        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		params.windowAnimations = android.R.style.Animation_Toast;
		//params.type = WindowManager.LayoutParams.TYPE_TOAST;
        windowManager.addView(container, params);
	}

	public void finish() {
		try{
			if (container != null && windowManager != null){
				context.unregisterReceiver(homeListenerReceiver);
				windowManager.removeView(container);
				container = null;
			}
		}catch(Exception e){
		}
	}
}
