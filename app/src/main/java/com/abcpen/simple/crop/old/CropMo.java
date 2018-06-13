package com.abcpen.simple.crop.old;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: zhaocheng
 * Date: 2015-08-28
 * Time: 15:52
 * Name:CropMo
 * Introduction:
 */
public class CropMo implements Parcelable {

    public int progress = 0;
    public boolean circleCrop = false;
    public int rotationDegree = 0;
    public int orientation = 0;
    public String imagePath;
    public String originImagePath;
    public boolean saving;
    public float angel = 0f;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.progress);
        dest.writeByte(circleCrop ? (byte) 1 : (byte) 0);
        dest.writeInt(this.rotationDegree);
        dest.writeInt(this.orientation);
        dest.writeString(this.imagePath);
        dest.writeString(this.originImagePath);
        dest.writeByte(saving ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.angel);
    }

    public CropMo() {
    }

    protected CropMo(Parcel in) {
        this.progress = in.readInt();
        this.circleCrop = in.readByte() != 0;
        this.rotationDegree = in.readInt();
        this.orientation = in.readInt();
        this.imagePath = in.readString();
        this.originImagePath = in.readString();
        this.saving = in.readByte() != 0;
        this.angel = in.readFloat();
    }

    public static final Creator<CropMo> CREATOR = new Creator<CropMo>() {
        public CropMo createFromParcel(Parcel source) {
            return new CropMo(source);
        }

        public CropMo[] newArray(int size) {
            return new CropMo[size];
        }
    };
}
