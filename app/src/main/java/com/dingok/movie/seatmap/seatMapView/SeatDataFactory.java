package com.dingok.movie.seatmap.seatMapView;

import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by yanzewei on 2017/2/15.
 */

public class SeatDataFactory {

    public SparseArray<SeatView> convertedData, selectedSeatData = new SparseArray<>();

    public SeatDataFactory() {
        this.convertedData = convertOld(getTextDataStr());
    }

    public SeatDataFactory(String dataStr) {
        //怕string格式不符, 想要在使用的時候改變or選擇轉換方法，所以應該改依賴 Interface
        //但如果轉換方法是由使用者來寫的話，好像也不方便，而且格式可能不是我們需要的？
        this.convertedData = convertOld(dataStr);
    }

    /**
     * 設定己選擇的座位資料，將在初始UI時顯示出來
     * @param selectedSeatData
     */
    public SeatDataFactory setSelectedSeatData(SparseArray<SeatView> selectedSeatData) {
        this.selectedSeatData = selectedSeatData;
        return this;
    }

    /**
     * 設定己選擇的座位資料，將在初始UI時顯示出來
     * @param selectedJSONArray
     */
    public SeatDataFactory setSelectedSeatDataByJSONArray(JSONArray selectedJSONArray) {
        SparseArray<SeatView> selectedSeatData = new SparseArray<>();
        for (int i = 0 ;i<selectedJSONArray.length();i++){
            JSONObject object = selectedJSONArray.optJSONObject(i);
            SeatView seatView = new SeatView(object.optInt("index")
                    ,object.optInt("x")
                    ,object.optInt("y")
                    ,object.optInt("state")
                    ,object.optString("row")
                    ,object.optString("col")
                    ,object.optString("area"));
            selectedSeatData.put(seatView.index(),seatView);
        }
        return setSelectedSeatData(selectedSeatData);
    }

    public static SparseArray<SeatView> convertOld(String data) {

        SparseArray<SeatView> list = new SparseArray<SeatView>();
        int maxX;
        String row;
        String col;
        String area;
        String status;

        try {
            JSONArray ja = new JSONArray(data);
            if (ja == null)
                throw new NullPointerException();
            JSONObject jo = ja.optJSONObject(0);
            if (jo == null)
                throw new NullPointerException();

            maxX = jo.getInt("MaxX");
            row = jo.getString("排號");
            col = jo.getString("位號");
            area = jo.getString("區號");
            status = jo.getString("狀況1");

            for (int a = 0, index = 0; index < status.length(); a += 3, index++) {
                int x = index % Integer.valueOf(maxX);
                int y = (int) (index / Integer.valueOf(maxX));
                //舊影城不用轉換16進位到10進位
                String tmpRow = row.substring(a, a + 3);
                String tmpCol = col.substring(a, a + 3);
                String tmpArea = area.substring(a, a + 3);
                //轉換前 +為走道 0為空位(area需為002、其它視為售出) 1為售出
                //轉換後 狀態代號 1可選 2選中 3佔用 4保留 5哭臉
                String tmpState = status.substring(index, index + 1);
                int state = 0;
                if (tmpState.equals("0") && tmpArea.equals("002")) {
                    state = 1;
                } else if (tmpState.equals("1") || tmpArea.equals("138")) {
                    state = 3;
                }
                SeatView holder = new SeatView(index, x, y, state, tmpRow, tmpCol, tmpArea);
                list.put(index, holder);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getTextDataStr() {
        return "[\n" +
                "  {\n" +
                "  \"AllwaysTrs\" : 0,\n" +
                "  \"Chang\" : 0,\n" +
                "  \"CreatDate\" : \"2017-01-18T15:04:47.55\",\n" +
                "  \"MapName\" : \"p90271701200705.png\",\n" +
                "  \"MaxX\" : 41,\n" +
                "  \"MaxY\" : 19,\n" +
                "  \"Notes\" : \"new\",\n" +
                "  \"SaleSeats\" : 0,\n" +
                "  \"TrsStatus\" : 0,\n" +
                "  \"位號\" : \"000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000028027026025024023000022021020019018017000000000000016015014013012011000010009008007006005004003002001000000000000024023022021020019018017016000000000000015014013000000000000012011010000000000000009008007006005004003002001000000000000000024023022021020019018017016000000000000015014013000000000000012011010000000000000009008007006005004003002001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000028027026025024023022021020000000000000019018017016015014013012011010000000000000009008007006005004003002001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n" +
                "  \"區號\" : \"000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002002002002002002000002002002002002002000000000000002002002002002002000002002002002002002002002002002000000000000002002002002002002002002002000000000000002002002000000000000002002002000000000000002002002002002002002002002000000000000000002002002002002002002002002000000000000002002002000000000000002002002000000000000002002002002002002002002002000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000138138138138138138138138138000000000000138138138138138138138138138138000000000000138138138138138138138138138000000000000000002002002002002002002002002000000000000138138002002002002002002138138000000000000002002002002002002002002002000000000000000002002002002002002002002002000000000000138138002002002002002002138138000000000000002002002002002002002002002000000000000000002002002002002002002002002000000000000002002002002002002002002002002000000000000138138138138138138002002002000000000000000002002002002002002002002002000000000000002002002002002002002002002002000000000000002002002002002002002002002000000000000000002002002002002002002002002000000000000002002002002002002002002002002000000000000002002002002002002002002002000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002002002002002002002002002000000000000002002002002002002002002002002000000000000002002002002002002002002002000000000000000138138138138138138138138138000000000000138138138138138138138138138138000000000000138138138138138138138138138000000000000000138138138138138138138138138000000000000138138138138138138138138138138000000000000138138138138138138138138138000000000000000138138138138138138138138138000000000000138138138138138138138138138138000000000000138138138138138138138138138000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n" +
                "  \"場地代碼\" : 9027,\n" +
                "  \"廳別\" : \"0007\",\n" +
                "  \"排列代碼\" : 1,\n" +
                "  \"排序\" : 0,\n" +
                "  \"排號\" : \"000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000013013013013013013000013013013013013013000000000000013013013013013013000013013013013013013013013013013000000000000012012012012012012012012012000000000000012012012000000000000012012012000000000000012012012012012012012012012000000000000000011011011011011011011011011000000000000011011011000000000000011011011000000000000011011011011011011011011011000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010010010010010010010010010000000000000010010010010010010010010010010000000000000010010010010010010010010010000000000000000009009009009009009009009009000000000000009009009009009009009009009009000000000000009009009009009009009009009000000000000000008008008008008008008008008000000000000008008008008008008008008008008000000000000008008008008008008008008008000000000000000007007007007007007007007007000000000000007007007007007007007007007007000000000000007007007007007007007007007000000000000000006006006006006006006006006000000000000006006006006006006006006006006000000000000006006006006006006006006006000000000000000005005005005005005005005005000000000000005005005005005005005005005005000000000000005005005005005005005005005000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004004004004004004004004004000000000000004004004004004004004004004004000000000000004004004004004004004004004000000000000000003003003003003003003003003000000000000003003003003003003003003003003000000000000003003003003003003003003003000000000000000002002002002002002002002002000000000000002002002002002002002002002002000000000000002002002002002002002002002000000000000000001001001001001001001001001000000000000001001001001001001001001001001000000000000001001001001001001001001001000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000\",\n" +
                "  \"日期\" : \"2017-01-20T00:00:00\",\n" +
                "  \"時間\" : \"1899-12-30T22:30:00\",\n" +
                "  \"更新\" : 0,\n" +
                "  \"更新傳檔\" : 1,\n" +
                "  \"更新時間\" : \"2017-01-19T20:57:17.247\",\n" +
                "  \"狀況1\" : \"+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++000000+000000++++100000+0000000000++++000000000++++010++++000++++000000000+++++000000000++++000++++000++++000000000++++++++++++++++++++++++++++++++++++++++++++++000000000++++0000000000++++000000000+++++000000000++++0000000000++++000000000+++++000000000++++0000000111++++100000000+++++000000000++++0000110000++++000000000+++++000000000++++0000000000++++000000000+++++000000000++++0000000000++++000000000++++++++++++++++++++++++++++++++++++++++++++++000000000++++0000000000++++000000000+++++000000000++++0000000000++++000000000+++++000000000++++0000000000++++000000000+++++000000000++++0000000000++++000000000+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\",\n" +
                "  \"狀況檔名\" : \"            \",\n" +
                "  \"節目代碼\" : 9100007713,\n" +
                "  \"螢幕在上\" : 0\n" +
                "  }\n" +
                "  ]";
    }
}


