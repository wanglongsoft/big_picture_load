### BitmapRegionDecoder之图片局部解析
&emsp;&emsp;Android中高清大图展示，这种问题需要考虑无非是两个点

 1.  图片压缩
 2. 防止OOM  
 
&emsp;&emsp; 如果要加上支持手势放大，让放大后的图片还能清晰展示，常用的办法就不行了。比方要加载张世界地图，或者清明上河图，如果按一般流程加载完了，就看不到原图的细节了，这是就需要BitmapRegionDecoder登场了
### 关键代码
&emsp;&emsp;BitmapRegionDecoder初始化，初始化需要一个InputStream
```java
 InputStream stream = null;
 InputStream stream_sub_one = null;
 InputStream stream_sub_two = null;
 try {
     stream = mContext.getAssets().open("shanghe.jpg");//从assets读取图片文件，获取InputStream 
     
     byteArrayOutputStream = new ByteArrayOutputStream();
     //新建一个ByteArrayOutputStream，由于InputStream 只能读取一次，ByteArrayOutputStream用于新建多个InputStream，便于读取

     byte[] buffer = new byte[1024];
     int len;
     while ((len = stream.read(buffer)) > -1) {//将InputStream读入byteArrayOutputStream
         byteArrayOutputStream.write(buffer, 0, len);
     }

     byteArrayOutputStream.flush();

     stream_sub_one = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
     stream_sub_two = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
     //新建两个InputStream

     mDecoder = BitmapRegionDecoder.newInstance(stream_sub_one, false);//初始化BitmapRegionDecoder

     mOptions = new BitmapFactory.Options();
     mOptions.inJustDecodeBounds = true;

     BitmapFactory.decodeStream(stream_sub_two, null, mOptions);//解析图片。获取图片的宽高
```
&emsp;&emsp;确定显示区域，在View的onSizeChanged回调时，获取ImageView的大小，确定最终显示图片大小
```java
 mShowWidth = mOptions.outWidth > mViewWidth ? mViewWidth : mOptions.outWidth;//mOptions.outWidth代表图片的宽度，mViewWidth代表显示图片的View的宽度
 mShowHeight = mOptions.outHeight > mViewHeight ? mViewHeight : mOptions.outHeight;//mOptions.outHeight 代表图片的高度，mViewWidth代表显示图片的View的宽度
```
&emsp;&emsp;初始化Bitmap
```java
  if(null == mRect) {
      mRect = new Rect(mPortX, mPortY, mShowWidth + mPortX, mShowHeight + mPortY);
      //参数分别代表，解析图片的左上右下
  } else {
      mRect.set(mPortX, mPortY, mShowWidth + mPortX, mShowHeight + mPortY);
  }

  mBitmap = mDecoder.decodeRegion(mRect, mOptions);//初始化Bitmap,mOptions会更新
```
&emsp;&emsp;重写onDraw函数
```java
 @Override
 protected void onDraw(Canvas canvas) {
     super.onDraw(canvas);
     Log.d(TAG, "onDraw: ");
     canvas.drawBitmap(mBitmap, 0, 0, mPaint);
 }
```
