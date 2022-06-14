package com.example.mcard.AdapersGroup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mcard.FragmentsAdditionally.GlobalProfileUserFragment;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.R;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;

import java.util.List;

public final class GlobalUsersFindAdapter extends BaseAdapter
{
    private View view;
    private GlobalDataFBManager globalDataFBManager;
    private GlobalUserInfoEntity infoAdapter;

    private final Context context;
    private final ThemeAppController themeAppController;
    private final String inputOperation;
    private final List<GlobalUserInfoEntity> globalUserInfoEntities;
    private final LayoutInflater layoutInflater;

    public static final String OPERATION_REQUEST = "REQUEST";
    public static final String OPERATION_ACCOUNT_INFO = "ACCOUNT";
    public static final String OPERATION_TOP_RATING = "RATING";

    private TextView userNicknameTV;
    private GlobalProfileUserFragment.UserIconLayout imageSelectAccount;

    @Override
    public int getCount()
    { return globalUserInfoEntities.size(); }

    @Override
    public Object getItem(int element_id)
    { return globalUserInfoEntities.get(element_id); }

    @Override
    public long getItemId(int element_id)
    { return element_id; }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View newView, ViewGroup viewGroup)
    {
        this.view = newView;
        if (view == null)
            this.view = layoutInflater.inflate(
                    R.layout.find_users_list
                    , viewGroup
                    , false);

        this.view.startAnimation(
                AnimationUtils.loadAnimation(
                        context, R.anim.animation_for_global_users_items));

        findObjects();
        this.infoAdapter = infoAdapter(position);

        this.themeAppController.settingsTextAdapter(
                this.userNicknameTV
                , infoAdapter.getUserGlobalID());

        final String variableText;
        switch (this.inputOperation)
        {
            case OPERATION_REQUEST:
               variableText = "\n" + infoAdapter.getNetworkActions().split("<-_-->")[1] + "\n";
               break;
            case OPERATION_ACCOUNT_INFO:
                variableText = "\nУчастники\n" + infoAdapter.getNetworkActions() + "\n";
                break;
            case OPERATION_TOP_RATING:
                variableText = "\nПубликации\n" + infoAdapter.getNetworkActions().split(" ")[0] + "\n";
                break;
            default: variableText = "";
        }

       this.globalDataFBManager
                .setGlobalUserPhoto(imageSelectAccount
                        , infoAdapter.getUserGlobalID()
                        , this.view.findViewById(R.id.loading));
        return view;
    }

    private GlobalUserInfoEntity infoAdapter(int position)
    { return (GlobalUserInfoEntity) getItem(position); }

    public GlobalUsersFindAdapter(
      @NonNull Context context
      , @NonNull ThemeAppController themeAppController
      , @NonNull List<GlobalUserInfoEntity> main_info
      , @NonNull String inputOperation)
    {
        this.context = context;
        this.themeAppController = themeAppController;
        this.globalUserInfoEntities = main_info;

        this.layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.inputOperation = inputOperation;
    }

    private void findObjects()
    {
        this.globalDataFBManager =
                new GlobalDataFBManager(context);
        this.userNicknameTV = view.findViewById(R.id.user_id);
        this.imageSelectAccount = view.findViewById(R.id.user_icon);
    }
}
