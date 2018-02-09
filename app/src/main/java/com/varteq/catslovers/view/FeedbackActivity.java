package com.varteq.catslovers.view;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.varteq.catslovers.R;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Toaster;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends BaseActivity {

    private String TAG = FeedbackActivity.class.getSimpleName();
    @BindView(R.id.subject_editText)
    EditText subjectEditText;
    @BindView(R.id.content_editText)
    EditText contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Log.d(TAG, "onCreate");
        ButterKnife.bind(this);
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    @OnClick(R.id.send_feedback_button)
    void sendFeedback() {
        Log.d(TAG, "sendFeedback");
        if (contentEditText.getText() == null || contentEditText.getText().toString().isEmpty()) {
            Toaster.shortToast("You should fill content field");
            return;
        }
        showWaitDialog();
        String content = contentEditText.getText().toString();
        String subject;
        if (subjectEditText.getText() == null || subjectEditText.getText().toString().isEmpty()) {
            subject = content.substring(0, content.length() > 20 ? 20 : content.length());
        } else
            subject = subjectEditText.getText().toString();
        Call<BaseResponse> call = ServiceGenerator.getApiServiceWithToken().sendFeedback(subject, content);
        call.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser(response) {

                        @Override
                        protected void onSuccess(Object data) {
                            Log.d(TAG, "sendFeedback onSuccess ");
                            Toaster.shortToast("feedback sent");
                            onBackPressed();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                            hideWaitDialog();
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Log.e(TAG, "sendFeedback onFailure " + t.getMessage());
                hideWaitDialog();
            }
        });
    }

    @OnClick(R.id.back_button)
    void goBack() {
        Log.d(TAG, "goBack");
        onBackPressed();
    }
}
