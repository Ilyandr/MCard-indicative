package com.example.mcard.GroupServerActions;

import static com.example.mcard.AdapersGroup.GlobalUsersFindAdapter.OPERATION_ACCOUNT_INFO;
import static com.example.mcard.GeneralInterfaceApp.MasterDesignCard.removeCacheApp;
import static com.example.mcard.GroupServerActions.SubscribeController.SUBSCRIBE_DATA;
import static com.example.mcard.GroupServerActions.SyncPersonalManager.PERSONAL_USERS;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mcard.AdapersGroup.BasicCardManagerAdapter;
import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.AdapersGroup.GlobalUsersFindAdapter;
import com.example.mcard.BasicAppActivity;
import com.example.mcard.FragmentsAdditionally.GlobalProfileUserFragment;
import com.example.mcard.FunctionalInterfaces.DelegateVoidInterface;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.SideFunctionality.ListViewComponent;
import com.example.mcard.R;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import com.example.mcard.UserAuthorization.StartAppActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GlobalDataFBManager extends BasicFireBaseManager
{
    public static final String GLOBAL_DATA_NAME = "GENERAL GLOBAL DATA";
    public static final String GLOBAL_LIST_CARD = "CARD LIST";
    public static final String CARDS = "CARDS";
    public static final String GLOBAL_LIST_ALL_USERS = "ALL USERS";
    public static final String STATUS_ACCOUNT_ONLINE = "ACTIVE USERS NOW";

    public static final char MODE_ONLINE_GET = 1;
    public static final char MODE_ONLINE_POST = 2;
    public static final char MODE_EXIT_ACCOUNT = 3;
    public static final char MODE_PUSH_REQUEST = 4;
    public static final char MODE_PUSH_IMPORT = 5;

    public static final String LINK_FIREBASE_STORAGE = "gs://mcarddb-3d9fd.appspot.com/";
    private final String LINK_DEFAULT_PHOTO_PROFILE =
            "https://firebasestorage.googleapis.com/v0/b/" +
            "mcarddb-3d9fd.appspot.com/o/" +
            "defaultGlobalUserIcon.png?alt=media" +
            "&token=ffdd21e1-c761-473e-940d-8042d59546cd";

    private final DatabaseReference generalGlobalRef;

    public GlobalDataFBManager(Context context)
    {
        super(context);
        this.generalGlobalRef = firebase_connection
                .getReference(GLOBAL_DATA_NAME);
    }

    public synchronized void getGlobalCardList(
     @NonNull CustomHeaderListView listView
     , @NonNull ListViewComponent listViewComponent)
    {
        final DatabaseReference getGlobalCardListRef = generalGlobalRef
                .child(GLOBAL_LIST_CARD)
                .child(CARDS);

        final List<CardInfoEntity> globalCardInfo = new LinkedList<>();
        ValueEventListener childListenerGetGlobalCardList = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                    globalCardInfo.add(singleSnapshot.getValue(CardInfoEntity.class));

                listView.setAdapter(new BasicCardManagerAdapter(
                        context, globalCardInfo, true));
                listViewComponent.createItemInList(listView);

                listView.checkEmptyList(false);
                removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                removeThisListener();
            }

            private void removeThisListener() {
                getGlobalCardListRef.removeEventListener(this);
            }
        };

        getGlobalCardListRef.addListenerForSingleValueEvent(
                childListenerGetGlobalCardList);
    }

    public static synchronized void checkAvaibleAccount(
      @NonNull Context context
      , @NonNull Runnable successActionAfter)
    {
        final SharedPreferencesManager sharedPreferencesManager =
                new SharedPreferencesManager(context);
        try
        {
            FirebaseDatabase
                    .getInstance()
                    .getReference(context.getString(R.string.FB_TABLE_NAME))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            if (!snapshot.hasChild(ACCOUNT_ID))
                                removedAction(context);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error)
                        { removedAction(context); }
                    });
            successActionAfter.run();
        } catch (NullPointerException accountDeleted)
        { removedAction(context); }
    }

    private static void removedAction(@NonNull Context context)
    {
       new CustomAppDialog(context)
                .buildEntityDialog(true)
                .setTitle(context.getString(R.string.dialogInfo))
                .setMessage(R.string.infoRemovedAccount, 3.25f)
                .setPositiveButton(context.getString(R.string.dialogButtonExit), (v) ->
                {
                    new DataInterfaceCard(context).removeAllData();
                    new CardManagerDB(context).removeDatabase();
                    removeCacheApp(context.getCacheDir());

                    FirebaseAuth.getInstance().signOut();
                    new SharedPreferencesManager(context).removeAllData();

                    context.startActivity(new Intent(context
                            , StartAppActivity.class));
                    ((BasicAppActivity) context).finish();
                }).setNegativeButton("", null)
                .setAdditionalButton("", null)
                .offExitActionPermission(true)
                .show();
    }

    public synchronized void pushCardToFB(
     @NonNull CardInfoEntity cardInfoEntity
     , @NonNull DelegateVoidInterface operationRefreshCard
     , @NonNull AppCompatTextView buttonPush)
    {
        final DatabaseReference pushCardToFBRef = generalGlobalRef
                .child(GLOBAL_LIST_CARD)
                .child(CARDS);

        final ValueEventListener eventListenerPushCardToFB = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                cardInfoEntity.setCardOwner(
                        sharedPreferencesManager.account_id(null));

                final Map<String, Object> map = new HashMap<>();
                map.put(cardInfoEntity.getUniqueIdentifier(), cardInfoEntity);

                final OnCompleteListener<Void> completeListenerPublish = task ->
                {
                    if (task.isSuccessful())
                    {
                        if (task.isSuccessful())
                        {
                            updateScorePublicCard(true);
                            operationRefreshCard.delegateFunction(cardInfoEntity);
                            buttonPush.setText("Отменить публикацию");
                        }
                        map.clear();
                    }
                    else
                        Toast.makeText(context
                            , "Возникла ошибка - повторите попытку заного"
                            , Toast.LENGTH_SHORT)
                            .show();
                };

                generalGlobalRef.child(GLOBAL_LIST_CARD)
                        .child(CARDS)
                        .updateChildren(map)
                        .addOnCompleteListener(completeListenerPublish);
                removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                removeThisListener();
            }

            private void removeThisListener() {
                pushCardToFBRef.removeEventListener(this);
            }
        };

        pushCardToFBRef.addListenerForSingleValueEvent(
                eventListenerPushCardToFB);
    }

    public synchronized void checkHaveCardInFB(
     @NonNull CardInfoEntity cardInfoEntity
     , @NonNull AppCompatTextView buttonPushCard)
    {
        if (!Objects.equals(sharedPreferencesManager.account_id(null)
                , cardInfoEntity.getCardOwner())
                && cardInfoEntity.getCardOwner() != null)
        {
            buttonPushCard.setOnClickListener((view -> Toast.makeText(context
                    , context.getString(R.string.messageWarningOwnerCard)
                    , Toast.LENGTH_SHORT).show()));
            return;
        }

        final DatabaseReference checkHaveCardInFBRef = generalGlobalRef
                .child(GLOBAL_LIST_CARD)
                .child(CARDS);

        final ValueEventListener eventListenerCheckHaveCardInFB = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.hasChild(
                        cardInfoEntity.getUniqueIdentifier()))
                    buttonPushCard.setText("Отменить публикацию");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                removeThisListener();
            }

            private void removeThisListener() {
                checkHaveCardInFBRef.removeEventListener(this);
            }
        };

        checkHaveCardInFBRef
                .addListenerForSingleValueEvent(eventListenerCheckHaveCardInFB);
    }

    public void removeCardInFB(
     @NonNull CardInfoEntity cardInfoEntity
     , @Nullable DelegateVoidInterface operationRefreshCard
     , @Nullable AppCompatTextView buttonPush)
    {
        final String thisAccountID =
                sharedPreferencesManager.account_id(null);

        if (!thisAccountID.equals(
                cardInfoEntity.getCardOwner())
                || buttonPush == null
                || operationRefreshCard == null)
            return;

        final DatabaseReference deleteCardInFBRef = generalGlobalRef
                .child(GLOBAL_LIST_CARD)
                .child(CARDS);

        final ValueEventListener valueEventListenerDeleteCardInFB = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if (snapshot.hasChild(
                        cardInfoEntity.getUniqueIdentifier()))
                {
                    final OnCompleteListener<Void> completeDeleteListener = task ->
                    {
                        if (buttonPush != null && operationRefreshCard != null)
                        {
                            if (task.isSuccessful())
                            {
                                cardInfoEntity.setCardOwner(null);
                                operationRefreshCard.delegateFunction(cardInfoEntity);

                                updateScorePublicCard(false);
                                buttonPush.setText("Поделиться");
                            }
                            else
                                Toast.makeText(context
                                    , "Возникла ошибка - повторите попытку заного"
                                    , Toast.LENGTH_SHORT)
                                    .show();
                        }
                    };

                    generalGlobalRef.child(GLOBAL_LIST_CARD)
                            .child(CARDS)
                            .child(cardInfoEntity.getUniqueIdentifier())
                            .removeValue()
                            .addOnCompleteListener(completeDeleteListener);
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { }
        };

        deleteCardInFBRef.addListenerForSingleValueEvent(
                valueEventListenerDeleteCardInFB);
    }

    public synchronized void updateLocaleOwnerInfo(
            @NonNull String updateCardInfoEntityID)
    {
        final DatabaseReference refAllUsersCards = this.generalUserReference
                .child(this.firebaseAuth.getUid())
                .child(this.USERS_INFO);

        final ValueEventListener listenerRefAllUsersCards = (new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotAllCards)
            {
                for (DataSnapshot singleUserCardEntity : snapshotAllCards.getChildren())
                    if (singleUserCardEntity.child("uniqueIdentifier")
                            .getValue(String.class).
                                    equals(updateCardInfoEntityID))
                        refAllUsersCards.child(updateCardInfoEntityID)
                                .child("uniqueIdentifier")
                                .setValue(sharedPreferencesManager.account_id(null))
                                .addOnCompleteListener((task) ->
                                        refAllUsersCards.removeEventListener(this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                refAllUsersCards.removeEventListener(this);
            }
        });

        refAllUsersCards.addListenerForSingleValueEvent(listenerRefAllUsersCards);
    }

    synchronized public void getCardOwner(
     @NonNull String cardInfoEntityID
     , @NonNull DelegateVoidInterface delegateFun)
    {
        final DatabaseReference getCardOwnerRef = generalGlobalRef
                .child(GLOBAL_LIST_CARD)
                .child(CARDS);

        final ValueEventListener eventListenerGetCardOwner = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if (snapshot.hasChild(cardInfoEntityID))
                {
                    final String cardOwner = snapshot
                            .child(cardInfoEntityID)
                            .getValue(CardInfoEntity.class)
                            .getCardOwner();

                    final DatabaseReference completeGetOwnerRef = generalGlobalRef
                            .child(GLOBAL_LIST_ALL_USERS)
                            .child(cardOwner);

                    final ValueEventListener completeListenerGetOwnerRef = new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
                        {
                            final String accountUID = snapshot
                                    .getValue()
                                    .toString()
                                    .replace(LINK_FIREBASE_STORAGE, "");
                            if (accountUID == null) return;

                            final DatabaseReference singleAccountInfo = generalUserReference
                                    .child(accountUID);

                            final ValueEventListener listenerSingleAccountInfo = new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot)
                                {
                                    delegateFun.delegateFunction(
                                            new GlobalUserInfoEntity(
                                                    cardOwner
                                                    , String.valueOf(snapshot.child(PERSONAL_USERS).getChildrenCount())
                                                    , accountUID));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            };

                            singleAccountInfo
                                    .addListenerForSingleValueEvent(listenerSingleAccountInfo);
                            removeThisListener();
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            removeThisListener();
                        }

                        private void removeThisListener() {
                            completeGetOwnerRef.removeEventListener(this);
                        }
                    };

                    completeGetOwnerRef
                            .addListenerForSingleValueEvent(completeListenerGetOwnerRef);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) { }
        };

        getCardOwnerRef
                .addListenerForSingleValueEvent(eventListenerGetCardOwner);
    }

    public void loadPhotoProfileToFB(
     @Nullable CustomAppDialog loadingDialog
     , @NonNull Uri photo_patch)
    {
        if (photo_patch != null)
        {
            FirebaseStorage
                    .getInstance()
                    .getReference()
                    .child(Objects.requireNonNull(FirebaseAuth
                            .getInstance()
                            .getCurrentUser())
                            .getUid())
                    .putFile(photo_patch)
                    .addOnSuccessListener(taskSnapshot ->
                    {
                        sharedPreferencesManager.push_data_profile("");
                        Toast.makeText(context
                                , context.getString(R.string.SuccessUpdatePhoto)
                                , Toast.LENGTH_SHORT).show();
                        if (loadingDialog != null)
                            new Handler(
                                    Looper.getMainLooper()).post(() ->
                                    loadingDialog.showLoadingDialog(false, null));
                    })
                    .addOnFailureListener(e ->
                            {
                                if (loadingDialog != null)
                                    new Handler(
                                            Looper.getMainLooper()).post(() ->
                                            loadingDialog.showLoadingDialog(false, null));
                                Toast.makeText(context
                                        , context.getString(R.string.FaultLoadPhoto)
                                        , Toast.LENGTH_SHORT).show();
                            }
                    );
        }
    }

    public void setGlobalUserPhoto(
     @NonNull GlobalProfileUserFragment.UserIconLayout imageView
     , @NonNull String globalUserId
     , @NonNull ProgressBar loadingView)
    {
        this.generalGlobalRef.child(GLOBAL_LIST_ALL_USERS)
                .child(globalUserId)
                .get()
                .addOnCompleteListener(getGlobalUserDataUID ->
                {
                    try
                    {
                        final String getGlobalUserUID = getGlobalUserDataUID
                                .getResult()
                                .getValue(String.class);

                        if (getGlobalUserUID == null)
                            return;

                        this.generalUserReference.child(getGlobalUserUID)
                                .child(SUBSCRIBE_DATA)
                                .get()
                                .addOnCompleteListener(getGlobalUserSubscribeData ->
                                {
                                    final boolean haveSubscribeDataGlobalUser = !(getGlobalUserSubscribeData
                                            .getResult()
                                            .getValue(String.class)
                                            .equals("-"));

                                    FirebaseStorage.getInstance()
                                            .getReferenceFromUrl(LINK_FIREBASE_STORAGE)
                                            .child(getGlobalUserUID)
                                            .getDownloadUrl()
                                            .addOnSuccessListener(uri -> loadProcess(uri
                                                    , imageView
                                                    , loadingView
                                                    , haveSubscribeDataGlobalUser))
                                            .addOnFailureListener(e -> loadProcess(Uri.parse(LINK_DEFAULT_PHOTO_PROFILE)
                                                    , imageView
                                                    , loadingView
                                                    , haveSubscribeDataGlobalUser));
                                });
                    } catch (RuntimeException runtimeException) {}
                });
    }

    private void loadProcess(Uri imageUri
            , GlobalProfileUserFragment.UserIconLayout imageLoad
            , ProgressBar loadingView
            , boolean haveSubscribe)
    {
        if (haveSubscribe)
            imageLoad.setColorBorder(
                    Color.parseColor("#FFD700"));

        Glide.with(((BasicAppActivity) context).getBaseContext())
                .load(imageUri)
                .skipMemoryCache(true)
                .listener(new RequestListener<>()
                {
                    @Override
                    public boolean onException(Exception e
                            , Uri model
                            , Target<GlideDrawable> target
                            , boolean isFirstResource)
                    { return false; }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource
                            , Uri model
                            , Target<GlideDrawable> target
                            , boolean isFromMemoryCache
                            , boolean isFirstResource)
                    {
                        loadingView.setScaleX(0f);
                        loadingView.clearAnimation();
                        return false;
                    }
                }).into(imageLoad);
    }

    synchronized public void findUsersByID(@Nullable String inputQueryID
            , @NonNull CustomHeaderListView listView
            , @NonNull List<GlobalUserInfoEntity> usersList)
    {
        usersList.clear();
        final Query resultQueryRef = (inputQueryID != null) ?
                generalGlobalRef.child(GLOBAL_LIST_ALL_USERS)
                .orderByKey()
                .startAt(inputQueryID)
                .endAt(inputQueryID + "\uf8ff")
                : generalGlobalRef.child(GLOBAL_LIST_ALL_USERS)
                .limitToFirst(30);

        final ValueEventListener eventListenerFindUsersByID = new ValueEventListener()
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
                    final String user_uid = singleSnapshot.getValue(String.class);

                    final DatabaseReference completeResultQueryRef = generalUserReference
                            .child(user_uid)
                            .child(PERSONAL_USERS);

                    final ValueEventListener eventListenerCompleteResultQueryRef = new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            final GlobalUserInfoEntity global_userInfoEntity =
                                    new GlobalUserInfoEntity(singleSnapshot.getKey()
                                    , String.valueOf(snapshot.getChildrenCount())
                                    , user_uid);
                            try
                            {
                                if (!global_userInfoEntity.getUserGlobalID()
                                        .equals(usersList.get(usersList.size() - 1).getUserGlobalID()))
                                    usersList.add(global_userInfoEntity);

                                else if (Integer.parseInt(global_userInfoEntity.getNetworkActions())
                                        > Integer.parseInt(usersList.get(usersList.size() - 1)
                                        .getNetworkActions()))
                                    usersList.set(usersList.size() - 1, global_userInfoEntity);
                            }
                            catch (ArrayIndexOutOfBoundsException exception)
                            { usersList.add(global_userInfoEntity); }

                            listView.removeAllViewsInLayout();
                            listView.setAdapter(new GlobalUsersFindAdapter(context
                                    , themeAppController
                                    , usersList
                                    , OPERATION_ACCOUNT_INFO));

                            listView.checkEmptyList(false);
                            removeThisListener();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { removeThisListener(); }

                        private void removeThisListener()
                        { completeResultQueryRef.removeEventListener(this); }
                    };

                    completeResultQueryRef
                            .addListenerForSingleValueEvent(eventListenerCompleteResultQueryRef);
                }
                removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { removeThisListener(); }
            private void removeThisListener() { resultQueryRef.removeEventListener(this); }
        };

        resultQueryRef
                .addListenerForSingleValueEvent(eventListenerFindUsersByID);
    }

    public void requestCardsActions(@NonNull String userInputUID
     , @NonNull String userInputID
     , @NonNull CardInfoEntity inputDataCard
     , char inputMode)
    {
        final DatabaseReference acceptInputRequestCardRef = generalUserReference
                .child((userInputUID == null) ? firebaseAuth.getUid() : userInputUID)
                .child(USERS_INFO);

        final ValueEventListener childListenerAcceptInputRequestCard = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                try
                {
                    for (DataSnapshot singleUserCardEntity : snapshot.getChildren())
                        if (singleUserCardEntity.child("uniqueIdentifier")
                                .getValue(String.class).
                                        equals(inputDataCard.getUniqueIdentifier()))
                        {
                            Toast.makeText(context
                                    , context.getString(R.string.warningCheckGlobalRCard)
                                    , Toast.LENGTH_LONG).show();
                            return;
                        }
                } catch (NullPointerException emptyCardsList) {}

                if (inputMode == MODE_PUSH_REQUEST)
                {
                    if (inputDataCard.getCardOwner() == null)
                        inputDataCard.setCardOwner(
                                sharedPreferencesManager.account_id(null));
                    else if (!inputDataCard.getCardOwner()
                            .equals(sharedPreferencesManager.account_id(null)))
                    {
                        Toast.makeText(context
                                , context.getString(R.string.messageWarningOwnerCard)
                                , Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                final HashMap<String, Object> pushDataCard =
                        new HashMap<>();
                pushDataCard.put(
                        inputDataCard.getUniqueIdentifier(), inputDataCard);

                final OnCompleteListener<Void> completeListenerAcceptInputRequestCard = task ->
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(context
                                , context.getString((inputMode == MODE_PUSH_REQUEST)
                                        ? R.string.successAcceptRequest : R.string.SuccessImport)
                                , Toast.LENGTH_SHORT).show();

                        if (inputMode == MODE_PUSH_REQUEST)
                        generalUserReference.child(Objects.requireNonNull(FirebaseAuth
                                .getInstance()
                                .getCurrentUser())
                                .getUid())
                                .child(CARD_OFFER)
                                .child(userInputID)
                                .removeValue()
                                .addOnCompleteListener(task1 -> pushDataCard.clear());
                        else pushDataCard.clear();
                    }
                    else
                    {
                        Toast.makeText(context
                             , "Произошла ошибка - повторите попытку заного"
                             , Toast.LENGTH_SHORT).show();
                        errorAction();
                    }
                };

                acceptInputRequestCardRef
                        .updateChildren(pushDataCard)
                        .addOnCompleteListener(completeListenerAcceptInputRequestCard);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { errorAction(); }

            private void errorAction()
            { acceptInputRequestCardRef.removeEventListener(this); }
        };

        acceptInputRequestCardRef
                .addListenerForSingleValueEvent(childListenerAcceptInputRequestCard);
    }

    public static void actionCheckNonExistAccount(
            DatabaseReference databaseReference, String thisUserUID, String accoutID)
    {
        databaseReference.child(thisUserUID)
                .child(MY_SUBSCRIBERS)
                .child(accoutID)
                .removeValue();
    }

    public void getListUserSubscribers(
            String singleUserUID, CustomHeaderListView listView)
    {
        final List<GlobalUserInfoEntity> globalUserInfoEntities =
                new LinkedList<>();
        final DatabaseReference getListUserSubscribersRef = generalUserReference
                .child(singleUserUID)
                .child(MY_SUBSCRIBERS);

        final ValueEventListener childListenerGetListUserSubscribers = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                final ThemeAppController themeAppController =
                        new ThemeAppController(
                                new DataInterfaceCard(context));

                for (DataSnapshot singleUserInfo : snapshot.getChildren())
                    generalUserReference.child(singleUserInfo
                            .getValue(String.class)
                            .split("com/")[1])
                            .child(PERSONAL_USERS)
                            .addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot_userCount)
                                {
                                    if (snapshot_userCount.getValue() == null)
                                    {
                                        actionCheckNonExistAccount(generalUserReference
                                                , singleUserUID
                                                , singleUserInfo.getKey());
                                        return;
                                    }

                                    globalUserInfoEntities.add(new GlobalUserInfoEntity(singleUserInfo.getKey()
                                            , String.valueOf(snapshot_userCount.getChildrenCount())
                                            , singleUserInfo.getValue().toString()));

                                    listView.setAdapter(new GlobalUsersFindAdapter(context
                                            , themeAppController
                                            , globalUserInfoEntities
                                            , OPERATION_ACCOUNT_INFO));
                                    listView.checkEmptyList(false);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            { globalUserInfoEntities.clear(); }
        };

        getListUserSubscribersRef
                .addListenerForSingleValueEvent(childListenerGetListUserSubscribers);
    }

    public void find_similarCards(CardInfoEntity data_singleCard
            , CustomHeaderListView listView
            , ListViewComponent listViewComponent)
    {
        final List<CardInfoEntity> similar_cards = new LinkedList<>();
        final String name_card = data_singleCard.getName();

        listViewComponent.registerListView();

        final DatabaseReference findSimilarCardsRef = generalGlobalRef
                .child(GLOBAL_LIST_CARD)
                .child(CARDS);

        final ValueEventListener eventListenerFindSimilarCards = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                    if ((Objects.requireNonNull(singleSnapshot
                            .child("name")
                            .getValue(String.class))
                            .equals(name_card))
                            && singleSnapshot.getValue(CardInfoEntity.class).hashCode() != data_singleCard.hashCode())
                        similar_cards.add(singleSnapshot.getValue(CardInfoEntity.class));

                listView.setAdapter(new BasicCardManagerAdapter(
                        context, similar_cards, true));
                listView.checkEmptyList(true);
                listViewComponent.createItemInList(listView);

                removeThisListener();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error)
            { removeThisListener(); }

            private void removeThisListener()
            { findSimilarCardsRef.removeEventListener(this); }
        };

        findSimilarCardsRef
                .addListenerForSingleValueEvent(eventListenerFindSimilarCards);
    }

    public void get_publicUserCardList(String user_id
            , CustomHeaderListView listView
            , ListViewComponent listViewComponent)
    {
        final List<CardInfoEntity> similar_cards = new LinkedList<>();
        listViewComponent.registerListView();

        final DatabaseReference getPublicUserCardListRef = generalGlobalRef
                .child(GLOBAL_LIST_CARD)
                .child(CARDS);

        final ValueEventListener eventListenerGetPublicUserCardList = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                CardInfoEntity singleCardInfoEntity;

                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    singleCardInfoEntity = singleSnapshot
                            .getValue(CardInfoEntity.class);

                    if (singleCardInfoEntity
                            .getCardOwner()
                            .equals(user_id))
                        similar_cards.add(singleCardInfoEntity);
                }

                listView.setAdapter(
                        new BasicCardManagerAdapter(
                                context, similar_cards, false));

                listView.checkEmptyList(true);
                listViewComponent.createItemInList(listView);
                removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error)
            { removeThisListener(); }

            private void removeThisListener()
            { getPublicUserCardListRef.removeEventListener(this); }
        };

        getPublicUserCardListRef
                .addListenerForSingleValueEvent(eventListenerGetPublicUserCardList);
    }

    private synchronized <K, V extends Comparable<?
            super V>> Map<K, V> sortByValue(
                    @NonNull Map<K, V> map, int limit)
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        final Map<K, V> result =
                new LinkedHashMap<>();
        Collections.sort( list, (o1, o2) -> (o2.getValue())
                .compareTo(o1.getValue()));

        if (list.size() > limit)
            list = list.subList(0, limit);

        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey()
                    , entry.getValue());
        return result;
    }
}