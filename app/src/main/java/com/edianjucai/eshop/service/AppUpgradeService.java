package com.edianjucai.eshop.service;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.alibaba.fastjson.JSON;
import com.edianjucai.eshop.R;
import com.edianjucai.eshop.model.entity.InitActUpgradeModel;
import com.edianjucai.eshop.model.entity.RequestModel;
import com.edianjucai.eshop.server.InterfaceServer;
import com.edianjucai.eshop.util.ModelUtil;
import com.edianjucai.eshop.util.PackageUtil;
import com.edianjucai.eshop.util.SDDialogUtil;
import com.edianjucai.eshop.util.ToastUtils;
import com.edianjucai.eshop.util.TypeParseUtil;
import com.ta.sunday.http.impl.SDAsyncHttpResponseHandler;
import com.ta.util.download.DownLoadCallback;
import com.ta.util.download.DownloadManager;

import org.apache.http.Header;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 更新服务
 * 
 * @author yhz
 */
public class AppUpgradeService extends Service {
	public static final String EXTRA_SERVICE_START_TYPE = "extra_service_start_type";

	private static final int DEFAULT_START_TYPE = 0;

	private int mStartType = DEFAULT_START_TYPE; // 0代表启动app时候程序自己检测，1代表用户手动检测版本

	public static final int mNotificationId = 100;

	private String mDownloadUrl = null;

	private NotificationManager mNotificationManager = null;

	private Notification mNotification = null;

	private PendingIntent mPendingIntent = null;

	private int mServerVersion = 0;

	private String mFileName = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// mDownloadUrl =
		// "http://gdown.baidu.com/data/wisegame/484af350ea7ba5bc/baidushoujizhushou_16783625.apk";
		initIntentData(intent);
		testUpgrade();
		return super.onStartCommand(intent, flags, startId);
	}

	private void initIntentData(Intent intent) {
        mStartType = intent.getIntExtra(EXTRA_SERVICE_START_TYPE, DEFAULT_START_TYPE);
	}

	private void testUpgrade() {
		PackageInfo info = PackageUtil.getCurrentAppPackageInfo(this, this.getPackageName());
		Map<String, Object> mapData = new HashMap<String, Object>();
		mapData.put("act", "version2");
		mapData.put("dev_type", "android");
		mapData.put("version", String.valueOf(info.versionCode));

		RequestModel model = new RequestModel(mapData);

		SDAsyncHttpResponseHandler handler = new SDAsyncHttpResponseHandler() {

			Dialog nDialog = null;
			@Override
			public void onStartInMainThread(Object result) {
				super.onStartInMainThread(result);
				if (mStartType == 1) {
					nDialog = SDDialogUtil.showLoading("正在检测新版本...");
				}
			}

			@Override
			public Object onSuccessInRequestThread(int statusCode, Header[] headers, String content) {
				try {
					InitActUpgradeModel model = JSON.parseObject(content, InitActUpgradeModel.class);
					return model;
				} catch (Exception e) {
					return null;
				}

			}

			@Override
			public void onSuccessInMainThread(int statusCode, Header[] headers, String content, Object result) {
				InitActUpgradeModel model = (InitActUpgradeModel) result;
				if (!ModelUtil.isActModelNull(model)) {
					switch (model.getResponse_code()) {
					case 0:
						ToastUtils.showToast("检查新版本失败!");
						break;
					case 1:
						if (isUpGrade(model)) {
							if (nDialog != null) {
								nDialog.dismiss();
							}
							showDialogUpgrade(model);
						} else {
							if (mStartType == 1) {
                                ToastUtils.showToast("当前已是最新版本!");
							}
						}
						break;
					default:
						break;
					}
				}

			}

			@Override
			public void onFailureInMainThread(Throwable error, String content, Object result) {
				stopSelf();
			}

			@Override
			public void onFinishInMainThread(Object result) {
				if (nDialog != null)
				{
					nDialog.dismiss();
				}
				stopSelf();
			}
		};
		InterfaceServer.getInstance().requestInterface(model, handler, true);

	}

	private boolean isUpGrade(InitActUpgradeModel model) {
		PackageInfo info = PackageUtil.getCurrentAppPackageInfo(this, this.getPackageName());
		int curVersion = info.versionCode;
		if (!TextUtils.isEmpty(model.getServerVersion()) && !TextUtils.isEmpty(model.getHasfile()) && !TextUtils.isEmpty(model.getFilename())) {
			initDownInfo(model);
			boolean hasfile = Integer.valueOf(model.getHasfile()) == 1 ? true : false;
			if (curVersion < mServerVersion && hasfile) {
                ToastUtils.showToast("发现新版本");
				return true;
			}
		}
		return false;
	}

	private void initDownInfo(InitActUpgradeModel model) {
		mDownloadUrl = model.getFilename();
		mServerVersion = Integer.valueOf(model.getServerVersion());
		mFileName = getString(R.string.app_name) + "_" + mServerVersion + ".apk";
	}

	private void showDialogUpgrade(final InitActUpgradeModel model) {
		final int forcedUpgrade = TypeParseUtil.getIntFromString(model.getForced_upgrade(), 0);
		Builder builder = new Builder(getApplicationContext(), AlertDialog.THEME_HOLO_LIGHT);
		builder.setTitle("更新内容");
		builder.setMessage(model.getAndroid_upgrade());
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				registerDownloder();
				if (forcedUpgrade == 1) {// 强制升级

				} else {
					arg0.dismiss();
				}
			}
		});

		if (forcedUpgrade == 1) {// 强制升级

		} else {
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
		}

		Dialog dialog = builder.create();
		if (forcedUpgrade == 1) {
			dialog.setCancelable(false);
		}
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();

	}

	private void initNotification() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotification = new Notification();
        // TODO: 2016-09-26 添加图标和XML文件图标
        mNotification.icon = R.mipmap.icon;
		mNotification.tickerText = mFileName + "正在下载中";
		mNotification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.service_download_view);

		Intent completingIntent = new Intent();
		completingIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		completingIntent.setClass(getApplication().getApplicationContext(), AppUpgradeService.class);
		mPendingIntent = PendingIntent.getActivity(AppUpgradeService.this, R.string.app_name, completingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotification.contentIntent = mPendingIntent;

		mNotification.contentView.setTextViewText(R.id.upgradeService_tv_appname, mFileName);
		mNotification.contentView.setTextViewText(R.id.upgradeService_tv_status, "下载中");
		mNotification.contentView.setProgressBar(R.id.upgradeService_pb, 100, 0, false);
		mNotification.contentView.setTextViewText(R.id.upgradeService_tv, "0%");

		mNotificationManager.cancel(mNotificationId);
		mNotificationManager.notify(mNotificationId, mNotification);

	}

	private void registerDownloder() {

		DownloadManager.getDownloadManager().setDownLoadCallback(new DownLoadCallback() {
			@Override
			public void onAdd(String url, Boolean isInterrupt) {
				super.onAdd(url, isInterrupt);
				initNotification();
			}

			@Override
			public void onStart() {
				super.onStart();
			}

			@Override
			public void onLoading(String url, long totalSize, long currentSize, long speed) {
				super.onLoading(url, totalSize, currentSize, speed);
				int progress = (int) ((currentSize * 100) / (totalSize));
				mNotification.contentView.setProgressBar(R.id.upgradeService_pb, 100, progress, false);
				mNotification.contentView.setTextViewText(R.id.upgradeService_tv, progress + "%");
				mNotificationManager.notify(mNotificationId, mNotification);
			}

			@Override
			public void onFinish(String url) {
				super.onFinish(url);

			}

			@Override
			public void onSuccess(String url, File file) {
				super.onSuccess(url, file);
				mNotification.contentView.setViewVisibility(R.id.upgradeService_pb, View.GONE);
				mNotification.defaults = Notification.DEFAULT_SOUND;
				mNotification.contentIntent = mPendingIntent;
				mNotification.contentView.setTextViewText(R.id.upgradeService_tv_status, "下载完成");
				mNotification.contentView.setTextViewText(R.id.upgradeService_tv, "100%");
				mNotificationManager.notify(mNotificationId, mNotification);
				mNotificationManager.cancel(mNotificationId);
				PackageUtil.installApkPackage(getApplicationContext(), file.getPath());
				ToastUtils.showToast("下载完成");
			}

			@Override
			public void onFailure(String url, String strMsg) {
				super.onFailure(url, strMsg);
                ToastUtils.showToast("下载失败");
			}

		});

		DownloadManager.getDownloadManager().addHandler(mDownloadUrl, mFileName);

	}

}