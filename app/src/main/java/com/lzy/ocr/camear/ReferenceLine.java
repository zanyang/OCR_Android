package com.lzy.ocr.camear;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**
 * 网格线
 *
 * @author lzy
 * @time 18-11-28 下午5:18
 */
public class ReferenceLine extends View {

    /**
     * 画笔
     */
    private Paint mLinePaint;

    /**
     * 测量后的高
     */
    private int mMeasureHeight;

    /**
     * 测量后的宽
     */
    private int mMeasureWidth;

    public ReferenceLine(Context context) {
        super(context);
        init();
    }

    public ReferenceLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReferenceLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.parseColor("#ffffffff"));
        mLinePaint.setStrokeWidth(1);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMeasureHeight = getMeasuredHeight();
        mMeasureWidth = getMeasuredWidth();

    }

    @Override
    protected void onDraw(Canvas canvas) {

        int width = mMeasureWidth / 3;
        int height = mMeasureHeight / 3;

        for (int i = width, j = 0; i < mMeasureWidth && j < 2; i += width, j++) {
            canvas.drawLine(i, 0, i, mMeasureHeight, mLinePaint);
        }
        for (int j = height, i = 0; j < mMeasureHeight && i < 2; j += height, i++) {
            canvas.drawLine(0, j, mMeasureWidth, j, mLinePaint);
        }
    }
}
