package com.dingok.movie.seatmap.seatMapView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.v4.util.LruCache;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yanzewei on 2017/2/15.
 */

public class SeatMapView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener, GestureDetector.OnGestureListener {

    Context cont;
    SeatSetting seatSetting;
    LruCache<Integer, Bitmap> bmpCache;
    SparseArray<SeatView> data;

    Paint paint = new Paint();
    int width, height, centerX, centerY, maxBuyCount, minBuyCount;
    int seatXMin, seatXMax, seatYMin, seatYMax, bmpWidth, bmpHeight;
    float seatSizeScale = 1, screenSplitY = 0.6f, zoomMin = 0.5f, zoomMax = 3, zoom = 2f, scale, stepX, stepY;
    RectF lowerZone, upperZone, seatAreaOrginalSize, seatAreaScaledSize, cube = new RectF(), pos = new RectF(), needShow = new RectF();
    PointF lastPoint, firstPoint = new PointF(), lastCube = new PointF(), cubeTarget = new PointF();
    boolean dragLowerZone = false, dragUpperZone = false, isScreenOrientationUp = true, isProhibitSpaceSeatRule = false;//, fling = false

    SparseArray<SeatView> selectedSeatData;
    SparseArray<SeatView> cryingSeat = new SparseArray<SeatView>();

    HashMap<String, Float> rulerValueX = new HashMap<String, Float>(), rulerValueY = new HashMap<String, Float>();
    Map<String, String> rulerTextX = new HashMap<String, String>(), rulerTextY = new HashMap<String, String>();

    TextPaint textPaint = new TextPaint();


    GestureDetector mGestureDetector;
//    ScaleGestureDetector mScaleGestureDetector;

    public SeatMapView(Context context, SeatSetting seatSetting) {
        super(context);

        cont = context;
        this.seatSetting = seatSetting;

        data = seatSetting.getData();
        isProhibitSpaceSeatRule = seatSetting.isProhibitSpaceSeatRule();
        isScreenOrientationUp = seatSetting.isScreenOrientationUp();
        seatSizeScale = seatSetting.getSeatSizeScale();
        screenSplitY = seatSetting.getScreenSplitY();
        zoom = seatSetting.getZoom();
        selectedSeatData = seatSetting.getSelectedSeatData();
        maxBuyCount = seatSetting.getMaxBuyCount();
        minBuyCount = seatSetting.getMinBuyCount();

        //單位是kb，只取總量的1/8來使用
        bmpCache = new LruCache<Integer, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8));

        getHolder().addCallback(this);
        setOnTouchListener(this);

        mGestureDetector = new GestureDetector(cont, this);
//        mScaleGestureDetector = new ScaleGestureDetector(cont, this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        setFocusable(true);
        setWillNotDraw(false);
        width = getWidth();
        height = getHeight();
        centerX = width / 2;
        centerY = height / 2;

        initStage();

    }

    Bitmap getBmp(int id) {
        Bitmap bmp = bmpCache.get(id);
        Bitmap scaledBitmap = bmp;
        if (bmp == null) {
            switch (id) {
                case 1:
                    bmp = BitmapFactory.decodeResource(cont.getResources(), seatSetting.getSeatBmpId());// 可選
                    break;
                case 2:
                    bmp = BitmapFactory.decodeResource(cont.getResources(), seatSetting.getSeatSelectedBmpId());// 選中
                    break;
                case 3:
                    bmp = BitmapFactory.decodeResource(cont.getResources(), seatSetting.getSeatSoldBmpId());// 佔用
                    break;
                case 4:
                    bmp = BitmapFactory.decodeResource(cont.getResources(), seatSetting.getSeatHoldBmpId());// 保留
                    break;
                case 5:
//                    bmp = BitmapFactory.decodeResource(cont.getResources(), seatSetting.getSeatSpaceBmpId());// 哭臉
                    bmp = BitmapFactory.decodeResource(cont.getResources(), seatSetting.getSeatBmpId());// 可選
                    break;
                case 6:
                    bmp = BitmapFactory.decodeResource(cont.getResources(), seatSetting.getArrowBmpId());// 觀賞方向
                    break;
            }
            //縮放圖片
            if (bmp != null) {
                Matrix matrix = new Matrix();
                //隨螢幕方向轉向圖片
                if (!isScreenOrientationUp) {
                    matrix.postRotate(180);
                }
                //箭頭固定大小
                if (id != 6) {
                    matrix.postScale(seatSizeScale, seatSizeScale);
                }
                scaledBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            }
            //緩存圖片下來
            if (scaledBitmap != null) {
                bmpCache.put(id, scaledBitmap);
            }
        }
        return scaledBitmap;
    }

    void initStage() {
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            synchronized (getHolder()) {
                if (canvas == null)
                    return;
                upperZone = new RectF(0, 0, width, height * screenSplitY);
                lowerZone = new RectF(0, height * screenSplitY, width, height);
                //抗線的鋸齒, 打开抗锯齿。抗锯齿是依赖于算法的，算法决定抗锯齿的效率
                //在我们绘制棱角分明的图像时，比如一个矩形、一张位图，我们不需要打开抗锯齿。
                paint.setAntiAlias(true);
                //抗圖的鋸齒，如果该项设置为true
                //则图像在动画进行中会滤掉对Bitmap图像的优化操作，加快显示速度
                paint.setFilterBitmap(true);
                //抗抖動(讓兩色之間的漸層色更細膩
                paint.setDither(true);

                // 取行、列總值
                int seatV, seatH;
                seatXMin = Integer.MAX_VALUE;
                seatXMax = Integer.MIN_VALUE;
                seatYMin = Integer.MAX_VALUE;
                seatYMax = Integer.MIN_VALUE;
                for (int a = 0; a < data.size(); a++) {
                    SeatView holder = (SeatView) data.get(a);
                    if (holder.state() == 0 || holder.state() == 200)
                        continue;
                    //取座標最大最小值
                    seatXMin = Math.min(seatXMin, holder.x());
                    seatXMax = Math.max(seatXMax, holder.x());
                    seatYMin = Math.min(seatYMin, holder.y());
                    seatYMax = Math.max(seatYMax, holder.y());
                }
                // max-min+1 是這段距離的總數、另外再+1是左右空白各1.0，總計是+3
                seatV = seatYMax - seatYMin + 3;
                seatH = seatXMax - seatXMin + 3;

                // 計算椅子所佔區塊大小
                bmpWidth = getBmp(1).getWidth();
                bmpHeight = getBmp(1).getHeight();
                float spaceV = bmpHeight * seatSetting.getSeatSpaceY();// 0是每一排上下的間距
                float spaceH = bmpWidth * seatSetting.getSeatSpaceX();// -0.15是每個位子左右的間距
                //-spaceH because add spaceH more one.
                seatAreaOrginalSize = new RectF(0, 0, (float) Math.ceil(((bmpWidth + spaceH) * seatH) - spaceH),
                        (float) Math.ceil(((bmpHeight + spaceV) * seatV) - spaceV));

                // 計算縮小比例
                float smallSeatRatio = lowerZone.width() / lowerZone.height();
                float needSeatRatio = (float) seatAreaOrginalSize.width() / seatAreaOrginalSize.height();
                //scale width or height make small zone matched size.
                if (needSeatRatio < smallSeatRatio) {
                    scale = lowerZone.height() / seatAreaOrginalSize.height();
                } else {
                    scale = lowerZone.width() / seatAreaOrginalSize.width();
                }

//                //計算最小縮放比例
//                float bigSeatRatio=upperZone.width()/upperZone.height();
//                if(needSeatRatio<bigSeatRatio){
//                    zoomMin= Math.max(zoomMin, upperZone.width()/seatAreaOrginalSize.width());
//                }else{
//                    zoomMin= Math.max(zoomMin, upperZone.height()/seatAreaOrginalSize.height());
//                }
//				System.out.println("zoomMin "+zoomMin+" scale "+scale);


                // 繪製椅子的區域
                seatAreaScaledSize = new RectF(0, 0, seatAreaOrginalSize.width() * scale, seatAreaOrginalSize.height() * scale);
                //move to lowerZone leftTop coordinates
                seatAreaScaledSize.offset(lowerZone.left, lowerZone.top);
                //move again to lowerZone center coordinates
                seatAreaScaledSize.offset((lowerZone.width() - seatAreaScaledSize.width()) / 2, (lowerZone.height() - seatAreaScaledSize.height()) / 2);
                //PointF用來存放座標
                lastPoint = new PointF(seatAreaScaledSize.centerX(), seatAreaScaledSize.centerY());
                //cube透明白色小區，設定初始座標
                cubeTarget.set(lastPoint);
                lastCube.set(lastPoint.x, lastPoint.y);

                // 遞增距離(一個座位的距離)
                stepX = (bmpWidth + spaceH);
                stepY = (bmpHeight + spaceV);

                // 畫底色
//				canvas.clipRect(0, 0, width, height, Op.REPLACE);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(seatSetting.getUpperZoneBackgroundColor());
                canvas.drawRect(upperZone, paint);
                paint.setColor(seatSetting.getLowerZoneBackgroundColor());
                canvas.drawRect(lowerZone, paint);

                // 起始座標
                int startX = (int) seatAreaScaledSize.left;
                int startY = (int) seatAreaScaledSize.top;

                // 繪製椅子
                for (int a = 0; a < data.size(); a++) {
                    SeatView holder = (SeatView) data.get(a);
                    if (holder.state() == 0 || holder.state() == 200)
                        continue;
                    // 1.0是保留左邊半個坐位的空間
                    int seatX = (int) (startX + (holder.x() - seatXMin + 1.0) * stepX * scale);
                    int seatY = (int) (startY + (holder.y() - seatYMin + 1.0) * stepY * scale);
                    canvas.save();
                    canvas.scale(scale, scale, seatX, seatY);
                    canvas.drawBitmap(getBmp(holder.state()), seatX, seatY, null);
                    canvas.restore();
                }


                Logger.e("bmpWidth = " + bmpWidth + "\n"
                        + "bmpHeight = " + bmpHeight + "\n"
                        + "spaceV = " + spaceV + "\n"
                        + "spaceH = " + spaceH + "\n"
                        + "lowerZone.width = " + lowerZone.width() + "\n"
                        + "lowerZone.height() = " + lowerZone.height() + "\n"
                        + "seatAreaOrginalSize.width() = " + seatAreaOrginalSize.width() + "\n"
                        + "seatAreaOrginalSize.height() = " + seatAreaOrginalSize.height() + "\n"
                        + "width = " + width + "\n"
                        + "height = " + height + "\n"
                        + "scale = " + scale + "\n");

            }
        } finally {
            if (canvas != null)
                getHolder().unlockCanvasAndPost(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 繪製下方區塊
        canvas.clipRect(lowerZone, Region.Op.REPLACE);
        // Op.DIFFERENCE，实际上就是求得的A和B的差集范围，即A－B，只有在此范围内的绘制内容才会被显示；
        // Op.REVERSE_DIFFERENCE，实际上就是求得的B和A的差集范围，即B－A，只有在此范围内的绘制内容才会被显示；；
        // Op.INTERSECT，即A和B的交集范围，只有在此范围内的绘制内容才会被显示；
        // Op.REPLACE，不论A和B的集合状况，B的范围将全部进行显示，如果和A有交集，则将覆盖A的交集范围；
        // Op.UNION，即A和B的并集范围，即两者所包括的范围的绘制内容都会被显示；
        // Op.XOR，A和B的补集范围，此例中即A除去B以外的范围，只有在此范围内的绘制内容才会被显示；

        // 繪製縮圖中 被選中的椅子
        paint.setAlpha(255);
        if (selectedSeatData.size() > 0) {
            for (int i = 0; i < selectedSeatData.size(); i++) {
                SeatView holder = selectedSeatData.valueAt(i);

                // 1.0是保留左邊半個坐位的空間
                int seatX = (int) (seatAreaScaledSize.left + (holder.x() - seatXMin + 1.0) * stepX * scale);
                int seatY = (int) (seatAreaScaledSize.top + (holder.y() - seatYMin + 1.0) * stepY * scale);
                canvas.save();
                canvas.scale(scale, scale, seatX, seatY);
                canvas.drawBitmap(getBmp(2), seatX, seatY, paint);
                canvas.restore();
            }
        }

        // 繪製縮圖中 哭泣的椅子
        if (cryingSeat.size() > 0 && isProhibitSpaceSeatRule) {
            for (int i = 0; i < cryingSeat.size(); i++) {
                SeatView map = cryingSeat.valueAt(i);

                // 1.0是保留左邊半個坐位的空間
                int seatX = (int) (seatAreaScaledSize.left + (map.x() - seatXMin + 1.0) * stepX * scale);
                int seatY = (int) (seatAreaScaledSize.top + (map.y() - seatYMin + 1.0) * stepY * scale);
                canvas.save();
                canvas.scale(scale, scale, seatX, seatY);
                canvas.drawBitmap(getBmp(5), seatX, seatY, paint);
                canvas.restore();
            }
        }

        // 更新 cube 的位置
        if (dragUpperZone) {
            float dx = lastPoint.x - firstPoint.x;
            float dy = lastPoint.y - firstPoint.y;
            float scaleRatio = scale / zoom;
            cubeTarget.set(lastCube.x - dx * scaleRatio, lastCube.y - dy * scaleRatio);
        } else if (dragLowerZone) {
            cubeTarget.set(lastPoint);
        }

        // 繪製 cube
        float targetW = seatAreaOrginalSize.width() * zoom;
        float targetH = seatAreaOrginalSize.height() * zoom;
        float ratioX = upperZone.width() / targetW;
        float ratioY = upperZone.height() / targetH;
        cube.offsetTo(0, 0);
        cube.right = seatAreaScaledSize.width() * ratioX;
        cube.bottom = seatAreaScaledSize.height() * ratioY;
        float tarX = cubeTarget.x - cube.width() / 2;
        float tarY = cubeTarget.y - cube.height() / 2;
        tarX = Math.max(seatAreaScaledSize.left, Math.min(seatAreaScaledSize.right - cube.width(), tarX));
        tarY = Math.max(seatAreaScaledSize.top, Math.min(seatAreaScaledSize.bottom - cube.height(), tarY));
        cube.offsetTo(tarX, tarY);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(seatSetting.getLowerZoneCubeColor());
        paint.setAlpha(seatSetting.getLowerZoneCubeAlpha());
        canvas.drawRect(cube, paint);

        // 繪製上方區塊
        // 先計算出 cube 位於整個縮圖中的左上、右下座標，以0~1的值表示
        pos.left = (cube.left - seatAreaScaledSize.left) / seatAreaScaledSize.width();
        pos.top = (cube.top - seatAreaScaledSize.top) / seatAreaScaledSize.height();
        pos.right = (cube.right - seatAreaScaledSize.left) / seatAreaScaledSize.width();
        pos.bottom = (cube.bottom - seatAreaScaledSize.top) / seatAreaScaledSize.height();

        // 將上方 0~1 的值乘上原尺寸、得到需要繪製的區域
        needShow.left = seatAreaOrginalSize.width() * pos.left;
        needShow.top = seatAreaOrginalSize.height() * pos.top;
        needShow.right = seatAreaOrginalSize.width() * pos.right;
        needShow.bottom = seatAreaOrginalSize.height() * pos.bottom;

        // 用來繪製尺標的資訊
        rulerValueX.clear();
        rulerTextX.clear();
        rulerValueY.clear();
        rulerTextY.clear();

        // 繪製上方大椅子
        canvas.clipRect(upperZone, Region.Op.REPLACE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(255);
        // 輪詢每一個坐位、算出坐位的左上座標
        for (int i = 0; i < data.size(); i++) {
            SeatView holder = (SeatView) data.get(i);
            if (holder.state() == 0 || holder.state() == 200)
                continue;
            // 1.0是保留左邊半個坐位的空間
            int seatX = (int) ((holder.x() - seatXMin + 1.0) * stepX);
            int seatY = (int) ((holder.y() - seatYMin + 1.0) * stepY);
            // 判斷座標是否在需要被繪製的區域中
            if (needShow.intersects(seatX, seatY, seatX + bmpWidth, seatY + bmpHeight)) {
                boolean isSelected = selectedSeatData.get(holder.index()) != null;
                boolean isCrying = cryingSeat.get(holder.index()) != null;
                float newX = (seatX - needShow.left) * zoom;
                float newY = (seatY - needShow.top) * zoom;
                int bmpId = holder.state();
                if (isSelected) {
                    bmpId = 2;
                } else if (isCrying && isProhibitSpaceSeatRule) {
                    bmpId = 5;
                }
                canvas.save();
                canvas.scale(zoom, zoom, newX, newY);
                canvas.drawBitmap(getBmp(bmpId), newX, newY, paint);

                //如果是空位才畫座位號碼標誌
                if (bmpId == 1 || bmpId == 5) {

                    int smallTextSizeDp = (int) (10 * seatSizeScale);
                    int smallTextAreaWidth = (int) convertDpToPixel(smallTextSizeDp, cont);
                    paint.setColor(seatSetting.getNotificationTextColor());
                    paint.setTextAlign(Paint.Align.CENTER);
                    paint.setTextSize(smallTextAreaWidth);

                    //因圖片為長方形，所以高的置中位子不同
                    if (isScreenOrientationUp) {
                        canvas.drawText(String.valueOf(String.valueOf(Integer.valueOf(holder.row()) + "-" + Integer.valueOf(holder.col())))
                                , newX + bmpWidth / 2
                                , (int)(newY + bmpHeight / 1.6 )
                                , paint);
                    } else {
                        canvas.drawText(String.valueOf(String.valueOf(Integer.valueOf(holder.row()) + "-" + Integer.valueOf(holder.col())))
                                , newX + bmpWidth / 2
                                , newY + bmpHeight / 2
                                , paint);
                    }
                }
                canvas.restore();
//                if (!rulerTextX.containsKey(holder.col())) {
//                    rulerTextX.put(holder.col(), String.valueOf(holder.col()));
//                    rulerValueX.put(holder.col(), newX + bmpWidth / 2 * zoom);
//                }
//                if (!rulerTextY.containsKey(holder.row())) {
//                    rulerTextY.put(holder.row(), String.valueOf(holder.row()));
//                    rulerValueY.put(holder.row(), newY + bmpHeight / 2 * zoom);
//                }
            }
        }

        // 繪製觀賞方向箭頭
        canvas.drawBitmap(getBmp(6), upperZone.right - getBmp(6).getWidth(), upperZone.bottom - getBmp(6).getHeight(), paint);

        // 繪製文字區域背景
        int textSizeDp = seatSetting.getNotificationTextSizeDp();
        int textAreaWidth = (int) convertDpToPixel(textSizeDp, cont);
//        canvas.clipRect(upperZone.left, upperZone.top, upperZone.right, upperZone.top + textAreaWidth, Region.Op.REPLACE);
//        canvas.drawColor(0x99000000);
//        canvas.clipRect(upperZone.left, upperZone.top + textAreaWidth, upperZone.left + textAreaWidth, upperZone.bottom, Region.Op.REPLACE);
//        canvas.drawColor(0x99000000);

//        // 繪製文字 邊邊小文字
//        int smallTextSizeDp = 10;
//        int smallTextAreaWidth = (int) convertDpToPixel(smallTextSizeDp, cont);
//        canvas.clipRect(upperZone, Region.Op.REPLACE);
//        paint.setColor(0x99ffffff);
//        paint.setTextAlign(Paint.Align.CENTER);
//        paint.setTextSize(smallTextAreaWidth);
//
//        // 這是讓文字垂直置中的算法
//        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
//        float offY = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
//
//        // 繪製水平文字
//        canvas.clipRect(upperZone.left + textAreaWidth, upperZone.top, upperZone.right, upperZone.top + textAreaWidth, Region.Op.REPLACE);
//        for (String key : rulerTextX.keySet()) {
//            float txtX = rulerValueX.get(key);
//            float txtY = upperZone.top + textAreaWidth / 2 + offY;
//            canvas.drawText(rulerTextX.get(key), txtX, txtY, paint);
//        }
//        // 繪製垂直文字
//        canvas.clipRect(upperZone.left, upperZone.top + textAreaWidth, upperZone.left + textAreaWidth, upperZone.bottom, Region.Op.REPLACE);
//        for (String key : rulerTextY.keySet()) {
//            float txtX = upperZone.left + textAreaWidth / 2;
//            float txtY = rulerValueY.get(key) + offY;
//            canvas.drawText(rulerTextY.get(key), txtX, txtY, paint);
//        }

        //繪製已選數量文字
        canvas.clipRect(upperZone, Region.Op.REPLACE);
        String prompt;
        if (selectedSeatData.size() == 0) {
            prompt = String.format(Locale.TRADITIONAL_CHINESE, "您可選取 %d 個位子。", maxBuyCount);
        } else if (maxBuyCount > selectedSeatData.size()) {
            prompt = String.format(Locale.TRADITIONAL_CHINESE, "您已選取 %d 個位子，還能再選 %d 個位子。", selectedSeatData.size(), maxBuyCount - selectedSeatData.size());
        } else {
            prompt = String.format(Locale.TRADITIONAL_CHINESE, "%d 個位子選取完成。", maxBuyCount);
        }

        //這裡用的方法支援文字換行
        textPaint.setColor(seatSetting.getNotificationTextColor());
        textPaint.setTextSize(textAreaWidth);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);
//		System.out.println(width+" "+textAreaWidth+" "+getBmp(5).getWidth()+" "+(width-textAreaWidth-getBmp(5).getWidth()));
        /**
         * 需要分行的字符串
         * 画笔对象
         * layout的宽度，字符串超出宽度时自动换行。
         * layout的对其方式，有ALIGN_CENTER， ALIGN_NORMAL， ALIGN_OPPOSITE 三种。
         * 相对行间距，相对字体大小，1.5f表示行间距为1.5倍的字体高度。
         * 在基础行距上添加多少
         * 实际行间距等于这两者的和。
         */
        StaticLayout layout = new StaticLayout(prompt, textPaint, width - textAreaWidth - getBmp(6).getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        canvas.save();
        //StaticLayout是默认画在Canvas的(0,0)点的，如果需要调整位置只能在draw之前用Canvas.translate移動起始坐标
        canvas.translate(upperZone.left + textAreaWidth, upperZone.bottom - layout.getHeight());
        layout.draw(canvas);
        canvas.restore();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    /**
     * View.OnTouchListener implement method
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:// 0=按下
                firstPoint.set(motionEvent.getX(), motionEvent.getY());
                lastPoint.set(firstPoint);
                if (lowerZone.contains(motionEvent.getX(), motionEvent.getY())) {
                    dragLowerZone = true;
                    dragUpperZone = false;
                } else {
                    dragLowerZone = false;
                    dragUpperZone = true;
                }
//                fling = false;
                break;
            case MotionEvent.ACTION_MOVE:// 2=移動
                if (motionEvent.getPointerCount() < 2) {
                    lastPoint.set(motionEvent.getX(), motionEvent.getY());
                }

                break;
            case MotionEvent.ACTION_UP:// 1=放開
                view.performClick();
            case MotionEvent.ACTION_CANCEL:// 3=放棄
                dragLowerZone = false;
                dragUpperZone = false;
                lastCube.set(cube.centerX(), cube.centerY());
                break;
        }

        mGestureDetector.onTouchEvent(motionEvent);
//        mScaleGestureDetector.onTouchEvent(motionEvent);

        //使view無效，如果正在顯示，則called onDraw
        invalidate();
        return true;
    }

    /**
     * GestureDetector.OnGestureListener implement method
     * (onDown, onShowPress, onScroll, onLongPress, onFling ,onSingleTapUp)
     */
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (upperZone.contains(e.getX(), e.getY())) {
            // 計算哪個位子被點到
            // 將點擊座標換算成原始尺寸中的座標
            float tarX = e.getX() / zoom + needShow.left;
            float tarY = e.getY() / zoom + needShow.top;

            // 輪詢每一個坐位、算出坐位的左上座標
            for (int i = 0; i < data.size(); i++) {
                SeatView holder = (SeatView) data.get(i);
                // 坐位若是不可選則略過
                if (holder.state() != 1)
                    continue;
                // 1.0是保留左邊半個坐位的空間
                int seatX = (int) ((holder.x() - seatXMin + 1.0) * stepX);
                int seatY = (int) ((holder.y() - seatYMin + 1.0) * stepY);
                // 比對 rect 與 point 是否重疊
                if (new RectF(seatX, seatY, seatX + stepX, seatY + stepY).contains(tarX, tarY)) {
                    if (selectedSeatData.get(holder.index()) == null && selectedSeatData.size() < maxBuyCount) {
                        selectedSeatData.put(holder.index(), holder);
                    } else {
                        selectedSeatData.remove(holder.index());
                    }
                    if (selectedSeatData.size() > 0 && !checkRule() && isProhibitSpaceSeatRule) {
                        Toast.makeText(cont, "提醒您：\n不可以間隔一個空位。", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        return true;
    }

    /**
     * clear Seat
     */
    public void clearSeat() {
        selectedSeatData.clear();
        cryingSeat.clear();
        invalidate();
    }

//    /**
//     * 如已選過座位，則設定勾選
//     *
//     * @param order 己選訂單
//     */
//    public void setOrder(JSONArray order) {
//        for (int a = 0; a < order.length(); a++) {
//            JSONObject jo = order.optJSONObject(a);
//            if (jo == null) continue;
//            int index = jo.optInt("index", -1);
//            int x = jo.optInt("x", -1);
//            int y = jo.optInt("y", -1);
//            int state = jo.optInt("state", -1);
//            String row = jo.optString("row", null);
//            String col = jo.optString("col", null);
//            String area = jo.optString("area", null);
//            if (index == -1 || x == -1 || y == -1 || state == -1 || row == null || col == null || area == null)
//                continue;
//            if (selectedSeatData.get(index) == null && selectedSeatData.size() < maxBuyCount) {
//                selectedSeatData.put(index, new SeatView(index, x, y, state, row, col, area));
//            }
//        }
//        invalidate();
//    }

    //選位規則，不能跳過一個空位
    boolean checkRule() {
        boolean flag = true;
        //先把已選的多個坐位、整理成陣列
        SparseArray<SparseArray<SeatView>> arr = new SparseArray<SparseArray<SeatView>>();
        for (int a = 0; a < selectedSeatData.size(); a++) {
            SeatView seatView = (SeatView) selectedSeatData.valueAt(a);
            SparseArray<SeatView> list = arr.get(seatView.y());
            if (list == null) {
                list = new SparseArray<SeatView>();
                arr.put(seatView.y(), list);
            }
            list.put(seatView.x(), seatView);
        }
        cryingSeat.clear();
        for (int a = 0; a < arr.size(); a++) {
            //得到一排的位子
            SparseArray<SeatView> list = arr.valueAt(a);
            //如果這一排裡選了兩個以上的位子
            if (list.size() > 1) {
                //由後面往前、兩個兩個做檢查
                for (int b = list.size() - 1; b > 0; b--) {
                    int second = list.keyAt(b);
                    int first = list.keyAt(b - 1);
                    //如果中間剛好間隔1，檢查中間的位子是否可選
                    if (second - first == 2) {
                        SeatView seatView = (SeatView) list.valueAt(b);
                        seatView = (SeatView) data.get(seatView.index() - 1);
                        if (seatView.state() == 1) {
                            cryingSeat.put(seatView.index(), seatView);
                            flag = false;
                        }
                    }
                }
            }
        }
        invalidate();
        return flag;
    }

    public boolean check() {
        boolean flag = false;
        if (selectedSeatData.size() < minBuyCount) {
            Toast.makeText(cont, String.format(Locale.TAIWAN, "您至少要選取 %d 個位子。", (minBuyCount - selectedSeatData.size())), Toast.LENGTH_SHORT).show();
        } else if (selectedSeatData.size() > 1 && !checkRule() && isProhibitSpaceSeatRule) {
            Toast.makeText(cont, "提醒您：\n不可以間隔一個空位。", Toast.LENGTH_SHORT).show();
        } else if (selectedSeatData.size() > 0) {
            flag = true;
        }
        return flag;
    }

    public JSONArray getResultByJSONArray() {
        JSONArray result = new JSONArray();
        for (int a = 0; a < selectedSeatData.size(); a++) {
            SeatView holder = (SeatView) selectedSeatData.valueAt(a);
            try {
                JSONObject seat = new JSONObject();
                seat.put("index", holder.index());
                seat.put("x", holder.x());
                seat.put("y", holder.y());
                seat.put("state", holder.state());
                seat.put("row", holder.row());
                seat.put("col", holder.col());
                seat.put("area", holder.area());
                result.put(seat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().densityDpi / 160f);
    }


}
