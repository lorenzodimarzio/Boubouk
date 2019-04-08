package it.uniroma1.boubouk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


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

    public static ProgressDialog createProgressDialog(Context context, String msg) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(true);
        progressDialog.setMessage(msg);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return progressDialog;
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

    public static boolean fileExists(Context context, String id) {
        String completePath = context.getFilesDir().getAbsolutePath() + "/" + id;
        File f = new File(completePath);
        return f.exists();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
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

    public static Bitmap loadImage(Context context, String id) {
        try {
            String completePath = context.getFilesDir().getAbsolutePath() + "/" + id;
            FileInputStream input = new FileInputStream(completePath);
            return BitmapFactory.decodeStream(input);
        } catch (FileNotFoundException e) {
            return BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.cover_not_available);
        }
    }

    public static boolean matchesSearch(String s, String filter) {
        return s.matches(".*([^\\w]|^)" + filter + ".*");
    }

    public static void saveImage(Context context, Bitmap bitmap, String id) {
        String completePath = context.getFilesDir().getAbsolutePath() + "/" + id;
        try {
            FileOutputStream out = new FileOutputStream(completePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void notifyNoConnection(Context context) {
        Util.createSingleButtonAlertDialog(
                context,
                context.getString(R.string.warning),
                context.getString(R.string.no_connection),
                context.getString(R.string.back),
                true
        ).show();
    }

    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Context context;
        ImageView bmImage;

        public DownloadImageTask(Context context, ImageView bmImage) {
            this.context = context;
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urlAndPath) {
            String url = urlAndPath[0];
            String path = urlAndPath[1];

            Bitmap mIcon;

            if (fileExists(context, path)) {
                mIcon = loadImage(context, path);
            } else {
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    mIcon = BitmapFactory.decodeStream(in);
                    saveImage(context, mIcon, path);
                } catch (Exception e) {
                    return null;
                }
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                bmImage.setImageBitmap(result);
            }
        }
    }
}
