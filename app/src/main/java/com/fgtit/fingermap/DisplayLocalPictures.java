package com.fgtit.fingermap;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import com.fgtit.adapter.ImageListAdapter;

import java.util.ArrayList;
import java.util.List;

public class DisplayLocalPictures extends AppCompatActivity {
    JobDB jobDB = new JobDB(this);
    private GridView gridView;
    private List<String> listOfImagesPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_local_pictures);

        gridView = findViewById(R.id.imgGridView);
        listOfImagesPath = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String asset = extras.getString("asset");
            listOfImagesPath =jobDB.getPictures(asset);
            gridView.setAdapter(new ImageListAdapter(this,listOfImagesPath));

        }else{
            return;
        }
    }

    public void showToast(String message)
    {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
