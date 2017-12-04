package com.quick.browseruc;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.List;
import java.util.concurrent.Exchanger;

public class MainActivity extends Activity {

    private static final String START_DIALOG_FLAG = "startDialog";
    private static final String DEFAULT_URL = "http://www.baidu.com";
    private static final String UC_BROWSER_PACKAGENAME = "com.UCMobile";
    private static int MSG_DISMISS_ACTIVITY = 997;
    private BaseToastActivity toastBg = null;

    private Handler mCloseHdl = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(MSG_DISMISS_ACTIVITY == msg.what){
                if(null != toastBg){
                    toastBg.finish();
                    toastBg = null;
                }
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        boolean isShowDialog = SharedPrefsUtil.getValue(this, START_DIALOG_FLAG, true);
        if (isShowDialog)
            showStartMsgDialog(this);
        else{
            startUCBrowser(this);
            delayClose();
        }
    }

    private void showStartMsgDialog(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(context.getResources().getString(R.string.app_start_msg_title));
        //builder.setMessage(context.getResources().getString(R.string.app_start_msg_body));

        View view = LayoutInflater.from(context).inflate(R.layout.view_message_dialog, null);
        builder.setView(view);

        final CheckBox cbNotShow = (CheckBox)view.findViewById(R.id.cbNotShow);
        builder.setPositiveButton(context.getResources().getString(R.string.app_start_msg_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPrefsUtil.putValue(context, START_DIALOG_FLAG, !cbNotShow.isChecked());
                        startUCBrowser(context);
                        delayClose();
                    }
                });

        builder.setNegativeButton(context.getResources().getString(R.string.app_start_msg_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeMe();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    private void delayClose(){
        mCloseHdl.sendEmptyMessageDelayed(MSG_DISMISS_ACTIVITY, 3000);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(3000);
//
//                } catch (Exception e){
//
//                }
//            }
//        }).start();
    }

    private void closeMe(){
        finish();
        //delayClose();
    }

    private void startUCBrowser(Context context){
        try {
            if (hasApplication(context, UC_BROWSER_PACKAGENAME)){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                //intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(DEFAULT_URL);
                intent.setData(content_url);
                intent.setClassName(UC_BROWSER_PACKAGENAME,"com.UCMobile.main.UCMobile");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                //if (isRunning(context) == false){
                    showStartBackground(context);
                //}
            } else{
                openLinkInDefaultBrowser(context, DEFAULT_URL);
            }
        } catch (Exception e){

        }
    }

    private void showStartBackground(Context context){
        toastBg = new BaseToastActivity(context, null);
        ImageView ivBg = new ImageView(context);
        ivBg.setImageDrawable(context.getDrawable(R.drawable.start_bg));
        toastBg.setContentView(ivBg);
    }

    private boolean hasApplication(Context context, String packageName){
        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> listPackageInfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < listPackageInfo.size(); i++) {
            if(listPackageInfo.get(i).packageName.equalsIgnoreCase(packageName)){
                return true;
            }
        }
        return false;
    }

    private boolean isRunning(Context context){
        boolean isAppRunning = false;
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(UC_BROWSER_PACKAGENAME) && info.baseActivity.getPackageName().equals(UC_BROWSER_PACKAGENAME)) {
                isAppRunning = true;
                //find it, break
                break;
            }
        }

        return isAppRunning;
    }

    private void openLinkInDefaultBrowser(Context context, String url){
        try{
            if (url == null)
                url = "http://google.com";
            else if (url.indexOf("://") == -1)
                url = "http://" + url;


            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            final String browserPackage = getBrowserPackage(intent, context);
            if (!browserPackage.isEmpty()) {
                intent.setPackage(browserPackage);
            }

            context.startActivity(intent);
        }catch (Exception e){

        }
    }

    private String getBrowserPackage(Intent intent, Context context) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> appsList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String tmpPkg = "", pkg = "";
        for (ResolveInfo app : appsList) {
            tmpPkg = app.activityInfo.packageName.toLowerCase();
            if ("com.android.chrome".equals(tmpPkg)) {
                return tmpPkg;
            }
            if (tmpPkg.contains("browse") || tmpPkg.contains("chrome")
                    || tmpPkg.contains("com.UCMobile.intl")
                    || tmpPkg.contains("org.mozilla.firefox")
                    || tmpPkg.contains("com.opera.mini.native")) {
                pkg = tmpPkg;
            }
        }
        return pkg;
    }

}
