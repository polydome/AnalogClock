package com.polydome.analogclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class AnalogClockView extends View {
    private final static double ANGLE_FULL = 2 * Math.PI;
    private final static double CLOCK_PHASE = - Math.PI / 2;

    private final Paint hourHandPaint;
    private final Paint minuteHandPaint;
    private final Path hourHandPath;
    private final Path minuteHandPath;
    private final Point center;

    private final Preferences prefs;
    private final State state;

    public void setTime(int hour, int minute) {
        state.setHour(hour);
        state.setMinute(minute);
        invalidate();
    }

    private Point getCenter() {
        return this.center;
    }

    public AnalogClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        prefs = Preferences.fromAttributes(attrs);
        state = new State(0, 0);

        hourHandPaint = createHandPaint(prefs.hourHand.getColor(), prefs.hourHand.getWidth());
        minuteHandPaint = createHandPaint(prefs.minuteHand.getColor(), prefs.minuteHand.getWidth());
        hourHandPath = new Path();
        minuteHandPath = new Path();
        center = new Point(0, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int size;

        if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) {
            size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        } else {
            size = Math.max(prefs.hourHand.getLength(), prefs.minuteHand.getLength()) * 2;
        }

        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        center.x = getWidth() / 2;
        center.y = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        hourHandPath.reset();
        minuteHandPath.reset();

        plotHourHandPath(hourHandPath, px(prefs.hourHand.getLength()), px(prefs.hourHand.getOffset()), state.getHour(), state.getMinute());
        canvas.drawPath(hourHandPath, hourHandPaint);

        plotMinuteHandPath(minuteHandPath, px(prefs.minuteHand.getLength()), px(prefs.minuteHand.getOffset()), state.getMinute());
        canvas.drawPath(minuteHandPath, minuteHandPaint);
    }

    private Paint createHandPaint(int color, int width) {
        Paint paint = new Paint();

        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        return paint;
    }

    private void lineAtAngle(Path dst, Point from, double angle, int length, int offset) {
        dst.moveTo(
                (float) (from.x + (offset * Math.cos(angle))),
                (float) (from.y + (offset * Math.sin(angle)))
        );

        dst.lineTo(
                (float) (from.x + (length * Math.cos(angle))),
                (float) (from.y + (length * Math.sin(angle)))
        );
    }

    private void plotHourHandPath(Path dst, int length, int offset, int hours, int minutes) {
        double handAngle =
                (hours % 12) * ANGLE_FULL / 12
                + minutes / 60F * ANGLE_FULL / 12
                + CLOCK_PHASE;

        lineAtAngle(dst, getCenter(), handAngle, length, offset);
        dst.close();
    }

    private void plotMinuteHandPath(Path dst, int length, int offset, int minutes) {
        double handAngle = minutes * ANGLE_FULL / 60 + CLOCK_PHASE;

        lineAtAngle(dst, getCenter(), handAngle, length, offset);
        dst.close();
    }

    private int px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
