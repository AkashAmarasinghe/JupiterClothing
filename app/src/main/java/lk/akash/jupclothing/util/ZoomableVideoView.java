package lk.akash.jupclothing.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class ZoomableVideoView extends VideoView {

    public ZoomableVideoView(Context context) {
        super(context);
    }

    public ZoomableVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomableVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}