package soft.znmd.control;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CC_ImageView extends AppCompatImageView {

    private final static String TAG = "CC_ImageView";

    private ImageView imageView = null;
    private Bitmap mBitmap = null;
    private BitmapRegionDecoder mDecoder = null;
    private ByteArrayOutputStream byteArrayOutputStream = null;
    private BitmapFactory.Options mOptions = null;
    private int mShowWidth = 0;
    private int mShowHeight = 0;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int SkipX = 0;
    private int SkipY = 0;
    private int MaxPortX = 0;
    private int MaxPortY = 0;
    private int mPortX = 0;
    private int mPortY = 0;
    private Rect mRect = null;
    private Context mContext = null;
    private Paint mPaint = null;
    private int lastX = 0;
    private int lastY = 0;
    private int currentX = 0;
    private int currentY = 0;

    public CC_ImageView(Context context) {
        super(context);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    public CC_ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        readImageFile();
    }

    public CC_ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        readImageFile();
    }

    private void readImageFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream stream = null;
                InputStream stream_sub_one = null;
                InputStream stream_sub_two = null;
                try {
                    stream = mContext.getAssets().open("large.jpg");

                    byteArrayOutputStream = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = stream.read(buffer)) > -1) {
                        byteArrayOutputStream.write(buffer, 0, len);
                    }

                    byteArrayOutputStream.flush();

                    stream_sub_one = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                    stream_sub_two = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

                    mDecoder = BitmapRegionDecoder.newInstance(stream_sub_one, false);

                    mOptions = new BitmapFactory.Options();
                    mOptions.inJustDecodeBounds = true;

                    BitmapFactory.decodeStream(stream_sub_two, null, mOptions);
                } catch (Exception e) {
                    Log.d(TAG, "Unable to decode stream: " + e);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            // do nothing here
                        }
                    }
                    if (stream_sub_one != null) {
                        try {
                            stream_sub_one.close();
                        } catch (IOException e) {
                            // do nothing here
                        }
                    }
                    if (stream_sub_two != null) {
                        try {
                            stream_sub_two.close();
                        } catch (IOException e) {
                            // do nothing here
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged w : " + w + " h : " + h);
        mViewWidth = w;
        mViewHeight = h;
        mShowWidth = mOptions.outWidth > mViewWidth ? mViewWidth : mOptions.outWidth;
        mShowHeight = mOptions.outHeight > mViewHeight ? mViewHeight : mOptions.outHeight;
        MaxPortX = mOptions.outWidth - mShowWidth;
        MaxPortY = mOptions.outHeight - mShowHeight;
        SkipX = MaxPortX / 40;
        SkipY = MaxPortY / 40;
        Log.d(TAG, "onSizeChanged MaxPortX : " + MaxPortX + " MaxPortY : " + MaxPortY);
        loadPartial();
    }

    private void loadPartial() {
        Log.d(TAG, "loadPartial mPortX : " + mPortX + " mPortY : " + mPortY);
        if(null == mRect) {
            mRect = new Rect(mPortX, mPortY, mShowWidth + mPortX, mShowHeight + mPortY);
        } else {
            mRect.set(mPortX, mPortY, mShowWidth + mPortX, mShowHeight + mPortY);
        }

        mBitmap = mDecoder.decodeRegion(mRect, mOptions);//mOptions会更新
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: ");
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        currentX = (int) event.getX();
        currentY = (int) event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if(currentX >= lastX) {
                    mPortX -= SkipX;
                    if(mPortX < 0) {
                        mPortX = 0;
                    }
                } else {
                    mPortX += SkipX;
                    if(mPortX > MaxPortX) {
                        mPortX = MaxPortX;
                    }
                }

                if(currentY >= lastY) {
                    mPortY -= SkipY;
                    if(mPortY < 0) {
                        mPortY = 0;
                    }
                } else {
                    mPortY += SkipY;
                    if(mPortY > MaxPortY) {
                        mPortY = MaxPortY;
                    }
                }

                loadPartial();
                invalidate();

                break;
             default:
                break;

        }

        lastX = currentX;
        lastY = currentY;
        return true;
    }
}
