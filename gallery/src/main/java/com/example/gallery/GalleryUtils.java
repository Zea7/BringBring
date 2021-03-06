package com.example.gallery;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class GalleryUtils {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refresh_layout;
    private LayoutInflater inflater;
    private Context content;
    private ViewGroup container;
    private Bundle savedInstanceState;
    private IVAdapter adapter;

    public GalleryUtils(LayoutInflater inflater, Context content, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.content = content;
        this.container = container;
        this.savedInstanceState = savedInstanceState;
    }

    public ViewGroup fragment_gallery() throws Exception {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_gallery, container, false);

        recyclerView = (RecyclerView)rootView.findViewById(R.id.icon_gallery);

        if(ContextCompat.checkSelfPermission(recyclerView.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            throw new Exception("저장소 권한 없이 읽기 시도");

        ArrayList<IVitem> list = getConstract(recyclerView);

        refresh_layout = (SwipeRefreshLayout) rootView.findViewById(com.example.gallery.R.id.swipe_layout);
        refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(recyclerView, "Refresh Success", Snackbar.LENGTH_SHORT).show();
                        refresh_layout.setRefreshing(false);

                        refresh();
                    }
                }, 500);
            }
        });

        adapter = new IVAdapter(recyclerView.getContext(), list);


        adapter.setOnImageClickListener(new IVAdapter.onImageClickListener() {
            @Override
            public void onImageClick(View v, int position) {
                LayoutInflater inflater = LayoutInflater.from(v.getContext());

            }
        });

        ImageButton button = (ImageButton) rootView.findViewById(R.id.camera_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context content = view.getContext();

                Intent intent = new Intent(content, CameraActivity.class);
                content.startActivity(intent);
                refresh();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new IVDecoration(recyclerView.getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        return rootView;
    }

    private ArrayList<IVitem> getConstract(View view){
        ArrayList <IVitem> list = new ArrayList<>();
        Uri uri;
        Uri uripath;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        String absolutePathofImage;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_TAKEN, MediaStore.Images.Media._ID};
        cursor = view.getContext().getContentResolver().query(uri, projection, null, null, MediaStore.MediaColumns.DATE_TAKEN + " DESC");
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        absolutePathofImage = cursor.getString(column_index_data);
        uripath = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))));

        list.add(new IVitem(absolutePathofImage, uripath));


        while (cursor.moveToNext()){
            absolutePathofImage = cursor.getString(column_index_data);
            uripath = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))));

            list.add(new IVitem(absolutePathofImage, uripath));
        }

        return list;
    }

    private void refresh(){
        ArrayList<IVitem> newlist = getConstract(recyclerView);
        adapter = new IVAdapter(recyclerView.getContext(), newlist);
        recyclerView.swapAdapter(adapter, true);
    }

}
