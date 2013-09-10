package com.duobaohui;
 
import org.apache.cordova.DroidGap; 
import android.os.Bundle;  
import android.view.Menu; 
import android.view.MenuItem; 
import java.io.BufferedReader;
import java.io.File; 
import java.io.FileOutputStream; 
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader; 
import java.net.HttpURLConnection; 
import java.net.URL; 
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse; 
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient; 
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet; 
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient; 
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject; 
import android.app.Activity; 
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog; 
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface; 
import android.content.Intent; 
import android.content.pm.PackageManager.NameNotFoundException; 
import android.net.Uri; 
import android.os.Environment; 
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import org.apache.cordova.*;
import android.util.Log;

public class MainActivity extends DroidGap {
	String verCode = "1.4";
	ProgressDialog pd = null;   
	String UPDATE_SERVERAPK = "ApkUpdateAndroid.apk";
	String download_url ="";
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setIntegerProperty("splashscreen", R.drawable.mobile720x1280);
        super.loadUrl(Config.getStartUrl() , 4000);

        final Handler hand = new Handler(){
        	public void handleMessage(Message msg){
        		download_url = msg.obj.toString();
        		if(!download_url.equals("")){
            		doNewVersionUpdate(download_url);
        		}
        	} 
        };

        new Thread()
		{
			@Override
			public void run()
			{

				// 创建一个HttpGet对象
				HttpGet get = new HttpGet(
					"http://www.duobaohui.com/api/version_m?version_id="+verCode);  //①
				try
				{
					HttpClient httpClient = new DefaultHttpClient();
					// 发送GET请求
					HttpResponse httpResponse = httpClient.execute(get);//②
					HttpEntity entity = httpResponse.getEntity();
					if (entity != null)
					{
						// 读取服务器响应
						BufferedReader br = new BufferedReader(
							new InputStreamReader(entity.getContent()));
						String line = null;
						String json = br.readLine();
						try {
						    JSONObject jsonObject = new JSONObject(json);
						    Iterator keys = jsonObject.keys();
						    Map<String, String> map = new HashMap<String, String>();
						    while (keys.hasNext()) {
						        String key = (String) keys.next();
						        map.put(key, jsonObject.getString(key));
						    }
						    System.out.println(map);// this map will contain your json stuff
						    download_url = map.get("download_url");
						    Message msg = new Message();
						    msg.obj = download_url;
						    hand.sendMessage(msg);
						  //  if(download_url != null){
						   // 	doNewVersionUpdate();
						    	
						   //  }
						} catch (JSONException e) {
						    e.printStackTrace();
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Log.v("tag" , "nano Bad");
				}
			}
		}.start();   
    }
	public void doNewVersionUpdate(final String download_url){
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本：");
		sb.append(verCode);
		sb.append(",是否更新");
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle("软件更新")
		.setMessage(sb.toString())
		.setPositiveButton("更新", new DialogInterface.OnClickListener() {
			 @Override
			 public void onClick(DialogInterface dialog, int which) {
				 pd = new ProgressDialog(MainActivity.this);
				 pd.setTitle("正在下载");
				 pd.setMessage("请稍后。。。");
				 pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				 downFile(download_url);
			 }
		}).setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which){
				
			}
		}).create();
		dialog.show();
	}
	public void downFile(final String url){
		pd.show();
		new Thread(){
			public void run(){
				 HttpClient client = new DefaultHttpClient();
				 HttpGet get = new HttpGet(url);
				 HttpResponse response;
				 try{
					 response = client.execute(get);
					 HttpEntity entity = response.getEntity();
					 long length = entity.getContentLength();
					 InputStream is = entity.getContent();
					 FileOutputStream fileOutputStream = null;
					 if(is != null){
						 File file = new File(Environment.getExternalStorageDirectory(),UPDATE_SERVERAPK);
						 fileOutputStream = new FileOutputStream(file);
						 byte[] b = new byte[1024];
						 int charb = -1;
						 int count = 0;
						 while((charb = is.read(b))!=-1){
							 fileOutputStream.write(b, 0, charb);
							 count += charb;
						 }
					 }
					 fileOutputStream.flush();
					 if(fileOutputStream!=null){
						 fileOutputStream.close();
					 }
					 down();
				 }catch(Exception e){
					 e.printStackTrace();
				 }
			}
		}.start();
	}
	 Handler handler = new Handler() {
		 @Override
		 public void handleMessage(Message msg) {
			 super.handleMessage(msg);
			 pd.cancel();
			 update();
		 }
	 };
	 public void down(){
		 new Thread(){
			 public void run(){
				 Message message = handler.obtainMessage();
				 handler.sendMessage(message);
			 }
		 }.start();
	 }
	 public void update(){
		 Intent intent = new Intent(Intent.ACTION_VIEW);
		 intent.setDataAndType(Uri.fromFile(new
				 File(Environment.getExternalStorageDirectory(),UPDATE_SERVERAPK))
				 , "application/vnd.android.package-archive");
		 startActivity(intent);
	 }
}
