package com.example.laply.msp_project;


// 움직임(1분 이상) + 걸음 수, 체류(5분 이상) + 장소 (지정, 실내외) 기록의 저장과 직접적인 정보 측정하는 곳

// 기본구조
// 1. 가속도 센서를 사용 ( 걸음 수 ) -- ap를 이용할 수 있으면 사용할 수도 있는 부분
// 2. gps 센서 사용 ( 실외 지정 장소 파악 ) -- 체류가 발생하면 현재 지정된 실외 위치에 있는지 확인할 때 사용 ( 운동장, 텔동 좌표값 ppt 참고 )
// 3. ap 센서 사용 ( 실내 지정 장소 파악 ) -- 체류가 발생하면 현재 지정된  실내 위치에 있는지 확인할 때 사용 ( 실습실, 다산 1층 ap값 직접 구현 )
// 4. 조도센서? 사용 ( 실내, 실외 파악 ) -- 실내 인지 실외인지 파악할 수 있도록 구현
// 5. 데이터 저장 움직임. 체류 발생시 저장 ( 혹은 하루 끝나면저장이나 사용자에의한 저장, 앱종료시 저장 많은 방법이 있다.)

// 직접적으로 센서 값과 여러가지 수행방법이 포함되는 곳으로 정확성과 데이터 절약이 필요하다.
//------------------------------------------- 5/31
// 추가내용

// 이 부분 정리 필요  -- 알람 매니저 사용하는 방법 정확하게 숙지하고
// 알람 매니저를 사용 5초당 한번씩 걸음 확인
// 걸음이 있으면 깸 걸음 수 측정 1분이 지나고 일정한시간 ( 30초 ~ 1분 )멈춤이 있으면 저장
// 걸음이 없는체로 5분이 지나면 깨서 위치 측정하고 위치측정이 끝나면 다시 5초당 한번씩 걸음확인 -- 걸음이 ( 30초 ~ 1분 )이 있으면 저장

// 알람 시작시 사용
// am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,0,5000,mRecevier );

// 센서 호출시 사용 - alarm-manager랑 같이 사용
//sm.registerListener(accl, accSensor, SensorManager.SENSOR_DELAY_NORMAL);

// wifi 스캔시 사용
// wm.startScan();

// gps 수신을 시작할때 사용
// lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, mGpsListener);

// 마무리 정리
// 걸음 수 측정하기 직접 수행하면서
// gps 측정 및 확인 필요
// ap 값 측정하기
// 화면이 꺼진 상태에서 데이터

// 집에 가서 -> 마무리하고 기능 작동 잘되는지 마무리 테스트 + ppt 작성 + Battery Historian을 사용하여 자원 사용 정보 얻기
// 내일 아침 직접테스트 위치 + 사용 하는 거 캡쳐 마무리

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {
    private static final String LOGTAG = "MainService";

    WifiManager wm;
    LocationManager lm;
    AlarmManager am;
    SensorManager sm;
    TextFileManager mFileMgr;

    private PowerManager.WakeLock wakeLock;
    Sensor accSensor;

    int steps = 0;
    int movenumber = 0, stopnumber = 0;     // 지정된 시간 움직, 체류 했을때 분기점이 되어줄 변수들

    IntentFilter filterwifi;
    save_ap Location1 = new save_ap();      // 4층교실
    save_ap Location2 = new save_ap();      // 다산1층
    save_gps Location3 = new save_gps();    // 운동장
    save_gps Location4 = new save_gps();    // 텔동

    // gps 관련
    AlertReceiver1 receiver1;
    AlertReceiver2 receiver2;
    PendingIntent proximityIntent1;
    PendingIntent proximityIntent2;

    // 알람 관련
    PendingIntent pendingIntent; //첫 알람
    private CountDownTimer timer;
    private StepMonitor accelMonitor;
    private long period = 10000;
    private static final long activeTime = 1000;
    private static final long periodForMoving = 5000;
    private static final long periodIncrement = 5000;
    private static final long periodMax = 10000;

    // 분기점 변수들
    int CHECK_WHAT_TO_DO = 0;        // 어떤 일을 해야하는지 알려주는 변수         - 1. movestart, 2. movestop, 3. staystart, 4. staystop, 0. null
    int base = CHECK_WHAT_TO_DO;     // CHECK_WHAT_TO_DO의 변화 확인하는 변수
    int place = 0;                   // 체류일때 어떻게 체류하는지 알려주는 변수    - 1. 지정 실내, 2.지정 실외, 3. 실내 , 4. 실외,  0. null
    int ap_location = 0;             // 지정 실내일때 어느 위치 인지 확인하는 변수  - 1. 4층 교실, 2. 다산 1층
    int gps_location = 0;            // 지정 실외일때 어느 위치 인지 확인하는 변수  - 1. 운동장, 2. 텔동
    Date starttime, endtime;         // 움직인, 체류한 시간을 계산하는데 사용하는 변수
    long staytime = 0, movetime = 0; // 움직인 체류한 시간을 출력하는데 사용하는 변수 (( 단위 - 초 ) 머무른 시간 계산하는데 사용 )
    String start_date, end_date;     // 움직인 체류한 시간을 출력하는데 사용하는 변수 ( HH:mm ~ HH:mm )

    //시간 데이터
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat mFo = new SimpleDateFormat("HH:mm"); //출력의 모습을 설정

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(LOGTAG, "onCreate");
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //파일 사용 부분 처리
        mFileMgr = new TextFileManager();
        mFileMgr.save("------------------------------------------------------------------------------------\n");
        //센서 사용 부분 처리
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        setlocationdata_ap();   // ap 데이터 설정
        setlocationdata_gps();  // gps 데이터 설정

       StartAlarm();

        super.onCreate();
    }

    //-- 알람 센서 -----------------------------------------------------------------------------------
    // 알람 간격 및 사용 부분 처리 필요
    private void StartAlarm() {
        Log.d(LOGTAG, "StartAlarm");
        // Alarm 발생 시 전송되는 broadcast를 수신할 receiver 등록
        IntentFilter intentFilter1 = new IntentFilter("main.alarm");
        registerReceiver(startAlarmReceiver, intentFilter1);

        // Alarm이 발생할 시간이 되었을 때, 안드로이드 시스템에 전송을 요청할 broadcast를 지정
        Intent intent = new Intent("main.alarm");
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000, pendingIntent);
    } //처음 알람을 만드는 구문 - 1초후 알람 발생
    private BroadcastReceiver startAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOGTAG, "startAlarmReceiver");
            if (intent.getAction().equals("main.alarm")) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AdaptiveDutyCyclingStepMonitor_Wakelock");
                wakeLock.acquire();
                accelMonitor = new StepMonitor(context);
                accelMonitor.onStart();
                // 1초 동안 계산
                timer = new CountDownTimer(activeTime, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        // stop the accel data update
                        accelMonitor.onStop();
                        // 움직임 여부에 따라 다음 alarm 설정
                        boolean moving = accelMonitor.isMoving();
                        setNextAlarm(moving);
                        if (accelMonitor.isMoving()) { //움직인다면
                            if (CHECK_WHAT_TO_DO == 3) {
                                CHECK_WHAT_TO_DO = 4;
                            } // 5분이상 멈췄었지만 이제는 멈춰있지 않는다면 endStayCase();
                            stopnumber = 0; //움직임을 줬을때 초기화
                            steps += accelMonitor.NUMBER_OF_STEPS_PER_SEC + 6;
                            movenumber++;
                            // 6*10 = 60 1분 ==> 10
                            Log.d(LOGTAG, "step => " + accelMonitor.NUMBER_OF_STEPS_PER_SEC+ " " +steps);
                            if (movenumber > 2) {
                                CHECK_WHAT_TO_DO = 1;
                            } // 1분 이상 움직였을때  StartMoveCase();
                        } else { // 움직이지 않는다면
                            if (CHECK_WHAT_TO_DO == 1) {
                                CHECK_WHAT_TO_DO = 2;
                            } // 1분 이상 움직였지만 이제는 움직이지 않는다면 endMoveCase();
                            else steps = 0; //1분 이상 움직이지 않고 멈췄을때 step 수 초기화
                            movenumber = 0; // 움직임을 멈추었을때 초기화
                            stopnumber++;
                            //6+11+16+ 21*12 = 285 => 약 4분 45 초 ==> 15
                            if (stopnumber > 2) {    // 5분이상 움직임이 없었을때  StartStayCase();
                                CHECK_WHAT_TO_DO = 3;
                            }
                        }
                        // 값의 변경이 있을때 만 수행
                        if (base != CHECK_WHAT_TO_DO) {
                            CheckMain();
                            base = CHECK_WHAT_TO_DO;
                        }
                        // When you finish your job, RELEASE the wakelock
                        wakeLock.release();
                        wakeLock = null;
                    } // 센서모니터로 계산된 움직임 여부에 따라 실행 분기
                };
                timer.start();
            }
        }
    };
    private void setNextAlarm(boolean moving) {
        Log.d(LOGTAG, "setNextAlarm");

        // 움직임이면 5초 period로 등록
        // 움직임이 아니면 5초 증가, max 10초로 제한
        if (moving) {
            period = periodForMoving;
        } else {
            period = period + periodIncrement;
            if (period >= periodMax) {
                period = periodMax;
            }
        }

        // 다음 alarm 등록
        Intent in = new Intent("main.alarm");
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, in, 0);
        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + period - activeTime, pendingIntent);
    } // 다음알람설정 체류에 따라 값이 변함
    //-- 걸음 수 센서 --------------------------------------------------------------------------------
    // 꺼 놓으면 안되는거 수정필요
    public class StepMonitor implements SensorEventListener {
        private static final String LOGTAG = "Step_Monitor";

        private Context context;
        private SensorManager mSensorManager;
        private Sensor mLinear;

        // 움직임 여부를 나타내는 bool 변수: true이면 움직임, false이면 안 움직임
        private boolean isMoving;

        // onStart() 호출 이후 onStop() 호출될 때까지 센서 데이터 업데이트 횟수를 저장하는 변수
        private int sensingCount;

        // 센서 데이터 업데이트 중 움직임으로 판단된 횟수를 저장하는 변수
        private int movementCount;

        // 움직임 여부를 판단하기 위한 3축 가속도 데이터의 RMS 값의 기준 문턱값
        private static final double RMS_THRESHOLD = 1.5;

        private static final double NUMBER_OF_STEPS_PER_SEC = 1.5;

        public StepMonitor(Context context) {
            this.context = context;

            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            mLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }

        public void onStart() {
            // SensorEventListener 등록
            if (mLinear != null) {
                Log.d(LOGTAG, "Register Accel Listener!");
                mSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_GAME);
            }
            // 변수 초기화
            isMoving = false;
            sensingCount = 0;
            movementCount = 0;
        }

        public void onStop() {
            // SensorEventListener 등록 해제
            if (mSensorManager != null) {
                Log.d(LOGTAG, "Unregister Accel Listener!");
                mSensorManager.unregisterListener(this);
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        // 센서 데이터가 업데이트 되면 호출
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(LOGTAG, "StepMonitor: onSensorChanged called");
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                sensingCount++;
                float[] values = event.values.clone();

                detectMovement(values);
            }
        }

        private void detectMovement(float[] values) {
            // 현재 업데이트 된 accelerometer x, y, z 축 값의 Root Mean Square 값 계산
            double rms = Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
            Log.d(LOGTAG, "rms: " + rms);

            // 계산한 rms 값을 threshold 값과 비교하여 움직임이면 count 변수 증가
            if (rms > RMS_THRESHOLD) {
                movementCount++; // 움직인 횟수 값으로 걸음을 알 수 있을까? 측정 후 확인필요. //1초동안 분석 움직임이있었다면 대략적으로 1초에 15스탭
            }

            // 여기서 걸음 수 측정 steps

        }

        // 일정 시간 동안 움직임 판단 횟수가 센서 업데이트 횟수의 50%를 넘으면 움직임으로 판단
        public boolean isMoving() {
            if (sensingCount == 0) {
                isMoving = false;
                return isMoving;
            }

            double ratio = (double) movementCount / (double) sensingCount;
            if (ratio >= 0.5) {
                isMoving = true;
            } else {
                isMoving = false;
            }
            return isMoving;
        }
    }
    //-- 분기점 CHECK_WHAT_TO_DO --------------------------------------------------------------------
    // 1. movestart, 2. movestop, 3. staystart, 4.staystop
    public void CheckMain() {
        Log.d(LOGTAG, "Check" + CHECK_WHAT_TO_DO);
        if (CHECK_WHAT_TO_DO == 1) {
            StartMoveCase();
        } else if (CHECK_WHAT_TO_DO == 2) {
            EndMoveCase();
        } else if (CHECK_WHAT_TO_DO == 3) {
            StartStayCase();
        } else if (CHECK_WHAT_TO_DO == 4) {
            EndStayCase();
        }
    }
    //-- 움직임 데이터  ------------------------------------------------------------------------------- ok
    public void StartMoveCase() { // 시작 부분 - 시작시간 처리한다.
        Log.d(LOGTAG, "StartMoveCase");
        starttime = new Date(System.currentTimeMillis());
    }   // 움직임 시작
    public void EndMoveCase() {
        Log.d(LOGTAG, "EndMoveCase");
        GetMoveTime();
        SetMove();
    }     // 움직임 끝
    public void GetMoveTime() {
        endtime = new Date(System.currentTimeMillis());
        movetime = (endtime.getTime() - starttime.getTime() + 60000) / 60000;
        end_date = mFo.format(endtime);
        start_date = mFo.format(starttime);
    }     // 움직임 시간 계산
    public void SetMove() {
        mFileMgr.save(start_date + "~" + end_date  + " 이동 - " + movetime + " 분. " + steps + " 걸음\n");
       steps = 0;
    }         // 움직임, 시간 기록
    //-- 체류 데이터 ---------------------------------------------------------------------------------
    // 지정된 실내외 작동 정확히 수정하고 실내인지 실외인지 알수있는 방법 찾기
    // 순서 1.   저장 실내 ap값 분석   - 지정된 실내 확인
    //     2.   저장 실외 gps값 측정
    //     3.   만약 지정된 시간안에 gps 값이 받아지지 않는다면 지정되지 않은 실내 -지정되지 않은 실내확인
    //     3-1. 그거 랑 위치 분석해서 해도 될듯
    //     4.   gps값 분석 - 지정된 실외, 지정되지 않은 실외 확인

    public void StartStayCase() {
        Log.d(LOGTAG, "StartStayCase");
        starttime = new Date(System.currentTimeMillis());
        StartWifi();  // - 순서 1
    }   // 체류시작
    public void EndStayCase() {
        Log.d(LOGTAG, "EndStayCase");
        GetStayTime();
        SetStay();
    }     // 체류 끝
    public void GetStayTime(){
        //분 단위 endtime - starttime + 5분
        endtime = new Date(System.currentTimeMillis());
        staytime = (endtime.getTime() - starttime.getTime() + 300000) / 60000;
        end_date = mFo.format(endtime);
        start_date = mFo.format(starttime);
    }      // 체류 시간 계
    public void SetStay(){
        GetStayTime(); //4가지 케이스중 하나에는 입력
        switch (place){ // 체류시 지정 실내 - 1, 지정 실외 - 2, 실내 - 3 , 실외 - 4
            case 1: {
                Log.d(LOGTAG, "지정실내 - " + place);
                if(ap_location == 1){
                    mFileMgr.save(start_date + "~" + end_date + " " + Location1.GetName() + " - " + staytime + " 분\n");
                } else if(ap_location == 2){
                    mFileMgr.save(start_date + "~" + end_date + " " + Location2.GetName() + " - " + staytime + " 분\n");
                }
                break;
            }
            case 2: {
                Log.d(LOGTAG, "지정실외 - " + place);
                if(gps_location == 1){
                    mFileMgr.save(start_date + "~" + end_date  + " " + Location3.GetName() + " - " + staytime + " 분\n");
                } else if(gps_location == 2){
                    mFileMgr.save(start_date + "~" + end_date  + " " + Location4.GetName() + " - " + staytime + " 분\n");
                }
                break;
            }
            case 3: {
                Log.d(LOGTAG, "실내 - " + place);
                mFileMgr.save( start_date + "~" + end_date  + " " + "실내 - " + staytime + " 분\n");
                break;
            }
            case 4: {
                Log.d(LOGTAG, "실외 - " + place);
                mFileMgr.save( start_date + "~" + end_date  + " " + "실외 - " + staytime + " 분\n");
                break;
            }
        }
        //자원의 종료
        EndGps();
        EndWifi();
    }          // 체류, 시간 기록

    public void StartGps(){
        LocationAlarm();
        if (ActivityCompat.checkSelfPermission(MainService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) { return;}

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0, mGpsListener);

        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                if(place == 0){ stay_in(); EndGps(); }
            }
        }; //일정 시간동안 gps가 잡히지 않는다면 지정되지 않은 실내

        Timer timer = new Timer();
        timer.schedule(t,20000); // 20초 이후에도 값이 없으면 실내

    }         // gps  관련 실행 함수
    public void StartWifi(){
        filterwifi = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filterwifi.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filterwifi);
        if (wm.isWifiEnabled() == false)
            wm.setWifiEnabled(true);
        wm.startScan();
    }        // wifi 관련 실행 함수
    public void EndGps(){
        try {
            // 자원해제 gps위치
            lm.removeUpdates(mGpsListener);
            lm.removeProximityAlert(proximityIntent1);
            unregisterReceiver(receiver1);
            lm.removeProximityAlert(proximityIntent2);
            unregisterReceiver(receiver2);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }           // gps  관련 종료 함수
    public void EndWifi(){
        try {
            unregisterReceiver(mWifiReceiver);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }          // wifi 관련 종료 함수
    public void ScanResults(){
        try { if (wm.getScanResults() != null) {
            //Log.d(LOGTAG, "1. " + wm.getScanResults().get(0).SSID + " " + wm.getScanResults().get(1).SSID);
            // Log.d(LOGTAG, "1. " + Location2.GetSSRD(0, 0) + " " + Location2.GetSSRD(0, 1));
            if (wm.getScanResults().get(0).SSID.equals(Location1.GetSSRD(0, 0))
                    && Location1.GetRSSD_s(0, 0) < wm.getScanResults().get(0).level) {
                if (wm.getScanResults().get(1).SSID.equals(Location1.GetSSRD(0, 1))
                        && Location1.GetRSSD_s(0, 1) < wm.getScanResults().get(1).level) {
                    place = 1; ap_location = 1; //장소1일때 case1
                }
            } else if (wm.getScanResults().get(0).SSID.equals(Location1.GetSSRD(1, 0))
                    && Location1.GetRSSD_s(1, 0) < wm.getScanResults().get(0).level) {
                if (wm.getScanResults().get(1).SSID.equals(Location1.GetSSRD(1, 1))
                        && Location1.GetRSSD_s(1, 1) < wm.getScanResults().get(1).level) {
                    place = 1; ap_location = 1; //장소1일때 case2
                }
            }
            if (wm.getScanResults().get(0).SSID.equals(Location2.GetSSRD(0, 0))
                    && Location1.GetRSSD_s(0, 0) < wm.getScanResults().get(0).level) {
                if (wm.getScanResults().get(1).SSID.equals(Location2.GetSSRD(0, 1))
                        && Location1.GetRSSD_s(0, 1) < wm.getScanResults().get(1).level) {
                    place = 1; ap_location = 2; // 장소 2일때
                }
            }
        }} catch (IndexOutOfBoundsException ex) { ex.printStackTrace(); }
    }      // wifiscan 된 값을 비교해서 위치 선택

    public class save_ap {
        String ap_name;
        String[][] ap_SSRD;
        int[][] ap_RSSI_s;
        int[][] ap_RSSI_l;

        //case가 여러개 필요할듯
        public void SetAp(String name, String[][] SSRD, int[][] RSSIs, int[][] RSSIl) {
            ap_name = name;
            ap_SSRD = SSRD;
            ap_RSSI_s = RSSIs;
            ap_RSSI_l = RSSIl;
        }

        public String GetSSRD(int Case, int i) {
            return ap_SSRD[Case][i];
        }

        public int GetRSSD_s(int Case, int i) {
            return ap_RSSI_s[Case][i];
        }

        public int GetRSSD_l(int Case, int i) {
            return ap_RSSI_l[Case][i];
        }

        public String GetName() {
            return ap_name;
        }
    }               // ap데이터를 저장한 클래스 ap데이터를 받아올 수 있다.
    public void setlocationdata_ap() {
        String name1 = "401";
        String[][] SSRD1 = {{"406", "KUTAP_N"}, {"KUTAP_N", "406"}, {"KUTAP", "KUTAP_N"}, {"KUTAP", "406"}}; // 추가 될 수 있음
        int[][] RSSI1_s = {{-55, -69}, {-73, -52}, {-50, -50}, {-51, -59}};
        int[][] RSSI1_l = {{-70, -89}, {-93, -72}};
        String name2 = "다산정보관 1층 로비";
        String[][] SSRD2 = {{"KOREATECH", "KOREATECH"}, {"KUTAP_N","KOREATECH"},{"KUTAP_N", "KUTAP_N"}};
        int[][]  RSSI2_s = {{-62, -58}, {-45, -60}, {-50, -70}};
        int[][]  RSSI2_l = {{-85, -85},};

        /*String name2 = "랩실";
        String[][] SSRD2 = {{"uoc5G", "KangLab5G"}, {"", ""}};
        int[][] RSSI2 = {{-64, -80}, {-54, -95}};
        /*String name2 = "집";
        String[][] SSRD2 = {{"iptime", "hy1838"}, {"달_2", "iptime"}};
        int[][] RSSI2 = {{-64, -80}, {-54, -95}};*/
        Location1.SetAp(name1, SSRD1, RSSI1_s, RSSI1_l);
        Location2.SetAp(name2, SSRD2, RSSI2_s, RSSI1_l);
    }   // 데이터 클레스에 저장 - 장소가 넓기 때문에 여러가지 케이스가 있을수 있다. 정확한 장소 확인을 위해 케이스를 여러가지 준비
    public class save_gps {
        String gps_name;
        double latitude;
        double longitude;
        float radius;

        public void SetGps(String name, double latitude, double longitude, float radius) {
            gps_name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
        }

        public String GetName(){ return gps_name; }

    }              // gps데이터를 저장한 클래스 ap데이터를 받아올 수 있다.
    public void setlocationdata_gps() {
        String name1 = "운동장";
        double latitude1 = 36.762581;
        double longitude1 = 127.284527;
        float radius1 = 80;
        String name2 = "텔레토비 동산";
        double latitude2 = 36.764215;
        double longitude2 = 127.282173;
        float radius2 = 50;

        Location3.SetGps(name1, latitude1, longitude1, radius1);
        Location4.SetGps(name2, latitude2, longitude2, radius2);
    }  // 데이터 클레스에 저장 - 장소가 넓기 때문에 여러가지 케이스가 있을수 있다. 정확한 장소 확인을 위해 케이스를 여러가지 준

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.d(LOGTAG, " wifi - scan되고 어디 인지 확인 ");
                // 검색이 되었을 때 기준으로 값을 출력하기 위해 사용하였다.

                ScanResults();
                Log.d(LOGTAG, "스캔 값에 따른 변화 place - " + place+" ap_location - " + ap_location );
                if ( place == 0 ) { StartGps(); EndWifi(); } // 와이파이 위치 검출이 안됬을때 gps 수행 - 순서 2
            }
        }
    }; // 실내 저장된 위치 채류시 확인 브로드캐스트

    public void LocationAlarm(){
        Log.d(LOGTAG, " gps Location 알람 설정완료");
        receiver1 = new AlertReceiver1();
        IntentFilter filter1 = new IntentFilter("운동장");
        registerReceiver(receiver1, filter1);

        // ProximityAlert 등록을 위한 PendingIntent 객체 얻기
        Intent intent1 = new Intent("운동장");
        proximityIntent1 = PendingIntent.getBroadcast(this, 0, intent1, 0);

        // 근접 경보 등록 메소드 범위안에 gps가 실행되면서 AlertReceiver1가 실행되고 안에 있다면 저장
        try { lm.addProximityAlert(Location3.latitude, Location3.longitude, Location3.radius, -1, proximityIntent1);
        } catch (SecurityException e) { e.printStackTrace(); }

        receiver2 = new AlertReceiver2();
        IntentFilter filter2 = new IntentFilter("텔레토비동산");
        registerReceiver(receiver2, filter2);

        // ProximityAlert 등록을 위한 PendingIntent 객체 얻기
        Intent intent2 = new Intent("텔레토비동산");
        proximityIntent2 = PendingIntent.getBroadcast(this, 0, intent2, 0);

        // 근접 경보 등록 메소드
        try { lm.addProximityAlert(Location4.latitude, Location4.longitude,Location4.radius, -1, proximityIntent2);
        } catch (SecurityException e) { e.printStackTrace();
        }
    } // GPS 지정된 장소 바운더리 설정 - gps 값을 받아올 수 있는 환경에서 확인 필요
    public class AlertReceiver1 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            place = 2; gps_location = 1; EndGps();
            Log.d(LOGTAG, "스캔 값에 따른 변화 place - " + place + " ap_location - " + gps_location );
        }
    } // 1번 위치일 때  gps 값을 받아올 수 있는 환경에서 확인 필요
    public class AlertReceiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            place = 2; gps_location = 2; EndGps();
            Log.d(LOGTAG, "스캔 값에 따른 변화 place - " + place + " ap_location - " + gps_location );
        }
    } // 2번 위치일 때
    public LocationListener mGpsListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Toast.makeText(MainService.this, "gps - " + location.getLongitude(), Toast.LENGTH_SHORT).show();
            Log.d(LOGTAG, " gps값 변경 - "+ location.getLongitude() + " " + location.getLatitude() );
            if(place == 0) { stay_out(); EndGps(); }
        }
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
        public void onProviderEnabled(String s) {
        }
        public void onProviderDisabled(String s) {
        }
    }; // gps값이 변화시 사용
    public void stay_in(){
        place = 3;
        Log.d(LOGTAG, " place - " + place );
    }    // 지정되지 않은 실내일때 - OK
    public void stay_out(){
        place = 4;
        Log.d(LOGTAG, " place - " + place );
    }   // 지정되지 않은 실외일때 - OK
    //------종료시 수행--------------------------------------------------------------------------------
    public void onDestroy() {
        try {
            unregisterReceiver(startAlarmReceiver); // Alarm 발생 시 전송되는 broadcast 수신 receiver를 해제
            unregisterReceiver(mWifiReceiver);      // wifiscan 발생 시 전송되는 broadcast 수신 receiver를 해제
        } catch(IllegalArgumentException ex) { ex.printStackTrace(); }
        // AlarmManager에 등록한 alarm 취소
        am.cancel(pendingIntent);

        try {
            // 자원해제 gps위치
            lm.removeProximityAlert(proximityIntent1);
            unregisterReceiver(receiver1);
            lm.removeProximityAlert(proximityIntent2);
            unregisterReceiver(receiver2);

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

        // release all the resources you use
        if(timer != null)
            timer.cancel();
        if(wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }
}

