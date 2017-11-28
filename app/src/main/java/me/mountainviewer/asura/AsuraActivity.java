package me.mountainviewer.asura;

/* Copyright 2017 Yuri Faria

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

@SuppressLint("SetJavaScriptEnabled")
public class AsuraActivity extends AppCompatActivity implements BluetoothService.OnBluetoothEventCallback, View.OnClickListener {

    //Bluetooth Variables
    private BluetoothService mService;
    private BluetoothWriter mWriter;

    //Running Variables
    private String applicationRunning;

    RecyclerView appsView;
    LinearLayout searchLayout;
    TextView installAppTextView;
    private List<App> appsListData;

    WebView AsuraCoreView;

    // Downloader
    private ThinDownloadManager downloadManager;
    private static final int DOWNLOAD_THREAD_POOL_SIZE = 4;
    private MyDownloadDownloadStatusListenerV1 myDownloadStatusListener = new MyDownloadDownloadStatusListenerV1();
    private MyDownloadDownloadStatusListenerV2 myDownloadStatusListener2 = new MyDownloadDownloadStatusListenerV2();

    final int PERMISSIONS_REQUEST_CODE = 0;
    final int FILE_PICKER_REQUEST_CODE = 1;

    private AppPreferences appPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asura);

        appPreferences = new AppPreferences(this);

        appsView = (RecyclerView) findViewById(R.id.recyclerView);
        appsView.setAdapter(new StoreItemAdapter(this, appsListData));

        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        installAppTextView = (TextView) findViewById(R.id.installAppTextView);

        RecyclerView.LayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        appsView.setLayoutManager(layout);

        appsListData = new ArrayList<>();
        getStoreNew(100, 0);

        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        writeSleep("#fFreedom");
        writeSleep("#1414ASURA");
        writeSleep("#0726PROJECT");
        writeSleep("#ended");

        applicationRunning = "AsuraSystem";

        AsuraCoreView = (WebView) findViewById(R.id.webViewCore);
        AsuraCoreView.setWebViewClient(new AsuraCore());
        AsuraCoreView.getSettings().setJavaScriptEnabled(true);
        AsuraCoreView.addJavascriptInterface(AsuraActivity.this, "core");

        AsuraCoreView.loadUrl("file:///data/data/" + getApplicationContext().getPackageName() + "/core/index.html");

        final Handler logoHandler = new Handler();
        logoHandler.postDelayed(new Runnable(){
            @Override
            public void run(){
                importApps();
            }
        }, 10000);

        downloadManager = new ThinDownloadManager(DOWNLOAD_THREAD_POOL_SIZE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.disconnect();

    }

    //XML Functions
    public void storeClick(View v) {
        installAppTextView.setVisibility(View.GONE);
        searchLayout.setVisibility(View.VISIBLE);
        getStoreNew(100, 0);
    }

    public void myAppsClick(View v) {
        installAppTextView.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.GONE);
        getMyApps();
    }

    public void searchApps(View v){
        EditText search = (EditText) findViewById(R.id.searchApp);
        appsListData = new ArrayList<>();
        assert search != null;
        getStoreSearch(100, 0, search.getText().toString());
    }

    //Bluetooth Functions

    @Override
    public void onDataRead(byte[] bytes, int i) {
        char resultData = new String(bytes).toCharArray()[0];
        switch (resultData){
            case '0':
                AsuraCoreView.loadUrl("javascript:" + applicationRunning + ".onTop()");
                break;
            case '1':
                AsuraCoreView.loadUrl("javascript:" + applicationRunning + ".onDown()");
                break;
            case '2':
                AsuraCoreView.loadUrl("javascript:" + applicationRunning + ".onLeft()");
                break;
            case '3':
                AsuraCoreView.loadUrl("javascript:" + applicationRunning + ".onRight()");
                break;
            case '4':
                AsuraCoreView.loadUrl("javascript:" + applicationRunning + ".destroy()");
                break;
        }
    }

    @Override
    public void onStatusChange(BluetoothStatus bluetoothStatus) {
        if(bluetoothStatus == BluetoothStatus.NONE){
            Toast.makeText(this, "Glass Disconnected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SelectGlassActivity.class));
        }
    }

    @Override
    public void onDeviceName(String s) {
    }

    @Override
    public void onToast(String s) {
    }

    @Override
    public void onDataWrite(byte[] bytes) {
    }

    @Override
    public void onClick(View v) {
    }

    // Core Functions

    @JavascriptInterface
    public void setApplication(String applicationName, String applicationRunningName){
        applicationRunning = applicationName;
    }


    @JavascriptInterface
    public void setView(String[] view){
        Log.w("InitialView", Arrays.toString(view));
        for (String aView : view) {
            writeSleep(aView);
            Log.w("View", aView);
        }
        Log.w("View", "#ended");
        writeSleep("#ended");
    }

    public class AsuraCore extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }
    }

    private void writeSleep(final String msg) {
        mWriter.writeln(msg);
        SystemClock.sleep(50);
    }

    private void importApps(){
        String[] jsAppsInstalled = appPreferences.get("appsJs");

        AsuraCoreView.loadUrl("javascript:AsuraSystemImportApps('" + Arrays.toString(jsAppsInstalled) + "')");
    }

    // Store Functions

    public void getStoreNew(int quantity, int positionStart){
        String url = "http://asuraglass.tk/backend/getAppsGetAPI.php?quantity=" + quantity + "&startPosition=" + positionStart + "&order=new";
        getStore(url);
    }

    public void getStoreSearch(int quantity, int positionStart, String search){
        String url = "http://asuraglass.tk/backend/getAppsGetAPI.php?quantity=" + quantity + "&startPosition=" + positionStart + "&order=search&search=" + search;
        getStore(url);
    }

    private void getStore(String url){
        appsListData = new ArrayList<>();
        appsView.setAdapter(new StoreItemAdapter(this, appsListData));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest json = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        getStoreApps(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", String.valueOf(error));
                    }
                }
        );

        requestQueue.add(json);
    }

    private void getStoreApps(JSONArray response){
        int counter = 0;
        String[] nameAppsInstalled = appPreferences.get("appsInstalled");

        while(counter < response.length()){
            final JSONObject json;
            try {
                json = response.getJSONObject(counter);

                if(!Arrays.asList(nameAppsInstalled).contains(json.getString("name"))) {
                    final String nameApp = json.getString("name");
                    final String description = json.getString("description");
                    final String img = json.getString("img");
                    final String imgUrl = "http://asuraglass.tk/upload/img/" + img;
                    final String js = json.getString("js");
                    final String jsUrl = "http://asuraglass.tk/upload/js/" + js;

                    View.OnClickListener downloadFunction = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Uri downloadUri = Uri.parse(jsUrl);
                            final Uri destinationUri = Uri.parse("data/data/" + getApplicationContext().getPackageName() + "/core/apps/" + js.split("_")[1]);
                            final DownloadRequest downloadRequest = new DownloadRequest(downloadUri).
                                    setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                                    .setDownloadContext(nameApp)
                                    .setStatusListener(myDownloadStatusListener);

                            final Uri downloadUri2 = Uri.parse(imgUrl);
                            final Uri destinationUri2 = Uri.parse("data/data/" + getApplicationContext().getPackageName() + "/core/apps/img/" + img.split("_")[1]);
                            final DownloadRequest downloadRequest2 = new DownloadRequest(downloadUri2).
                                    setDestinationURI(destinationUri2).setPriority(DownloadRequest.Priority.HIGH)
                                    .setDownloadContext(nameApp)
                                    .setStatusListener(myDownloadStatusListener2);

                            downloadManager.add(downloadRequest);
                            downloadManager.add(downloadRequest2);
                        }
                    };

                    appsListData.add(new App(nameApp, description, imgUrl, downloadFunction));
                    appsView.setAdapter(new StoreItemAdapter(this, appsListData));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            counter++;
        }
    }

    class MyDownloadDownloadStatusListenerV1 implements DownloadStatusListenerV1 {
        @Override
        public void onDownloadComplete(DownloadRequest request) {
            appPreferences.add("appsInstalled", request.getDownloadContext().toString());
            appPreferences.add("appsJs", request.getDestinationURI().toString().split("/")[5]);
        }

        @Override
        public void onDownloadFailed(DownloadRequest request, int errorCode, String errorMessage) {
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgress(DownloadRequest request, long totalBytes, long downloadedBytes, int progress) {
        }
    }

    class MyDownloadDownloadStatusListenerV2 implements DownloadStatusListenerV1 {
        @Override
        public void onDownloadComplete(DownloadRequest request) {
            appPreferences.add("appsImg", request.getDestinationURI().toString().split("/")[6]);
            getStoreNew(100, 0);
        }

        @Override
        public void onDownloadFailed(DownloadRequest request, int errorCode, String errorMessage) {
            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProgress(DownloadRequest request, long totalBytes, long downloadedBytes, int progress) {
        }
    }

    // MyApps Functions

    private void getMyApps(){
        appsListData = new ArrayList<>();
        appsView.setAdapter(new StoreItemAdapter(this, appsListData));

        final String[] imgAppsInstalled = appPreferences.get("appsImg");
        final String[] nameAppsInstalled = appPreferences.get("appsInstalled");
        final String[] jsAppsInstalled = appPreferences.get("appsJs");

        int counter = 0;
        while(counter < nameAppsInstalled.length) {

            final int finalCounter = counter;
            View.OnClickListener desinstallFunction = new View.OnClickListener() {
                @SuppressWarnings("SuspiciousMethodCalls")
                @Override
                public void onClick(View v) {
                    File jsFile = new File("data/data/" + getApplicationContext().getPackageName() + "/core/apps/", jsAppsInstalled[finalCounter]);
                    File imgFile = new File("data/data/" + getApplicationContext().getPackageName() + "/core/apps/img/", imgAppsInstalled[finalCounter]);
                    jsFile.delete();
                    imgFile.delete();

                    appPreferences.remove("appsImg", imgAppsInstalled[finalCounter]);
                    appPreferences.remove("appsInstalled", nameAppsInstalled[finalCounter]);
                    appPreferences.remove("appsJs", jsAppsInstalled[finalCounter]);

                    importApps();
                    getMyApps();
                }
            };

            appsListData.add(new App(nameAppsInstalled[finalCounter], "Application", imgAppsInstalled[finalCounter], desinstallFunction));
            appsView.setAdapter(new MyAppsAdapter(this, appsListData));
            counter++;
        }
    }

    // Install non-store app Functions
    public void installApp(View v){
        checkPermissionsAndOpenFilePicker();
    }

    private void checkPermissionsAndOpenFilePicker() {
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "Allow external storage reading", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            openFilePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openFilePicker();
                } else {
                    Toast.makeText(this, "Accept the request", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void openFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .withHiddenFiles(true)
                .withFilter(Pattern.compile(".*\\.js$"))
                .withFilterDirectories(true)
                .withTitle("Select JS")
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);

            if (path != null) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);

                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data/data/" + getApplicationContext().getPackageName() + "/core/apps/", fileName)));
                    writer.write(readFile(path));
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                appPreferences.add("appsInstalled", fileName.split(".js")[fileName.split(".js").length - 1]);
                appPreferences.add("appsImg", fileName.split(".js")[fileName.split(".js").length - 1] + ".jpg");
                appPreferences.add("appsJs", fileName);

                getMyApps();
            }
        }
    }

    private String readFile(String path){
        String filename = path.substring(path.lastIndexOf("/") + 1);
        path = path.substring(0, path.lastIndexOf("/"));

        StringBuilder text = new StringBuilder();

        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(path, filename)));
            String line;

            while((line = br.readLine()) != null){
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return text.toString();
    }
}