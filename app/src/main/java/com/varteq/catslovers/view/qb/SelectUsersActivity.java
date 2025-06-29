package com.varteq.catslovers.view.qb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.R;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.qb.adapter.CheckboxUsersAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectUsersActivity extends QBBaseActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 2;
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);

    private static final String EXTRA_QB_DIALOG = "qb_dialog";
    private static final String EXTRA_QB_PHONE_NUMBER = "phone_number";
    private static final String TAG = SelectUsersActivity.class.getSimpleName();

    private ListView usersListView;
    private ProgressBar progressBar;
    private CheckboxUsersAdapter usersAdapter;
    private long lastClickTime = 0l;

    public static void start(Context context) {
        Intent intent = new Intent(context, SelectUsersActivity.class);
        context.startActivity(intent);
    }

    /**
     * Start activity for picking users
     *
     * @param activity activity to return result
     * @param code     request code for onActivityResult() method
     *                 <p>
     *                 in onActivityResult there will be 'ArrayList<QBUser>' in the intent extras
     *                 which can be obtained with SelectPeopleActivity.EXTRA_QB_USERS key
     */
    public static void startForResult(Activity activity, int code) {
        startForResult(activity, null, code, null);
    }

    public static void startForResult(Activity activity, Fragment fragment, int code) {
        startForResult(activity, fragment, code, null);
    }

    public static void startForResult(Activity activity, int code, QBChatDialog dialog) {
        startForResult(activity, null, code, dialog);
    }

    public static void startForResult(Activity activity, Fragment fragment, int code, QBChatDialog dialog) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);
        intent.putExtra(EXTRA_QB_DIALOG, dialog);
        if (fragment != null)
            fragment.startActivityForResult(intent, code);
        else
            activity.startActivityForResult(intent, code);
    }

    public static void startForResultWithNumber(Activity activity, int code, String phoneNumber) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);
        intent.putExtra(EXTRA_QB_PHONE_NUMBER, phoneNumber);
        activity.startActivityForResult(intent, code);
    }

    public static void startForResultWithNumber(Activity activity, Fragment fragment, int code, String phoneNumber) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);
        intent.putExtra(EXTRA_QB_PHONE_NUMBER, phoneNumber);
        fragment.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_users);
        getSupportActionBar().setElevation(0);

        progressBar = _findViewById(R.id.progress_select_users);
        usersListView = _findViewById(R.id.list_select_users);

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, usersListView, false);
        listHeader.setText(R.string.select_users_list_hint);
        usersListView.addHeaderView(listHeader, null, false);

        if (isEditingChat()) {
            setActionBarTitle(R.string.select_users_edit_chat);
        } else {
            setActionBarTitle(R.string.select_users_create_chat);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);

        getAllowedUsers();
        //loadUsersFromQb();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
            return super.onOptionsItemSelected(item);
        }
        lastClickTime = SystemClock.uptimeMillis();

        switch (item.getItemId()) {
            case R.id.menu_select_people_action_done:
                if (usersAdapter != null) {
                    List<QBUser> users = usersAdapter.getSelectedUsers();
                    if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        passResultToCallerActivity();
                    } else {
                        Toaster.shortToast(R.string.select_users_choose_users);
                    }
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.layout_root);
    }

    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        setResult(RESULT_OK, result);
        finish();
    }

    private void loadUsersFromQb(List<String> usersLogins) {
        /*List<String> tags = new ArrayList<>();
        if (getIntent()!=null && getIntent().hasExtra(EXTRA_QB_PHONE_NUMBER))
            tags.add(getIntent().getStringExtra(EXTRA_QB_PHONE_NUMBER));*/
        //tags.add("+380935772101");
        //tags.add(AppController.getSampleConfigs().getUsersTag());

        progressBar.setVisibility(View.VISIBLE);
        //ArrayList<String> userId = new ArrayList();
        //userId.add("0");
        //QBUsers.getUsersByFilter(usersLogins, "number id gt ", null).performAsync(
        QBUsers.getUsersByLogins(usersLogins, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                        QBChatDialog dialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_QB_DIALOG);

                        usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, result);
                        if (dialog != null) {
                            usersAdapter.addSelectedUsers(dialog.getOccupants());
                        }
                        usersListView.setAdapter(usersAdapter);

                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        showErrorSnackbar(R.string.select_users_get_users_error, e,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        loadUsersFromQb(usersLogins);
                                    }
                                });
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private boolean isEditingChat() {
        return getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null;
    }

    private void getAllowedUsers() {

        Call<BaseResponse<List<RUser>>> call = ServiceGenerator.getApiServiceWithToken().getAllowedUsers();
        call.enqueue(new Callback<BaseResponse<List<RUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RUser>>> call, Response<BaseResponse<List<RUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RUser>>(response) {

                        @Override
                        protected void onSuccess(List<RUser> data) {
                            List<String> usersLogins = new ArrayList<>();
                            for (RUser user : data)
                                usersLogins.add(String.valueOf(user.getUserId()));
                            loadUsersFromQb(usersLogins);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                com.varteq.catslovers.utils.Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RUser>>> call, Throwable t) {
                com.varteq.catslovers.utils.Log.e(TAG, "getAllowedUsers onFailure " + t.getMessage());
            }
        });
    }
}
