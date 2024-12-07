package com.vanstone.redsysa90prokeypos.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

public class DisplayHint {
	private ProgressDialog progressDialog;
	private static Toast toast;
	public static final int DIALOG_PROCESS = 0; // 过程显示
	public static final int TOAST = 3; // 显示几秒自动关闭

	public class CommHandler extends Handler {
		public CommHandler() {
		}

		public CommHandler(Looper looper) {
			super(looper);
		}

		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String key = bundle.getString("key");
			if (key.equals(DIALOG_PROCESS + "")) // 过程显示提示信息
			{
				commDialog(bundle.getString("value"), (Context) msg.obj);
			} else if (key.equals(TOAST + "")) // 显示提示信息
			{
				displayToast(bundle.getString("value"), (Context) msg.obj);
			}
		}
	}

	private void commDialog(String message, Context context) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(message);
		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
			}
		});
		progressDialog.show();
	}

	private void displayToast(String message, Context context) {
		closeCommDialog();
		if (toast == null) {
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		} else {
			toast.cancel();
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		}
		toast.setGravity(Gravity.CENTER, 0, 250);
		toast.show();
	}
	
	public void displayHint(String message, Context context) {
		HandlerThread handlerThread = new HandlerThread("handler_thread");
		handlerThread.start();
		CommHandler myHandler = new CommHandler(handlerThread.getLooper());
		Message msg = myHandler.obtainMessage();
		msg.obj = context;
		Bundle bundle = new Bundle();
		bundle.putString("value", message);
		bundle.putString("key", TOAST + "");
		msg.setData(bundle);
		myHandler.sendMessage(msg);
	}

	public void openCommDialog(String message, Context context) {
		HandlerThread handlerThread = new HandlerThread("handler_thread");
		handlerThread.start();
		CommHandler myHandler = new CommHandler(handlerThread.getLooper());
		Message msg = myHandler.obtainMessage();
		msg.obj = context;
		Bundle bundle = new Bundle();
		bundle.putString("value", message);
		bundle.putString("key", DIALOG_PROCESS + "");
		msg.setData(bundle);
		myHandler.sendMessage(msg);
	}

	public void closeCommDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

}
