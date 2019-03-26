package it.uniroma1.boubouk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;


public class Util {

    public static boolean checkConnection(Context context) {
        boolean isConnected = isConnected(context);
        if (!isConnected) {
            createSingleButtonAlertDialog(context,
                    context.getString(R.string.warning),
                    context.getString(R.string.check_connection),
                    context.getString(R.string.ok),
                    true
            );
        }
        return isConnected;
    }

    public static AlertDialog createDoubleButtonAlertDialog(
            final Context context, String title, String msg, String buttonStringPositive,
            String buttonStringNegative, boolean cancelable) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(cancelable)
                .setPositiveButton(buttonStringPositive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                )
                .setNegativeButton(buttonStringNegative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(cancelable);
        return alertDialog;
    }

    public static AlertDialog createSingleButtonAlertDialog(
            final Context context, String title, String msg, String buttonString,
            boolean cancelable) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(title)
                .setMessage(msg)
                .setCancelable(cancelable)
                .setPositiveButton(buttonString, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(cancelable);
        return alertDialog;
    }

    public static AlertDialog createDoubleButtonAlertDialogWithCustomView(
            final Context context, String title, View view, String buttonStringPositive,
            String buttonStringNegative, boolean cancelable) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(title)
                .setView(view)
                .setCancelable(cancelable)
                .setPositiveButton(buttonStringPositive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                )
                .setNegativeButton(buttonStringNegative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(cancelable);
        return alertDialog;
    }

    public static AlertDialog createSingleButtonAlertDialogWithCustomView(
            final Context context, String title, View view, String buttonString,
            boolean cancelable) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(title)
                .setView(view)
                .setCancelable(cancelable)
                .setPositiveButton(buttonString, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(cancelable);
        return alertDialog;
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }
    }
}
