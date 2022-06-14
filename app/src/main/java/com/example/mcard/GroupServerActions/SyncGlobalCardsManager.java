package com.example.mcard.GroupServerActions;

import static com.example.mcard.BasicAppActivity.setListItemCards;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.R;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public final class SyncGlobalCardsManager extends BasicFireBaseManager
{
    private final List<CardInfoEntity> listFireBase;
    private List<CardInfoEntity> listLocaleCards;
    private final CardManagerDB userChangeGlobalDB;
    private final CustomHeaderListView changeListView;
    private final String user_key;

    public static final char MODE_INFINITE = 0;
    public static final char MODE_SINGLE = 1;

    public SyncGlobalCardsManager(
     @NonNull Context context
     , @NonNull CustomHeaderListView listView)
    {
        super(context);

        this.listFireBase = new ArrayList<>();
        this.userChangeGlobalDB =
                new CardManagerDB(context);
        this.changeListView = listView;

        this.user_key = Objects.requireNonNull(FirebaseAuth
                .getInstance()
                .getCurrentUser())
                .getUid();
    }

    public void basicSyncController(char inputMode)
    {
        final boolean autoSyncInfo = sharedPreferencesManager
                .synchronization_mode(null);
        final DatabaseReference getListDataFromServer = generalUserReference
                .child(this.user_key)
                .child(USERS_INFO);

        final ValueEventListener generalEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                dataSyncInvalidate();
                listLocaleCards = userChangeGlobalDB
                        .readAllCards();

                for (DataSnapshot singleSnapshot : snapshot.getChildren())
                    if (!singleSnapshot.getValue().toString().equals("null"))
                        listFireBase.add(singleSnapshot.getValue(CardInfoEntity.class));

                if (listFireBase.size() > listLocaleCards.size())
                    saveCardInLocaleDB();
                else if ((listFireBase.size() < listLocaleCards.size()))
                    removeCardInLocaleDB();
                else
                    updateCardInLocaleDB();

                setListItemCards(context
                        , userChangeGlobalDB
                        , changeListView
                        , false);

                listFireBase.clear();
                listLocaleCards.clear();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error)
            { removeThisListener(); }

            private void removeThisListener()
            { getListDataFromServer.removeEventListener(this); }
        };

        if (autoSyncInfo && inputMode == MODE_SINGLE)
            Toast.makeText(context
                    , context.getString(R.string.errorSyncSingle)
                    , Toast.LENGTH_SHORT)
                    .show();

        else if (autoSyncInfo && inputMode == MODE_INFINITE)
            getListDataFromServer.addValueEventListener(
                    generalEventListener);

        else if (!autoSyncInfo && inputMode == MODE_SINGLE)
        {
            getListDataFromServer.addListenerForSingleValueEvent(
                    generalEventListener);
            Toast.makeText(context
                    , context.getString(R.string.successSyncData)
                    , Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private synchronized void saveCardInLocaleDB()
    {
        final int sizeLocaleList =
                listLocaleCards.size();

        for (int i = 0; i < sizeLocaleList; i++)
        {
            for (int j = 0; j < this.listFireBase.size(); j++)
            {
                if (listFireBase.get(j)
                        .getUniqueIdentifier()
                        .equals(listLocaleCards.get(i)
                                .getUniqueIdentifier()))
                {
                    this.listFireBase.remove(j);
                    break;
                }
            }
        }

        this.sharedPreferencesManager.score_addCard(
                this.listFireBase.size());

        for (int i = 0; i < this.listFireBase.size(); i++)
            this.userChangeGlobalDB
                    .saveAndFlashDB(
                            listFireBase.get(i), () ->
                                    setListItemCards(context
                                            , userChangeGlobalDB
                                            , changeListView
                                            , this.listLocaleCards.isEmpty()));

    }

    public synchronized void saveCardInGlobalDB()
    {
        dataSyncInvalidate();
        this.generalUserReference.child(this.user_key)
                .child(this.USERS_INFO)
                .setValue(this.userChangeGlobalDB
                        .readAllCards())
                .addOnCompleteListener(unused ->
                {
                    if (unused.isSuccessful())
                        Toast.makeText(context
                                , (context.getString(R.string.SuccessSync))
                                , Toast.LENGTH_SHORT)
                                .show();
                });
    }

    @SuppressLint("SimpleDateFormat")
    private void dataSyncInvalidate()
    {
        this.sharedPreferencesManager
                .last_syncServer(
                        new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                                .format(Calendar.getInstance().getTime()));
    }

    private synchronized void updateCardInLocaleDB()
    {
        final int sizeLocaleList = listLocaleCards.size()
                , sizeServerList = listFireBase.size();

        for (int i = 0; i < sizeServerList; i++)
        {
            for (int j = 0; j < sizeLocaleList; j++)
            {
                if (listFireBase.get(i)
                        .getUniqueIdentifier()
                        .equals(listLocaleCards.get(j)
                                .getUniqueIdentifier()))
                {
                    if (listFireBase.get(i).hashCode()
                            != listLocaleCards.get(j).hashCode())
                    {
                        userChangeGlobalDB.replaceObjectDB(
                                listFireBase.get(i));
                        break;
                    }
                }
            }
        }
    }

    private synchronized void removeCardInLocaleDB()
    {
        final int sizeServerList =
                listFireBase.size();

        if (sizeServerList == 0)
        {
            saveCardInGlobalDB();
            return;
        }

        for (int i = 0; i < sizeServerList; i++)
        {
            for (int j = 0; j < this.listLocaleCards.size(); j++)
            {
                if (listFireBase.get(i)
                        .getUniqueIdentifier()
                        .equals(listLocaleCards.get(j)
                                .getUniqueIdentifier()))
                {
                    this.listLocaleCards.remove(j);
                    break;
                }
            }
        }

        this.sharedPreferencesManager.score_addCard(
                this.listLocaleCards.size());

        for (int i = 0; i < this.listLocaleCards.size(); i++)
            this.userChangeGlobalDB.removeObjectDB(
                    this.listLocaleCards.get(i)
                            .getUniqueIdentifier());
    }
}
