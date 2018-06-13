package com.abcpen.simple.util;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.util.SparseArray;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;

import com.abcpen.simple.jsplugin.Answer;
import com.abcpen.simple.jsplugin.ClassModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Collection of utility functions used in this package.
 */
public class Util {

	private static final String TAG = "db.Util";
	public static final int ORIENTATION_HYSTERESIS = 5;

	private Util() {

	}

	private static long lastClickTime;
	public synchronized static boolean isFastClick() {
		long time = System.currentTimeMillis();
		if ( time - lastClickTime < 500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/*
	 * Compute the sample size as a function of minSideLength and
	 * maxNumOfPixels. minSideLength is used to specify that minimal width or
	 * height of a bitmap. maxNumOfPixels is used to specify the maximal size in
	 * pixels that are tolerable in terms of memory usage.
	 * 
	 * The function returns a sample size based on the constraints. Both size
	 * and minSideLength can be passed in as IImage.UNCONSTRAINED, which
	 * indicates no care of the corresponding constraint. The functions prefers
	 * returning a sample size that generates a smaller bitmap, unless
	 * minSideLength = IImage.UNCONSTRAINED.
	 */

	public static Bitmap transform(Matrix scaler, Bitmap source,
                                   int targetWidth, int targetHeight, boolean scaleUp) {

		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to mFilter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
					source.getHeight(), scaler, true);
			source.recycle();// by zark.s
		} else {
			b1 = source;
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
				targetHeight);

		if (b1 != source) {
			b1.recycle();
		}

		return b2;
	}

	public static void closeSilently(Closeable c) {

		if (c == null)
			return;
		try {
			c.close();
		} catch (Throwable t) {
			// do nothing
		}
	}

	// Returns Options that set the puregeable flag for Bitmap decode.
	public static BitmapFactory.Options createNativeAllocOptions() {

		BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inNativeAlloc = true;
		return options;
	}

	// Thong added for rotate
	public static Bitmap rotateImage(Bitmap src, float degree, ImageView iv) {
		if (src == null) {
			return null;
		}
		float width = src.getWidth();
		float height = src.getHeight();
		double scale = Math.max(width, height) / Math.min(width, height);

		Matrix matrix = new Matrix();
		matrix.setRotate(degree);
		Bitmap lastbmp = null;
		try {
			src = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
					src.getHeight(), matrix, true);
			// 矩形的边长及特定垂直角的余弦
			double oriMinBorder = Math.min(width, height);
			double oriMaxBorder = Math.max(width, height);
			double oriLeanBorder = Math.sqrt(width * width + height * height);
			double cosOriLongBorder = (oriMinBorder * oriMinBorder
					+ oriLeanBorder * oriLeanBorder - oriMaxBorder
					* oriMaxBorder)
					/ (2 * oriMinBorder * oriLeanBorder);

			// 旋转角的正弦、余弦
			double sinRotate;
			double cosRotate = Math.cos(Math.PI * Math.abs(degree) / 180);

			boolean changeAngle = false;
			if (cosRotate < cosOriLongBorder) {
				degree = 90 - Math.abs(degree);
				changeAngle = true;
			}

			// 计算最终的旋转角正弦、余弦
			sinRotate = Math.sin(Math.PI * Math.abs(degree) / 180);
			cosRotate = Math.cos(Math.PI * Math.abs(degree) / 180);

			// 2.
			double sinA = Math.min(width, height) / oriLeanBorder;
			double cosA = Math.sqrt(1 - sinA * sinA);

			double sinC = sinA * cosRotate + sinRotate * cosA;
			double a = sinA * oriLeanBorder / sinC;

			double resultBorderShort = Math.sqrt((a * a) / (1 + scale * scale));
			double resultBorderLong = resultBorderShort * scale;

			double newWidth = width > height ? resultBorderLong
					: resultBorderShort;
			double newHeight = height > width ? resultBorderLong
					: resultBorderShort;

			if (changeAngle) {
				double tmpWidth = newWidth;
				newWidth = newHeight;
				newHeight = tmpWidth;
			}
			// add end
			lastbmp = Bitmap.createBitmap(src,
					(int) (src.getWidth() - newWidth) / 2,
					(int) (src.getHeight() - newHeight) / 2,
					(int) Math.abs(newWidth), (int) Math.abs(newHeight));
		} catch (OutOfMemoryError ex) {
			return src;
		}
		if (lastbmp.equals(src)) {
		} else {
			src.recycle();
		}

		return lastbmp;
	}

	public static Bitmap btnRotateImage(Bitmap src, float degree) {
		if (src == null) {
			return null;
		}
		Matrix matrix = new Matrix();
		matrix.setRotate(degree);
		Bitmap bmp = null;
		try {
			bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(),
					src.getHeight(), matrix, true);
		} catch (OutOfMemoryError ex) {
			return src;
		}
		if (bmp.equals(src)) {
		} else {
			src.recycle();
		}
		return bmp;
	}

	public static void savePreviewBitmap(File file, Bitmap bm) {
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
		try {
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getOrientationInDegree(Activity activity) {

		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;

		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		return degrees;
	}

	public static int roundOrientation(int orientation, int orientationHistory) {
		boolean changeOrientation = false;
		if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
			changeOrientation = true;
		} else {
			int dist = Math.abs(orientation - orientationHistory);
			dist = Math.min(dist, 360 - dist);
			changeOrientation = (dist >= 45 + ORIENTATION_HYSTERESIS);
		}
		if (changeOrientation) {
			return ((orientation + 45) / 90 * 90) % 360;
		}
		return orientationHistory;
	}

	/**
	 * 获取缩略图
	 * @param path
	 * @return
	 */
	public static Bitmap getBitmapThumbnailForPath(Context context, String path){
		if (context==null) return null;
		int imageMaxSize;
		ContentResolver mContentResolver = context.getApplicationContext().getContentResolver();
		Uri uri = Uri.fromFile(new File(path));
		InputStream in = null;
		try {
			in = mContentResolver.openInputStream(uri);
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			BitmapFactory.decodeStream(in, null, o);
			in.close();

			int scale = 1;
			int screenW = 0;
			try {
				WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				screenW = Math.max(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
			} catch (Exception e) {/*NO-OP*/}
			int widthLong = Math.max(o.outWidth, o.outHeight);
			if (screenW > 600) { // 使用屏幕长宽
				if (widthLong <= (screenW * 1.5)) {
					scale = 1;
				} else if (widthLong <= (screenW * 3)) {
					scale = 2;
				} else if (widthLong <= (screenW * 6)){
					scale = 4;
				} else {
					scale = 8;
				}
			} else { // 最保守的长宽
				if (o.outWidth <= (1280*1.5) ) {
					scale = 1;
				} if (o.outWidth <= (1280*3) ) {
					scale = 2;
				} else if (o.outWidth <= (1280*6) ) {
					scale = 4;
				} else {
					scale = 8;
				}
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			o2.inPurgeable = true;
			o2.inInputShareable = true;
			in = mContentResolver.openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(in, null, o2);
			in.close();
			return b;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return null;
	}


	public static int calculateInSampleSize(int outWidth,int outHeight,
											int reqWidth, int reqHeight) {
		final int height = outHeight;
		final int width = outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			} else {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			}

			final float totalPixels = width * height;
			final float totalReqPixelsCap = reqWidth * reqHeight;
			while (totalPixels / inSampleSize > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		return inSampleSize;
	}



	/** 读取原始图片 */
	public static Bitmap getOriginalBitmap(String path) {

			Bitmap myBitmap = null;
			boolean saveOk = false;
			int index = 0;
			while (!saveOk) {
				try {
					BitmapFactory.Options opt = new BitmapFactory.Options();
					opt.inSampleSize = 1 << index;
					index++;
					myBitmap = BitmapFactory.decodeFile(path, opt);
					saveOk = true;
				} catch (OutOfMemoryError error) {
					saveOk = false;
				}
			}
			return myBitmap;
	}

	/**
	 * 图片角度剪切
	 * @param bmp
	 * @param angel
	 * @return
	 */
	public static Bitmap cropAngelImg(Bitmap bmp, float angel) {
		float width = bmp.getWidth();
		float height = bmp.getHeight();
		double scale = Math.max(width, height) / Math.min(width, height);

		float angelInMethod = angel;
		if (Math.abs(angelInMethod) > 1f) {
			if (angelInMethod < 0) {
				angelInMethod = (int) Math.abs(angelInMethod);
			} else {
				angelInMethod = -(int) angelInMethod;
			}
		} else {
			angelInMethod = 0f;
		}

		Matrix matrix = new Matrix();
		matrix.setRotate(angelInMethod);
		bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
				matrix, true);
		// 矩形的边长及特定垂直角的余弦
		double oriMinBorder = Math.min(width, height);
		double oriMaxBorder = Math.max(width, height);
		double oriLeanBorder = Math.sqrt(width * width + height * height);
		double cosOriLongBorder = (oriMinBorder * oriMinBorder + oriLeanBorder
				* oriLeanBorder - oriMaxBorder * oriMaxBorder)
				/ (2 * oriMinBorder * oriLeanBorder);

		// 旋转角的正弦、余弦
		double sinRotate;
		double cosRotate = Math.cos(Math.PI * Math.abs(angelInMethod) / 180);

		boolean changeAngle = false;
		if (cosRotate < cosOriLongBorder) {
			angelInMethod = 90 - Math.abs(angelInMethod);
			changeAngle = true;
		}

		// 计算最终的旋转角正弦、余弦
		sinRotate = Math.sin(Math.PI * Math.abs(angelInMethod) / 180);
		cosRotate = Math.cos(Math.PI * Math.abs(angelInMethod) / 180);

		// 2.
		double sinA = Math.min(width, height) / oriLeanBorder;
		double cosA = Math.sqrt(1 - sinA * sinA);

		double sinC = sinA * cosRotate + sinRotate * cosA;
		double a = sinA * oriLeanBorder / sinC;

		double resultBorderShort = Math.sqrt((a * a) / (1 + scale * scale));
		double resultBorderLong = resultBorderShort * scale;

		double newWidth = width > height ? resultBorderLong : resultBorderShort;
		double newHeight = height > width ? resultBorderLong
				: resultBorderShort;

		if (changeAngle) {
			double tmpWidth = newWidth;
			newWidth = newHeight;
			newHeight = tmpWidth;
		}

		bmp = Bitmap.createBitmap(bmp, (int) (bmp.getWidth() - newWidth) / 2,
				(int) (bmp.getHeight() - newHeight) / 2,
				(int) Math.abs(newWidth), (int) Math.abs(newHeight));

		return bmp;
	}
	// public static boolean isTokenValid(Context ctx) {
	// if (ctx != null) {
	// String cookie = PrefAppStore.getCookie(ctx);
	// long expire_time = PrefAppStore.getCookieExpires(ctx);
	// long now = System.currentTimeMillis();
	// if (TextUtils.isEmpty(cookie) || expire_time < now) {
	// return false;
	// }
	// }
	// return true;
	// }


	public static JSONArray objToJson(final ArrayList<Answer> answers) {
		JSONArray jsonArray = null;
		jsonArray = new JSONArray();
		try {
			for (int i = 0; i < answers.size(); i++) {
				JSONObject jsonObj = new JSONObject();
				Answer answerObj = answers.get(i);
				String body = answerObj.questionHtml;
				String answer = answerObj.questionAnswer;
				String answer_analysis = answerObj.quesitonAnalysis;
				String tags = answerObj.questionTags;
				String questionId = answerObj.questionId;
				String image_id = answerObj.imgUuid;
				String timestamp =
						getFavorDateFormat(answerObj.updateTimestamp);
				int kind = 0;
				ClassModel classModel = getClassModelById(answerObj.subject);
				if (classModel != null) {
					kind = answerObj.subject ;
				}
				jsonObj.put("body", body);
				jsonObj.put("answer", answer);
				jsonObj.put("analysis", answer_analysis);
				jsonObj.put("tags", tags);
				jsonObj.put("timestamp", timestamp);
				jsonObj.put("subject", kind);
				jsonObj.put("questionId", questionId);
				jsonObj.put("image_id", image_id);

				jsonArray.put(jsonObj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonArray;
	}

	public static String getFavorDateFormat(long millis) {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(new Date(millis));
	}

	private static HashMap<String, Integer> classNameToId;
	private static SparseArray<ClassModel> idToClassName;

	private static void initToModel() {
		if (idToClassName == null) {
			idToClassName = new SparseArray<ClassModel>();
			for (int i = 0; i < 9; i++) {
				ClassModel model = new ClassModel();
				model.setClassId(i);
				int drawable = 0;
				String className = "";
				int titleRes = 0;
				switch (i) {
					case 0:
						className = "数学";

						break;
					case 1:
						className = "语文";

						break;
					case 2:
						className = "英语";

						break;
					case 3:
						className = "政治";


						break;
					case 4:
						className = "历史";


						break;
					case 5:
						className = "地理";


						break;
					case 6:
						className = "物理";


						break;
					case 7:
						className = "化学";

						break;
					case 8:
						className = "生物";


						break;
					default:
						break;
				}
				model.setClassName(className);
				model.setClassDrawable(drawable);
				model.setTitleRes(titleRes);
				idToClassName.append(i, model);
			}
		}
	}

	private static void initToId() {
		if (classNameToId == null) {
			classNameToId = new HashMap<String, Integer>();
			classNameToId.put("数学", 0);
			classNameToId.put("语文", 1);
			classNameToId.put("英语", 2);
			classNameToId.put("政治", 3);
			classNameToId.put("历史", 4);
			classNameToId.put("地理", 5);
			classNameToId.put("物理", 6);
			classNameToId.put("化学", 7);
			classNameToId.put("生物", 8);
		}
	}

	public static ClassModel getClassModelById(int id) {
		initToModel();
		return idToClassName.get(id);
	}



}
