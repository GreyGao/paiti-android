package com.abcpen.simple;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abcpen.model.AnswerModel;
import com.abcpen.simple.jsplugin.Answer;
import com.abcpen.simple.util.Util;

import org.apache.cordova.DroidGap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ResultActivity extends DroidGap {


    private static String INTENT_IMAGE_URL = "intent_image_url";
    private static String INTENT_CONTENT = "intent_content";
    private static String INTEN_IMAGE_ID = "inten_image_id";

    private TextView mLeftTxt;

    public ResultActivity activity = null;

    private String mImageUrl;
    private ArrayList<AnswerModel> mContent;
    private String mImageId;


    private RelativeLayout audiosLayout;

    public static void startResultActivity(Context context, String imageUrl, String imageid,
                                           ArrayList<AnswerModel> data) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(INTENT_IMAGE_URL, imageUrl);
        intent.putExtra(INTENT_CONTENT, data);
        intent.putExtra(INTEN_IMAGE_ID, imageid);
        context.startActivity(intent);
    }

    public void bindIntent() {
        mImageUrl = getIntent().getStringExtra(INTENT_IMAGE_URL);
        mContent = getIntent().getParcelableArrayListExtra(INTENT_CONTENT);
        mImageId = getIntent().getStringExtra(INTEN_IMAGE_ID);
    }

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setIntegerProperty("loadUrlTimeoutValue", 30 * 1000);
        setContentView(R.layout.ac_result_title_webview);
        bindIntent();
        initView();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        loadUrl("file:///android_asset/www/index.html");
        activity = this;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        activity = null;
        super.onDestroy();
    }


//    public JSONObject getUserInfo() {
//        JSONObject jsonObj = new JSONObject();
//        String userAgent = PrefAppStore.getUserAgent(this);
//        String token = PrefAppStore.getToken(this);
//        String cookie = PrefAppStore.getCookie(this);
//        String mobileNumber = PrefAppStore.getUserMobile(this);
//        try {
//            jsonObj.put("user_agent", userAgent);
//            jsonObj.put("token", token);
//            jsonObj.put("cookie", cookie);
//            jsonObj.put("mobile", mobileNumber);
//            jsonObj.put("isMember", mIsXxbMember);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObj;
//    }


    public void logJS(JSONArray data) {
        String log_name = "";
        String log_string = "";
        try {
            log_name = (String) data.get(0);
            log_string = (String) data.get(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(log_name, log_string);
    }

    public void findView() {
        mLeftTxt = findViewById(R.id.nav_back);

        appView = findViewById(R.id.webview);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            WebSettings ws = appView.getSettings();
            ws.setMediaPlaybackRequiresUserGesture(false);
        }
        mLeftTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                finish();
            }
        });

    }


    private void initView() {
        findView();

        appView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    finish();
                    return true;
                }
                return onKeyDown(keyCode, event);
            }
        });
    }


    public void setTitle(JSONArray array) throws JSONException {

    }


    public JSONObject showLoading() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("status", 0);
            JSONObject question = new JSONObject();
            question.put("image_path", getImagePath());
            obj.put("question", question);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private String getImagePath() {
        return mImageUrl;
    }

    private JSONObject initData(int status, String imagePath, JSONArray array) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("status", status);
            JSONObject question = new JSONObject();
            question.put("image_path", imagePath);
            question.put("image_id", mImageId);
            obj.put("question", question);
            obj.put("machine_answers", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private JSONObject showEmptyQuestion() {
        final JSONObject obj = initData(-1, getImagePath(), new JSONArray());
        return obj;
    }

    /**
     * 缓存
     */
    private JSONObject genQuestionObj(int status) {
        ArrayList<Answer> answers = new ArrayList<>();
        for (int i = 0; i < mContent.size(); i++) {
            Answer answer = convert(mContent.get(i), i);
            if (answer != null) {
                answers.add(answer);
            }
        }

        if (answers == null) {
            return null;
        }

        final JSONArray answersJson = Util.objToJson(answers);

        final JSONObject obj = initData(status, getImagePath(),
                answersJson);
        try {
            obj.put("index", 0);
            obj.put("is_ask", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    private Answer convert(AnswerModel answerModel, int index) {
        Answer answer = new Answer();
        if (answerModel == null)
            return answer;
        answer.imgUrl = answerModel.image_url;
        answer.imgUuid = mImageId;
        answer.questionIndex = index;
        answer.questionId = answerModel.question_id;
        answer.questionAnswer = answerModel.quesiton_answer;
        answer.quesitonAnalysis = answerModel.quesiton_answer;
        answer.questionBody = answerModel.question_body;
        answer.questionHtml = answerModel.question_html;
        answer.subject = answerModel.subject;
        return answer;
    }

    public JSONObject showQuestion() {
        if (TextUtils.isEmpty(mImageId) || mContent == null || mContent.size() == 0) {
            return showEmptyQuestion();
        }
        return genQuestionObj(2);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }


}