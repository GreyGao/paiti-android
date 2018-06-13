package com.abcpen.simple.jsplugin;

import android.app.Activity;

import com.abcpen.simple.ResultActivity;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyPlugin extends CordovaPlugin {

    public static final String Action_SHOW_RESULT = "showResult";

    public static final String ACTION_PIC = "viewPic";

    public static final String ACTION_HELP = "helps";

    public static final String ACTION_REPORT_ERROR = "reportError";

    public static final String ACTION_TITLE = "setTitle";

    public static final String ACTION_DATA = "getUserData";

    public static final String ACTION_INVITE = "beginInvite";

    public static final String ACTION_RETRY_LINK = "reTryLink";

    public static final String ACTION_SHOW_DISCUS = "showDiscuss";

    public static final String ACTION_CACHE_INDEX = "cacheIndex";

    public static final String ACTION_EVALUATE = "evaluate";

    public static final String ACTION_PAY_AUDIO = "payAudio";

    public static final String ACTION_RESULT_TITLE = "resultTitle";

    public static final String ACTION_REQUEST_AUDIO = "requestAudio";

    public static final String ACTION_PLAY_AUDIO = "playAudio";
    public static final String ACTION_UPDATE_COURSE = "updateCourse";
    public static final String ACTION_SHOW_TEACHER = "showTeacher";
    public static final String ACTION_INTERACTIVE = "interactive";

    public static final String ACTION_DATA_RESULT = "getUserInfo";
    public static final String ACTION_REQ_COURSE_FAIL = "requestCourseFail";

    public static final String ACTION_SHOW_QUESTION = "showQuestion";
    public static final String ACTION_SHOW_LOADING = "showLoading";
    public static final String ACTION_REQUEST_TEACHERS = "requestTeachers";
    public static final String ACTION_PAY_EXERCISE = "payExercise";

    public static final String ACTION_WEBVIEW_LOG = "logJS";

    public static final String ACTION_POST_MEDIAS = "postMedias";
    public static final String ACTION_FULL_SCREEN_PLAY = "fullScreenBoard";
    public static final String ACTION_PAY_BOARD = "payBoard";

    public static final String ACTION_PLAY_NO_VIDEO = "playNoVideo";


    /**
     * all these method is called on js thread ,so notUI thread
     */
    @Override
    public boolean execute(String action, JSONArray data,
                           CallbackContext callbackContext) throws JSONException {


        if (Action_SHOW_RESULT.equals(action)) {
        } else if (ACTION_POST_MEDIAS.equals(action)) {

        } else if (ACTION_FULL_SCREEN_PLAY.equals(action)) {
            // TODO

            return true;
        } else if (ACTION_PAY_BOARD.equals(action)) {
            // TODO
            return true;
        } else if (ACTION_PLAY_NO_VIDEO.equals(action)) {
            // TODO

            return true;
        } else if (ACTION_SHOW_LOADING.equals(action)) {
            Activity activity = this.cordova.getActivity();
            if (activity instanceof ResultActivity) {
                ResultActivity resultActivity = (ResultActivity) activity;
                JSONObject object = resultActivity.showLoading();
                callbackContext.success(object);
            }
            return true;
        } else if (ACTION_SHOW_QUESTION.equals(action)) {
            Activity activity = this.cordova.getActivity();
            if (activity instanceof ResultActivity) {
                ResultActivity resultActivity = (ResultActivity) activity;
                JSONObject object = resultActivity.showQuestion();
                callbackContext.success(object);
            }
            return true;
        } else if (ACTION_CACHE_INDEX.equals(action)) {

        } else if (ACTION_PIC.equals(action)) {
            Activity activity = this.cordova.getActivity();
            if (activity instanceof ResultActivity) {
                ResultActivity resultActivity = (ResultActivity) activity;
                callbackContext.success();
            }
            return true;
        } else if (ACTION_HELP.equals(action)) {

        } else if (ACTION_REPORT_ERROR.equals(action)) {

            return true;
        } else if (ACTION_PAY_AUDIO.equals(action)) {

            return true;
        } else if (ACTION_EVALUATE.equals(action)) {

        } else if (ACTION_TITLE.equals(action)) {

        } else if (ACTION_DATA.equals(action)) {

            return true;
        } else if (ACTION_INVITE.equals(action)) {

        } else if (ACTION_RETRY_LINK.equals(action)) {

            return true;
        } else if (ACTION_SHOW_DISCUS.equals(action)) {

            return true;
        } else if (ACTION_RESULT_TITLE.equals(action)) {

            return true;
        } else if (ACTION_REQUEST_AUDIO.equals(action)) {

        } else if (ACTION_PLAY_AUDIO.equals(action)) {

        } else if (ACTION_UPDATE_COURSE.equals(action)) {

        } else if (ACTION_INTERACTIVE.equals(action)) {

        } else if (ACTION_SHOW_TEACHER.equals(action)) {

        } else if (ACTION_DATA_RESULT.equals(action)) {

            return true;
        } else if (ACTION_REQ_COURSE_FAIL.equals(action)) {

        } else if (ACTION_REQUEST_TEACHERS.equals(action)) {

        } else if (ACTION_PAY_EXERCISE.equals(action)) {

        } else if (ACTION_WEBVIEW_LOG.equals(action)) {
            Activity activity = this.cordova.getActivity();
            if (activity instanceof ResultActivity) {
                ResultActivity resultActivity = (ResultActivity) activity;
                resultActivity.logJS(data);
            }
        }

        callbackContext.error("Invalid action");
        return false;
    }
}
