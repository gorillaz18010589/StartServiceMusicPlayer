package com.example.startservicemusicplayer;
//撥放音樂,暫停音樂,取消音樂有以下四個流程
//1.三個重要生命流程Override
//2.取得要撥放的音樂物件,在onCreate時物件實體化
//3.設置三種常數狀態參數,當使用者按下PlayOrPAuse建時,當intent傳來StartService時被調用,收到Intent判斷三種狀態,設置播放音樂,或暫停,或等等..
//4.當Service被摧毀時調用,當Activity被摧毀時,停止播放音樂

//設置音樂seekbar用執行續排任務,每0.5秒將音樂撥放的位置參數用sendBroadcast傳遞出去給Activity寫的廣播接受
//5.設置音樂最大持續時間的參數,利用廣播傳遞給Activity接收
//6.利用Timer給UpdateTask任務,每0.5秒取得一次MediaPlayer的現在撥放的進度參數,用sendBroadcast傳遞給Activity
//7.自己寫一個UpdateTask類別繼承老爸TimerTask,每5秒取得音樂撥放的進度,並傳遞給Activity
//8.取消Timer任務,清掉裡面任務,給予空值

//新加入功能,用startForeground,加notification創建頻道即可使讓Service活下來記得開權限



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class PlayMusicService extends Service {
    private MediaPlayer mediaPlayer;
    private String TAG ="hank";
    private Timer timer;

    public final static int ACTION_PLAY = 1; //撥放
    public final static int ACTION_PAUSE = 2; //暫停
    public final static int ACTION_SEEKTO = 3;

    public final static String CHANNEL_ID ="hank";
    public PlayMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return null;
    }

    //1.Service第一次幢建時初始化
    @Override
    public void onCreate() {
        super.onCreate();
        //取得要撥放的音樂物件
        mediaPlayer = MediaPlayer.create(this,R.raw.music);

        //9.將推撥設定於前警啟動
        final  Notification[]notifications = {new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("音樂mp3")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()};
        startForeground(1,notifications[0]);

        //10.建立推撥頻道
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,"頻道名", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        //5.設置音樂最大持續時間的參數,利用廣播傳遞給Activity接收
        Intent intent = new Intent("formService");
        intent.putExtra("max",mediaPlayer.getDuration()); //取得文件持續的時間,用intente參數送出
        sendBroadcast(intent);

        //6.利用Timer給UpdateTask任務,每0.5秒取得一次MediaPlayer的現在撥放的進度參數,用sendBroadcast傳遞給Activity
        timer = new Timer();
        timer.schedule(new UpdateTask(),0,500); //設定任務每5秒抓一次音樂位置
        Log.v(TAG,"PlayMusicService=>onCreate()");
    }


    //7.自己寫一個UpdateTask類別繼承老爸TimerTask,每5秒取得音樂撥放的進度,並傳遞給Activity
    private  class  UpdateTask extends TimerTask {
        @Override
        public void run() {
            //如果有初始化mediaPlayer物件,而且音樂正在撥放的話
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                //將現在音樂撥放的進度mediaPlayer.getCurrentPosition()參數,用sendBroadcast傳遞給Activity
                Intent intent = new Intent("formService");
                intent.putExtra("wherenow",mediaPlayer.getCurrentPosition());//傳遞現在音樂撥放的進度參數
                sendBroadcast(intent);//傳遞給Activity的廣播接受
                Log.v(TAG,"PlayMusicService=>UpdateTask()");
            }
        }
    }

    //2.當intent傳來StartService時被調用,收到Intent判斷三種狀態,設置播放音樂,或暫停,或等等..
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action",-1);

        //3.設置三種常數狀態參數,當使用者按下PlayOrPAuse建時,當intent傳來StartService時被調用,收到Intent判斷三種狀態,設置播放音樂,或暫停,或等等..
        switch (action){
            case ACTION_PLAY:  //當收到撥放Action時
                //如果因樂沒有再撥放,讓音樂開始撥放
                if(!mediaPlayer.isPlaying()) mediaPlayer.start();
                break;
            case ACTION_PAUSE://當收到暫停Action時
                //如果音樂正在撥放,讓音樂暫停
                if(mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case ACTION_SEEKTO:

                break;
        }
        Log.v(TAG,"PlayMusicService=>onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    //*.當Service被摧毀時調用,當Activity被摧毀時,停止播放音樂
    @Override
    public void onDestroy() {
        super.onDestroy();



        //4.當Service被摧毀時調用,當Activity被摧毀時,停止播放音樂
        if(mediaPlayer != null){//如果MediaPlayer因為Service onCreate被創建時不為空
          if(mediaPlayer.isPlaying())mediaPlayer.stop(); //如果音樂正在撥放,讓音樂停止
          mediaPlayer.release(); //清空音樂
          mediaPlayer = null; //音樂物件清空初始值
        }

        //8.取消Timer任務,清掉裡面任務,給予空值
        if(timer != null){
            timer.cancel();
            timer.purge();
            timer = null;
        }


        Log.v(TAG,"PlayMusicService=>onDestroy()");
    }
}
