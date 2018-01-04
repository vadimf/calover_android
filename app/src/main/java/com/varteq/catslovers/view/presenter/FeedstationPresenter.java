package com.varteq.catslovers.view.presenter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.varteq.catslovers.api.BaseParser;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.api.entity.BaseResponse;
import com.varteq.catslovers.api.entity.ErrorData;
import com.varteq.catslovers.api.entity.ErrorResponse;
import com.varteq.catslovers.api.entity.RFeedstation;
import com.varteq.catslovers.api.entity.RUser;
import com.varteq.catslovers.model.Feedstation;
import com.varteq.catslovers.model.GroupPartner;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.Toaster;
import com.varteq.catslovers.view.FeedstationActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedstationPresenter {

    private String TAG = FeedstationPresenter.class.getSimpleName();

    private FeedstationActivity view;

    public FeedstationPresenter(FeedstationActivity view) {
        this.view = view;
    }

    public void onPetImageSelected(Uri uri, List photoList, RecyclerView.Adapter photosAdapter) {
        if (uri != null) {
            Log.d(TAG, "onImageSelected " + uri);
            photoList.add(0, uri);
            photosAdapter.notifyItemInserted(0);
        }
    }

    public void saveFeedstation(Feedstation feedstation) {

        Call<BaseResponse<RFeedstation>> call = ServiceGenerator.getApiServiceWithToken().createFeedstation(feedstation.getName(),
                feedstation.getAddress(), feedstation.getDescription(), 0,
                feedstation.getLocation().latitude, feedstation.getLocation().longitude);


        call.enqueue(new Callback<BaseResponse<RFeedstation>>() {
            @Override
            public void onResponse(Call<BaseResponse<RFeedstation>> call, Response<BaseResponse<RFeedstation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RFeedstation>(response) {

                        @Override
                        protected void onSuccess(RFeedstation data) {
                            view.savedSuccessfully();
                            //checkChat(data.getId(), data.getName());
                            createChat(data.getId(), data.getName());
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                            if (error != null && error.getCode() == 422)
                                Toaster.longToast("You should fill station data");
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RFeedstation>> call, Throwable t) {
                Log.e(TAG, "createFeedstation onFailure " + t.getMessage());
            }
        });
    }

    public void addGroupPartner(Integer feedstationId, String phone) {
        phone = phone.replace(" ", "");
        phone = phone.replace("(", "");
        phone = phone.replace(")", "");

        Call<BaseResponse<RUser>> call = ServiceGenerator.getApiServiceWithToken().inviteUserToFeedstation(feedstationId, phone);

        call.enqueue(new Callback<BaseResponse<RUser>>() {
            @Override
            public void onResponse(Call<BaseResponse<RUser>> call, Response<BaseResponse<RUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RUser>(response) {

                        @Override
                        protected void onSuccess(RUser data) {
                            getGroupPartners(feedstationId);
                            /*GroupPartner partner = from(data, feedstationId);
                            if (partner != null && partner.getStatus() != null &&
                                    partner.getStatus().equals(GroupPartner.Status.JOINED))
                                addUserToChat(partner.getUserId(), feedstationId);*/
                            //else addInvitedUserToChat(phone, feedstationId);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RUser>> call, Throwable t) {
                Log.e(TAG, "inviteUser onFailure " + t.getMessage());
            }
        });
    }

    private void checkChat(Integer feedstationId, String name) {
        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() < 1)
                    createChat(feedstationId, name);
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }

    public static void addUserToChat(Integer userId, Integer feedstationId) {
        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() > 0)
                    ChatHelper.getInstance().addUserToDialog(qbChatDialogs.get(0), String.valueOf(userId), new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialogs, Bundle bundle) {
                            int i = 0;
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            int i = 0;
                        }
                    });
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }

    /*private void addInvitedUserToChat(String phone, Integer feedstationId) {
        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() > 0 && !QBChatInfo.containsInvitedUser(qbChatDialogs.get(0).getCustomData(), phone))
                    ChatHelper.getInstance().addInvitedUserToDialog(qbChatDialogs.get(0), phone, new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialogs, Bundle bundle) {
                            int i = 0;
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            int i = 0;
                        }
                    });
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }*/

    /*public void checkInvitedUsers(List<GroupPartner> users, Feedstation feedstationId){
        if (users==null || users.isEmpty()) return;

        ChatHelper.getInstance().getDialogForFeedstation(feedstationId, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                if (qbChatDialogs.size() < 1) return;

                List<String> invitedList = QBChatInfo.getInvitedUsers(qbChatDialogs.get(0).getCustomData());
                if (invitedList==null) return;

                List<GroupPartner> usersToAdd = new ArrayList<>();

                for (String phone : invitedList) {
                    for (int i = users.size()-1; i>-1; i--){
                        if (users.get(i).getPhone().equals(phone) && users.get(i).getStatus().equals(GroupPartner.Status.JOINED)){
                            usersToAdd.add(users.get(i));
                            break;
                        }
                    }
                }





                    ChatHelper.getInstance().addInvitedUserToDialog(qbChatDialogs.get(0), phone, new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialogs, Bundle bundle) {
                            int i = 0;
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            int i = 0;
                        }
                    });
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }*/

    private void createChat(Integer id, String name) {
        ChatHelper.getInstance().createEmptyPublicDialog(id, name, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                ChatHelper.getInstance().updateFeedstations();
            }

            @Override
            public void onError(QBResponseException e) {
                int i = 0;
            }
        });
    }

    public void getGroupPartners(Integer feedstationId) {
        if (feedstationId == null) return;

        Call<BaseResponse<List<RUser>>> call = ServiceGenerator.getApiServiceWithToken().getFeedstationUsers(feedstationId);

        call.enqueue(new Callback<BaseResponse<List<RUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<RUser>>> call, Response<BaseResponse<List<RUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<List<RUser>>(response) {

                        @Override
                        protected void onSuccess(List<RUser> data) {
                            List<GroupPartner> partners = new ArrayList<>();
                            for (RUser user : data) {
                                partners.add(from(user, feedstationId));
                                /*if (!from(user, feedstationId).isAdmin() )//&& from(user, feedstationId).getUserId() == 6)
                                    deleteGroupPartner(feedstationId, from(user, feedstationId).getUserId());*/
                                /*GroupPartner partner = from(user, feedstationId);
                                if (partner.getStatus().equals(GroupPartner.Status.JOINED) && !partner.isAdmin())
                                    addUserToChat(partner.getUserId(), feedstationId);*/
                            }
                            view.refreshGroupPartners(partners);
                            /*if (feedstation.getIsPublic() && feedstation.getUserRole() != null &&
                                    feedstation.getUserRole().equals(Feedstation.UserRole.ADMIN))
                                checkInvitedUsers(partners, feedstation);*/
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<RUser>>> call, Throwable t) {
                Log.e(TAG, "getGroupPartners onFailure " + t.getMessage());
            }
        });
    }

    private GroupPartner from(RUser user, Integer feedstationId) {
        GroupPartner.Status status = GroupPartner.Status.INVITED;
        if (user.getStatus() != null) {
            if (user.getStatus().equals("joined"))
                status = GroupPartner.Status.JOINED;
            else if (user.getStatus().equals("invited"))
                status = GroupPartner.Status.INVITED;
            else if (user.getStatus().equals("requested"))
                status = GroupPartner.Status.REQUESTED;
        } else return null;

        boolean isAdmin = false;
        if (user.getRole().equals("admin"))
            isAdmin = true;

        String name = user.getUserInfo().getName();
        if (user.getUserId().equals(Integer.parseInt(Profile.getUserId(view)))) {
            name = "You";
            /*if (status.equals(GroupPartner.Status.INVITED))
                joinFeedstation(feedstationId);*/
        }
        return new GroupPartner(null, user.getUserId(), name, user.getUserInfo().getPhone(), status, isAdmin);
    }

    public void updateFeedstation(Feedstation feedstation) {

        Call<BaseResponse<RFeedstation>> call = ServiceGenerator.getApiServiceWithToken().updateFeedstation(feedstation.getId(), feedstation.getName(),
                feedstation.getAddress(), feedstation.getDescription(), 0,
                feedstation.getLocation().latitude, feedstation.getLocation().longitude);


        call.enqueue(new Callback<BaseResponse<RFeedstation>>() {
            @Override
            public void onResponse(Call<BaseResponse<RFeedstation>> call, Response<BaseResponse<RFeedstation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<RFeedstation>(response) {

                        @Override
                        protected void onSuccess(RFeedstation data) {
                            view.savedSuccessfully();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            Log.d(TAG, error.getMessage() + error.getCode());
                            if (error != null && error.getCode() == 422)
                                Toaster.longToast("You should fill station data");
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<RFeedstation>> call, Throwable t) {
                Log.e(TAG, "updateFeedstation onFailure " + t.getMessage());
            }
        });
    }

    public void followFeedstation(Integer feedstationId) {

        if (feedstationId == null) return;

        Call<BaseResponse<ErrorData>> call = ServiceGenerator.getApiServiceWithToken().followFeedstation(feedstationId);
        call.enqueue(new Callback<BaseResponse<ErrorData>>() {
            @Override
            public void onResponse(Call<BaseResponse<ErrorData>> call, Response<BaseResponse<ErrorData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<ErrorData>(response) {

                        @Override
                        protected void onSuccess(ErrorData data) {
                            view.onSuccessFollow();
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ErrorData>> call, Throwable t) {
                Log.e(TAG, "getFeedstations onFailure " + t.getMessage());
            }
        });
    }

    public void deleteGroupPartner(Integer feedstationId, int userId) {

        if (feedstationId == null) return;

        Call<BaseResponse<ErrorData>> call = ServiceGenerator.getApiServiceWithToken().deleteUserFromFeedstation(feedstationId, userId);
        call.enqueue(new Callback<BaseResponse<ErrorData>>() {
            @Override
            public void onResponse(Call<BaseResponse<ErrorData>> call, Response<BaseResponse<ErrorData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    new BaseParser<ErrorData>(response) {

                        @Override
                        protected void onSuccess(ErrorData data) {
                            getGroupPartners(feedstationId);
                        }

                        @Override
                        protected void onFail(ErrorResponse error) {
                            if (error != null)
                                Log.d(TAG, error.getMessage() + error.getCode());
                        }
                    };
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ErrorData>> call, Throwable t) {
                Log.e(TAG, "joinFeedstation onFailure " + t.getMessage());
            }
        });
    }
}
