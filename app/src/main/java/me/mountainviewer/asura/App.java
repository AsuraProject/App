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

import android.view.View;

public class App {

    private String imageUrl;
    private String appName;
    private String description;
    private View.OnClickListener clickFunction;

    public App(String appName, String description, String imageUrl, View.OnClickListener clickFunction){
        this.appName = appName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.clickFunction = clickFunction;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public String getAppName(){
        return appName;
    }

    public String getDescription(){
        return description;
    }

    public View.OnClickListener getDownloadFunction(){
        return clickFunction;
    }
}
