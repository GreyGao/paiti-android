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

package com.abcpen.simple.crop.old;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.abcpen.simple.R;


// This class is used by CropImage to display a highlighted cropping rectangle
// overlayed with the image. There are two coordinate spaces in use. One is
// image, another is screen. computeLayout() uses mMatrix to map from image
// space to screen space.
public class HighlightView {

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
    private int formType = 1;

    public void setFromType(int fromType) {
        this.formType = fromType;
    }

    public HighlightView(View ctx) {

        mContext = ctx;
    }

    private void init() {

        android.content.res.Resources resources = mContext.getResources();
        mResizeDrawableWidth = resources
                .getDrawable(R.drawable.camera_crop_width);
        mResizeDrawableHeight = resources
                .getDrawable(R.drawable.camera_crop_width);
        mResizeDrawableDiagonal = resources
                .getDrawable(R.drawable.camera_crop_width);
    }

    boolean mIsFocused;
    boolean mHidden;

    public boolean hasFocus() {

        return mIsFocused;
    }

    public void setFocus(boolean f) {

        mIsFocused = f;
    }

    public void setHidden(boolean hidden) {

        mHidden = hidden;
    }

    protected void draw(Canvas canvas) {

        if (mHidden) {
            return;
        }

        Path path = new Path();
        if (!hasFocus()) {
            mOutlinePaint.setColor(0xFF000000);
            canvas.drawRect(mDrawRect, mOutlinePaint);
        } else {
            Rect viewDrawingRect = new Rect();
            mContext.getDrawingRect(viewDrawingRect);
            if (mCircle) {

                canvas.save();

                float width = mDrawRect.width();
                float height = mDrawRect.height();
                path.addCircle(mDrawRect.left + (width / 2), mDrawRect.top
                        + (height / 2), width / 2, Path.Direction.CW);
                mOutlinePaint.setColor(0xFFffd944);

                canvas.clipPath(path, Region.Op.DIFFERENCE);
                canvas.drawRect(viewDrawingRect, hasFocus() ? mFocusPaint
                        : mNoFocusPaint);

                canvas.restore();

            } else {

                Rect topRect = new Rect(viewDrawingRect.left,
                        viewDrawingRect.top, viewDrawingRect.right,
                        mDrawRect.top);
                // if (viewDrawingRect.right - viewDrawingRect.left < 50f
                // || viewDrawingRect.bottom - viewDrawingRect.top < 50f) {
                // return;
                // }
                if (topRect.width() > 0 && topRect.height() > 0) {
                    canvas.drawRect(topRect, hasFocus() ? mFocusPaint
                            : mNoFocusPaint);
                }
                Rect bottomRect = new Rect(viewDrawingRect.left,
                        mDrawRect.bottom, viewDrawingRect.right,
                        viewDrawingRect.bottom);
                if (bottomRect.width() > 0 && bottomRect.height() > 0) {
                    canvas.drawRect(bottomRect, hasFocus() ? mFocusPaint
                            : mNoFocusPaint);
                }
                Rect leftRect = new Rect(viewDrawingRect.left, topRect.bottom,
                        mDrawRect.left, bottomRect.top);
                if (leftRect.width() > 0 && leftRect.height() > 0) {
                    canvas.drawRect(leftRect, hasFocus() ? mFocusPaint
                            : mNoFocusPaint);
                }
                Rect rightRect = new Rect(mDrawRect.right, topRect.bottom,
                        viewDrawingRect.right, bottomRect.top);
                if (rightRect.width() > 0 && rightRect.height() > 0) {
                    canvas.drawRect(rightRect, hasFocus() ? mFocusPaint
                            : mNoFocusPaint);
                }

                path.addRect(new RectF(mDrawRect), Path.Direction.CW);

                // mOutlinePaint.setColor(0xFFFF8A00);
                // if (mMode == ModifyMode.Grow) {
                // mOutlinePaint.setColor(0xFF6100);
                // } else {
                mOutlinePaint.setColor(0xFFffd944);// 0xFFFF6100
                // }
            }

            canvas.drawPath(path, mOutlinePaint);

            if (!mCircle) {
                final float left = mDrawRect.left;
                final float right = mDrawRect.right;
                final float top = mDrawRect.top;
                final float bottom = mDrawRect.bottom;
                // canvas.drawCircle(left, top, CORNER_RADIUS, mCornorPaint);
                // canvas.drawCircle(right, top, CORNER_RADIUS, mCornorPaint);
                // canvas.drawCircle(left, bottom, CORNER_RADIUS, mCornorPaint);
                // canvas.drawCircle(right, bottom, CORNER_RADIUS,
                // mCornorPaint);

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

            // if (!mCircle) {
            // int mThreeWidth = ((mDrawRect.right - mDrawRect.left) / 3);
            // int mThreeHeight = ((mDrawRect.bottom - mDrawRect.top) / 3);
            // // canvas.drawRect(left, top, right, bottom, paint);
            // canvas.drawLine(mDrawRect.left + mThreeWidth, mDrawRect.top,
            // mDrawRect.left + mThreeWidth, mDrawRect.bottom,
            // mCornorPaint);
            // canvas.drawLine(mDrawRect.left + mThreeWidth * 2,
            // mDrawRect.top, mDrawRect.left + mThreeWidth * 2,
            // mDrawRect.bottom, mCornorPaint);
            //
            // canvas.drawLine(mDrawRect.left, mDrawRect.top + mThreeHeight,
            // mDrawRect.right, mDrawRect.top + mThreeHeight,
            // mCornorPaint);
            // canvas.drawLine(mDrawRect.left, mDrawRect.top + mThreeHeight
            // * 2, mDrawRect.right, mDrawRect.top + mThreeHeight * 2,
            // mCornorPaint);
            // }
            if (mCircle) {
                int width = mResizeDrawableDiagonal.getIntrinsicWidth();
                int height = mResizeDrawableDiagonal.getIntrinsicHeight();

                int d = (int) Math.round(Math.cos(/* 45deg */Math.PI / 4D)
                        * (mDrawRect.width() / 2D));
                int x = mDrawRect.left + (mDrawRect.width() / 2) + d - width
                        / 2;
                int y = mDrawRect.top + (mDrawRect.height() / 2) - d - height
                        / 2;
                mResizeDrawableDiagonal.setBounds(x, y, x
                        + mResizeDrawableDiagonal.getIntrinsicWidth(), y
                        + mResizeDrawableDiagonal.getIntrinsicHeight());
                // mResizeDrawableDiagonal.draw(canvas);
            } else {
//				int left = mDrawRect.left;
//				int right = mDrawRect.right;
//				int top = mDrawRect.top;
//				int bottom = mDrawRect.bottom;
//
//				int widthWidth = mResizeDrawableWidth.getIntrinsicWidth() / 2;
//				int widthHeight = mResizeDrawableWidth.getIntrinsicHeight() / 2;
//				int heightHeight = mResizeDrawableHeight.getIntrinsicHeight() / 2;
//				int heightWidth = mResizeDrawableHeight.getIntrinsicWidth() / 2;
//
//				int xMiddle = mDrawRect.left
//						+ ((mDrawRect.right - mDrawRect.left) / 2);
//				int yMiddle = mDrawRect.top
//						+ ((mDrawRect.bottom - mDrawRect.top) / 2);
//
//				mResizeDrawableWidth
//						.setBounds(left - widthWidth, yMiddle - widthHeight,
//								left + widthWidth, yMiddle + widthHeight);
//				mResizeDrawableWidth.draw(canvas);
//				// canvas.drawCircle(left - widthWidth, yMiddle - widthHeight,
//				// CORNER_RADIUS, mCornorPaint);
//				// canvas.drawCircle(right, top, CORNER_RADIUS, mCornorPaint);
//				// canvas.drawCircle(left, bottom, CORNER_RADIUS, mCornorPaint);
//				// canvas.drawCircle(right, bottom, CORNER_RADIUS,
//				// mCornorPaint);
//				mResizeDrawableWidth.setBounds(right - widthWidth, yMiddle
//						- widthHeight, right + widthWidth, yMiddle
//						+ widthHeight);
//				mResizeDrawableWidth.draw(canvas);
//
//				mResizeDrawableHeight.setBounds(xMiddle - heightWidth, top
//						- heightHeight, xMiddle + heightWidth, top
//						+ heightHeight);
//				mResizeDrawableHeight.draw(canvas);
//
//				mResizeDrawableHeight.setBounds(xMiddle - heightWidth, bottom
//						- heightHeight, xMiddle + heightWidth, bottom
//						+ heightHeight);
//				mResizeDrawableHeight.draw(canvas);
            }
        }
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

    public void setMoveAble(boolean b) {
        isCanMove = b;
    }

    // Determines which edges are hit by touching at (x, y).
    public int getHit(float x, float y) {
        Rect r = computeLayout();
        // final float hysteresis =20F;//30F;
        final float hysteresis = mContext.getResources().getDimension(
                R.dimen.crop_image_select_radius);
        int retval = GROW_NONE;

        if (mCircle) {
            float distX = x - r.centerX();
            float distY = y - r.centerY();
            int distanceFromCenter = (int) Math.sqrt(distX * distX + distY
                    * distY);
            int radius = mDrawRect.width() / 2;
            int delta = distanceFromCenter - radius;
            if (Math.abs(delta) <= hysteresis) {
                if (Math.abs(distY) > Math.abs(distX)) {
                    if (distY < 0) {
                        retval = GROW_TOP_EDGE;
                    } else {
                        retval = GROW_BOTTOM_EDGE;
                    }
                } else {
                    if (distX < 0) {
                        retval = GROW_LEFT_EDGE;
                    } else {
                        retval = GROW_RIGHT_EDGE;
                    }
                }
            } else if (distanceFromCenter < radius) {
                retval = MOVE;
            } else {
                retval = GROW_NONE;
            }
        } else {
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
        }
        return retval;
    }

    // Handles motion (dx, dy) in screen space.
    // The "edge" parameter specifies which edges the user is dragging.
    void handleMotion(int edge, float dx, float dy, boolean fixWidth) {

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

            // // only one edge can move
            // if ((edge & GROW_LEFT_EDGE) != 0) {
            // moveEdge(GROW_LEFT_EDGE, xDelta, fixWidth);
            // } else if ((edge & GROW_RIGHT_EDGE) != 0) {
            // moveEdge(GROW_RIGHT_EDGE, xDelta, fixWidth);
            // } else if ((edge & GROW_TOP_EDGE) != 0) {
            // moveEdge(GROW_TOP_EDGE, yDelta, fixWidth);
            // } else if ((edge & GROW_BOTTOM_EDGE) != 0) {
            // moveEdge(GROW_BOTTOM_EDGE, yDelta, fixWidth);
            // }

            // 允许边角拖动
            if ((edge & GROW_LEFT_EDGE) != 0) {
                moveEdge(GROW_LEFT_EDGE, xDelta, fixWidth);
            }
            if ((edge & GROW_RIGHT_EDGE) != 0) {
                moveEdge(GROW_RIGHT_EDGE, xDelta, fixWidth);
            }
            if ((edge & GROW_TOP_EDGE) != 0) {
                moveEdge(GROW_TOP_EDGE, yDelta, fixWidth);
            }
            if ((edge & GROW_BOTTOM_EDGE) != 0) {
                moveEdge(GROW_BOTTOM_EDGE, yDelta, fixWidth);
            }

            // growBy((((edge & GROW_LEFT_EDGE) != 0) ? -1 : 1) * xDelta,
            // (((edge & GROW_TOP_EDGE) != 0) ? -1 : 1) * yDelta);
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

        mDrawRect = computeLayout();
        invalRect.union(mDrawRect);
        invalRect.inset(-30, -30);
        mContext.invalidate(invalRect);
    }

    // Grows the cropping rectange by (dx, dy) in image space.
    void growBy(float dx, float dy) {

        if (mMaintainAspectRatio) {
            if (dx != 0) {
                dy = dx / mInitialAspectRatio;
            } else if (dy != 0) {
                dx = dy * mInitialAspectRatio;
            }
        }

        // Don't let the cropping rectangle grow too fast.
        // Grow at most half of the difference between the image rectangle and
        // the cropping rectangle.
        RectF r = new RectF(mCropRect);
        if (dx > 0F && r.width() + 2 * dx > mImageRect.width()) {
            float adjustment = (mImageRect.width() - r.width()) / 2F;
            dx = adjustment;
            if (mMaintainAspectRatio) {
                dy = dx / mInitialAspectRatio;
            }
        }
        if (dy > 0F && r.height() + 2 * dy > mImageRect.height()) {
            float adjustment = (mImageRect.height() - r.height()) / 2F;
            dy = adjustment;
            if (mMaintainAspectRatio) {
                dx = dy * mInitialAspectRatio;
            }
        }
        // switch(getHit(x, y))
        // r.set(r.left, r.top, r.right+dx, r.bottom);
        r.inset(-dx, -dy);

        // Don't let the cropping rectangle shrink too fast.
        final float widthCap = 25F;
        if (r.width() < widthCap) {
            r.inset(-(widthCap - r.width()) / 2F, 0F);
        }
        float heightCap = mMaintainAspectRatio ? (widthCap / mInitialAspectRatio)
                : widthCap;
        if (r.height() < heightCap) {
            r.inset(0F, -(heightCap - r.height()) / 2F);
        }

        // Put the cropping rectangle inside the image rectangle.
        if (r.left < mImageRect.left) {
            r.offset(mImageRect.left - r.left, 0F);
        } else if (r.right > mImageRect.right) {
            r.offset(-(r.right - mImageRect.right), 0);
        }
        if (r.top < mImageRect.top) {
            r.offset(0F, mImageRect.top - r.top);
        } else if (r.bottom > mImageRect.bottom) {
            r.offset(0F, -(r.bottom - mImageRect.bottom));
        }

        mCropRect.set(r);
        mDrawRect = computeLayout();
        mContext.invalidate();
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

    void moveEdge(int edge, float d, boolean fixWidth) {
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
            rect.left = 0.0F;
        if (0.0F > rect.top)
            rect.top = 0.0F;
        if (this.mImageRect.right < rect.right)
            rect.right = this.mImageRect.right;
        if (this.mImageRect.bottom < rect.bottom)
            rect.bottom = this.mImageRect.bottom;
        this.mCropRect.set(rect);
        this.mDrawRect = computeLayout();
        this.mContext.invalidate();
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

    public void invalidate() {

        mDrawRect = computeLayout();
    }

    public void setup(Matrix m, Rect imageRect, RectF cropRect, boolean circle,
                      boolean maintainAspectRatio) {

        if (circle) {
            maintainAspectRatio = true;
        }
        mMatrix = new Matrix(m);

        mCropRect = cropRect;
        mImageRect = new RectF(imageRect);
        mMaintainAspectRatio = maintainAspectRatio;
        mCircle = circle;

        mInitialAspectRatio = mCropRect.width() / mCropRect.height();
        mDrawRect = computeLayout();

        mFocusPaint.setARGB(125, 50, 50, 50);
        // mCornorPaint.setARGB(255, 255, 97, 0);
        mCornorPaint.setColor(0xFFffd944);
        mNoFocusPaint.setARGB(125, 50, 50, 50);
        mOutlinePaint.setStrokeWidth(3F);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setAntiAlias(true);

        mMode = ModifyMode.None;
        init();
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
    private boolean mCircle = false;

    private Drawable mResizeDrawableWidth;
    private Drawable mResizeDrawableHeight;
    private Drawable mResizeDrawableDiagonal;

    private final Paint mFocusPaint = new Paint();
    private final Paint mCornorPaint = new Paint();
    private final Paint mNoFocusPaint = new Paint();
    private final Paint mOutlinePaint = new Paint();
}
