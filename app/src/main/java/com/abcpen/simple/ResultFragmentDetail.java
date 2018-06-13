package com.abcpen.simple;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


/**
 * Created by zhaocheng on 2018/4/13.
 */

public class ResultFragmentDetail extends Fragment {


    public static final String QUESTION_CONTENT = "QUESTION_CONTENT";
    public static final String QUESTION_ANSWER = "QUESTION_ANSWER";

    private WebView webQuestion, webAnswer;


    public static ResultFragmentDetail getInstance(String question, String answer) {

        ResultFragmentDetail fragmentDetail = new ResultFragmentDetail();
        Bundle bundle = new Bundle();
        bundle.putString(QUESTION_ANSWER, answer);
        bundle.putString(QUESTION_CONTENT, question);
        fragmentDetail.setArguments(bundle);
        return fragmentDetail;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frament_result, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {

        Bundle arguments = getArguments();

        webQuestion = view.findViewById(R.id.web_question);
        webAnswer = view.findViewById(R.id.web_answer);

        String answer = arguments.getString(QUESTION_ANSWER);
        String question = arguments.getString(QUESTION_CONTENT);

        webAnswer.setHorizontalScrollBarEnabled(false);
        webQuestion.setHorizontalScrollBarEnabled(false);

        webQuestion.getSettings().setDefaultFontSize(12);
        webAnswer.getSettings().setDefaultFontSize(12);

        webQuestion.loadData(question, "text/html; charset=UTF-8", null);
        webAnswer.loadData(answer, "text/html; charset=UTF-8", null);

    }


}
