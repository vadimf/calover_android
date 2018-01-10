package com.varteq.catslovers.view.fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.R;
import com.varteq.catslovers.managers.DialogsManager;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.utils.qb.QbChatDialogMessageListenerImp;
import com.varteq.catslovers.utils.qb.QbDialogHolder;
import com.varteq.catslovers.utils.qb.callback.QbEntityCallbackImpl;
import com.varteq.catslovers.view.presenter.MessagesPresenter;
import com.varteq.catslovers.view.qb.ChatActivity;
import com.varteq.catslovers.view.qb.DialogsActivity;
import com.varteq.catslovers.view.qb.SelectUsersActivity;
import com.varteq.catslovers.view.qb.adapter.DialogsAdapter;
import com.varteq.catslovers.view.qb.dialog.ProgressDialogFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class MessagesFragment extends Fragment implements DialogsManager.ManagingDialogsCallbacks {

    private static final String TAG = DialogsActivity.class.getSimpleName();
    private static final int REQUEST_SELECT_PEOPLE = 174;
    private static final int REQUEST_DIALOG_ID_FOR_UPDATE = 165;

    @BindView(R.id.list_dialogs_chats_friends)
    ListView dialogsFriendsListView;
    @BindView(R.id.list_dialogs_chats_groups)
    ListView dialogsGroupsListView;
    @BindView(R.id.progress_dialogs)
    ProgressBar progressBar;
    //@BindView(R.id.fab_dialogs_new_chat)
    //FloatingActionButton fab;
    @BindView(R.id.swipy_refresh_layout)
    SwipyRefreshLayout setOnRefreshListener;
    @BindView(R.id.friends_type_button_layout)
    FrameLayout friendsTypeButtonLayout;
    @BindView(R.id.groups_type_button_layout)
    FrameLayout groupsTypeButtonLayout;
    @BindView(R.id.friends_type_button)
    Button friendsTypeButton;
    @BindView(R.id.groups_type_button)
    Button groupsTypeButton;

    private QBRequestGetBuilder requestBuilder;
    private Menu menu;
    private int skipRecords = 0;
    private boolean isProcessingResultInProgress;
    private ActionMode currentActionMode;

    private BroadcastReceiver pushBroadcastReceiver;
    //private GooglePlayServicesHelper googlePlayServicesHelper;
    private DialogsAdapter friendsDialogsAdapter;
    private DialogsAdapter groupsDialogsAdapter;
    private QBChatDialogMessageListener allDialogsMessagesListener;
    private SystemMessagesListener systemMessagesListener;
    private QBSystemMessagesManager systemMessagesManager;
    private QBIncomingMessagesManager incomingMessagesManager;
    private DialogsManager dialogsManager;
    private QBUser currentUser;

    private MessagesPresenter presenter;

    private List<QBChatDialog> friendsDialogsList;
    private List<QBChatDialog> groupsDialogsList;
    private boolean isFriendsChat = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi();

        //setActionBarTitle(getString(R.string.dialogs_logged_in_as, currentUser.getFullName()));
        //setActionBarTitle(getString(R.string.chat_menu));

        isProcessingResultInProgress = true;
        progressBar.setVisibility(View.VISIBLE);
        presenter.checkQBLogin();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //googlePlayServicesHelper = new GooglePlayServicesHelper();

        //pushBroadcastReceiver = new PushBroadcastReceiver();

        allDialogsMessagesListener = new AllDialogsMessageListener();
        systemMessagesListener = new SystemMessagesListener();

        dialogsManager = new DialogsManager();

        currentUser = ChatHelper.getCurrentUser();
        presenter = new MessagesPresenter(this);

        friendsDialogsList = new ArrayList<>();
        groupsDialogsList = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        //googlePlayServicesHelper.checkPlayServicesAvailable(this);

        //LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver, new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
    }

    @Override
    public void onPause() {
        super.onPause();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterQbChatListeners();
    }

    public void onUsersSelescted(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            isProcessingResultInProgress = true;
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                if (isPrivateDialogExist(selectedUsers)) {
                    selectedUsers.remove(ChatHelper.getCurrentUser());
                    QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(selectedUsers.get(0));
                    isProcessingResultInProgress = false;
                    ChatActivity.startForResult(getActivity(), REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog);
                } else {
                    ProgressDialogFragment.show(getActivity().getSupportFragmentManager(), R.string.create_chat);
                    createDialog(selectedUsers);
                }
            } else if (requestCode == REQUEST_DIALOG_ID_FOR_UPDATE) {
                if (data != null) {
                    String dialogId = data.getStringExtra(ChatActivity.EXTRA_DIALOG_ID);
                    loadUpdatedDialog(dialogId);
                } else {
                    isProcessingResultInProgress = false;
                    updateDialogsList();
                }
            }
        } else {
            updateDialogsAdapters();
        }
    }

    private void switchFriendsChat() {
        isFriendsChat = true;
        dialogsFriendsListView.setVisibility(View.VISIBLE);
        dialogsGroupsListView.setVisibility(View.INVISIBLE);
        friendsTypeButtonLayout.setBackgroundResource(R.drawable.messages_type_selected_shape);
        groupsTypeButtonLayout.setBackgroundResource(R.drawable.messages_type_unselected_shape);
        friendsTypeButton.setTextAppearance(getContext(), R.style.PrimaryTextButton);
        groupsTypeButton.setTextAppearance(getContext(), R.style.SecondaryTextButton);
    }

    private void switchGroupsChat() {
        isFriendsChat = false;
        dialogsFriendsListView.setVisibility(View.INVISIBLE);
        dialogsGroupsListView.setVisibility(View.VISIBLE);
        groupsTypeButtonLayout.setBackgroundResource(R.drawable.messages_type_selected_shape);
        friendsTypeButtonLayout.setBackgroundResource(R.drawable.messages_type_unselected_shape);
        groupsTypeButton.setTextAppearance(getContext(), R.style.PrimaryTextButton);
        friendsTypeButton.setTextAppearance(getContext(), R.style.SecondaryTextButton);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onUsersSelescted(requestCode, resultCode, data);
    }

    private boolean isPrivateDialogExist(ArrayList<QBUser> allSelectedUsers) {
        ArrayList<QBUser> selectedUsers = new ArrayList<>();
        selectedUsers.addAll(allSelectedUsers);
        selectedUsers.remove(ChatHelper.getCurrentUser());
        return selectedUsers.size() == 1 && QbDialogHolder.getInstance().hasPrivateDialogWithUser(selectedUsers.get(0));
    }

    private void loadUpdatedDialog(String dialogId) {
        ChatHelper.getInstance().getDialogById(dialogId, new QbEntityCallbackImpl<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog result, Bundle bundle) {
                isProcessingResultInProgress = false;
                QbDialogHolder.getInstance().addDialog(result);
                updateDialogsAdapters();
            }

            @Override
            public void onError(QBResponseException e) {
                isProcessingResultInProgress = false;
            }
        });
    }

    /*@Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.layout_root);
    }

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
    }*/

    private void userLogout() {
        /*ChatHelper.getInstance().destroy();
        SubscribeService.unSubscribeFromPushes(DialogsActivity.this);
        SharedPrefsHelper.getInstance().removeQbUser();
        SignUpActivity.start(DialogsActivity.this);
        QbDialogHolder.getInstance().clear();
        ProgressDialogFragment.hide(getSupportFragmentManager());
        finish();*/
    }

    private void updateDialogsList() {
        requestBuilder.setSkip(skipRecords = 0);
        loadDialogsFromQb(true, true);
    }

    //@OnClick(R.id.fab_dialogs_new_chat)
    public void onStartNewChatClick(View view) {
        //showChooseNumberDialog();
        if (progressBar.getVisibility() != View.VISIBLE)
            SelectUsersActivity.startForResult(getActivity(), REQUEST_SELECT_PEOPLE);
    }

    private void showChooseNumberDialog() {
        final Dialog dialog = new Dialog(getContext());
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_test_choose_phone_number);

        Button okButton = dialog.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> {
            String number = ((EditText) dialog.findViewById(R.id.code_EditText)).getText().toString();
            if (number.length() > 10) {
                dialog.dismiss();
                SelectUsersActivity.startForResultWithNumber(getActivity(), REQUEST_SELECT_PEOPLE, number);
            } else {
                ((EditText) dialog.findViewById(R.id.code_EditText)).setError("Incorrect number");
            }
        });

        dialog.show();
    }

    private void initUi() {

        sortDialogs();

        friendsDialogsAdapter = new DialogsAdapter(getContext(), friendsDialogsList);
        groupsDialogsAdapter = new DialogsAdapter(getContext(), groupsDialogsList);

        /*LinearLayout emptyHintLayout = _findViewById(R.id.layout_chat_empty);
        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, dialogsFriendsListView, false);
        listHeader.setText(R.string.dialogs_list_hint);
        dialogsFriendsListView.setEmptyView(emptyHintLayout);
        dialogsFriendsListView.addHeaderView(listHeader, null, false);*/

        dialogsFriendsListView.setAdapter(friendsDialogsAdapter);
        dialogsFriendsListView.setDivider(getResources().getDrawable(R.drawable.dialog_list_divider));
        dialogsFriendsListView.setDividerHeight(2);
        dialogsGroupsListView.setAdapter(groupsDialogsAdapter);
        dialogsGroupsListView.setDivider(getResources().getDrawable(R.drawable.dialog_list_divider));
        dialogsGroupsListView.setDividerHeight(2);

        dialogsFriendsListView.setOnItemClickListener((parent, view, position, id) -> {
            QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
            if (currentActionMode == null) {
                ChatActivity.startForResult(getActivity(), REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog);
            } else {
                friendsDialogsAdapter.toggleSelection(selectedDialog);
            }
        });

        dialogsFriendsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
            ((AppCompatActivity) getActivity()).startSupportActionMode(new DeleteActionModeCallback());
            friendsDialogsAdapter.selectItem(selectedDialog);
            return true;
        });
        dialogsGroupsListView.setOnItemClickListener((parent, view, position, id) -> {
            QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
            if (currentActionMode == null) {
                ChatActivity.startForResult(getActivity(), REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog);
            } else {
                friendsDialogsAdapter.toggleSelection(selectedDialog);
            }
        });

        dialogsGroupsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
            ((AppCompatActivity) getActivity()).startSupportActionMode(new DeleteActionModeCallback());
            groupsDialogsAdapter.selectItem(selectedDialog);
            return true;
        });

        requestBuilder = new QBRequestGetBuilder();

        setOnRefreshListener.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                requestBuilder.setSkip(skipRecords += ChatHelper.DIALOG_ITEMS_PER_PAGE);
                loadDialogsFromQb(true, false);
            }
        });
        setOnRefreshListener.setColorSchemeResources(R.color.colorPrimary);
    }

    private void registerQbChatListeners() {
        incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

        if (incomingMessagesManager != null) {
            incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener != null
                    ? allDialogsMessagesListener : new AllDialogsMessageListener());
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.addSystemMessageListener(systemMessagesListener != null
                    ? systemMessagesListener : new SystemMessagesListener());
        }

        dialogsManager.addManagingDialogsCallbackListener(this);
    }

    private void unregisterQbChatListeners() {
        if (incomingMessagesManager != null) {
            incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener);
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
        }

        dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    public void registerQbChatListenersLoadDialogs() {
        registerQbChatListeners();
        if (QbDialogHolder.getInstance().getDialogs().size() > 0) {
            loadDialogsFromQb(true, true);
        } else {
            loadDialogsFromQb(false, true);
        }
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        isProcessingResultInProgress = false;
                        dialogsManager.sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog);
                        ChatActivity.startForResult(getActivity(), REQUEST_DIALOG_ID_FOR_UPDATE, dialog);
                        ProgressDialogFragment.hide(getActivity().getSupportFragmentManager());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        isProcessingResultInProgress = false;
                        ProgressDialogFragment.hide(getActivity().getSupportFragmentManager());
                        //showErrorSnackbar(R.string.dialogs_creation_error, null, null);
                    }
                }
        );
    }

    private void loadDialogsFromQb(final boolean silentUpdate, final boolean clearDialogHolder) {
        isProcessingResultInProgress = true;
        if (!silentUpdate) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                isProcessingResultInProgress = false;
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);

                if (clearDialogHolder) {
                    QbDialogHolder.getInstance().clear();
                }
                QbDialogHolder.getInstance().addDialogs(dialogs);
                updateDialogsAdapters();
            }

            @Override
            public void onError(QBResponseException e) {
                isProcessingResultInProgress = false;
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDialogsAdapters() {
        sortDialogs();
        friendsDialogsAdapter.updateList(friendsDialogsList);
        groupsDialogsAdapter.updateList(groupsDialogsList);
    }

    private void sortDialogs() {
        friendsDialogsList.clear();
        groupsDialogsList.clear();
        for (QBChatDialog dialog : QbDialogHolder.getInstance().getDialogs().values()) {
            if (dialog.getType().equals(QBDialogType.PRIVATE)) {
                friendsDialogsList.add(dialog);
            } else {
                groupsDialogsList.add(dialog);
            }
        }
    }

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {
        updateDialogsAdapters();
    }

    @Override
    public void onDialogUpdated(String chatDialog) {
        updateDialogsAdapters();
    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {
        updateDialogsAdapters();
    }

    public void showError(String message) {
        Toaster.longToast(message);
        isProcessingResultInProgress = false;
        progressBar.setVisibility(View.GONE);
        setOnRefreshListener.setRefreshing(false);
    }

    private class DeleteActionModeCallback implements ActionMode.Callback {

        public DeleteActionModeCallback() {
            //fab.hide();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode_dialogs, menu);
            currentActionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_dialogs_action_delete:
                    deleteSelectedDialogs();
                    if (currentActionMode != null) {
                        currentActionMode.finish();
                    }
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
            friendsDialogsAdapter.clearSelection();
            //fab.show();
        }

        private void deleteSelectedDialogs() {
            final Collection<QBChatDialog> selectedDialogs;
            if (isFriendsChat)
                selectedDialogs = friendsDialogsAdapter.getSelectedItems();
            else
                selectedDialogs = groupsDialogsAdapter.getSelectedItems();

            ChatHelper.getInstance().deleteDialogs(selectedDialogs, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
                    QbDialogHolder.getInstance().deleteDialogs(dialogsIds);
                    updateDialogsAdapters();
                }

                @Override
                public void onError(QBResponseException e) {
                    /*showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteSelectedDialogs();
                                }
                            });*/
                }
            });
        }
    }

    @OnClick(R.id.friends_type_button)
    public void onFriendsTypeButtonClicked() {
        switchFriendsChat();
    }


    @OnClick(R.id.groups_type_button)
    public void onGroupsTypeButtonClicked() {
        switchGroupsChat();
    }

    /*private class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            Log.v(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
            requestBuilder.setSkip(skipRecords = 0);
            loadDialogsFromQb(true, true);
        }
    }*/

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
            dialogsManager.onSystemMessageReceived(qbChatMessage);
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {

        }
    }

    private class AllDialogsMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer senderId) {
            if (!senderId.equals(ChatHelper.getCurrentUser().getId())) {
                dialogsManager.onGlobalMessageReceived(dialogId, qbChatMessage);
            }
        }
    }
}
