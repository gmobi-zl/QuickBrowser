package com.quick.browseruc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import java.util.List;

public class MainActivity extends Activity {

    private static final String START_DIALOG_FLAG = "startDialog";
    private static final String DEFAULT_URL = "http://www.baidu.com";

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
            closeMe();
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
                        closeMe();
                    }
                });

        builder.setNegativeButton(context.getResources().getString(R.string.app_start_msg_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //Toast.makeText(MainActivity.this, "negative: " + which, Toast.LENGTH_SHORT).show();
                        closeMe();
                    }
                });

        builder.show();
    }

    private void closeMe(){
        finish();
    }

    private void startUCBrowser(Context context){
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            //intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(DEFAULT_URL);
            intent.setData(content_url);

            if (hasApplication(context, "com.UCMobile"))
                intent.setClassName("com.UCMobile","com.UCMobile.main.UCMobile");
            else{
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e){

        }
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

}
