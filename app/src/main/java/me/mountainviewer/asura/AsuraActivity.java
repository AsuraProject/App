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
import android.content.SharedPreferences;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asura);

        appsListData = new ArrayList<>();
        getStoreNew(100, 0);

        appsView = (RecyclerView) findViewById(R.id.recyclerView);
        appsView.setAdapter(new StoreItemAdapter(this, appsListData));

        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        installAppTextView = (TextView) findViewById(R.id.installAppTextView);

        RecyclerView.LayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        appsView.setLayoutManager(layout);

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
        appsListData = new ArrayList<>();
        getStoreNew(100, 0);
    }

    public void myAppsClick(View v) {
        installAppTextView.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.GONE);
        appsListData = new ArrayList<>();
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
        SharedPreferences appsInstalled = getSharedPreferences("preferencesApp", MODE_PRIVATE);
        Set<String> jsAppsInstalled = appsInstalled.getStringSet("appsJs", new HashSet<String>());

        AsuraCoreView.loadUrl("javascript:AsuraSystemImportApps('" + jsAppsInstalled.toString() + "')");
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
        SharedPreferences appsInstalled = getSharedPreferences("preferencesApp", MODE_PRIVATE);

        Set<String> nameAppsInstalled = appsInstalled.getStringSet("appsInstalled", new HashSet<String>());

        while(counter < response.length()){
            final JSONObject json;
            try {
                json = response.getJSONObject(counter);

                if(!nameAppsInstalled.contains(json.getString("name"))) {
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
            SharedPreferences appsInstalled = getSharedPreferences("preferencesApp", MODE_PRIVATE);

            Set<String> nameAppsInstalled = appsInstalled.getStringSet("appsInstalled", new HashSet<String>());
            Set<String> jsAppsInstalled = appsInstalled.getStringSet("appsJs", new HashSet<String>());

            nameAppsInstalled.add(request.getDownloadContext().toString());
            jsAppsInstalled.add(request.getDestinationURI().toString().split("/")[5]);

            SharedPreferences.Editor appsEditor = appsInstalled.edit();
            appsEditor.remove("appsInstalled");
            appsEditor.remove("appsJs");
            appsEditor.apply();
            
            appsEditor.putStringSet("appsInstalled", nameAppsInstalled);
            appsEditor.putStringSet("appsJs", jsAppsInstalled);
            appsEditor.apply();
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
            SharedPreferences appsInstalled = getSharedPreferences("preferencesApp", MODE_PRIVATE);
            Set<String> imgAppsInstalled = appsInstalled.getStringSet("appsImg", new HashSet<String>());
            imgAppsInstalled.add(request.getDestinationURI().toString().split("/")[6]);

            SharedPreferences.Editor appsEditor = appsInstalled.edit();
            appsEditor.remove("appsImg");
            appsEditor.apply();
            appsEditor.putStringSet("appsImg", imgAppsInstalled);
            appsEditor.apply();

            getMyApps();
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
        final SharedPreferences appsInstalled = getSharedPreferences("preferencesApp", MODE_PRIVATE);

        final Set<String> imgAppsInstalled = appsInstalled.getStringSet("appsImg", new HashSet<String>());
        final Set<String> nameAppsInstalled = appsInstalled.getStringSet("appsInstalled", new HashSet<String>());
        final Set<String> jsAppsInstalled = appsInstalled.getStringSet("appsJs", new HashSet<String>());

        int counter = 0;
        while(counter < nameAppsInstalled.size()) {

            final int finalCounter = counter;
            View.OnClickListener desinstallFunction = new View.OnClickListener() {
                @SuppressWarnings("SuspiciousMethodCalls")
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor appsEditor = appsInstalled.edit();

                    File jsFile = new File("data/data/" + getApplicationContext().getPackageName() + "/core/apps/", jsAppsInstalled.toArray()[finalCounter].toString());
                    File imgFile = new File("data/data/" + getApplicationContext().getPackageName() + "/core/apps/img/", imgAppsInstalled.toArray()[finalCounter].toString());
                    jsFile.delete();
                    imgFile.delete();

                    appsEditor.remove("appsImg");
                    appsEditor.remove("appsInstalled");
                    appsEditor.remove("appsJs");
                    appsEditor.apply();

                    imgAppsInstalled.remove(imgAppsInstalled.toArray()[finalCounter]);
                    nameAppsInstalled.remove(nameAppsInstalled.toArray()[finalCounter]);
                    jsAppsInstalled.remove(jsAppsInstalled.toArray()[finalCounter]);

                    appsEditor.putStringSet("appsImg", imgAppsInstalled);
                    appsEditor.putStringSet("appsInstalled", nameAppsInstalled);
                    appsEditor.putStringSet("appsJs", jsAppsInstalled);
                    appsEditor.apply();

                    importApps();

                    appsListData = new ArrayList<>();
                    getMyApps();
                }
            };

            appsListData.add(new App(nameAppsInstalled.toArray()[finalCounter].toString(), "Application", imgAppsInstalled.toArray()[finalCounter].toString(), desinstallFunction));
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

                SharedPreferences appsInstalled = getSharedPreferences("preferencesApp", MODE_PRIVATE);

                Set<String> nameAppsInstalled = appsInstalled.getStringSet("appsInstalled", new HashSet<String>());
                Set<String> jsAppsInstalled = appsInstalled.getStringSet("appsJs", new HashSet<String>());

                nameAppsInstalled.add(fileName.split(".js")[fileName.split(".js").length - 1]);
                jsAppsInstalled.add(fileName);

                SharedPreferences.Editor appsEditor = appsInstalled.edit();
                appsEditor.remove("appsInstalled");
                appsEditor.remove("appsJs");
                appsEditor.apply();
                
                appsEditor.putStringSet("appsInstalled", nameAppsInstalled);
                appsEditor.putStringSet("appsJs", jsAppsInstalled);
                appsEditor.apply();

                Set<String> imgAppsInstalled = appsInstalled.getStringSet("appsImg", new HashSet<String>());
                imgAppsInstalled.add(fileName.split(".js")[fileName.split(".js").length - 1] + ".jpg");

                appsEditor.remove("appsImg");
                appsEditor.apply();
                appsEditor.putStringSet("appsImg", imgAppsInstalled);
                appsEditor.apply();

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