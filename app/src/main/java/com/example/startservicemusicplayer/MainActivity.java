package com.example.startservicemusicplayer;
//Intent大全:https://codertw.com/%E7%A8%8B%E5%BC%8F%E8%AA%9E%E8%A8%80/596266/
//1.配置Music Plyaer.xnl 將MP3音樂檔案放入資源區
//2.Res創建raw資源區將音樂MP3放入資源區
//3.創建PlayMusicService
//4.準備boolean isPlaying,讓PlayOrPause按鈕依照是否再撥放,改變撥放還是暫停的文字
//5.使用者按下開始或暫停建,依照isPlaying的狀態,給予不同的參數StartService 傳遞參數給Service
//6使用者按停止鍵,設定並且關閉StopSerVice
//====================================以上為Activiy 音樂撥放,暫停,關閉流程

//開始接收Service傳來的音樂進度參數利用廣播接受
// 一個intent filter是一個IntentFilter類的實例。但是，android系統必須在組件未啓動的情況下就知道它的能力，因此intent filter一般不會在java代碼中設置，
// 而是在應用的manifest文件中作爲<intent-filter>元素的方式聲明。一個例外是，爲broadcast receiver註冊動態的filter，可以調用Context.registerReceiver()方法，
// 通過直接實例化IntentFilter對象創建。
//7.準備廣播接受,將收到的參數設定在SeekBar
//8.在OnStart時註冊廣播並且用Intent filter
//
//====================================以上為Activity進度跳隨著音樂跑動流程

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
    Button btnPlayPause;
    boolean isPlaying; //判斷音樂是否撥放中
    String TAG = "hank";
    SeekBar seekBar;
    MyReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isPlaying = false; //初始化第一次預設為false
        btnPlayPause = findViewById(R.id.playOrPause);

        seekBar = findViewById(R.id.seekbar);
        Log.v(TAG, "MainActivity=>onCreate()");
    }

    //registerReceiver(BroadcastReceiver receiver, IntentFilter filter)://註冊intent動態(1.自訂的廣播類別, 2.自訂的intneFilter)(回傳值Intent)
    //IntentFilter(String action)://intentFilter物件實體化("自己寫的Itent名稱")
    //8.在OnStart時註冊廣播並且用Intent filter
    @Override
    protected void onStart() {
        super.onStart();
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter("formService");//intentFilter物件實體化("自己寫的Itent名稱")
        registerReceiver(myReceiver, intentFilter);//註冊intent動態(1.自訂的廣播類別, 2.自訂的intneFilter)
        Log.v(TAG, "MainActivity=>onStart()");
    }

    //unregisterReceiver(BroadcastReceiver receiver)://取消廣播註冊(receiver)
    //9.停止時取消廣播註冊
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myReceiver);
        Log.v(TAG, "MainActivity=>onStop()");
    }

    //按鈕開始或暫停鍵
    public void playOrPause(View view) {
        //4.判斷PlayOrPause按鈕依照是否再撥放,改變撥放還是暫停的文字
        isPlaying = !isPlaying;
        if (isPlaying) { //如果正在撥放,那按鈕出現暫停
            btnPlayPause.setText("暫停");
        } else { //如果沒有再撥放,按鈕出現撥放
            btnPlayPause.setText("撥放");
        }

        //5.使用者依據暫停或撥放給予參數Action傳遞到Service
        Intent intent = new Intent(this, PlayMusicService.class);
        //如果使用者按下正在撥放的話,傳遞ACTION_PLAY參數,如果使用者沒有撥放的話傳遞ACTION_PAUSE參數
        intent.putExtra("action",
                isPlaying ? PlayMusicService.ACTION_PLAY : PlayMusicService.ACTION_PAUSE); //
        startService(intent); //將參數傳遞到onStartCommand()
        Log.v(TAG, "MainActivity=>playOrPause");
    }


    //6.使用者按停止鍵
    public void stopPlay(View view) {
        isPlaying = false;
        btnPlayPause.setText("撥放");
        Intent intent = new Intent(this, PlayMusicService.class);
        stopService(intent); //傳遞給Service => Ondestoy銷毀
        Log.v(TAG, "MainActivity=>stopPlay");
    }

    //7.準備廣播接受,將收到的參數設定在SeekBar
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //設定進度條最大的音樂時間
            int max = intent.getIntExtra("max", -1); //取得Service傳來的音樂持續時間
            if (max >= 0) {
                seekBar.setMax(max);//設定最大進度條為整首個時間
                Log.v(TAG, "MainActivity=>MyReceiver => setMax:" + max);
            }

            //設定音樂進度條的當前聽取位置
            int wherenow = intent.getIntExtra("wherenow", -1); //取得Service傳來的當前音樂位置
            if (wherenow >= 0) {
                seekBar.setProgress(wherenow); //設定進度條為當前音樂撥放位置
                Log.v(TAG, "MainActivity=>MyReceiver => wherenow:" + wherenow);
            }
            Log.v(TAG, "MainActivity=>MyReceiver");
        }
    }
}
