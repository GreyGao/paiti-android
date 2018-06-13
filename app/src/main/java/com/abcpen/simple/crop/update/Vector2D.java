package com.abcpen.simple.crop.update;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;

public class Vector2D {
  static final String TAG=Vector2D.class.getName();
	private float x;
	private float y;

	public Vector2D() {
	}

	public Vector2D(Vector2D v) {
		this.x = v.x;
		this.y = v.y;
	}

	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getLength() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public Vector2D set(Vector2D other) {
		x = other.getX();
		y = other.getY();
		return this;
	}

	public Vector2D set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector2D add(Vector2D value) {
		//Log.v(TAG, "delta x,y:"+value.getX()+","+value.getY());
		this.x += value.getX();
		this.y += value.getY();
		return this;
	}

	public static Vector2D subtract(Vector2D lhs, Vector2D rhs) {
		return new Vector2D(lhs.x - rhs.x, lhs.y - rhs.y);
	}

	public static float getDistance(Vector2D lhs, Vector2D rhs) {
		Vector2D delta = Vector2D.subtract(lhs, rhs);
		return delta.getLength();
	}

	public static float getSignedAngleBetween(Vector2D a, Vector2D b) {
		Vector2D na = getNormalized(a);
		Vector2D nb = getNormalized(b);

		return (float)(Math.atan2(nb.y, nb.x) - Math.atan2(na.y, na.x));
	}
	
	public static float calDistancePointToLine(Point p0, Point p1, Point p2)
	{
		float dis=0.0f;
		double p0p1= Math.sqrt(Math.pow(p0.x-p1.x, 2)+ Math.pow(p0.y-p1.y, 2));
		double p0p2= Math.sqrt(Math.pow(p0.x-p2.x, 2)+ Math.pow(p0.y-p2.y, 2));
		double p1p2= Math.sqrt(Math.pow(p1.x-p2.x, 2)+ Math.pow(p1.y-p2.y, 2));
		double p=(p0p1+p0p2+p1p2)/2.0;
		double s= Math.sqrt(p*(p-p0p1)*(p-p0p2)*(p-p1p2));
		dis=(float) (2*s/p1p2);
		return dis;
	}
	
	public static float calMinDistanceToFourSides(Point p0, Point p1, Point p2, Point p3, Point p4)
	{
		float dis=0.0f;
		dis= Math.min(calDistancePointToLine(p0, p1, p2), calDistancePointToLine(p0, p2, p3));
		dis= Math.min(dis, calDistancePointToLine(p0, p3, p4));
		dis= Math.min(dis, calDistancePointToLine(p0, p4, p1));
		return dis;
	}
	
	public static boolean checkPointInRotataedRectangle(Point cropPoint, RectF rotatedRect, float degrees)
	{
		//boolean result=false;
		Matrix matrix=new Matrix();
		float centerX=rotatedRect.centerX();
		float centerY=rotatedRect.centerY();
		matrix.postRotate(-degrees, centerX, centerY);
		RectF rect=new RectF(rotatedRect);
		matrix.mapRect(rect);
		float[] pts={cropPoint.x, cropPoint.y};
		matrix.mapPoints(pts);
		if(pts[0]<rect.right && pts[0]>rect.left &&
		   pts[1]<rect.bottom && pts[1]>rect.top)
		{
			return true;
		}
		return false;
	}

	public static Vector2D getNormalized(Vector2D v) {
		float l = v.getLength();
		if (l == 0)
			return new Vector2D();
		else
			return new Vector2D(v.x / l, v.y / l);

	}

	@Override
	public String toString() {
		return String.format("(%.4f, %.4f)", x, y);
	}
}
