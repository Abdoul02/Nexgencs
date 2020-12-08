package com.fgtit.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.fingermap.R;
import com.fgtit.models.SessionManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CommonFunction {

    Context context;
    public Dialog dialog;
    Dialog customDialog;
    ProgressDialog pDialog;

    ImageView customDialogImg;
    TextView customDialogTextView;

    public CommonFunction(Context context) {
        this.context = context;
    }

    public void showToast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void setDialog(boolean show) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View vl = inflater.inflate(R.layout.progress, null);
        builder.setView(vl);
        dialog = builder.create();
        if (show) {
            dialog.show();
        } else {
            dialog.cancel();
        }
    }

    public void showCustomDialog(String title, String message, String imgPath,
                                 DialogInterface.OnClickListener okBtn,
                                 DialogInterface.OnClickListener cancelBtn,
                                 String positiveBtnText, String negativeBtnText) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View vl = inflater.inflate(R.layout.dialog_enrolfinger, null);
        customDialogImg = (ImageView) vl.findViewById(R.id.imageView1);
        customDialogTextView = (TextView) vl.findViewById(R.id.textview1);
        Bitmap image = getImage(imgPath);
        if(image != null){
            customDialogImg.setImageBitmap(image);
        }
        customDialogTextView.setText(message);

        builder.setView(vl);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveBtnText, okBtn);
        builder.setNegativeButton(negativeBtnText, cancelBtn);
        builder.setOnCancelListener(DialogInterface::dismiss);

        customDialog = builder.create();
        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();
    }

    private Bitmap getImage(String path) {
        File imgFile = new File(path);
        if (imgFile.exists()) {
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return null;
    }

    public void cancelDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void showProgressDialog() {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Uploading information...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(pDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    public void showProgress(int progress) {
        pDialog.setProgress(progress);
    }

    public String getDateAndTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    public boolean checkTextLength(String text) {
        if (text.trim().length() > 0) {
            return true;
        }
        return false;
    }

    public boolean deleteFile(String path) {
        File fileToDelete = new File(path);
        if (fileToDelete.exists()) {
            return fileToDelete.delete();
        }
        return false;
    }

    public List<String> RetriveCapturedImagePath(String path) {
        List<String> tFileList = new ArrayList<String>();
        File f = new File(path);
        if (f.exists()) {
            File[] files = f.listFiles();
            Arrays.sort(files);

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory())
                    continue;
                tFileList.add(file.getPath());
            }
        }
        return tFileList;
    }

    public int companyId() {
        SessionManager session = new SessionManager(context);
        HashMap<String, String> manager = session.getUserDetails();
        return Integer.parseInt(manager.get(SessionManager.KEY_COMPID));
    }

}
