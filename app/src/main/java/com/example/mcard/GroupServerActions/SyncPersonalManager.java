package com.example.mcard.GroupServerActions;

import static com.example.mcard.AdapersGroup.BasicCardManagerAdapter.percentageSimilarityResult;
import static com.example.mcard.AdapersGroup.BasicCardManagerAdapter.transliterateCardName;
import static com.example.mcard.AdapersGroup.GlobalUsersFindAdapter.OPERATION_ACCOUNT_INFO;
import static com.example.mcard.GroupServerActions.GlobalDataFBManager.actionCheckNonExistAccount;
import static com.example.mcard.GlobalListeners.NetworkListener.getStatusNetwork;

import android.content.Context;
import android.os.Build;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentActivity;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.AdapersGroup.GlobalUsersFindAdapter;
import com.example.mcard.FragmentsAdditionally.GlobalProfileUserFragment;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.R;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SyncPersonalManager extends BasicFireBaseManager
{
    public static final String PERSONAL_USERS = "USER INFO";
    private static final String FRIENDS_LIST = "MY FRIENDS";
    private static final String BLACK_LIST = "BLACK LIST";
    public static final String PERSONAL_USER_NAME = "nickname";
    private static final String USER_ROLE = "ROLE";
    private static final String LINK_DESIGN_CARDS = "gs://mcarddb-3d9fd.appspot.com/storage card design/";

    private final List<CardInfoEntity> personalСardsList;
    private final CardManagerDB cardManager_db;
    private final String groupLink;

    public SyncPersonalManager(Context context)
    {
        super(context);
        this.cardManager_db = new CardManagerDB(context);
        this.personalСardsList = new ArrayList<>();
        this.groupLink = FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getUid();
    }

    public void checkNewUser()
    {
        final DatabaseReference referenceCheckNewUserAuth = generalUserReference
                .child(groupLink)
                .child(PERSONAL_USERS);

        final ValueEventListener valueListenerNewUser = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                final String myUID = generationPersonalUID();
                if (!snapshot.hasChild(myUID)
                        || !snapshot.child(myUID).hasChild(USER_ROLE))
                    generalUserReference
                            .child(groupLink)
                            .child(PERSONAL_USERS)
                            .child(myUID)
                            .child(USER_ROLE)
                            .setValue(snapshot.getChildrenCount() == 0
                                    ? context.getString(R.string.roleAdmin)
                                    : snapshot.getChildrenCount() > 3
                                    ? context.getString(R.string.roleAdditional)
                                    : context.getString(R.string.roleSmallAdmin))
                            .addOnCompleteListener(task ->  clearThisListener());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error)
            { clearThisListener(); }

            private void clearThisListener()
            { referenceCheckNewUserAuth.removeEventListener(this); }
        };

        referenceCheckNewUserAuth
                .addListenerForSingleValueEvent(valueListenerNewUser);
    }

    public synchronized void checkUserRoleForPermissions(
      @StringRes int permissionForActionID
      , @StringRes int errorMessageID
      , @NonNull Runnable actionsAfterChecked
      , @NonNull CustomAppDialog loadingDialog)
    {
        if (!getStatusNetwork())
        {
            Toast.makeText(
                    context
                    , R.string.offlineNetworkMSG
                    , Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        loadingDialog.showLoadingDialog(true
                , context.getString(R.string.infoLoadingDialogCheckData));

        this.generalUserReference
                .child(groupLink)
                .child(PERSONAL_USERS)
                .child(generationPersonalUID())
                .child(USER_ROLE)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        loadingDialog.showLoadingDialog(
                                false, null);

                        if (snapshot.getValue(String.class)
                                .equals(context.getString(permissionForActionID))
                                || snapshot.getValue(String.class)
                                .equals(context.getString(R.string.roleAdmin)))
                            actionsAfterChecked.run();
                        else
                            Toast.makeText(
                                    context
                                    , errorMessageID
                                    , Toast.LENGTH_SHORT)
                                    .show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    public static String generationPersonalUID()
    {
        return (Build.BRAND
                + (Build.BRAND.hashCode() / 2) + ""
                + System.getProperty("os.version") + ""
                + (android.os.Build.DEVICE.hashCode() % 2 - 5) + ""
                + (android.os.Build.MODEL.hashCode() * 3 + 7)
                + (Build.DISPLAY.hashCode() + Build.ID.hashCode()))
                .replaceAll("-", "").replaceAll("\\.", "");
    }

    public void checkSubscriptionAccessRights(
      @NonNull String globalUserID
     , @NonNull Runnable globalProfileUserFragment)
    {
        generalUserReference
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                       if (snapshot.child(MY_SUBSCRIBERS).hasChild(globalUserID))
                       {
                           if (snapshot.child(PERSONAL_USERS)
                                   .child(generationPersonalUID())
                                   .child(FRIENDS_LIST)
                                   .hasChild(globalUserID))
                               globalProfileUserFragment.run();
                           else
                               Toast.makeText(context
                                   , context.getString(R.string.warningSendMessageUser)
                                   , Toast.LENGTH_LONG)
                                   .show();
                       }
                       else
                           Toast.makeText(context
                               , "Данное действие недоступно, пользователь \""
                                       + globalUserID + "\" - не подписан на вас"
                               , Toast.LENGTH_LONG)
                               .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    public void getListFriends(
     @NonNull CustomHeaderListView listView
     , @NonNull List<GlobalUserInfoEntity> users_list
     , boolean operation)
    {
        users_list.clear();
        final Query resultQuery;

        final ValueEventListener eventListenerGetListFriends = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                final ThemeAppController themeAppController =
                        new ThemeAppController(
                                new DataInterfaceCard(context));

                for (DataSnapshot singleSnapshot
                        : dataSnapshot.getChildren())
                {
                  final String user_uid = operation ?
                          singleSnapshot.getValue(String.class).split("com/")[1]
                          : singleSnapshot.getValue(String.class);

                  final DatabaseReference referenceRecognition = generalUserReference
                            .child(user_uid)
                            .child(PERSONAL_USERS);

                    referenceRecognition.addListenerForSingleValueEvent(new ValueEventListener()
                     {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot snapshotUserCount)
                         {
                             if (snapshotUserCount.getValue() == null)
                             {
                                 actionCheckNonExistAccount(generalUserReference
                                         , firebaseAuth.getUid()
                                         , singleSnapshot.getKey());
                                 return;
                             }

                             GlobalUserInfoEntity global_userInfoEntity =
                                     new GlobalUserInfoEntity(singleSnapshot.getKey()
                                     , String.valueOf(snapshotUserCount.getChildrenCount())
                                     , user_uid);
                             try
                             {
                                 if (!global_userInfoEntity
                                         .getUserGlobalID()
                                         .equals(users_list.get(users_list.size() - 1).getUserGlobalID()))
                                     users_list.add(global_userInfoEntity);

                                 else if (Integer.parseInt(global_userInfoEntity.getNetworkActions())
                                         > Integer.parseInt(users_list.get(users_list.size() - 1).getNetworkActions()))
                                     users_list.set(users_list.size() - 1, global_userInfoEntity);
                             }
                             catch (ArrayIndexOutOfBoundsException exception)
                             { users_list.add(global_userInfoEntity); }

                             listView.removeAllViewsInLayout();
                             listView.setAdapter(new GlobalUsersFindAdapter(context
                                     , themeAppController
                                     , users_list
                                     , OPERATION_ACCOUNT_INFO));

                             listView.checkEmptyList(false);
                             removeThisListener();
                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError error) { removeThisListener(); }

                         private void removeThisListener()
                         { referenceRecognition.removeEventListener(this); }
                     });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        if (!operation) resultQuery = generalUserReference
                .child(groupLink)
                .child(PERSONAL_USERS)
                .child(generationPersonalUID())
                .child(FRIENDS_LIST)
                .orderByKey();

        else resultQuery = generalUserReference
                .child(groupLink)
                .child(MY_SUBSCRIBERS)
                .orderByKey();

        resultQuery
                .addListenerForSingleValueEvent(eventListenerGetListFriends);
    }

    public void checkSelectedUser(
      @NonNull AppCompatTextView btnSubscribe
      , @NonNull String userId)
    {
        final DatabaseReference checkSelectedUserRef = generalUserReference.child(groupLink)
                .child(PERSONAL_USERS)
                .child(generationPersonalUID())
                .child(FRIENDS_LIST);

        final ValueEventListener eventListenerCheckSelectedUser = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    if (Objects.requireNonNull(
                            dataSnapshot.getKey()).contains(userId))
                    {
                        btnSubscribe.setText("Отписаться");
                        break;
                    }
                } removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { removeThisListener(); }

            private void removeThisListener()
            { checkSelectedUserRef.removeEventListener(this); }
        };

        checkSelectedUserRef
                .addListenerForSingleValueEvent(eventListenerCheckSelectedUser);
    }

    public void addNewFriend(
      @NonNull String userId
      , @NonNull String userUid
      , @NonNull AppCompatTextView button_block
      , @NonNull AppCompatTextView btnInfoSubscribe)
    {
        if (button_block
                .getText()
                .toString()
                .equals("Заблокировать"))
        {
            final Object object_user, object_subscriber;
            final Map<String, Object> push_subscribe = new HashMap<>();
            final Map<String, Object> push_subscriber = new HashMap<>();

            object_user = userUid;
            object_subscriber = GlobalDataFBManager.LINK_FIREBASE_STORAGE + groupLink;

            push_subscribe.put(userId, object_user);
            push_subscriber.put(sharedPreferencesManager.account_id(null), object_subscriber);

            final OnCompleteListener<Void> completeListenerAddNewFriend = task ->
            {
                if (task.isSuccessful())
                {
                    final OnCompleteListener<Void> completeListener = task1 ->
                    {
                        if (task1.isSuccessful())
                        {
                            Toast.makeText(context
                                    , "Подписка оформлена"
                                    , Toast.LENGTH_SHORT)
                                    .show();
                            btnInfoSubscribe.setText("Отписаться");
                        }
                        else
                            Toast.makeText(context
                                , "Произошла ошибка - повторите попытку заного"
                                , Toast.LENGTH_SHORT)
                                .show();

                        push_subscribe.clear();
                        push_subscriber.clear();
                    };

                    generalUserReference.child(userUid)
                            .child(MY_SUBSCRIBERS)
                            .updateChildren(push_subscriber)
                            .addOnCompleteListener(completeListener);
                }
                else Toast.makeText(context
                        , "Произошла ошибка - повторите попытку заного"
                        , Toast.LENGTH_SHORT).show();
            };

            generalUserReference.child(groupLink)
                    .child(PERSONAL_USERS)
                    .child(generationPersonalUID())
                    .child(FRIENDS_LIST)
                    .updateChildren(push_subscribe)
                    .addOnCompleteListener(completeListenerAddNewFriend);
        }
        else Toast.makeText(context
                , "Произошла ошибка - вы заблокировали данного пользователя"
                , Toast.LENGTH_SHORT).show();
    }

    public void removeUserFriend(
     @NonNull String userId
     , @NonNull String globalUserUid
     , @NonNull AppCompatTextView btnSubscribe)
    {
        final OnCompleteListener<Void> completeListenerRemoveUserFriend = task ->
        {
            if (task.isSuccessful())
            {
                generalUserReference.child(globalUserUid)
                        .child(MY_SUBSCRIBERS)
                        .child(sharedPreferencesManager.account_id(null))
                        .removeValue()
                        .addOnCompleteListener(task1 ->
                        {
                            if (task1.isSuccessful())
                            {
                                Toast.makeText(context
                                        , "Подписка отменена"
                                        , Toast.LENGTH_SHORT).show();
                                btnSubscribe.setText("Подписаться");
                            }
                            else
                                Toast.makeText(context
                                    , "Произошла ошибка - повторите попытку заного"
                                    , Toast.LENGTH_SHORT)
                                        .show();
                        });
            }
            else Toast.makeText(context
                    , "Произошла ошибка - повторите попытку заного"
                    , Toast.LENGTH_SHORT).show();
        };

        generalUserReference.child(groupLink)
                .child(PERSONAL_USERS)
                .child(generationPersonalUID())
                .child(FRIENDS_LIST)
                .child(userId)
                .removeValue()
                .addOnCompleteListener(completeListenerRemoveUserFriend);
    }

    public void addInBlackList(
      @NonNull String userUid
      , @NonNull String userId
      , @NonNull AppCompatTextView buttonAdd
      , @NonNull AppCompatTextView buttonSubscribe)
    {
        final DatabaseReference completeStartBlackListRef = generalUserReference
                .child(userUid)
                .child(PERSONAL_USERS);

        final HashMap<String, Object> data_add = new HashMap<>();
        data_add.put(userId, "");

        final OnCompleteListener<Void> completeListenerAddInBlackList = task ->
        {
            if (task.isSuccessful())
            {
                final ValueEventListener completeValueListenerBlackList = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
                    {
                        final String get_accountID = sharedPreferencesManager.account_id(null);

                        for (DataSnapshot singleSnapshot : snapshot.getChildren())
                            if (singleSnapshot.child(FRIENDS_LIST).hasChild(get_accountID))
                                singleSnapshot.child(FRIENDS_LIST)
                                        .child(get_accountID)
                                        .getRef()
                                        .removeValue();

                        removeUserFriend(userId
                                , userUid
                                , buttonSubscribe);

                        Toast.makeText(context
                                , "Пользователь успешно заблокирован"
                                , Toast.LENGTH_SHORT).show();

                        buttonAdd.setText("Разблокировать");
                        data_add.clear();
                        removeThisListener();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) { removeThisListener(); }

                    private void removeThisListener()
                    { completeStartBlackListRef.removeEventListener(this); }
                };

                completeStartBlackListRef
                        .addListenerForSingleValueEvent(completeValueListenerBlackList);
            }
            else Toast.makeText(context
                    , "Произошла ошибка - повторите попытку заного"
                    , Toast.LENGTH_SHORT).show();
        };

        generalUserReference.child(groupLink)
                .child(BLACK_LIST)
                .updateChildren(data_add)
                .addOnCompleteListener(completeListenerAddInBlackList);
    }

    public void removeFromBlackList(
      @NonNull String userId
      , @NonNull AppCompatTextView buttonAdd)
    {
        final OnCompleteListener<Void> completeListenerRemoveFromBlackList = task ->
        {
            if (task.isSuccessful())
            {
                Toast.makeText(
                        context
                        , "Пользователь успешно разблокирован"
                        , Toast.LENGTH_SHORT)
                        .show();
                buttonAdd.setText("Заблокировать");
            }
            else
                Toast.makeText(context
                    , "Произошла ошибка - повторите попытку заного"
                    , Toast.LENGTH_SHORT)
                    .show();
        };

       generalUserReference.child(groupLink)
             .child(BLACK_LIST)
             .child(userId)
             .removeValue()
             .addOnCompleteListener(completeListenerRemoveFromBlackList);
    }

    public void checkUserInBlackListStageSecond(
     @NonNull String userId
     , @NonNull AppCompatTextView buttonAdd)
    {
        final DatabaseReference checkUserInBlackListOwnerRef = generalUserReference
                .child(groupLink)
                .child(BLACK_LIST);

        final ValueEventListener eventListenerCheckUserInBlackListOwner = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if (snapshot.hasChild(userId))
                    buttonAdd.setText("Разблокировать");
                removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error)
            { removeThisListener(); }

            private void removeThisListener()
            { checkUserInBlackListOwnerRef.removeEventListener(this); }
        };

        checkUserInBlackListOwnerRef
                .addListenerForSingleValueEvent(eventListenerCheckUserInBlackListOwner);
    }

    public void checkUserInBlackListStageFirst(String user_uid
            , FragmentActivity input_fragment
            , GlobalProfileUserFragment nextOpenFragment)
    {
        final DatabaseReference checkUserInBlackListGuestRef = generalUserReference
                .child(user_uid)
                .child(BLACK_LIST);

        final ValueEventListener eventListenerCheckUserInBlackListGuest = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if (snapshot.hasChild(sharedPreferencesManager.account_id(null)))
                    Toast.makeText(context
                            , context.getString(R.string.warningBlockAccountCheck)
                            , Toast.LENGTH_SHORT).show();
                else
                    input_fragment
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                            .replace(R.id.main_linear, nextOpenFragment)
                            .commit();

                removeThisListener();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { removeThisListener(); }

            private void removeThisListener()
            { checkUserInBlackListGuestRef.removeEventListener(this); }
        };

        checkUserInBlackListGuestRef
                .addListenerForSingleValueEvent(eventListenerCheckUserInBlackListGuest);
    }

    synchronized public static void setRealDesignCard(@NonNull String cardName
     , @NonNull RoundRectCornerImageView roundRectCornerImageView
     , @NonNull SurfaceView surfaceViewClear
     , @NonNull ArrayList<TextView> viewInCard
     , @NonNull AppCompatButton btnSetResult)
    {
        final FirebaseStorage storage =
                FirebaseStorage.getInstance();
        final StorageReference storageRef = storage
                .getReferenceFromUrl(LINK_DESIGN_CARDS);

        storageRef.listAll()
                .addOnSuccessListener(listResult ->
        {
            final CustomAppDialog loadingDialog =
                    new CustomAppDialog(btnSetResult.getContext());
            loadingDialog.showLoadingDialog(
                    true, btnSetResult.getContext().getString
                            (R.string.loadingInfoCardDesign));

            for (StorageReference singleListResult : listResult.getItems())
            {
                if (percentageSimilarityResult(
                        transliterateCardName(cardName) + ".jpg"
                        ,  singleListResult.getName()) >= 75)
                {
                    storageRef.child(singleListResult.getName())
                            .getDownloadUrl()
                            .addOnSuccessListener(request ->
                            {
                                surfaceViewClear.setScaleY(0f);
                                surfaceViewClear.setScaleX(0f);

                                roundRectCornerImageView.setScaleX(1f);
                                viewInCard.get(0).setScaleX(0f);

                                new MasterDesignCard(btnSetResult.getContext(), viewInCard)
                                        .setCardDesignNetwork(
                                                request.toString()
                                                , cardName
                                                , roundRectCornerImageView
                                                , btnSetResult);
                                loadingDialog.showLoadingDialog(false, null);
                            });
                    return;
                }
            }
            loadingDialog.showLoadingDialog(false, null);
            Toast.makeText(btnSetResult.getContext()
                    , R.string.warningHaveAltDesign
                    , Toast.LENGTH_LONG)
                    .show();
        });
    }

    public static void downloadAllAltDesignCards(
      @NonNull List<String> allLocaleDataName
     , @NonNull CustomHeaderListView updateList
     , @NonNull CardManagerDB cardManagerDB)
    {
        final MasterDesignCard masterDesignCard =
                new MasterDesignCard(updateList.getContext()
                        , cardManagerDB
                        , updateList);

        FirebaseStorage.getInstance()
                .getReferenceFromUrl(LINK_DESIGN_CARDS)
                .listAll()
                .addOnSuccessListener(listResult ->
                {
                    final int localeListSize = allLocaleDataName.size()
                            , globalListSize = listResult.getItems().size();
                    final List<StorageReference> allGlobalDataName =
                            new ArrayList(listResult.getItems());

                    for (int localeIndex = 0; localeIndex < localeListSize; localeIndex++)
                    {
                        for (int globaleIndex = 0; globaleIndex < globalListSize; globaleIndex++)
                        {
                            if (percentageSimilarityResult(
                                    transliterateCardName(allLocaleDataName.get(localeIndex))
                                    , allGlobalDataName
                                            .get(globaleIndex)
                                            .getName()
                                            .replaceFirst(".jpg", "")) >= 75)
                            {
                                final int finalLocaleIndex = localeIndex;
                                allGlobalDataName
                                        .get(globaleIndex)
                                        .getDownloadUrl()
                                        .addOnSuccessListener(getLink ->
                                                masterDesignCard.setCardDesignNetwork(
                                                        getLink.toString()
                                                        , allLocaleDataName.get(finalLocaleIndex)
                                                        , null
                                                        , null));
                            }
                        }
                    }
                });
    }
}
