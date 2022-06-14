package com.example.mcard.FragmentsAdditionally;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.R;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import java.util.Objects;

public final class UserSubscribersFragment extends Fragment implements GeneralStructApp
{
    private View view;
    private AppCompatImageButton btnBack;
    private CustomHeaderListView customHeaderListView;

    private BasicFireBaseManager basicFireBaseManager;
    private GlobalDataFBManager globalDataFBManager;
    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_global_user_subscribers, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.btnBack =
                view.findViewById(R.id.btn_back);
        this.customHeaderListView =
                view.findViewById(R.id.listview_subscribers);

        this.basicFireBaseManager =
                new BasicFireBaseManager(requireContext());
        this.globalDataFBManager =
                new GlobalDataFBManager(requireContext());
        this.sharedPreferencesManager =
                new SharedPreferencesManager(requireContext());
    }

    @Override
    public void drawableView()
    {
        final ThemeAppController themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.main_linear));
        themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , "Активность");
        themeAppController.setOptionsButtons(btnBack);
    }

    @Override
    public void basicWork()
    {
        this.globalDataFBManager.getListUserSubscribers(
                ((GlobalUserInfoEntity) requireArguments()
                        .getSerializable(GeneralRulesDB.TRANSACTION_KEY))
                        .getUserGlobalUID()
                , this.customHeaderListView);

        this.customHeaderListView.setOnItemClickListener((parent, view1, position, id) ->
        {
            final GlobalUserInfoEntity globalUserInfoEntity = (GlobalUserInfoEntity) parent
                    .getAdapter()
                    .getItem(position);

            if (Objects.equals(globalUserInfoEntity.getUserGlobalID()
                    , this.sharedPreferencesManager.account_id(null)))
            {
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                        .replace(R.id.main_linear, new PersonalProfileFragment())
                        .commit();

                requireActivity().onBackPressed();
                return;
            }

            final GlobalProfileUserFragment globalProfileUser_fragment =
                    new GlobalProfileUserFragment();
            final Bundle bundle = new Bundle();

            bundle.putSerializable(GeneralRulesDB.TRANSACTION_KEY, globalUserInfoEntity);
            globalProfileUser_fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                    .replace(R.id.main_linear, globalProfileUser_fragment)
                    .commit();
        });

        this.btnBack.setOnClickListener(v ->
                requireActivity().onBackPressed());
    }
}