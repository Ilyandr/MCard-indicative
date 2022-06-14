package com.example.mcard.GroupServerActions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.CommercialAction.YandexADS.MediationNetworkEntity;
import com.example.mcard.CommercialAction.YandexADS.RewardedMobileMediationManager;
import com.example.mcard.FunctionalInterfaces.NetworkConnection;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GlobalListeners.NetworkListener;
import com.example.mcard.R;
import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.AdapersGroup.GlobalUsersFindAdapter;
import com.example.mcard.BasicAppActivity;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.UserAuthorization.ReceptionAppActivity;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import com.example.mcard.UserAuthorization.StartAppActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.example.mcard.AdapersGroup.GlobalUsersFindAdapter.OPERATION_REQUEST;
import static com.example.mcard.GroupServerActions.GlobalDataFBManager.GLOBAL_DATA_NAME;
import static com.example.mcard.GroupServerActions.GlobalDataFBManager.GLOBAL_LIST_ALL_USERS;
import static com.example.mcard.GroupServerActions.SyncPersonalManager.PERSONAL_USERS;
import static com.example.mcard.GroupServerActions.SyncPersonalManager.PERSONAL_USER_NAME;
import static com.example.mcard.GroupServerActions.SyncPersonalManager.generationPersonalUID;
import static com.example.mcard.GeneralInterfaceApp.MasterDesignCard.removeCacheApp;
import kotlin.Pair;

public class BasicFireBaseManager implements NetworkConnection
 , OnFailureListener
 , OnSuccessListener<AuthResult>
{
    protected final FirebaseAuth firebaseAuth;
    protected final FirebaseDatabase firebase_connection;
    protected final DatabaseReference generalUserReference;
    protected final SharedPreferencesManager sharedPreferencesManager;

    private String getLogin
            , getPassword
            , accountName
            , code_operation;

    protected final Context context;
    private CustomAppDialog customAppDialog;

    protected final String DATA_CARD = "DATA CARDS";
    protected final String USERS_INFO = "USERS INFO";
    private final String DATA_REGISTER = "DATA CREATE ACCOUNT";
    protected final String CARD_OFFER = "CARD_OFFER";
    public static final String MY_SUBSCRIBERS = "SUBSCRIBERS";

    public static final String operation_code_write_inDB = "REGISTER";
    public static final String operation_code_read_fromDB = "AUTHENTICATION";
    public static final String ACCOUNT_ID = "ACCOUNT ID";
    public static final String SCORE_PUBLIC_CARD = "SCORE PUBLIC CARDS";

    public BasicFireBaseManager(
     @Nullable String getLogin
     , @Nullable String getPassword
     , @Nullable String accountName
     , @NonNull Context context)
    {
        this.sharedPreferencesManager =
                new SharedPreferencesManager(context);

        this.getLogin = getLogin;
        this.getPassword = getPassword;
        this.accountName = accountName;
        this.context = context;

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebase_connection = FirebaseDatabase.getInstance();
        this.generalUserReference =
                this.firebase_connection.getReference(
                        context.getString(R.string.FB_TABLE_NAME));
    }

    public BasicFireBaseManager(Context context)
    {
        this.sharedPreferencesManager =
                new SharedPreferencesManager(context);
        this.context = context;

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebase_connection = FirebaseDatabase.getInstance();
        this.generalUserReference =
                this.firebase_connection.getReference(
                        context.getString(R.string.FB_TABLE_NAME));
    }

    public synchronized final void registerInServer(
      @Nullable CustomAppDialog customAppDialog
      , @NonNull String inputOperation)
    {
        this.customAppDialog = customAppDialog;
        this.code_operation = inputOperation;

        this.firebase_connection
                .getReference(GLOBAL_DATA_NAME)
                .child(GlobalDataFBManager.GLOBAL_LIST_ALL_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (!snapshot.hasChild(accountName))
                    firebaseAuth.createUserWithEmailAndPassword(getLogin, getPassword)
                            .addOnSuccessListener(BasicFireBaseManager.this)
                            .addOnFailureListener(BasicFireBaseManager.this);
                else
                {
                    Toast.makeText(context
                            , context.getString(R.string.ErrorAccName)
                            , Toast.LENGTH_SHORT)
                            .show();
                    cancelLoadDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public synchronized final void authenticationInServer(
     @Nullable CustomAppDialog customAppDialog
     , @NonNull String inputOperation
     , @NonNull FragmentActivity callActivity)
    {
        this.code_operation = inputOperation;
        this.customAppDialog = customAppDialog;

        this.firebaseAuth.signInWithEmailAndPassword(
                        getLogin, getPassword)
                .addOnSuccessListener(this)
                .addOnFailureListener(this);
    }

    private synchronized void startBasicActivity()
    {
        this.sharedPreferencesManager.setUserData(getLogin
                , getPassword);

        final Intent startActivity = new Intent(context
                , BasicAppActivity.class);

        startActivity.putExtra("", "");
        this.context.startActivity(startActivity);
        ((AppCompatActivity) this.context).finish();
    }

    public synchronized final void removeAccount(
     @NonNull String passwordConfirm
      , @NonNull FragmentActivity fragmentActivity)
    {
        final String data_user = this.sharedPreferencesManager
                .setUserData(null, null);

        if (passwordConfirm.equals(data_user.split(" ")[1])
                && checkNetwork())
        {
            final CustomAppDialog loadingDialog =
                    new CustomAppDialog(context);
            loadingDialog.showLoadingDialog(
                    true, context.getString(R.string.infoLoadingDeleteAccount));

            FirebaseAuth.getInstance().getCurrentUser()
           .reauthenticate(EmailAuthProvider
           .getCredential(data_user.split(" ")[0]
                   , data_user.split(" ")[1]))
           .addOnSuccessListener(unused ->
           {
               final CardManagerDB cardManagerDB = new CardManagerDB(context);
               final GlobalDataFBManager globalDataFBManager = new GlobalDataFBManager(context);
               final List<CardInfoEntity> allCardData = cardManagerDB
                       .readAllCards();

               for (int i = 0; i < allCardData.size(); i++)
                   globalDataFBManager.removeCardInFB(
                           allCardData.get(i)
                           , null, null);

               this.firebase_connection
                  .getReference(GLOBAL_DATA_NAME)
                  .child(GlobalDataFBManager.GLOBAL_LIST_ALL_USERS)
                  .child(sharedPreferencesManager.account_id(null))
                  .removeValue()
                  .addOnSuccessListener(unused1 ->
               {
                   FirebaseStorage
                           .getInstance()
                           .getReference()
                           .child(FirebaseAuth.getInstance().getUid())
                           .delete();

                   this.generalUserReference.child(
                           FirebaseAuth.getInstance().getUid())
                           .removeValue()
                           .addOnSuccessListener(unused2 ->
                                   FirebaseAuth.getInstance()
                                           .getCurrentUser()
                                           .delete()
                                           .addOnCompleteListener(authRemove ->
                                           {
                                               new DataInterfaceCard(context).removeAllData();
                                               this.sharedPreferencesManager.removeAllData();
                                               new CardManagerDB(context).removeDatabase();
                                               removeCacheApp(this.context.getCacheDir());

                                               loadingDialog.showLoadingDialog(false, null);
                                               Toast.makeText(context
                                                       , "Аккаунт успешно удалён"
                                                       , Toast.LENGTH_LONG)
                                                       .show();

                                               fragmentActivity.startActivity(new Intent(context
                                                       , StartAppActivity.class));
                                               fragmentActivity.finish();
                                           }))
                           .addOnFailureListener(e ->
                           {
                               loadingDialog.showLoadingDialog(false, null);
                               Toast.makeText(context
                                       , context.getString(R.string.errorRemoveAccount)
                                       , Toast.LENGTH_LONG)
                                       .show();
                           });
               });
           }).addOnFailureListener(e -> Toast.makeText(context
                    , "Ошибка - введены неверные данные. Повторите попытку"
                    , Toast.LENGTH_SHORT).show());
        }
        else if (!checkNetwork())
        Toast.makeText(context
                , context.getString(R.string.offlineNetworkMSG)
                , Toast.LENGTH_SHORT).show();

        else
        Toast.makeText(context
                , "Ошибка. Неверный пароль"
                , Toast.LENGTH_SHORT).show();
    }

    protected void exitAccount()
    {
        this.firebaseAuth.signOut();
        removeCacheApp(
                this.context.getCacheDir());
    }

    public synchronized final void getInfoDataRegister(
     @NonNull String inputAccountUID
     , @NonNull TextView textView)
    {
        final DatabaseReference referenceDataRegistration = generalUserReference
                .child(inputAccountUID)
                .child(DATA_REGISTER);

        final ValueEventListener valueListenerDataRegistration = new ValueEventListener()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                textView.setText("Дата регистрации: "
                        + snapshot.getValue(String.class));
                removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { removeThisListener(); }

            private void removeThisListener()
            { referenceDataRegistration.removeEventListener(this); }
        };

        referenceDataRegistration
                .addListenerForSingleValueEvent(valueListenerDataRegistration);
    }

    private synchronized void setSubscribeAccountData()
    {
        generalUserReference.child(firebaseAuth.getUid())
                .child(SubscribeController.SUBSCRIBE_DATA)
                .get()
                .addOnSuccessListener(resultSubscribeTask ->
                        this.sharedPreferencesManager.haveAccountSubscribe(
                                !resultSubscribeTask
                                        .getValue(String.class)
                                        .equals(SubscribeController.SUBSCRIBE_NONE)));
    }

    public synchronized final void changePassword(
     @Nullable CustomAppDialog customAppDialog
     , @NonNull String passwordOld
     , @NonNull String passwordNew)
    {
        if (checkNetwork())
        {
            this.customAppDialog = customAppDialog;
            final AuthCredential authCredential = EmailAuthProvider
                    .getCredential(sharedPreferencesManager
                    .setUserData(null, null)
                    .split(" ")[0], passwordOld);

            this.firebaseAuth.getCurrentUser()
                    .reauthenticate(authCredential)
                    .addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    FirebaseAuth
                      .getInstance()
                      .getCurrentUser()
                      .updatePassword(passwordNew)
                      .addOnCompleteListener(task1 ->
                      {
                          changePasswordInRTDB(FirebaseAuth.getInstance()
                                  .getCurrentUser()
                                  .getUid()
                                  , passwordNew);

                          if (task1.isSuccessful())
                              sharedPreferencesManager
                                  .setUserData(sharedPreferencesManager.setUserData(
                                          null, null)
                                          .split(" ")[0]
                                          , passwordNew);
                          cancelLoadDialog();
                      });
                }
                else
                {
                    cancelLoadDialog();
                    Toast.makeText(context
                            , "Ошибка - введён неверный пароль, повторите попытку ещё раз"
                            , Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
        else
        {
            cancelLoadDialog();
            Toast.makeText(context
                    , context.getString(R.string.offlineNetworkMSG)
                    , Toast.LENGTH_SHORT)
                    .show();
        }
    }

    synchronized public final void restorePassword(
     @Nullable CustomAppDialog customAppDialog
     , @NonNull String nowLogin
     , @NonNull String accountID
     , @NonNull String newPassword
     , @NonNull FragmentActivity callFragment)
    {
        this.customAppDialog = customAppDialog;
        firebase_connection.getReference(GLOBAL_DATA_NAME)
            .child(GlobalDataFBManager.GLOBAL_LIST_ALL_USERS)
            .addListenerForSingleValueEvent(new ValueEventListener()
            {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshotCurrentUserUID)
           {
               if (!snapshotCurrentUserUID.hasChild(accountID))
                   return;

               final String currentUserUID = snapshotCurrentUserUID
                       .child(accountID)
                       .getValue(String.class);

               generalUserReference.child(currentUserUID)
                       .child("password")
                       .addListenerForSingleValueEvent(new ValueEventListener()
                       {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot)
                           {
                              firebaseAuth.signInWithEmailAndPassword(nowLogin
                              , snapshot.getValue(String.class))
                              .addOnSuccessListener(task ->
                                      firebaseAuth.getInstance()
                                           .getCurrentUser()
                                           .updatePassword(newPassword)
                                           .addOnCompleteListener(task1 ->
                                           {
                                               changePasswordInRTDB(currentUserUID
                                                       , newPassword);

                                               if (task1.isSuccessful())
                                                   callFragment.onBackPressed();
                                               cancelLoadDialog();
                                           })
                              )
                              .addOnFailureListener(e ->
                              {
                                  cancelLoadDialog();
                                  Toast.makeText(context
                                          , context.getString(R.string.errorServerChangePassword)
                                          , Toast.LENGTH_SHORT)
                                          .show();
                              });
                           }
                   @Override
                  public void onCancelled(@NonNull DatabaseError error)
                   { cancelLoadDialog(); }
               });
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void changePasswordInRTDB(String userUID, String newPassword)
    {
        this.generalUserReference.child(userUID)
              .child("password")
              .setValue(newPassword)
              .addOnSuccessListener(unused -> Toast.makeText(context
                      , context.getString(R.string.successChangePassword)
                      , Toast.LENGTH_SHORT).show())
              .addOnFailureListener(e -> Toast.makeText(context
                      , context.getString(R.string.errorServerChangePassword)
                      , Toast.LENGTH_SHORT).show());
    }

    public synchronized final void changeLogin(
     @Nullable CustomAppDialog customAppDialog
     , @NonNull String loginOld
     , @NonNull String loginNew)
    {
        if (checkNetwork())
        {
            this.customAppDialog = customAppDialog;
            final AuthCredential change_password = EmailAuthProvider.getCredential(loginOld
                    , sharedPreferencesManager.setUserData(null, null)
                            .split(" ")[1]);

           this.firebaseAuth.getCurrentUser()
                    .reauthenticate(change_password)
                    .addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    FirebaseAuth
                            .getInstance()
                            .getCurrentUser()
                            .updateEmail(loginNew)
                            .addOnCompleteListener(task1 ->
                    {
                        if (task1.isSuccessful())
                        {
                            Toast.makeText(context
                                    , "Логин успешно изменён"
                                    , Toast.LENGTH_SHORT)
                                    .show();

                            this.sharedPreferencesManager.setUserData(loginNew
                                    , sharedPreferencesManager.setUserData(null, null)
                                            .split(" ")[1]);
                        } else
                            Toast.makeText(context
                                    , "Произошла ошибка, повторите поппытку ещё раз"
                                    , Toast.LENGTH_SHORT)
                                    .show();
                        cancelLoadDialog();
                    });
                }
                else
                {
                    cancelLoadDialog();
                    Toast.makeText(context
                            , "Ошибка - введён неверный логин, повторите попытку ещё раз"
                            , Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
        else
        {
            cancelLoadDialog();
            Toast.makeText(context
                    , context.getString(R.string.offlineNetworkMSG)
                    , Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void cancelLoadDialog()
    {
        if (this.customAppDialog != null)
            this.customAppDialog.showLoadingDialog(
                    false, null);
    }

    synchronized public final void updateScorePublicCard(
            boolean operation)
    {
        final String user_uid = Objects.requireNonNull(FirebaseAuth
                .getInstance()
                .getCurrentUser())
                .getUid();

        final DatabaseReference referenceUpdateScorePublicCard = generalUserReference
                .child(user_uid)
                .child(SCORE_PUBLIC_CARD);

        final ValueEventListener valueListenerUpdateScorePublicCard = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                final int score = Integer.parseInt(Objects.requireNonNull(
                        snapshot.getValue()).toString());

                generalUserReference.child(user_uid)
                .child(SCORE_PUBLIC_CARD)
                .setValue((operation ? score + 1 : score - 1));

                removeThisListener();
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error)
            { removeThisListener(); }
            private void removeThisListener()
            { referenceUpdateScorePublicCard.removeEventListener(this); }
        };

        referenceUpdateScorePublicCard
                .addListenerForSingleValueEvent(valueListenerUpdateScorePublicCard);
    }

    @Override
    public boolean checkNetwork() {
        return NetworkListener.getStatusNetwork();
    }

    @Override
    synchronized public final void onSuccess(
            @NonNull AuthResult authResult)
    {
        if (code_operation.equals(operation_code_write_inDB))
        {
            final String user_uid = firebaseAuth.getUid();
            final UserAccountDataEntity user_accountDataEntity =
                    new UserAccountDataEntity(getLogin, getPassword);

            @SuppressLint("SimpleDateFormat") OnSuccessListener<Void> successListenerAuth = unused ->
            generalUserReference.child(user_uid)
               .child(USERS_INFO)
               .child(DATA_CARD)
               .setValue("null")
               .addOnSuccessListener(next0 -> generalUserReference.child(user_uid)
                .child(SubscribeController.SUBSCRIBE_DATA)
                .setValue(SubscribeController.SUBSCRIBE_NONE)
                .addOnSuccessListener(next1 -> generalUserReference.child(user_uid)
                .child(GlobalDataFBManager.STATUS_ACCOUNT_ONLINE)
                .setValue(0)
                .addOnSuccessListener(next2 -> generalUserReference.child(user_uid)
                .child(DATA_REGISTER)
                .setValue(new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime()))
                .addOnSuccessListener(next3 -> generalUserReference.child(user_uid)
                .child(SCORE_PUBLIC_CARD)
                .setValue(0)
                .addOnSuccessListener(next4 -> generalUserReference.child(user_uid)
                .child(SubscribeController.FIND_GEO_SCORE)
                .setValue(new GeoFindEntity(SubscribeController.FIND_GEO_LIMIT_NOSUB, SubscribeController.realDate()))
                .addOnSuccessListener(next5 ->
                {
                    new SyncPersonalManager(context).checkNewUser();
                    sharedPreferencesManager.account_id(accountName);

                    generalUserReference.child(user_uid)
                       .child(ACCOUNT_ID)
                       .setValue(accountName)
                       .addOnSuccessListener(next6 -> firebase_connection.getReference(GLOBAL_DATA_NAME)
                          .child(GlobalDataFBManager.GLOBAL_LIST_ALL_USERS)
                          .child(accountName)
                          .setValue(user_uid)
                          .addOnSuccessListener(next7 ->
                          {
                              Toast.makeText(context
                                      , context.getString(R.string.SuccessRegister)
                                      , Toast.LENGTH_SHORT).show();
                              startBasicActivity();
                          }));
                }))))));

            generalUserReference.child(user_uid)
                    .setValue(user_accountDataEntity)
                    .addOnSuccessListener(successListenerAuth);
        }
        else if (code_operation.equals(operation_code_read_fromDB))
        {
           final ValueEventListener eventCheckingTheExcess = new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                   final boolean haveSubscribeAccount =
                           !snapshot.child(SubscribeController.SUBSCRIBE_DATA)
                            .getValue()
                            .toString()
                            .equals(SubscribeController.SUBSCRIBE_NONE);

                    final long allUserUID =
                            snapshot.child(PERSONAL_USERS).getChildrenCount();
                    final SyncPersonalManager syncPersonalManager =
                            new SyncPersonalManager(context);

                    if (allUserUID < 5)
                    {
                        if (allUserUID < 3 || haveSubscribeAccount)
                        successCheck(syncPersonalManager);
                        else
                            emergencyCheck(
                                    snapshot, syncPersonalManager);
                    }
                    else
                    {
                        Toast.makeText(
                                context
                                , R.string.warningAuthSub
                                , Toast.LENGTH_LONG)
                                .show();
                        exitAccount();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }

                synchronized private void successCheck(
                        @NonNull SyncPersonalManager syncPersonalCardsManager)
                {
                    syncPersonalCardsManager.checkNewUser();

                    new SubscribeController(context)
                            .checkSubscribe(SubscribeController.MODE_GET
                              , null
                              , null
                              , null
                              , null);

                    if (sharedPreferencesManager.account_id(null)
                            .equals("---"))
                        downloadUserId();

                    setSubscribeAccountData();
                    syncGlobalUserPhoto();
                }

               private void emergencyCheck(DataSnapshot snapshot
                       , SyncPersonalManager personal_dataUserCard)
               {
                   RewardedMobileMediationManager.buildAndShowAd(
                           context
                           , new MediationNetworkEntity(
                                   R.string.warningAuth
                                   , null
                                   , new Pair((Runnable) () ->
                                   successCheck(personal_dataUserCard)
                                   , (Runnable) () -> exitAccount())
                                   , null));
               }
            };

            this.generalUserReference.child(firebaseAuth.getUid())
                    .addListenerForSingleValueEvent(eventCheckingTheExcess);
        }
        try { cancelLoadDialog(); }
        catch (Exception ignored) { }
    }

    public synchronized void checkSubscribeUsersCount()
    {
        this.generalUserReference.child(
                firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        new SyncPersonalManager(context).checkNewUser();

                        final boolean haveSubscribeAccount =
                                !snapshot.child(SubscribeController.SUBSCRIBE_DATA)
                                        .getValue()
                                        .toString()
                                        .equals(SubscribeController.SUBSCRIBE_NONE);

                        final long allUserUID =
                                snapshot.child(PERSONAL_USERS).getChildrenCount();

                        if (allUserUID < 5)
                            if (allUserUID > 3 && !haveSubscribeAccount)
                                RewardedMobileMediationManager.buildAndShowAd(
                                        context
                                        , new MediationNetworkEntity(
                                                R.string.warningAuth
                                                , null
                                                , new Pair(null
                                                , (Runnable) ((BasicAppActivity) context)::finish)
                                                , null));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    public synchronized final void updateLocaleUserNickname(
            @NonNull String userInputNewName)
    {
        if (!checkNetwork())
        {
            sharedPreferencesManager.
                    updateUserLocaleNickname(true);
            return;
        }
        generalUserReference.child(FirebaseAuth.getInstance().getUid())
                .child(PERSONAL_USERS)
                .child(generationPersonalUID())
                .child(PERSONAL_USER_NAME)
                .setValue(userInputNewName)
                .addOnCompleteListener(resultTask ->
                        sharedPreferencesManager.updateUserLocaleNickname(
                                !resultTask.isSuccessful()));
    }

    private void downloadUserId()
    {
        final DatabaseReference referenceDownloadUserId = generalUserReference
                .child(firebaseAuth.getUid())
                .child(ACCOUNT_ID);

        final ValueEventListener valueListenerDownloadUserId = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                sharedPreferencesManager.account_id(
                        snapshot.getValue(String.class));

                generalUserReference.child(firebaseAuth.getUid())
                        .child(PERSONAL_USERS)
                        .child(generationPersonalUID())
                        .addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot)
                            {
                                if (snapshot.hasChild(PERSONAL_USER_NAME))
                                    sharedPreferencesManager.nameUserProfile(
                                            snapshot.child(PERSONAL_USER_NAME)
                                                    .getValue(String.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });

                removeThisListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { removeThisListener(); }

            private void removeThisListener()
            { referenceDownloadUserId.removeEventListener(this); }
        };

        referenceDownloadUserId
                .addListenerForSingleValueEvent(valueListenerDownloadUserId);
    }

    public synchronized final void downloadInputCardOffer(
      @NonNull CustomHeaderListView listView)
    {
        generalUserReference.child(firebaseAuth.getUid())
                .child(CARD_OFFER)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                final List<GlobalUserInfoEntity> allOfferList =
                        new LinkedList<>();
                final ThemeAppController themeAppController =
                        new ThemeAppController(
                                new DataInterfaceCard(context));

                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                {
                    firebase_connection
                            .getReference(GLOBAL_DATA_NAME)
                            .child(GLOBAL_LIST_ALL_USERS)
                            .child(singleSnapshot.getKey())
                            .addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot valueSnapshot)
                                {
                                    try
                                    {
                                        allOfferList.add(new GlobalUserInfoEntity(
                                                singleSnapshot.getKey()
                                                , singleSnapshot.getValue(String.class)
                                                , valueSnapshot.getValue(String.class)));

                                        listView.setAdapter(new GlobalUsersFindAdapter(context
                                                , themeAppController
                                                , allOfferList
                                                , OPERATION_REQUEST));
                                    }
                                    catch (NullPointerException userAccountDeleted)
                                    {
                                        generalUserReference.child(firebaseAuth.getUid())
                                                .child(CARD_OFFER)
                                                .child(singleSnapshot.getKey())
                                                .removeValue();
                                    }
                                    finally
                                    { listView.checkEmptyList(false); }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void checkInputRequestCards(
            @NonNull AppCompatImageButton buttonRequestDrawable)
    {
        if (this.checkNetwork())
        {
            this.generalUserReference.child(firebaseAuth.getUid())
                .child(this.CARD_OFFER)
                .addValueEventListener(new ValueEventListener()
                {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot)
                 {
                    if (snapshot.getChildrenCount() > 0)
                        buttonRequestDrawable.setImageTintList(
                                ColorStateList.valueOf(Color.YELLOW));
                 }
                 @Override
                 public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public void rejectRequestAction(String inputUserAccountID
            , FragmentActivity fragmentActivity)
    {
        if (this.checkNetwork())
        this.generalUserReference.child(firebaseAuth.getUid())
                .child(this.CARD_OFFER)
                .child(inputUserAccountID)
                .removeValue()
                .addOnSuccessListener(unused ->
                {
                    Toast.makeText(context
                            , "Запрос пользователя \"" + inputUserAccountID + "\" успешно отклонён"
                            , Toast.LENGTH_LONG).show();
                    fragmentActivity.onBackPressed();
                })
                .addOnFailureListener(e -> Toast.makeText(context
                        , context.getString(R.string.errorRejectRequestCard)
                        , Toast.LENGTH_SHORT).show());

        else Toast.makeText(context
                , context.getString(R.string.offlineNetworkMSG)
                , Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SimpleDateFormat")
    synchronized public void sendGlobalOfferCard(@NonNull String inputUserTextOffer
     , @NonNull String globalUserUID)
    {
        final HashMap<String, Object> dataSendOffer =
                new HashMap<>();
        dataSendOffer.put(
                sharedPreferencesManager.account_id(null)
                , inputUserTextOffer
                        + "<-_-->" + new SimpleDateFormat("dd.MM.yyyy")
                        .format(Calendar.getInstance()
                                .getTime()));

       generalUserReference.child(globalUserUID)
               .child(CARD_OFFER)
               .updateChildren(dataSendOffer)
               .addOnSuccessListener((unused) ->
               {
                   dataSendOffer.clear();
                   Toast.makeText(context
                           , context.getString(R.string.successGlobalOfferCard)
                           , Toast.LENGTH_LONG)
                           .show();
               });
    }

    @SuppressLint({"StaticFieldLeak", "DEPRECATION"})
    synchronized private void syncGlobalUserPhoto()
    {
       final Runnable actionNext = () ->
       {
           Toast.makeText(context
                   , "Вход выполнен"
                   , Toast.LENGTH_SHORT).show();

           if (this.customAppDialog != null)
               this.customAppDialog.showLoadingDialog(false, null);
           startBasicActivity();
       };

       if (sharedPreferencesManager
               .path_userIconProfile(null) != null)
       {
           actionNext.run();
           return;
       }

        FirebaseStorage.getInstance()
                .getReference()
                .child(firebaseAuth.getUid())
                .getDownloadUrl()
                .addOnSuccessListener(completeDownloadURL ->
                {
                    final File photoFile = new File(
                            context.getCacheDir(), "userPhoto.jpg");
                    try
                    {
                        final FileOutputStream fileOutputStream =
                                new FileOutputStream(photoFile);

                        Glide.with(context)
                                .load(completeDownloadURL)
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
                                        new AsyncTask<Bitmap, Void, Boolean>()
                                        {
                                            @Override
                                            protected Boolean doInBackground(Bitmap... bitmaps)
                                            {
                                                return bitmaps[0].compress(
                                                        Bitmap.CompressFormat.JPEG
                                                        , 80
                                                        , fileOutputStream);
                                            }

                                            @Override
                                            protected void onPostExecute(Boolean completeSave)
                                            {
                                                super.onPostExecute(completeSave);
                                                if (completeSave)
                                                    sharedPreferencesManager.path_userIconProfile(
                                                            photoFile.getPath());
                                                try
                                                {
                                                    fileOutputStream.flush();
                                                    fileOutputStream.close();
                                                } catch (IOException ignored) { }
                                                finally
                                                { actionNext.run(); }
                                            }
                                        }.execute(((GlideBitmapDrawable)
                                                resource.getCurrent()).getBitmap());
                                        return false;
                                    }
                                }).into(500, 500);
                    } catch (FileNotFoundException e)
                    { actionNext.run(); }
                })
                .addOnFailureListener(
                        unused -> actionNext.run());
    }

    public synchronized final void restoreAccount(
      @Nullable CustomAppDialog customAppDialog
      , @NonNull String userID
      , @NonNull String userLogin)
    {
        this.customAppDialog = customAppDialog;
        try
        {
            this.firebase_connection
                    .getReference(GLOBAL_DATA_NAME)
                    .child(GLOBAL_LIST_ALL_USERS)
                    .child(userID)
                    .addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot)
                        {
                            try
                            {
                                BasicFireBaseManager.this.generalUserReference
                                        .child(snapshot.getValue(String.class))
                                        .child("login")
                                        .addListenerForSingleValueEvent(new ValueEventListener()
                                        {
                                            @SuppressLint("RestrictedApi")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot)
                                            {
                                                final String userEmailAccount =
                                                        snapshot.getValue(String.class);

                                                if (!userEmailAccount.equals(userLogin))
                                                {
                                                    cancelLoadDialog();
                                                    Toast.makeText(context
                                                            , R.string.errorRestoreAccountEmail
                                                            , Toast.LENGTH_LONG)
                                                            .show();
                                                    return;
                                                }

                                                BasicFireBaseManager.this.firebaseAuth
                                                        .sendPasswordResetEmail(userEmailAccount)
                                                        .addOnCompleteListener(task ->
                                                        {
                                                            Toast.makeText(context
                                                                    , task.isSuccessful()
                                                                            ? R.string.restorePasswordAccountInfo
                                                                            : R.string.errorRestore
                                                                    , Toast.LENGTH_LONG)
                                                                    .show();

                                                            cancelLoadDialog();
                                                            ((ReceptionAppActivity) context).onBackPressed();
                                                        });
                                            }
                                            @Override public void onCancelled(
                                                    @NonNull DatabaseError error) { }
                                        });
                            }
                            catch (NullPointerException userErrorInputLogin)
                            {
                                cancelLoadDialog();
                                Toast.makeText(context
                                        , R.string.errorRestoreAccountID
                                        , Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                        @Override
                        public void onCancelled(
                                @NonNull DatabaseError error) { }
                    });
        }
        catch (NullPointerException userErrorInputID)
        {
            cancelLoadDialog();
            Toast.makeText(context
                    , R.string.errorRestoreAccountID
                    , Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onFailure(@NonNull Exception e)
    {
        if (context.getClass()
                != StartAppActivity.class)
        {
            if (this.customAppDialog != null)
                this.customAppDialog.showLoadingDialog(false, null);

            if (code_operation.equals(
                    operation_code_read_fromDB))
            Toast.makeText(context
                    , "Ошибка ввода логина или пароля, повторите попытку ещё раз"
                    , Toast.LENGTH_SHORT)
                    .show();

            else if (code_operation.equals(
                    operation_code_write_inDB))
            Toast.makeText(context
                    , "Ошибка - вы уже зарегистрированы"
                    , Toast.LENGTH_SHORT)
                    .show();
        }
        else
        {
            Toast.makeText(context
                    , "Обнаружено изменение логина или пароля - подтвердите данные аккаунта"
                    , Toast.LENGTH_LONG)
                    .show();
            this.context.startActivity(new Intent(context
                    , ReceptionAppActivity.class));
        }
        try { cancelLoadDialog(); }
        catch (Exception ignored) { }
    }

    public synchronized final void avaibleAdditionalPayment(
      @NonNull AppCompatButton additionalPaymentBtn
      , @NonNull View.OnClickListener actionForThisBtn)
    {
        if (!NetworkListener.getStatusNetwork())
            return;

        final CustomAppDialog loadingDialog =
                new CustomAppDialog(this.context);
        loadingDialog.showLoadingDialog(
                true, this.context.getString(R.string.loadingInfo));

        this.firebase_connection
                .getReference(context.getString(R.string.paymentAccept))
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        loadingDialog.showLoadingDialog(false, null);

                        if (snapshot.getValue(Boolean.class))
                        {
                            additionalPaymentBtn.setScaleX(1f);
                            additionalPaymentBtn.setOnClickListener(actionForThisBtn);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }
}
