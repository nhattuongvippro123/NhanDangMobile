package com.example.nhavo.map4d123;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nhavo.services.APIClient;
import com.example.nhavo.services.UploadService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class nofi extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    Button btnCa, btnFo, btnSend, btnClose;
    ImageView imgHinh;
    EditText editTextDescription;
    int REQUEST_CODE_CAMERA = 123;
    int REQUEST_CODE_FOLDER = 456;
    String strDate, strTime;
    private String imagePath, imageName;
    Connection con;
    private String un,pass,db,ip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nofi);
        AnhXa();
        verifyStoragePermissions(nofi.this);
        btnCa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        });
        btnFo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                Intent result = Intent.createChooser(intent,getText(R.string.choose_file));
                startActivityForResult(result,REQUEST_CODE_FOLDER);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),imageName,Toast.LENGTH_LONG).show();
                //database(imageName);
                buttonSend_onClick(v);
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null){
            if (requestCode == REQUEST_CODE_CAMERA) {
                Uri uri = data.getData();
                imagePath = getRealPathFromURI(uri);
                imageName = imagePath.substring(imagePath.lastIndexOf("/")+1);
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                imgHinh.setImageBitmap(bitmap);
                //imageName = String.valueOf(imgHinh.getTag());
                DateFormat df = new SimpleDateFormat("yyyyMMdd");
                strDate = df.format(new Date());
                DateFormat dfT = new SimpleDateFormat("hhmmss");
                strTime = dfT.format(new Date());

            }
            else if (requestCode == REQUEST_CODE_FOLDER){
                Uri uri = data.getData();
                imagePath = getRealPathFromURI(uri);
                imgHinh.setImageURI(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void buttonSend_onClick(View v){
        try {
            File file = new File(imagePath);
            RequestBody photoContent = RequestBody.create(MediaType.parse("multipart/form-data"),file);
            MultipartBody.Part photo = MultipartBody.Part.createFormData("photo",file.getName(),photoContent);
            RequestBody description = RequestBody.create(MediaType.parse("text/plain"),
                    editTextDescription.getText().toString());
            UploadService uploadService = APIClient.getClient().create(UploadService.class);
            uploadService.Upload(photo,description).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        Toast.makeText(getApplicationContext(),getText(R.string.success),Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private String getRealPathFromURI(Uri contentUri){
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(),contentUri,proj,null,null,null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return  result;
    }

    private static void verifyStoragePermissions(Activity activity){
        int permission  = ActivityCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void AnhXa (){
        imgHinh = (ImageView) findViewById(R.id.imgHinh);
        btnCa = (Button) findViewById(R.id.btnCamera);
        btnFo = (Button) findViewById(R.id.btnFolder);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnClose = (Button) findViewById(R.id.btnClose);
        editTextDescription = (EditText) findViewById(R.id.editTextDescription);
    }
}
