package com.abcpen.simple.crop.update;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;


public class CropImgView extends View implements OnTouchListener {

	private final static String TAG = CropImgView.class.getName();

	private int mMaxTextureSize;
	float mLastX, mLastY;
	int mMotionEdge;

	private Bitmap mOriginBitmap;
	private Bitmap bitmap;
	private Bitmap mCropedBitmap;
	private Bitmap mCanvasBitmap;
	private Paint mBitmapPaint = new Paint();
	private int width;
	private int height;
	private Matrix transform = new Matrix();

	private int mCanvasWidth;
	private int mCanvasHeight;
	
	private Vector2D position = new Vector2D();
	private float scale = 1;
	private float angle = 0;
	private float mIntialAngle=0;

	private Vector2D mTouchPosition = new Vector2D();
	private float mTouchScale = 1;
	private float mTouchAngle = 0;

	private Vector2D mLastDeltaVec = new Vector2D();
	private float mLastDeltaScale = 1.0f;
	private float mLastDeltaAngle = 0;

	private TouchManager touchManager = new TouchManager(2);
	private boolean isInitialized = false;

	private float mDLeft;
	private float mDTop;
	private float mDRight;
	private float mDBottom;

	// Debug helpers to draw lines between the two touch points
	private Vector2D vca = null;
	private Vector2D vcb = null;
	private Vector2D vpa = null;
	private Vector2D vpb = null;

	/**
	 * Attempt to use EXIF information on the image to rotate it. Works for
	 * external files only.
	 */
	public static final int ORIENTATION_USE_EXIF = -1;
	/** Display the image file in its native orientation. */
	public static final int ORIENTATION_0 = 0;
	/** Rotate the image 90 degrees clockwise. */
	public static final int ORIENTATION_90 = 90;
	/** Rotate the image 180 degrees. */
	public static final int ORIENTATION_180 = 180;
	/** Rotate the image 270 degrees clockwise. */
	public static final int ORIENTATION_270 = 270;

	private CropBoxView mHighlightView;

	public CropImgView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public CropImgView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CropImgView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CropImgView(Context context, Bitmap bitmap) {
		super(context);

		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();
		int[] maxSizes = new int[1];
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSizes, 0);
		Log.v(TAG, "max texture size:" + maxSizes[0]);
		int maxSize = maxSizes[0];
		float scale = 1.0f;
		if (width <= maxSize && height <= maxSize) {

		} else {

			if (width >= height) {
				scale = maxSize / (float) width;
			} else {
				scale = maxSize / (float) height;
			}
		}
		int sWidth = (int) (width * scale);
		int sHeight = (int) (height * scale);
		Log.v(TAG, "scaled widht x height:" + sWidth + "," + sHeight);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, sWidth,
				sHeight, false);
		this.bitmap = scaledBitmap;
		this.width = scaledBitmap.getWidth();
		this.height = scaledBitmap.getHeight();
		setOnTouchListener(this);
	}

	private static float getDegreesFromRadians(float angle) {
		return (float) (angle * 180.0 / Math.PI);
	}
	
	

	public Bitmap getOriginBitmap() {
		return mOriginBitmap;
	}

	public void setOriginBitmap(Bitmap mOriginBitmap) {
		this.mOriginBitmap = mOriginBitmap;
	}

	public int getMaximumTextureSize() {
		EGL10 egl = (EGL10) EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		// Initialise
		int[] version = new int[2];
		egl.eglInitialize(display, version);

		// Query total number of configurations
		int[] totalConfigurations = new int[1];
		egl.eglGetConfigs(display, null, 0, totalConfigurations);

		// Query actual list configurations
		EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
		egl.eglGetConfigs(display, configurationsList, totalConfigurations[0],
				totalConfigurations);

		int[] textureSize = new int[1];
		int maximumTextureSize = 0;

		// Iterate through all the configurations to located the maximum texture
		// size
		for (int i = 0; i < totalConfigurations[0]; i++) {
			// Only need to check for width since opengl textures are always
			// squared
			egl.eglGetConfigAttrib(display, configurationsList[i],
					EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

			// Keep track of the maximum texture size
			if (maximumTextureSize < textureSize[0]) {
				maximumTextureSize = textureSize[0];
			}

			// Log.i(TAG, Integer.toString(textureSize[0]));
		}

		// Release
		egl.eglTerminate(display);
		Log.i(TAG,
				"Maximum GL texture size: "
						+ Integer.toString(maximumTextureSize));

		return maximumTextureSize;

	}

	public int getScreenOrientation()
	{
	    Display getOrient = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
	    int orientation = Configuration.ORIENTATION_UNDEFINED;
	    if(getOrient.getWidth()==getOrient.getHeight()){
	        orientation = Configuration.ORIENTATION_SQUARE;
	    } else{ 
	        if(getOrient.getWidth() < getOrient.getHeight()){
	            orientation = Configuration.ORIENTATION_PORTRAIT;
	        }else { 
	             orientation = Configuration.ORIENTATION_LANDSCAPE;
	        }
	    }
	    return orientation;
	}
	
	public void setBitmap(Bitmap bitmap) {
		mOriginBitmap = bitmap;
		mMaxTextureSize = getMaximumTextureSize();
		mMaxTextureSize=mMaxTextureSize>2048?2048:mMaxTextureSize;//fix later

//		int ht;
//		int wt;
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		int orientation=getScreenOrientation();
		Log.d(TAG, "orientation:"+orientation);
//		if(orientation==Configuration.ORIENTATION_LANDSCAPE)
//		{
//			ht = displaymetrics.widthPixels;
//			wt =displaymetrics.heightPixels; 
//		}else{
		    mCanvasWidth = (int) (displaymetrics.widthPixels-60*displaymetrics.density);
			mCanvasHeight = displaymetrics.heightPixels;

		//}

		Log.v(TAG, "screen size:" + mCanvasWidth + "," + mCanvasHeight);
		int maxScreenSide = Math.max(mCanvasWidth, mCanvasHeight);
		this.width = bitmap.getWidth();
		this.height = bitmap.getHeight();

		// int maxSize =getMaximumTextureSize();
		int maxSize;
		// if (maxSize <= 0) {
		maxSize = maxScreenSide * 1;
		// return;
		if (mMaxTextureSize <= 0) {
			Log.v(TAG, "max texture size is 0.");
			mMaxTextureSize = maxScreenSide * 2;
		}
		// }
		float scale = 1.0f;
		if (width <= maxSize && height <= maxSize) {

		} else {

			if (width >= height) {
				scale = maxSize / (float) width;
			} else {
				scale = maxSize / (float) height;
			}
		}
		int sWidth = (int) (width * scale);
		int sHeight = (int) (height * scale);
		
		//mTouchPosition.set(wt / 2, ht / 2);
		Log.v(TAG, "scaled widht x height:" + sWidth + "," + sHeight);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, sWidth,
				sHeight, false);
//		float agl=LiveAaNative.getAnglePixels(scaledBitmap);
//		Log.v(TAG, "initial angle:"+agl);
//		mIntialAngle=Math.abs(agl);

		this.bitmap = scaledBitmap;
		this.width = scaledBitmap.getWidth();
		this.height = scaledBitmap.getHeight();
		// setOnTouchListener(this);
		float bitmapAspectRatio = this.height / (float) this.width;
		float cavansAspectRatio = mCanvasHeight / mCanvasWidth;
		float bitmapScale = 1.0F;
		if (cavansAspectRatio > bitmapAspectRatio) {
			float initBitmapWidth = mCanvasWidth;
			// float
			// initBitmapHeight=initBitmapWidth/((float)this.width)*this.height;
			bitmapScale = initBitmapWidth / ((float) this.width);
		} else {
			float initBitmapHeight = mCanvasHeight;
			// float initBitmapWidth=initBitmapHeight/this.height*this.width;
			bitmapScale = initBitmapHeight / ((float) this.height);
		}
		// transform.reset();
		// transform.postScale(bitmapScale, bitmapScale);
		this.scale = bitmapScale;
		mTouchScale=bitmapScale;
		Log.v(TAG, "bitmap scale:" + scale);
		mHighlightView = new CropBoxView(this);
		Matrix matrix=new Matrix();
		matrix.postRotate(mIntialAngle);
		RectF rect=new RectF(400, 300, 150,100);
		//matrix.mapRect(rect);
		mHighlightView.setup(matrix, new Rect(0, 0, mCanvasWidth, mCanvasHeight),rect , false);
		invalidate();
		center(true);
	}

	public Bitmap cropBitmap() {
		if(mCropedBitmap!=null){
			mCropedBitmap.recycle();
			mCropedBitmap=null;
		}
		if (mOriginBitmap == null) {
			Log.w(TAG, "original bitmap is null");
			return null;
		}
		
		if(mCanvasBitmap != null)
		{
			mCanvasBitmap.recycle();
			mCanvasBitmap=null;
		}
		float scaleInverse = (float) (1.0 / scale);

		int originWidth = mOriginBitmap.getWidth();
		int originHeight = mOriginBitmap.getHeight();
		float originCurrentScale = originWidth / (float) this.width;
		scaleInverse *= originCurrentScale;

		int canvasWidth = (int) (this.getWidth() * scaleInverse);
		int canvasHeight = (int) (this.getHeight() * scaleInverse);
		Log.v(TAG, "canvas Width x Height:" + canvasWidth + "," + canvasHeight);
		float fitScale = 1.0F;
		if (canvasWidth > mMaxTextureSize || canvasHeight > mMaxTextureSize) {
			if (canvasWidth > canvasHeight) {
				fitScale = mMaxTextureSize / (float) canvasWidth;
				scaleInverse *= fitScale;
				canvasHeight = (int) (canvasHeight / (float) canvasWidth * mMaxTextureSize);
				canvasWidth = mMaxTextureSize;
			} else {
				fitScale = mMaxTextureSize / (float) canvasHeight;
				scaleInverse *= fitScale;
				canvasWidth = (int) (canvasWidth / (float) canvasHeight * mMaxTextureSize);
				canvasHeight = mMaxTextureSize;
			}
		}

		Matrix matrix = new Matrix();

		matrix.reset();
		matrix.postRotate(mIntialAngle);
		matrix.postTranslate(-originWidth / 2.0f, -originHeight / 2.0f);
		matrix.postRotate(getDegreesFromRadians(angle));
		matrix.postScale(fitScale, fitScale);
		matrix.postTranslate(position.getX() * scaleInverse, position.getY()
				* scaleInverse);

		Log.v(TAG, "scale:" + scale + ", fitScale:" + fitScale);
		// float
		// density=getContext().getResources().getDisplayMetrics().density;
		// Log.v(TAG, "bitmap density:"+density);

		mCanvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight,
				Config.ARGB_8888);

		Canvas canvas = new Canvas(mCanvasBitmap);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Rect croptRect = mHighlightView.getCropRect();
		Rect croptRectInScreen = mHighlightView.getCropRectInScreen();

		croptRect.left *= scaleInverse;
		croptRect.right *= scaleInverse;
		croptRect.top *= scaleInverse;
		croptRect.bottom *= scaleInverse;
		Log.v(TAG, "croptRect:" + croptRect.toString() + ", croptRectInScreen"
				+ croptRectInScreen.toString());
		canvas.drawRect(croptRect, paint);
		// if (crop) {
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		// } else {
		// paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
		// }
		// canvas.drawBitmap(this.bitmap, 0, 0, paint);
		// bitmap.setDensity(160);
		canvas.drawBitmap(mOriginBitmap, matrix, paint);
//		mOriginBitmap.recycle();
//		mOriginBitmap=null;
		mCropedBitmap = Bitmap.createBitmap(mCanvasBitmap, croptRect.left,
				croptRect.top, croptRect.width(), croptRect.height());
		Log.v(TAG, "croped image size:" + mCropedBitmap.getWidth() + ","
				+ mCropedBitmap.getHeight());

	  return mCropedBitmap;
	}

	public void destroy()
	{
		if(mCropedBitmap!=null){
			mCropedBitmap.recycle();
			mCropedBitmap=null;
		}

		if(mCanvasBitmap != null)
		{
			mCanvasBitmap.recycle();
			mCanvasBitmap=null;
		}
//		if(mOriginBitmap!=null)
//		{
//			mOriginBitmap.recycle();
//			mOriginBitmap=null;
//		}
//		if(bitmap!=null)
//		{
//			bitmap.recycle();
//			bitmap=null;
//		}
	}
	public void cropOriginalBitmap() {

		Matrix matrix = new Matrix();

		matrix.postTranslate(-width / 2.0f, -height / 2.0f);
		matrix.postRotate(7);

		matrix.postTranslate((width - 1024) / 2.0f, (height - 768) / 2.0f);
		matrix.postScale(0.5f, 0.5f);
		float density = getContext().getResources().getDisplayMetrics().density;
		Log.v(TAG, "bitmap density:" + density);
		Bitmap cropedBmp = Bitmap.createBitmap(1024, 768, Config.ARGB_8888);

		Canvas canvas = new Canvas(cropedBmp);
		// canvas.save();
		// canvas.rotate(30);
		// canvas.clipRect(100, 100, 300, 200);
		// canvas.setDensity((int) (cropedBmp.getDensity()/density));
		// canvas.restore();
		// Bitmap cropedBmp =this.bitmap;// Bitmap.createBitmap(this.bitmap,100,
		// 100, 300, 200, matrix, false);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		// Path path = new Path();
		canvas.drawRect(0, 0, 1024, 768, paint);
		// canvas.drawPath(path, paint);
		// if (crop) {
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		// } else {
		// paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
		// }
		// canvas.drawBitmap(this.bitmap, 0, 0, paint);
		// bitmap.setDensity(160);
		canvas.drawBitmap(bitmap, matrix, paint);

		Log.v(TAG, "croped image size:" + cropedBmp.getWidth() + ","
				+ cropedBmp.getHeight());
		File outputDir = getContext().getExternalCacheDir();

		try {
			File file = File.createTempFile("xxb", ".jpg", outputDir);
			OutputStream os;
			os = new BufferedOutputStream(new FileOutputStream(file));
			cropedBmp.compress(Bitmap.CompressFormat.JPEG, 100, os);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
	}

	public void center(boolean isPortrait) {
		if (this.bitmap == null) {
			return;
		}

		int bmpWidth = this.bitmap.getWidth();
		int bmpHeight = this.bitmap.getHeight();
		int canvasWidth =mCanvasWidth;// 1280; // this.getWidth();
		int canvasHeight =mCanvasHeight;// 720;// this.getHeight();
		int padx = 40;
		int pady = 40;
		float cropScale = scale;
		Matrix matrix = new Matrix(this.transform);
		matrix.postScale(cropScale, cropScale);
		matrix.postTranslate((canvasWidth - bmpWidth * cropScale) / 2.0f,
				(canvasHeight - bmpHeight * cropScale) / 2.0f);
		RectF rect = new RectF(0, 0, bmpWidth, bmpHeight);
		matrix.mapRect(rect);
		// Log.d(TAG, "bitmap rect1:"+rect);
		matrix.reset();
		matrix.postTranslate(-rect.left, -rect.top);
		float scaleX = (rect.width() - padx * 2) / (float) rect.width();
		float scaleY = (rect.height() - pady * 2) / (float) rect.height();
		matrix.postScale(scaleX, scaleY);
		matrix.postTranslate(rect.left + padx, rect.top + pady);

		matrix.mapRect(rect);
		mHighlightView.setCropFrame(rect);
	}

	public void rotate() {
		angle += Math.PI / 2.0f;
		mHighlightView.rotate(90);
		this.invalidate();
	}

	public void moveBy(float dx, float dy) {
		mTouchPosition.add(new Vector2D(dx, dy));
		position.add(new Vector2D(dx, dy));
		invalidate();
	}

//	private boolean isCropFrameInBitmap() {
//		Rect rect = mHighlightView.getDrawRect();
//		RectF cropRect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
//		RectF bitmapRect = new RectF(0, 0, this.width, this.height);
//		Matrix matrix = new Matrix();
//		matrix.postTranslate(-position.getX(), -position.getY());
//		matrix.postScale(1 / scale, 1 / scale);
//		matrix.postRotate(-getDegreesFromRadians(angle));
//		matrix.postTranslate(width / 2.0f, height / 2.0f);
//		matrix.mapRect(cropRect);
//		return bitmapRect.contains(cropRect);
//	}

	private int getCollisionEdge(Rect highlightRect, boolean touchOnBg) {
		int edge = CropBoxView.MOVE;

		Rect rect = highlightRect;// mHighlightView.getDrawRect();
		RectF cropRect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
		RectF bitmapRect = new RectF(0, 0, this.width, this.height);
		Matrix matrix = new Matrix();
		matrix.postTranslate(-mTouchPosition.getX(), -mTouchPosition.getY());
		matrix.postScale(1 / mTouchScale, 1 / mTouchScale);
		matrix.postRotate(-getDegreesFromRadians(mTouchAngle));
		matrix.postTranslate(width / 2.0f, height / 2.0f);
		matrix.postRotate(-mIntialAngle);
		matrix.mapRect(cropRect);
		
		//Log.d(TAG, "cropRect:"+cropRect+",bitmapRect:"+bitmapRect);
		float dl = cropRect.left - bitmapRect.left;
		float dt = cropRect.top - bitmapRect.top;
		float dr = bitmapRect.right - cropRect.right;
		float db = bitmapRect.bottom - cropRect.bottom;
//		if(!touchOnBg)
//		{
//			float dx=Math.min(dl, 0);
//			float dy=Math.min(dt, 0);
//			bitmapRect.offset(dx, dy);
//			matrix.reset();
//			matrix.postTranslate(-width / 2.0f, -height / 2.0f);
//			matrix.postRotate(getDegreesFromRadians(mTouchAngle));
//			matrix.postScale( mTouchScale,   mTouchScale);
//			matrix.postTranslate(mTouchPosition.getX(), mTouchPosition.getY());
//			matrix.mapRect(bitmapRect);
//		   // bitmapRect.centerX()-;
//		    
//		}
		if (dl < 1) {
			edge = CropBoxView.GROW_LEFT_EDGE;
			//Log.v(TAG, "LEFT edge is collision:");
		}

		if (dt < 1) {
			edge |= CropBoxView.GROW_TOP_EDGE;
			//Log.v(TAG, "TOP edge is collision:");
		}
		if (dr < 1) {
			edge |= CropBoxView.GROW_RIGHT_EDGE;
			//Log.v(TAG, "RIGHT edge is collision:");
		}

		if (db < 1) {
			edge |= CropBoxView.GROW_BOTTOM_EDGE;
			//Log.v(TAG, "BOTTOM edge is collision:");
		}
		// return bitmapRect.contains(cropRect);
		mDLeft = dl;
		mDTop = dt;
		mDRight = dr;
		mDBottom = db;
		return edge;
	}

	private RectF calCropRect() {
		int edge = CropBoxView.MOVE;

		Rect rect = mHighlightView.getDrawRect();
		RectF cropRect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
		RectF bitmapRect = new RectF(0, 0, this.width, this.height);
		Matrix matrix = new Matrix();
		matrix.postTranslate(-position.getX(), -position.getY());
		matrix.postScale(1 / scale, 1 / scale);
		matrix.postRotate(-getDegreesFromRadians(angle));
		matrix.postTranslate(width / 2.0f, height / 2.0f);
		matrix.mapRect(cropRect);
		float dl = cropRect.left - bitmapRect.left;
		float dt = cropRect.top - bitmapRect.top;
		float dr = cropRect.right - bitmapRect.right;
		float db = cropRect.bottom - bitmapRect.bottom;
		RectF resRect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
		// if(dl<0)
		// {
		// edge=HighlightView.GROW_LEFT_EDGE;
		// Log.v(TAG, "LEFT edge is collision:");
		// resRect.left=bitmapRect.left;
		// }
		//
		// if(dt<0)
		// {
		// edge|=HighlightView.GROW_TOP_EDGE;
		// Log.v(TAG, "TOP edge is collision:");
		// resRect.top=bitmapRect.top;
		// }
		// if(dr>0)
		// {
		// edge|=HighlightView.GROW_RIGHT_EDGE;
		// Log.v(TAG, "RIGHT edge is collision:");
		// resRect.right=bitmapRect.right;
		// }
		//
		// if(db>0)
		// {
		// edge|=HighlightView.GROW_BOTTOM_EDGE;
		// Log.v(TAG, "BOTTOM edge is collision:");
		// resRect.bottom=bitmapRect.bottom;
		// }
		// Log.d(TAG, "cal crop rect000:"+resRect.toString());
		matrix.reset();
		// matrix.postTranslate(-width / 2.0f, -height / 2.0f);
		matrix.postRotate(getDegreesFromRadians(mLastDeltaAngle));
		matrix.postScale(mLastDeltaScale, mLastDeltaScale);
		matrix.postTranslate(mLastDeltaVec.getX(), mLastDeltaVec.getY());
		matrix.mapRect(resRect);
		Log.d(TAG, "cal crop rect111:" + resRect.toString());
		return resRect;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		// Log.d(TAG, "on layout:"+left+","+top+","+right+","+bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (this.bitmap == null) {
			return;
		}
		if (!isInitialized) {
			int w = getWidth();
			int h = getHeight();
			mTouchPosition.set(w / 2, h / 2);
			position.set(w / 2, h / 2);
			Log.d(TAG, "init w x h:"+w+","+h);
			isInitialized = true;
		}

		// Paint paint = new Paint();

		transform.reset();
		transform.postRotate(mIntialAngle);
		transform.postTranslate(-width / 2.0f, -height / 2.0f);
		transform.postRotate(getDegreesFromRadians(angle));
		transform.postScale(scale, scale);
		transform.postTranslate(position.getX(), position.getY());
		// Log.d(TAG, "scale:"+scale);
		canvas.drawBitmap(bitmap, transform, mBitmapPaint);
		mHighlightView.draw(canvas);
		try {
			mBitmapPaint.setColor(0xFF007F00);
			canvas.drawCircle(vca.getX(), vca.getY(), 64, mBitmapPaint);
			mBitmapPaint.setColor(0xFF7F0000);
			canvas.drawCircle(vcb.getX(), vcb.getY(), 64, mBitmapPaint);

			mBitmapPaint.setColor(0xFFFF0000);
			canvas.drawLine(vpa.getX(), vpa.getY(), vpb.getX(), vpb.getY(),
					mBitmapPaint);
			mBitmapPaint.setColor(0xFF00FF00);
			canvas.drawLine(vca.getX(), vca.getY(), vcb.getX(), vcb.getY(),
					mBitmapPaint);
		} catch (NullPointerException e) {
			// Just being lazy here...
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		vca = null;
		vcb = null;
		vpa = null;
		vpb = null;

		try {
			touchManager.update(event);

			if (touchManager.getPressCount() == 1) {
				vca = touchManager.getPoint(0);
				vpa = touchManager.getPreviousPoint(0);
				position.add(touchManager.moveDelta(0));

			} else {
				if (touchManager.getPressCount() == 2) {
					vca = touchManager.getPoint(0);
					vpa = touchManager.getPreviousPoint(0);
					vcb = touchManager.getPoint(1);
					vpb = touchManager.getPreviousPoint(1);

					Vector2D current = touchManager.getVector(0, 1);
					Vector2D previous = touchManager.getPreviousVector(0, 1);
					float currentDistance = current.getLength();
					float previousDistance = previous.getLength();

					if (previousDistance != 0) {
						scale *= currentDistance / previousDistance;
					}

					angle -= Vector2D.getSignedAngleBetween(current, previous);
				}
			}

			invalidate();
		} catch (Throwable t) {
			// So lazy...
		}
		return true;
	}

	private void touchOnBitmap(MotionEvent event) {
		vca = null;
		vcb = null;
		vpa = null;
		vpb = null;
		Region regin;
		try {
			touchManager.update(event);

			if (touchManager.getPressCount() == 1) {
				vca = touchManager.getPoint(0);
				vpa = touchManager.getPreviousPoint(0);
				Vector2D deltaVec = touchManager.moveDelta(0);
				mTouchPosition = new Vector2D(position);
				mTouchPosition.add(deltaVec);
				int collisionEdges = getCollisionEdge(mHighlightView
						.getDrawRect(),true);
				if (mDLeft < 1 || mDTop < 1 || mDRight < 1 || mDBottom < 1) {
					//Log.d(TAG, "collision ...");
					// mMotionEdge=HighlightView.MOVE;
				} else {

					position.add(deltaVec);
					mLastDeltaVec = deltaVec;
				}

			} else {
				if (touchManager.getPressCount() == 2) {
					vca = touchManager.getPoint(0);
					vpa = touchManager.getPreviousPoint(0);
					vcb = touchManager.getPoint(1);
					vpb = touchManager.getPreviousPoint(1);

					Vector2D current = touchManager.getVector(0, 1);
					Vector2D previous = touchManager.getPreviousVector(0, 1);
					float currentDistance = current.getLength();
					float previousDistance = previous.getLength();

					if (previousDistance != 0) {
						float deltaScale = currentDistance / previousDistance;
						mTouchScale = scale;
						mTouchScale *= deltaScale;
						int collisionEdges = getCollisionEdge(mHighlightView
								.getDrawRect(),true);
						if (mDLeft < 1 || mDTop < 1 || mDRight < 1
								|| mDBottom < 1) {
							//Log.d(TAG, "collision ...");
							// mMotionEdge=HighlightView.MOVE;
						} else {

							scale *= deltaScale;
							mLastDeltaScale = deltaScale;
						}

					}
					float deltaAngle = Vector2D.getSignedAngleBetween(current,
							previous);
					mTouchAngle = angle;
					mTouchAngle -= deltaAngle;
					int collisionEdges = getCollisionEdge(mHighlightView
							.getDrawRect(),true);
					if (mDLeft < 1 || mDTop < 1 || mDRight < 1 || mDBottom < 1) {
						Log.d(TAG, "collision ...");
						// mMotionEdge=HighlightView.MOVE;
					} else {

						angle -= deltaAngle;
						mLastDeltaAngle = -deltaAngle;
					}

				}
			}

			invalidate();
		} catch (Throwable t) {
			// So lazy...
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getPointerCount() > 1) {
			// HighlightView hv = mHighlightView;
			// RectF rect=this.getVisibleRect();
			// hv.setImageRect(rect);
			touchOnBitmap(event);
			return super.onTouchEvent(event);
		}

		// // if(mMotionEdge==HighlightView.GROW_NONE)
		// // {
		// int x = (int) event.getX();
		// int y = (int) event.getY();
		// if (mHighlightView != null) {
		// HighlightView hv = mHighlightView;
		// Rect frameRect = hv.getCropRect();
		// int edge = hv.getHit(event.getX(), event.getY());
		//
		// if (!frameRect.contains(x, y) && edge == HighlightView.GROW_NONE) {
		// super.onTouchEvent(event);
		// // RectF rect=this.getVisibleRect();
		// // //Log.v(VIEW_LOG_TAG, "VisibleRect:"+rect.toString());
		// // hv.setImageRect(rect);
		// // touchOnBitmap(event);
		// return true;
		// }
		// }
		// // }

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			// for (int i = 0; i < mHighlightViews.size(); i++) {
			CropBoxView hv = mHighlightView;
			int edge = hv.getHit(event.getX(), event.getY());
			mMotionEdge = edge;
			if (edge != CropBoxView.GROW_NONE) {

				// mMotionHighlightView = hv;
				mLastX = event.getX();
				mLastY = event.getY();
				mHighlightView
						.setMode((edge == CropBoxView.MOVE) ? CropBoxView.ModifyMode.Move
								: CropBoxView.ModifyMode.Grow);
				// break;
			} else {
				touchOnBitmap(event);
			}
			// }
			break;
		case MotionEvent.ACTION_UP:
			if (mMotionEdge != CropBoxView.GROW_NONE) {
				if (mHighlightView != null) {
					// centerBasedOnHighlightView(mHighlightView);
					mHighlightView.setMode(CropBoxView.ModifyMode.None);
					Vector2D vec = mHighlightView.getVectorRelativeToCenter();
					mHighlightView.moveBy(vec.getX(), vec.getY());
					this.moveBy(vec.getX(), vec.getY());
				}
				// mHighlightView = null;
			} else {
				touchOnBitmap(event);
			}

			break;
		case MotionEvent.ACTION_MOVE:
			if (mMotionEdge != CropBoxView.GROW_NONE) {
				if (mHighlightView != null) {
					float dx = event.getX() - mLastX;
					float dy = event.getY() - mLastY;
					Rect hightlightRect = mHighlightView.calTouchHighlightRect(
							mMotionEdge, dx, dy);

					int collisionEdges = getCollisionEdge(hightlightRect,false);
					if (mDLeft < 1 || mDTop < 1 || mDRight < 1 || mDBottom < 1) {
						Log.d(TAG, "collision ...");
//						if ((mDLeft < 1 && mDRight < 1)
//								|| (mDTop < 1 && mDBottom < 1)) {
//							return true;
//						}
//						mHighlightView.handleMotion(mMotionEdge, dx, dy);
//						this.moveBy(event.getX() - mLastX, event.getY()
//								- mLastY);
					} else {
						mHighlightView.handleMotion(mMotionEdge, dx, dy);
					}
					// // Log.v(TAG, "edges:"+collisionEdges);
					// if(dy<0 &&(mMotionEdge & HighlightView.GROW_TOP_EDGE)!=0
					// && (collisionEdges & HighlightView.GROW_BOTTOM_EDGE)!=0)
					// {
					// mMotionEdge=HighlightView.MOVE;
					// }
					//
					// mHighlightView.handleMotion(mMotionEdge, dx, dy);
					//
					// boolean isIn = isCropFrameInBitmap();
					// if (!isIn) {
					// this.moveBy(event.getX() - mLastX, event.getY()
					// - mLastY);
					// }

					mLastX = event.getX();
					mLastY = event.getY();

					// if (false) {
					// by zark.s ensureVisbile make edge shake.
					// This section of code is optional. It has some user
					// benefit in that moving the crop rectangle against
					// the edge of the screen causes scrolling but it means
					// that the crop rectangle is no longer fixed under
					// the user's finger.
					// ensureVisible(mMotionHighlightView);
					// }
				}
			} else {
				touchOnBitmap(event);
			}

			break;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			// center(true, true);
			break;
		case MotionEvent.ACTION_MOVE:
			// if we're not zoomed then there's no point in even allowing
			// the user to move the image around. This call to center puts
			// it back to the normalized location (with false meaning don't
			// animate).
			// if (getScale() == 1F) {
			// center(true, true);
			// }
			break;
		}

		return true;
	}
}
