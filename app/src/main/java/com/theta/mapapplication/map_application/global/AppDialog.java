package com.theta.mapapplication.map_application.global;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.theta.mapapplication.R;

/**
 * Created by ashish on 15/12/17.
 */

public class AppDialog {
    public static void showAppSettingDialogWithPositiveButton(Context context, String title, String msg,
                                                              DialogInterface.OnClickListener positiveClick){
        AlertDialog alertDialog = null;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle(title);
        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(context.getText(R.string.txt_countinue), positiveClick);

        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        // show it
        alertDialog.show();
    }
}
