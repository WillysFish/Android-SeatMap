package com.dingok.movie.seatmap.seatMapView;

import android.graphics.Color;
import android.util.SparseArray;

import com.dingok.movie.seatmap.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by yanzewei on 2017/2/23.
 */

public class SeatSetting {

    //電影螢幕方向(true在上、false在下)
    private boolean isScreenOrientationUp = true;

    //座位大小倍率、座位上下(Y)間距、座位左右(X)間距、手機螢幕Y軸分裂比、上下座位縮放倍率
    private float seatSizeScale = 1, seatSpaceY = 0, seatSpaceX = -0.15f, screenSplitY = 0.6f, zoom = 2;

    //最大購買張數、最小購買張數
    private int maxBuyCount = 6, minBuyCount = 1;

    //上方區塊底色、下方區塊底色
    private int upperZoneBackgroundColor = 0xff330000;
    private int lowerZoneBackgroundColor = Color.BLACK;

    //下方白色Cube透明度 & 顏色
    private int lowerZoneCubeAlpha = (int) (255 * 0.3);
    private int lowerZoneCubeColor = Color.WHITE;

    //提醒文字大小與顏色 (座位號碼與椅子連動縮放)
    private int notificationTextSizeDp = 15,notificationTextColor = Color.WHITE;

    //是否開啟禁止隔位購買規則
    private boolean isProhibitSpaceSeatRule = false;

    /**
     * 載入圖片
     * 預設圖片電影螢幕方向為上
     * (可選空位、選擇空位、已售出位、保留鎖位、間隔位、觀賞方向箭頭)
     */
    private int seatBmpId = R.drawable.seat;
    private int seatSelectedBmpId = R.drawable.seat_selected;
    private int seatSoldBmpId = R.drawable.seat_sold;
    private int seatHoldBmpId = R.drawable.seat_hold;
    private int seatSpaceBmpId = R.drawable.seat_crying;
    private int arrowBmpId = R.drawable.arrow;


    public SeatSetting() {
    }

//    public SeatSetting(boolean isScreenOrientationUp, int maxBuyCount, int minBuyCount, boolean isProhibitSpaceSeatRule) {
//        this.isScreenOrientationUp = isScreenOrientationUp;
//        this.maxBuyCount = maxBuyCount;
//        this.minBuyCount = minBuyCount;
//        this.isProhibitSpaceSeatRule = isProhibitSpaceSeatRule;
//    }

    //======= Setter method =======

    /**
     * 決定電影螢幕方向，true為上方，false為下方
     * @param screenOrientationUp 預設為true
     */
    public SeatSetting setScreenOrientationUp(boolean screenOrientationUp) {
        this.isScreenOrientationUp = screenOrientationUp;
        return this;
    }

    /**
     * 調整座位為 載入圖片大小 的幾倍
     * @param seatSizeScale 需於0~8 預設為1 Ex: 0.5f,1.5f,2f...
     */
    public SeatSetting setSeatSizeScale(float seatSizeScale) {
        if (seatSizeScale >0 && seatSizeScale<=8)
        this.seatSizeScale = seatSizeScale;
        return this;
    }

    /**
     * 調整座位的高度(Y軸)間距
     * @param seatSpaceY 預設為0 Ex: -0.15f,0.5f,2f...
     */
    public SeatSetting setSeatSpaceY(float seatSpaceY) {
        this.seatSpaceY = seatSpaceY;
        return this;
    }

    /**
     * 調整座位的寬度(X軸)間距
     * @param seatSpaceX 預設為-0.15 Ex: -0.15f,0.5f,2f...
     */
    public SeatSetting setSeatSpaceX(float seatSpaceX) {
        this.seatSpaceX = seatSpaceX;
        return this;
    }

    /**
     * 調整將手機螢幕一分為二的比例
     * @param screenSplitY 預設為0.6
     */
    public SeatSetting setScreenSplitY(float screenSplitY) {
        this.screenSplitY = screenSplitY;
        return this;
    }

    /**
     * 調整上下座位的比例倍率
     * @param zoom 需大於0 預設為2  Ex: 0.5f,1.5f,2f...
     */
    public SeatSetting setZoom(float zoom) {
        if (zoom>0)
        this.zoom = zoom;
        return this;
    }

    /**
     * 限制最多購買張數
     * @param maxBuyCount 預設為6
     */
    public SeatSetting setMaxBuyCount(int maxBuyCount) {
        this.maxBuyCount = maxBuyCount;
        return this;
    }

    /**
     * 限制最少購買張數
     * @param minBuyCount 預設為1
     */
    public SeatSetting setMinBuyCount(int minBuyCount) {
        this.minBuyCount = minBuyCount;
        return this;
    }

    /**
     * 設定上方區塊的顏色
     * @param upperZoneBackgroundColor
     */
    public SeatSetting setUpperZoneBackgroundColor(int upperZoneBackgroundColor) {
        this.upperZoneBackgroundColor = upperZoneBackgroundColor;
        return this;
    }

    /**
     * 設定下方區塊的顏色
     * @param lowerZoneBackgroundColor
     */
    public SeatSetting setLowerZoneBackgroundColor(int lowerZoneBackgroundColor) {
        this.lowerZoneBackgroundColor = lowerZoneBackgroundColor;
        return this;
    }

    /**
     * 設定下方透明小方塊的透明度
     * @param lowerZoneCubeAlpha 需在0~255之間 預設為(int) 255 * 0.3
     */
    public SeatSetting setLowerZoneCubeAlpha(int lowerZoneCubeAlpha) {
        this.lowerZoneCubeAlpha = lowerZoneCubeAlpha;
        return this;
    }

    /**
     * 設定下方透明小方塊的顏色
     * @param lowerZoneCubeColor 預設為白色
     */
    public SeatSetting setLowerZoneCubeColor(int lowerZoneCubeColor) {
        this.lowerZoneCubeColor = lowerZoneCubeColor;
        return this;
    }

    /**
     * 設定提醒文字的大小
     * @param notificationTextSizeDp 預設為15
     */
    public SeatSetting setNotificationTextSizeDp(int notificationTextSizeDp) {
        this.notificationTextSizeDp = notificationTextSizeDp;
        return this;
    }

    /**
     * 設定提醒文字的顏色
     * @param notificationTextColor 預設為白色
     */
    public SeatSetting setNotificationTextColor(int notificationTextColor) {
        this.notificationTextColor = notificationTextColor;
        return this;
    }

    /**
     * 是否禁止隔位購票
     * @param prohibitSpaceSeatRule 預設false不禁止
     */
    public SeatSetting setProhibitSpaceSeatRule(boolean prohibitSpaceSeatRule) {
        isProhibitSpaceSeatRule = prohibitSpaceSeatRule;
        return this;
    }

    /**
     * 設定 可選空位 圖片
     * @param seatBmpId
     */
    public SeatSetting setSeatBmpId(int seatBmpId) {
        this.seatBmpId = seatBmpId;
        return this;
    }

    /**
     * 設定 選擇空位 圖片
     * @param seatSelectedBmpId
     */
    public SeatSetting setSeatSelectedBmpId(int seatSelectedBmpId) {
        this.seatSelectedBmpId = seatSelectedBmpId;
        return this;
    }

    /**
     * 設定 已售出位 圖片
     * @param seatSoldBmpId
     */
    public SeatSetting setSeatSoldBmpId(int seatSoldBmpId) {
        this.seatSoldBmpId = seatSoldBmpId;
        return this;
    }

    /**
     * 設定 保留鎖位 圖片
     * @param seatHoldBmpId
     */
    public SeatSetting setSeatHoldBmpId(int seatHoldBmpId) {
        this.seatHoldBmpId = seatHoldBmpId;
        return this;
    }

    /**
     * 設定 間隔位 圖片
     * @param seatSpaceBmpId
     */
    public SeatSetting setSeatSpaceBmpId(int seatSpaceBmpId) {
        this.seatSpaceBmpId = seatSpaceBmpId;
        return this;
    }

    /**
     * 設定 觀賞方向箭頭 圖片
     * @param arrowBmpId
     */
    public SeatSetting setArrowBmpId(int arrowBmpId) {
        this.arrowBmpId = arrowBmpId;
        return this;
    }

    //======= Getter method =======

    public boolean isScreenOrientationUp() {
        return isScreenOrientationUp;
    }

    public float getSeatSizeScale() {
        return seatSizeScale;
    }

    public float getSeatSpaceY() {
        return seatSpaceY;
    }

    public float getSeatSpaceX() {
        return seatSpaceX;
    }

    public float getScreenSplitY() {
        return screenSplitY;
    }

    public float getZoom() {
        return zoom;
    }

    public int getMaxBuyCount() {
        return maxBuyCount;
    }

    public int getMinBuyCount() {
        return minBuyCount;
    }

    public int getUpperZoneBackgroundColor() {
        return upperZoneBackgroundColor;
    }

    public int getLowerZoneBackgroundColor() {
        return lowerZoneBackgroundColor;
    }

    public int getLowerZoneCubeAlpha() {
        return lowerZoneCubeAlpha;
    }

    public int getLowerZoneCubeColor() {
        return lowerZoneCubeColor;
    }

    public int getNotificationTextSizeDp() {
        return notificationTextSizeDp;
    }

    public int getNotificationTextColor() {
        return notificationTextColor;
    }

    public boolean isProhibitSpaceSeatRule() {
        return isProhibitSpaceSeatRule;
    }

    public int getSeatBmpId() {
        return seatBmpId;
    }

    public int getSeatSelectedBmpId() {
        return seatSelectedBmpId;
    }

    public int getSeatSoldBmpId() {
        return seatSoldBmpId;
    }

    public int getSeatHoldBmpId() {
        return seatHoldBmpId;
    }

    public int getSeatSpaceBmpId() {
        return seatSpaceBmpId;
    }

    public int getArrowBmpId() {
        return arrowBmpId;
    }
}
