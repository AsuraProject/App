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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        copyFileOrDir("core");

        SharedPreferences welcomeExisted = getSharedPreferences("preferencesApp", MODE_PRIVATE);
        if(welcomeExisted.getBoolean("isWelcomed", false)) {
            Intent intent = new Intent(MainActivity.this, SelectGlassActivity.class);
            startActivity(intent);
        }
    }


    public void goToActivity(View v){
        Set<String> apps = new HashSet<>();
        Set<String> appsJs = new HashSet<>();
        Set<String> appsImg = new HashSet<>();
        apps.add("Atual Hour");
        appsJs.add("hour.js");
        appsImg.add("hour.jpg");

        SharedPreferences.Editor welcomeEditor = getSharedPreferences("preferencesApp", MODE_PRIVATE).edit();
        welcomeEditor.putBoolean("isWelcomed", true);
        welcomeEditor.putStringSet("appsInstalled", apps);
        welcomeEditor.putStringSet("appsJs", appsJs);
        welcomeEditor.putStringSet("appsImg", appsImg);
        welcomeEditor.apply();

        Intent intent = new Intent(MainActivity.this, SelectGlassActivity.class);
        startActivity(intent);
    }

    public void copyFileOrDir(String path) {
        AssetManager assetManager = this.getAssets();
        String assets[];
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(path);
            } else {
                String fullPath = "/data/data/" + this.getPackageName() + "/" + path;
                File dir = new File(fullPath);
                if (!dir.exists()) {
                    dir.mkdir();
                    Log.w("tag", "Created");
                }
                for (String asset : assets) {
                    copyFileOrDir(path + "/" + asset);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private void copyFile(String filename) {
        AssetManager assetManager = this.getAssets();

        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + this.getPackageName() + "/" + filename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }
}
