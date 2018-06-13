package com.abcpen.simple.jsplugin;

import android.os.Parcel;
import android.os.Parcelable;


/** 答案表 */
public class Answer implements Parcelable {

	public String questionId;
	public String questionAnswer;
	public String quesitonAnalysis;
	public String questionBody = "";
	public String questionHtml = "";
	public String questionTags;
	public double questionScore;
	public String imgUuid;
	public String imgUrl;
	public int questionIndex = -1;
	public int subject;
	public long updateTimestamp = 0;
	public String audio_id = "";
	public int audio_gold = 0;
	public int audio_duration = 0;
	public String audio_url = "";
	public int audio_status = 0;
	public int audio_has_comment = 0;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(questionAnswer);
		dest.writeString(quesitonAnalysis);
		dest.writeString(questionBody);
		dest.writeString(questionHtml);
		dest.writeString(questionTags);
		dest.writeString(imgUuid);
		dest.writeString(imgUrl);
		dest.writeString(questionId);
		dest.writeInt(questionIndex);
		dest.writeInt(subject);
		dest.writeDouble(questionScore);
		dest.writeLong(updateTimestamp);
		dest.writeString(audio_id);
		dest.writeInt(audio_gold);
		dest.writeInt(audio_duration);
		dest.writeString(audio_url);
		dest.writeInt(audio_status);
		dest.writeInt(audio_has_comment);
	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		@Override
		public Answer createFromParcel(Parcel in) {
			Answer obj = new Answer();
			obj.questionAnswer = in.readString();
			obj.quesitonAnalysis = in.readString();
			obj.questionBody = in.readString();
			obj.questionHtml = in.readString();
			obj.questionTags = in.readString();
			obj.imgUuid = in.readString();
			obj.imgUrl = in.readString();
			obj.questionId = in.readString();
			obj.questionIndex = in.readInt();
			obj.subject = in.readInt();
			obj.questionScore = in.readDouble();
			obj.updateTimestamp = in.readLong();
			obj.audio_id = in.readString();
			obj.audio_gold = in.readInt();
			obj.audio_duration = in.readInt();
			obj.audio_url = in.readString();
			obj.audio_status = in.readInt();
			obj.audio_has_comment = in.readInt();
			return obj;
		}

		@Override
		public Answer[] newArray(int size) {
			return new Answer[size];
		}
	};
}
