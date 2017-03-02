package com.dingok.movie.seatmap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.dingok.movie.seatmap.seatMapView.SeatDataFactory;
import com.dingok.movie.seatmap.seatMapView.SeatMapView;
import com.dingok.movie.seatmap.seatMapView.SeatSetting;
import com.dingok.movie.seatmap.seatMapView.SeatView;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    Activity activity = this;
    FrameLayout seatContainer;
    Button commitButton;
    SeatMapView seatMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger
                .init("willy")                 // default PRETTYLOGGER or use just init()
                .methodCount(3);              // default 2

        seatContainer = (FrameLayout) findViewById(R.id.seatMap_container);
        commitButton = (Button) findViewById(R.id.seatMap_commitPostButton);

        //new 一個基本的 setting
        SeatSetting seatSetting = new SeatSetting(SeatDataFactory.testSeatData())
                .setScreenOrientationUp(false)
                .setProhibitSpaceSeatRule(true)
//                .setScreenSplitY(0.5f)
//                .setMaxBuyCount(5)
//                .setMinBuyCount(2)
//                .setSeatSizeScale(0.5f)
//                .setZoom(1f)
//                .setNotificationTextColor(Color.YELLOW)
//                .setNotificationTextSizeDp(20)
//                .setSeatSpaceX(0)
//                .setSeatSpaceY(0.5f)
//                .setUpperZoneBackgroundColor(Color.BLACK)
//                .setLowerZoneBackgroundColor(0xff330000)
//                .setLowerZoneCubeColor(Color.BLUE)
//                .setLowerZoneCubeAlpha(100)
                ;

        //如果是要修改訂單的話，設定己選座位的顯示
        if (getIntent().getBooleanExtra("isModify",false) ){
            try {
                JSONArray selectedJSONArray = new JSONArray(getIntent().getStringExtra("selectedDataStr"));
                seatSetting.setSelectedSeatDataByJSONArray(selectedJSONArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        seatMapView = new SeatMapView(this, seatSetting);
        seatContainer.addView(seatMapView);

        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //符合規則後送出資料
                if (seatMapView.check()) {
                    Intent intent = new Intent(activity, LaunchActivity.class);
                    intent.putExtra("result", seatMapView.getResultByJSONArray().toString());
                    setResult(Constant.MAIN_ACTIVITY, intent);
                    finish();
                }
            }
        });
    }
}
