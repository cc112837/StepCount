package com.cc.stepcount;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.cc.stepcount.db.MySqliteOpenHelper;
import com.cc.stepcount.service.StepCounterService;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import me.pedometer.StepDetector;

public class MainActivity extends Activity {
    private ColorfulRingProgressView crpv;
    private TextView tv_alldata;
    private TextView stepTv;
    private ContentValues values;
    private MySqliteOpenHelper helper;
    private SQLiteDatabase database;
    private GraphView linechart;
    private Thread thread;  //定义线程对象
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String time;
    private ArrayList<HashMap<String,String>>  list;
    int step;
    private int total_step = 0;   //走的总步数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_alldata = (TextView) findViewById(R.id.tv_alldata);
        initDB();
        sp = getSharedPreferences("step", MODE_PRIVATE);
        editor = sp.edit();
        time = sp.getString("time", null);
        step = sp.getInt("step", 0);
        crpv = (ColorfulRingProgressView) findViewById(R.id.crpv);
        stepTv = (TextView) findViewById(R.id.activity_main_step_tv);
        stepTv.setText(step+ "");// 显示当前步数
        crpv.setPercent((step) / 10);
        linechart = (GraphView) findViewById(R.id.linechart);
        tv_alldata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AllDataActivity.class);
                startActivity(intent);
            }
        });
        aboutChart();
        initThreadToPedometer();
        startService(new Intent(this, StepCounterService.class));
        writeSql();
    }

    private void initDB() {
        helper = new MySqliteOpenHelper(this, FinalValue.DB_NAME, null,
                FinalValue.DB_VERSION);
        database = helper.getReadableDatabase();
    }


    private void aboutChart() {
        int d = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 0),
                new DataPoint(1,5432),
                new DataPoint(2, 3232),
                new DataPoint(3, 8686),
                new DataPoint(4, 1211),
                new DataPoint(5, 2000),
                new DataPoint(6, 6434),
                new DataPoint(7, 4231),
                new DataPoint(8, 0)
        });
        linechart.addSeries(series);
        linechart.getLegendRenderer().setTextColor(Color.WHITE);
        // style
        series.setColor(Color.rgb(191, 226, 226));
        series.setThickness(2);
        // legend
        series.setTitle("步数");
        linechart.getLegendRenderer().setVisible(true);
        linechart.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.MIDDLE);


    }



    private void initThreadToPedometer() {
        if (thread == null) {

            thread = new Thread() {// 子线程用于监听当前步数的变化

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    super.run();
                    int temp = 0;
                    while (true) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (StepCounterService.FLAG) {
                            Message msg = new Message();
                            if (temp != StepDetector.CURRENT_STEP) {
                                temp = StepDetector.CURRENT_STEP;
                            }
                            handler.sendMessage(msg);// 通知主线程
                        }
                    }
                }
            };
            thread.start();
        }
    }

    // 当创建一个新的Handler实例时, 它会绑定到当前线程和消息的队列中,开始分发数据
    // Handler有两个作用, (1) : 定时执行Message和Runnalbe 对象
    // (2): 让一个动作,在不同的线程中执行.

    Handler handler = new Handler() {// Handler对象用于更新当前步数,定时发送消息，调用方法查询数据用于显示？？？？？？？？？？
        //主要接受子线程发送的数据, 并用此数据配合主线程更新UI
        //Handler运行在主线程中(UI线程中), 它与子线程可以通过Message对象来传递数据,
        //Handler就承担着接受子线程传过来的(子线程用sendMessage()方法传递Message对象，(里面包含数据)
        //把这些消息放入主线程队列中，配合主线程进行更新UI。

        @Override                  //这个方法是从父类/接口 继承过来的，需要重写一次
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);        // 此处可以更新UI
            countStep();          //调用步数方法
            stepTv.setText(total_step +step+ "");// 显示当前步数
            crpv.setPercent((total_step+step) / 10);
            editor.putInt("step", total_step+step);
            editor.commit();
        }
    };

    private void countStep() {
        if (StepDetector.CURRENT_STEP % 2 == 0) {
            total_step = StepDetector.CURRENT_STEP;
        } else {
            total_step = StepDetector.CURRENT_STEP + 1;
        }
        total_step = StepDetector.CURRENT_STEP;
    }


    public void writeSql() {
        int d = getTime();
        if (!(d + "").equals(time)) {
            values.put("title", step + "");
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int mm = Calendar.getInstance().get(Calendar.MINUTE);
            int ss = Calendar.getInstance().get(Calendar.SECOND);
            values.put("time", hour + ":" + mm + ":" + ss);
            database.insert(FinalValue.TB_STEP, null, values);
            editor.putString("time", d + "");
            StepDetector.CURRENT_STEP=0;
            step=0;
            editor.commit();
        }
    }

    private int getTime() {
        long l = System.currentTimeMillis()-24*60*60*1000; //前一天天的毫秒数
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        int d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        values = new ContentValues();
        values.put("day", sf.format(new Date(l)));
        return d;
   }


    @Override
    protected void onDestroy() {
        editor.putInt("step", total_step+step);
        editor.commit();
        super.onDestroy();
    }
}
