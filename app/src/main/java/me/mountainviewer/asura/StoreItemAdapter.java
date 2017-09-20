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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class StoreItemAdapter extends RecyclerView.Adapter {

    private List<App> apps;
    private Context context;

    public StoreItemAdapter(Context context, List<App> apps){
        this.apps = apps;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.storelist, parent, false);

        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        StoreViewHolder holder = (StoreViewHolder) viewHolder;
        App app  = apps.get(position);

        Picasso.with(context).load(app.getImageUrl()).resize(90, 90).into(holder.imageApp);
        holder.appName.setText(app.getAppName());
        holder.description.setText(app.getDescription());
        holder.downloadApp.setOnClickListener(app.getDownloadFunction());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public class StoreViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageApp;
        final TextView appName;
        final TextView description;
        final TextView downloadApp;

        public StoreViewHolder(View view) {
            super(view);
            imageApp = (ImageView) view.findViewById(R.id.imageItem);
            appName = (TextView) view.findViewById(R.id.appNameItem);
            description = (TextView) view.findViewById(R.id.descriptionItem);
            downloadApp = (TextView) view.findViewById(R.id.downloadItem);
        }
    }
}