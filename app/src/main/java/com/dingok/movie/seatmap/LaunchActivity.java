package com.dingok.movie.seatmap;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LaunchActivity extends AppCompatActivity {

    Activity activity = this;
    Button startBookingButton;
    TextView ticketInfoTextView;
    JSONArray selectedTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);

        startBookingButton = (Button) findViewById(R.id.startBookingButton);
        ticketInfoTextView = (TextView) findViewById(R.id.ticketInfotextView);

        startBookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, MainActivity.class);

                //如果是要修改訂單則傳訂單資料過去
                if (startBookingButton.getText().toString().equals("MODIFY ORDER")) {
                    intent.putExtra("isModify",true);
                    intent.putExtra("selectedDataStr",selectedTickets.toString());
                }

                startActivityForResult(intent, Constant.LAUNCH_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.LAUNCH_ACTIVITY && resultCode == Constant.MAIN_ACTIVITY) {
            try {
                String arrayStr = data.getStringExtra("result");
                selectedTickets = new JSONArray(arrayStr);
                Logger.json(selectedTickets.toString());

                //顯示回傳資料
                String infoStr = "[ ";
                for (int i = 0; i < selectedTickets.length(); i++) {
                    JSONObject ticket = selectedTickets.getJSONObject(i);
                    infoStr += String.valueOf(Integer.valueOf(ticket.getString("row"))) + "-" + String.valueOf(Integer.valueOf(ticket.getString("col"))) + ", ";
                }
                infoStr = infoStr.substring(0, infoStr.length() - 2) + " ]";
                ticketInfoTextView.setText(infoStr);
                ticketInfoTextView.setTextSize(22);

                //更變按鈕文字
                startBookingButton.setText("MODIFY ORDER");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
