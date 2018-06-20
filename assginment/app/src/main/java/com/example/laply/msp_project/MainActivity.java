package com.example.laply.msp_project;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

// 움직임(1분 이상) + 걸음 수, 체류(5분 이상) + 장소 (지정, 실내외) 표기가 일어날 장소
// 직접적으로 메인 서비스와의 intent를 사용하여 정보를 받을 공간
// 기본구조
// - 오늘의 날자 표기
// - 기록되는 기록 부분 표기
// + subactivity로 넘어가는 부분 포함된다.
// 사용할 기능들의 퍼미션을 받아야하는 공간이기도하다.
//---------------------------------------------/---- 5/31
// 추가사항

public class MainActivity extends AppCompatActivity {

    TextView logText, timeText;
    TextFileManager mFileMgr;

    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat mFo = new SimpleDateFormat("yyyy-MM-dd"); //출력의 모습을 설정
    String now_date = mFo.format(date);
    Intent intent;

    boolean isPermitted_1 = false;
    boolean isPermitted_2 = false;
    boolean isPermitted_3 = false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;

    static final int REQUEST_ENABLE_AP = 1;
    static final int REQUEST_ENABLE_DISCOVER = 2;
    static final int REQUEST_ENABLE_GPS = 3;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},1000);

        mFileMgr = new TextFileManager();

        logText = (TextView) findViewById(R.id.logText);
        logText.setMovementMethod(new ScrollingMovementMethod());
        timeText = (TextView)findViewById(R.id.timeText);
        timeText.setText(now_date);
        logText.setText(mFileMgr.load());

        intent = new Intent(this, MainService.class);
        startService(intent);
    }



    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        // 요청 코드에 따라 처리할 루틴을 구분해줌
        switch(requestCode) {
            case REQUEST_ENABLE_AP:
                if(responseCode == RESULT_OK) {
                } else if(responseCode == RESULT_CANCELED) {
                    Toast.makeText(this, "사용자가 discoverable을 허용하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            case REQUEST_ENABLE_DISCOVER:
                if(responseCode == RESULT_CANCELED) {
                    Toast.makeText(this, "사용자가 discoverable을 허용하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            case REQUEST_ENABLE_GPS:
                if(responseCode == RESULT_CANCELED) {
                    Toast.makeText(this, "사용자가 discoverable을 허용하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void requestRuntimePermission() {
        ACCESS_COARSE_LOCATION();
        WRITE_EXTERNAL_STORAGE();
        ACCESS_FINE_LOCATION();
    }

    public void ACCESS_COARSE_LOCATION() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            isPermitted_1 = true;
        }
    }
    public void WRITE_EXTERNAL_STORAGE() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            isPermitted_2 = true;
        }
    }
    public void ACCESS_FINE_LOCATION() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            isPermitted_3 = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { isPermitted_1 = true; } else { isPermitted_1 = false; }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { isPermitted_2 = true; } else { isPermitted_2 = false; }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { isPermitted_3 = true; } else { isPermitted_3 = false; }
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        stopService(intent);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        logText.setText(mFileMgr.load());
        super.onStart();
    }


    public void click1(View view) {
        logText.setText(mFileMgr.load());
    }
}


