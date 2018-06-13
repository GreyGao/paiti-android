/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.abcpen.simple.crop.update;


import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import com.abcpen.simple.R;


// This class is used by CropImage to display a highlighted cropping rectangle
// overlayed with the image. There are two coordinate spaces in use. One is
// image, another is screen. computeLayout() uses mMatrix to map from image
// space to screen space.
public class CropBoxView {

	private static final String TAG = "HighlightView";
	View mContext; // The View displaying the image.

	public static final int GROW_NONE = (1 << 0);
	public static final int GROW_LEFT_EDGE = (1 << 1);
	public static final int GROW_RIGHT_EDGE = (1 << 2);
	public static final int GROW_TOP_EDGE = (1 << 3);
	public static final int GROW_BOTTOM_EDGE = (1 << 4);
	public static final int MOVE = (1 << 5);

	private float minDis = 120f;
	boolean isCanMove = true;

	public CropBoxView(View ctx) {

		mContext = ctx;
	}

	boolean mHidden;


	public void setHidden(boolean hidden) {

		mHidden = hidden;
	}

	protected void draw(Canvas canvas) {

		if (mHidden) {
			return;
		}

		//Path path = new Path();
//		if (!hasFocus()) {
//			mOutlinePaint.setColor(0xFF000000);
//			canvas.drawRect(mDrawRect, mOutlinePaint);
//		} else {
//			Rect viewDrawingRect = new Rect();
//			mContext.getDrawingRect(viewDrawingRect);
//				Rect topRect = new Rect(viewDrawingRect.left,
//						viewDrawingRect.top, viewDrawingRect.right,
//						mDrawRect.top);
//				// if (viewDrawingRect.right - viewDrawingRect.left < 50f
//				// || viewDrawingRect.bottom - viewDrawingRect.top < 50f) {
//				// return;
//				// }
//				if (topRect.width() > 0 && topRect.height() > 0) {
//					canvas.drawRect(topRect, hasFocus() ? mFocusPaint
//							: mNoFocusPaint);
//				}
//				Rect bottomRect = new Rect(viewDrawingRect.left,
//						mDrawRect.bottom, viewDrawingRect.right,
//						viewDrawingRect.bottom);
//				if (bottomRect.width() > 0 && bottomRect.height() > 0) {
//					canvas.drawRect(bottomRect, hasFocus() ? mFocusPaint
//							: mNoFocusPaint);
//				}
//				Rect leftRect = new Rect(viewDrawingRect.left, topRect.bottom,
//						mDrawRect.left, bottomRect.top);
//				if (leftRect.width() > 0 && leftRect.height() > 0) {
//					canvas.drawRect(leftRect, hasFocus() ? mFocusPaint
//							: mNoFocusPaint);
//				}
//				Rect rightRect = new Rect(mDrawRect.right, topRect.bottom,
//						viewDrawingRect.right, bottomRect.top);
//				if (rightRect.width() > 0 && rightRect.height() > 0) {
//					canvas.drawRect(rightRect, hasFocus() ? mFocusPaint
//							: mNoFocusPaint);
//				}

				//path.addRect(new RectF(mDrawRect), Path.Direction.CW);
				
				mOutlinePaint.setColor(0xFFFFFFFF);// 0xFFFF6100
			  // canvas.drawPath(path, mOutlinePaint);
			   canvas.drawRect(mDrawRect, mOutlinePaint);
				final float left = mDrawRect.left;
				final float right = mDrawRect.right;
				final float top = mDrawRect.top;
				final float bottom = mDrawRect.bottom;

				canvas.drawRect(left - 14f, top - 14f, left - 7f, top + 25f,
						mCornorPaint);
				canvas.drawRect(left - 14f, top - 14f, left + 25f, top - 7f,
						mCornorPaint);

				canvas.drawRect(right - 25f, top - 14f, right + 14f, top - 7f,
						mCornorPaint);
				canvas.drawRect(right + 7f, top - 14f, right + 14f, top + 25f,
						mCornorPaint);

				canvas.drawRect(left - 14f, bottom - 25f, left - 7f,
						bottom + 14f, mCornorPaint);
				canvas.drawRect(left - 14f, bottom + 7f, left + 25f,
						bottom + 14f, mCornorPaint);

				canvas.drawRect(right + 7f, bottom - 25f, right + 14f,
						bottom + 14f, mCornorPaint);
				canvas.drawRect(right - 25f, bottom + 7f, right + 14f,
						bottom + 14f, mCornorPaint);
		}
	//}

	public RectF getImageRect() {
		return mImageRect;
	}

	public void setImageRect(RectF mImageRect) {
		this.mImageRect = mImageRect;
	}
	
 
	public void setCropFrame(RectF rect){
		  mCropRect.set(rect);
		  mDrawRect = computeLayout();
		  mContext.invalidate();
	}
	
	public void rotate(int degrees)
	{
		mMatrix.postRotate(degrees, mDrawRect.centerX(), mDrawRect.centerY());
		 mDrawRect = computeLayout();
		 mContext.invalidate();
		 mCropRect.set(mDrawRect);
		 mMatrix.reset();
	}
	
	public Vector2D getVectorRelativeToCenter(){
		float dx=mImageRect.centerX()-mDrawRect.centerX();
		float dy=mImageRect.centerY()-mDrawRect.centerY();
		return new Vector2D(dx, dy);
	}

	public ModifyMode getMode() {

		return mMode;
	}

	public void setMode(ModifyMode mode) {

		if (mode != mMode) {
			mMode = mode;
			mContext.invalidate();
		}
	}
	
	public void setMoveAble(boolean b){
		isCanMove=b;
	}

	// Determines which edges are hit by touching at (x, y).
	public int getHit(float x, float y) {
		Rect r = computeLayout();
		// final float hysteresis =20F;//30F;
		final float hysteresis = mContext.getResources().getDimension(
				R.dimen.crop_image_select_radius);
		int retval = GROW_NONE;

//		if (mCircle) {
//			float distX = x - r.centerX();
//			float distY = y - r.centerY();
//			int distanceFromCenter = (int) Math.sqrt(distX * distX + distY
//					* distY);
//			int radius = mDrawRect.width() / 2;
//			int delta = distanceFromCenter - radius;
//			if (Math.abs(delta) <= hysteresis) {
//				if (Math.abs(distY) > Math.abs(distX)) {
//					if (distY < 0) {
//						retval = GROW_TOP_EDGE;
//					} else {
//						retval = GROW_BOTTOM_EDGE;
//					}
//				} else {
//					if (distX < 0) {
//						retval = GROW_LEFT_EDGE;
//					} else {
//						retval = GROW_RIGHT_EDGE;
//					}
//				}
//			} else if (distanceFromCenter < radius) {
//				retval = MOVE;
//			} else {
//				retval = GROW_NONE;
//			}
//		} else {
			// verticalCheck makes sure the position is between the top and
			// the bottom edge (with some tolerance). Similar for horizCheck.
			boolean verticalCheck = (y >= r.top - hysteresis)
					&& (y < r.bottom + hysteresis);
			boolean horizCheck = (x >= r.left - hysteresis)
					&& (x < r.right + hysteresis);

			// Check whether the position is near some edge(s).
			if ((Math.abs(r.left - x) < hysteresis) && verticalCheck) {
				retval |= GROW_LEFT_EDGE;
			}
			if ((Math.abs(r.right - x) < hysteresis) && verticalCheck) {
				retval |= GROW_RIGHT_EDGE;
			}
			if ((Math.abs(r.top - y) < hysteresis) && horizCheck) {
				retval |= GROW_TOP_EDGE;
			}
			if ((Math.abs(r.bottom - y) < hysteresis) && horizCheck) {
				retval |= GROW_BOTTOM_EDGE;
			}

			// Not near any edge but inside the rectangle: move.
			if (retval == GROW_NONE && (r.contains((int) x, (int) y))) {
				retval = MOVE;
			}
		//}
		return retval;
	}
	
private Rect calTouchHighlightRectMoveEdge(int edge, float d) {
		
		RectF rect = new RectF(this.mCropRect);
		float ratio = mDrawRect.width() / mCropRect.width();

		switch (edge) {
		case GROW_LEFT_EDGE: {
			rect.left = d + rect.left;
			if (minDis / ratio + rect.left > rect.right)
				rect.left = (rect.right - minDis / ratio);
		}
			break;
		case GROW_RIGHT_EDGE: {
			rect.right = d + rect.right;
			if (minDis / ratio + rect.left > rect.right)
				rect.right = minDis / ratio + rect.left;
		}
			break;
		case GROW_TOP_EDGE: {
			rect.top = d + rect.top;
			if (minDis / ratio + rect.top > rect.bottom)
				rect.top = rect.bottom - minDis / ratio;
		}
			break;
		case GROW_BOTTOM_EDGE: {
			rect.bottom = d + rect.bottom;
			if (minDis / ratio + rect.top > rect.bottom)
				rect.bottom = minDis / ratio + rect.top;
		}
			break;
		default:
			break;
		}
		
		if(rect.left<mImageRect.left)
			rect.left=mImageRect.left;
		if(rect.top<mImageRect.top)
			rect.top=mImageRect.top;
		if (this.mImageRect.right < rect.right)
			rect.right = this.mImageRect.right;
		if (this.mImageRect.bottom < rect.bottom)
			rect.bottom = this.mImageRect.bottom;
		Rect desRect = computeLayout(rect);
		return desRect;
	}

	public Rect calTouchHighlightRect(int edge, float dx, float dy)
	{
		
		if (edge == GROW_NONE) {
			return this.mDrawRect;
		} else if (edge == MOVE) {
			Rect r = computeLayout();
			dx=dx * (mCropRect.width() / r.width());
			dy=dy * (mCropRect.height() / r.height());
			
			RectF cropRect = new RectF(mCropRect);

			cropRect.offset(dx, dy);

			// Put the cropping rectangle inside image rectangle.
			cropRect.offset(Math.max(0, mImageRect.left - mCropRect.left),
					Math.max(0, mImageRect.top - mCropRect.top));

			cropRect.offset(Math.min(0, mImageRect.right - mCropRect.right),
					Math.min(0, mImageRect.bottom - mCropRect.bottom));
			//Log.v(TAG,"Image Rect:"+mImageRect.toString()+",CropRect:"+mCropRect.toString());
			return computeLayout(cropRect);
		} else {
			Rect r = computeLayout();
			if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & edge) == 0) {
				dx = 0;
			}

			if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & edge) == 0) {
				dy = 0;
			}

			// Convert to image space before sending to growBy().
			float xDelta = dx * (mCropRect.width() / r.width());
			float yDelta = dy * (mCropRect.height() / r.height());
			// 允许边角拖动
			if ((edge & GROW_LEFT_EDGE) != 0) {
				return calTouchHighlightRectMoveEdge(GROW_LEFT_EDGE, xDelta);
			}
			if ((edge & GROW_RIGHT_EDGE) != 0) {
				return calTouchHighlightRectMoveEdge(GROW_RIGHT_EDGE, xDelta);
			}
			if ((edge & GROW_TOP_EDGE) != 0) {
				return calTouchHighlightRectMoveEdge(GROW_TOP_EDGE, yDelta);
			}
			if ((edge & GROW_BOTTOM_EDGE) != 0) {
				return calTouchHighlightRectMoveEdge(GROW_BOTTOM_EDGE, yDelta);
			}
		}
		
		return this.mDrawRect;
	}

	// Handles motion (dx, dy) in screen space.
	// The "edge" parameter specifies which edges the user is dragging.
	void handleMotion(int edge, float dx, float dy) {

		Rect r = computeLayout();
		if (edge == GROW_NONE) {
			return;
		} else if (edge == MOVE) {
			// Convert to image space before sending to moveBy().
			moveBy(dx * (mCropRect.width() / r.width()),
					dy * (mCropRect.height() / r.height()));
		} else {
			if (((GROW_LEFT_EDGE | GROW_RIGHT_EDGE) & edge) == 0) {
				dx = 0;
			}

			if (((GROW_TOP_EDGE | GROW_BOTTOM_EDGE) & edge) == 0) {
				dy = 0;
			}

			// Convert to image space before sending to growBy().
			float xDelta = dx * (mCropRect.width() / r.width());
			float yDelta = dy * (mCropRect.height() / r.height());
			// 允许边角拖动
			if ((edge & GROW_LEFT_EDGE) != 0) {
				moveEdge(GROW_LEFT_EDGE, xDelta);
			}
			if ((edge & GROW_RIGHT_EDGE) != 0) {
				moveEdge(GROW_RIGHT_EDGE, xDelta);
			}
			if ((edge & GROW_TOP_EDGE) != 0) {
				moveEdge(GROW_TOP_EDGE, yDelta);
			}
			if ((edge & GROW_BOTTOM_EDGE) != 0) {
				moveEdge(GROW_BOTTOM_EDGE, yDelta);
			}
		}
	}

	// Grows the cropping rectange by (dx, dy) in image space.
	void moveBy(float dx, float dy) {

		Rect invalRect = new Rect(mDrawRect);

		mCropRect.offset(dx, dy);

		// Put the cropping rectangle inside image rectangle.
		mCropRect.offset(Math.max(0, mImageRect.left - mCropRect.left),
				Math.max(0, mImageRect.top - mCropRect.top));

		mCropRect.offset(Math.min(0, mImageRect.right - mCropRect.right),
				Math.min(0, mImageRect.bottom - mCropRect.bottom));
		//Log.v(TAG,"Image Rect:"+mImageRect.toString()+",CropRect:"+mCropRect.toString());
		mDrawRect = computeLayout();
		invalRect.union(mDrawRect);
		invalRect.inset(-30, -30);
		mContext.invalidate(invalRect);
	}

	boolean canMove(int edge, float d) {
		RectF rect = new RectF(this.mCropRect);
		float ratio = mDrawRect.width() / mCropRect.width();
		switch (edge) {
		case GROW_LEFT_EDGE: {
			rect.left = d + rect.left;
			if (minDis / ratio + rect.left > rect.right)
				rect.left = (rect.right - minDis / ratio);
		}
			break;
		case GROW_RIGHT_EDGE: {
			rect.right = d + rect.right;
			if (minDis / ratio + rect.left > rect.right)
				rect.right = minDis / ratio + rect.left;
		}
			break;
		case GROW_TOP_EDGE: {
			rect.top = d + rect.top;
			if (minDis / ratio + rect.top > rect.bottom)
				rect.top = rect.bottom - minDis / ratio;
		}
			break;
		case GROW_BOTTOM_EDGE: {
			rect.bottom = d + rect.bottom;
			if (minDis / ratio + rect.top > rect.bottom)
				rect.bottom = minDis / ratio + rect.top;
		}
			break;
		default:
			break;
		}

		if (0.0F > rect.left)
			return false;
		if (0.0F > rect.top)
			return false;
		if (this.mImageRect.right < rect.right)
			return false;
		if (this.mImageRect.bottom < rect.bottom)
			return false;
		return true;
	}

	void moveEdge(int edge, float d) {
		
		RectF rect = new RectF(this.mCropRect);
		float ratio = mDrawRect.width() / mCropRect.width();

		switch (edge) {
		case GROW_LEFT_EDGE: {
			rect.left = d + rect.left;
			if (minDis / ratio + rect.left > rect.right)
				rect.left = (rect.right - minDis / ratio);
		}
			break;
		case GROW_RIGHT_EDGE: {
			rect.right = d + rect.right;
			if (minDis / ratio + rect.left > rect.right)
				rect.right = minDis / ratio + rect.left;
		}
			break;
		case GROW_TOP_EDGE: {
			rect.top = d + rect.top;
			if (minDis / ratio + rect.top > rect.bottom)
				rect.top = rect.bottom - minDis / ratio;
		}
			break;
		case GROW_BOTTOM_EDGE: {
			rect.bottom = d + rect.bottom;
			if (minDis / ratio + rect.top > rect.bottom)
				rect.bottom = minDis / ratio + rect.top;
		}
			break;
		default:
			break;
		}
		
		if(rect.left<mImageRect.left)
			rect.left=mImageRect.left;
		if(rect.top<mImageRect.top)
			rect.top=mImageRect.top;
		if (this.mImageRect.right < rect.right)
			rect.right = this.mImageRect.right;
		if (this.mImageRect.bottom < rect.bottom)
			rect.bottom = this.mImageRect.bottom;
		this.mCropRect.set(rect);
		this.mDrawRect = computeLayout();
		//Log.v(TAG, "mCropRect:"+mCropRect.toShortString()+", mDrawRect:"+mDrawRect.toShortString());
		this.mContext.invalidate();
	}

	public Rect getDrawRect()
	{
		return this.mDrawRect;
	}
	// Returns the cropping rectangle in image space.
	public Rect getCropRect() {

		return new Rect((int) mCropRect.left, (int) mCropRect.top,
				(int) mCropRect.right, (int) mCropRect.bottom);
	}

	public Rect getCropRectInScreen() {
		return new Rect(this.mDrawRect.left, this.mDrawRect.top,
				this.mDrawRect.right, this.mDrawRect.bottom);
	}

	// Maps the cropping rectangle from image space to screen space.
	private Rect computeLayout() {

		RectF r = new RectF(mCropRect.left, mCropRect.top, mCropRect.right,
				mCropRect.bottom);
		mMatrix.mapRect(r);
		return new Rect(Math.round(r.left), Math.round(r.top),
				Math.round(r.right), Math.round(r.bottom));
	}

	private Rect computeLayout(RectF srcRect) {

		RectF r = new RectF(srcRect.left, srcRect.top, srcRect.right,
				srcRect.bottom);
		mMatrix.mapRect(r);
		return new Rect(Math.round(r.left), Math.round(r.top),
				Math.round(r.right), Math.round(r.bottom));
	}
	
	public void invalidate() {

		mDrawRect = computeLayout();
	}

	public void setup(Matrix m, Rect imageRect, RectF cropRect, boolean maintainAspectRatio) {
		mMatrix = new Matrix(m);

		mCropRect = cropRect;
		mImageRect = new RectF(imageRect);
		mMaintainAspectRatio = maintainAspectRatio;
		mInitialAspectRatio = mCropRect.width() / mCropRect.height();
		mDrawRect = computeLayout();
		 //mFocusPaint.setARGB(125, 50, 50, 50);
		mCornorPaint.setARGB(255, 255, 97, 0);
		mCornorPaint.setColor(0xFFFFFFFF);
		mOutlinePaint.setStrokeWidth(3F);
		mOutlinePaint.setStyle(Paint.Style.STROKE);
		mOutlinePaint.setAntiAlias(true);

		mMode = ModifyMode.None;
	}

	enum ModifyMode {
		None, Move, Grow
	}

	private ModifyMode mMode = ModifyMode.None;

	Rect mDrawRect; // in screen space
	private RectF mImageRect; // in image space
	RectF mCropRect; // in image space
	Matrix mMatrix;

	private boolean mMaintainAspectRatio = false;
	private float mInitialAspectRatio;

	private final Paint mCornorPaint = new Paint();
	//private final Paint mNoFocusPaint = new Paint();
	private final Paint mOutlinePaint = new Paint();
}
