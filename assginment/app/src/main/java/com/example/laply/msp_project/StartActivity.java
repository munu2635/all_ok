package com.example.laply.msp_project;

// 앱 실행시 처음으로 표기 되는 부분
// 일정 시간후 MainActivity로 이동한다.
//------------------------------------------------- 5/31
// 추가사항

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class StartActivity extends Activity {
    boolean isPermitted_1 = false;
    boolean isPermitted_2 = false;
    boolean isPermitted_3 = false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;

    static final int REQUEST_ENABLE_AP = 1;
    static final int REQUEST_ENABLE_DISCOVER = 2;
    static final int REQUEST_ENABLE_GPS = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity", "start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        requestRuntimePermission();

        Intent intent = new Intent(this, MainActivity.class);

        try { Thread.sleep(1000);
        } catch (InterruptedException e) { e.printStackTrace(); }

        startActivity(intent);
        finish();
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
                break;
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
        if (ContextCompat.checkSelfPermission(StartActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(StartActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            isPermitted_1 = true;


            if (ContextCompat.checkSelfPermission(StartActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(StartActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            } else {
                isPermitted_2 = true;


                if (ContextCompat.checkSelfPermission(StartActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(StartActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                    } else {
                        ActivityCompat.requestPermissions(StartActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                    }
                } else {
                    isPermitted_3 = true;
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermitted_1 = true;
                } else {
                    isPermitted_1 = false;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermitted_2 = true;
                } else {
                    isPermitted_2 = false;
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isPermitted_3 = true;
                } else {
                    isPermitted_3 = false;
                }
                return;
            }
        }
    }

}




