package com.varteq.catslovers.utils.qb;

import android.util.SparseArray;

import com.quickblox.users.model.QBUser;
import com.varteq.catslovers.realm.RealmQBUser;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Basically in your app you should store users in database
 * And load users to memory on demand
 * We're using runtime SpaceArray holder just to simplify app logic
 */
public class QbUsersHolder {

    private static QbUsersHolder instance;

    private SparseArray<QBUser> qbUserSparseArray;

    public static synchronized QbUsersHolder getInstance() {
        if (instance == null) {
            instance = new QbUsersHolder();
        }

        return instance;
    }

    private QbUsersHolder() {
        qbUserSparseArray = new SparseArray<>();
        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<RealmQBUser> results = myRealm.where(RealmQBUser.class).findAll();
        for (RealmQBUser user : results)
            putUser(user.getAsQBUser());
        myRealm.close();
    }

    public void putUsers(List<QBUser> users) {
        ArrayList<QBUser> updateInRealm = new ArrayList<>();
        for (QBUser user : users) {
            if (qbUserSparseArray.get(user.getId()) == null ||
                    qbUserSparseArray.get(user.getId()).getUpdatedAt().before(user.getUpdatedAt()))
                updateInRealm.add(user);
            putUser(user);
        }
        if (updateInRealm.isEmpty()) return;
        Realm myRealm = Realm.getDefaultInstance();
        myRealm.executeTransaction(realm -> {
            ArrayList<RealmQBUser> realmQBUsers = new ArrayList<>();
            for (QBUser user : updateInRealm)
                realmQBUsers.add(new RealmQBUser(user));
            myRealm.insertOrUpdate(realmQBUsers);
            myRealm.close();
        });
    }

    private void putUser(QBUser user) {
        qbUserSparseArray.put(user.getId(), user);
    }

    public QBUser getUserById(int id) {
        return qbUserSparseArray.get(id);
    }

    public List<QBUser> getUsersByIds(List<Integer> ids) {
        List<QBUser> users = new ArrayList<>();
        for (Integer id : ids) {
            QBUser user = getUserById(id);
            if (user != null) {
                users.add(user);
            }
        }

        return users;
    }

}
