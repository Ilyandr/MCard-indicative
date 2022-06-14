package com.example.mcard.FragmentsAdditionally;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.GroupServerActions.SyncPersonalManager;
import com.example.mcard.R;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public final class GlobalSearchUsersFragment extends Fragment implements
  AdapterView.OnItemClickListener
  , TextWatcher
  , GeneralStructApp
{
    private View view;
    private AppCompatEditText getInfoSearch;
    private AppCompatImageButton btnBack;

    private BasicFireBaseManager basicFireBaseManager;
    private GlobalDataFBManager globalDataFBManager;

    private CustomHeaderListView usersListOutput;
    private List<GlobalUserInfoEntity> infoEntityList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view =
                inflater.inflate(
                        R.layout.fragment_global_find
                        , container
                        , false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.infoEntityList = new ArrayList<>();
        this.basicFireBaseManager =
                new BasicFireBaseManager(requireContext());
        this.globalDataFBManager =
                new GlobalDataFBManager(requireContext());

        this.getInfoSearch =
                view.findViewById(R.id.get_find_info);
        this.btnBack =
                view.findViewById(R.id.btn_back);
        this.usersListOutput =
                view.findViewById(R.id.list_item_search);
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
                view.findViewById(R.id.mainLinearLayout));
        themeAppController.setOptionsButtons(btnBack);
    }

    @Override
    public void basicWork()
    {
        getPrimaryUsersList();
        this.getInfoSearch.addTextChangedListener(this);
        this.usersListOutput.setOnItemClickListener(this);
        this.view.findViewById(R.id.btn_back).setOnClickListener( v ->
                requireActivity().onBackPressed());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
      this.usersListOutput.removeAllViewsInLayout();

      if (getInfoSearch.getText()
              .toString()
              .length() >= 1)
          this.globalDataFBManager.findUsersByID(
                  getInfoSearch.getText().toString()
              , usersListOutput
              , infoEntityList);

      else this.infoEntityList.clear();
    }

    @Override
    public void afterTextChanged(Editable s) { }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        final GlobalUserInfoEntity globalUserInfoEntity = (GlobalUserInfoEntity)
                parent.getAdapter().getItem(position);

        if (!new SharedPreferencesManager(requireContext())
                .account_id(null)
                .equals(globalUserInfoEntity.getUserGlobalID()))
        {
            final GlobalProfileUserFragment globalProfileUser_fragment =
                    new GlobalProfileUserFragment();
            final Bundle bundle = new Bundle();

            bundle.putSerializable(GeneralRulesDB.TRANSACTION_KEY
                    , globalUserInfoEntity);
            globalProfileUser_fragment.setArguments(bundle);

            new SyncPersonalManager(requireContext())
                    .checkUserInBlackListStageFirst(
                            globalUserInfoEntity.getUserGlobalUID()
                            , requireActivity()
                            , globalProfileUser_fragment);
        }
        else
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                    .replace(R.id.main_linear, new PersonalProfileFragment())
                    .commit();
    }

    private synchronized void getPrimaryUsersList()
    {
        this.globalDataFBManager.findUsersByID(
                null
                , usersListOutput
                , infoEntityList);
    }

    @Override
    public void onDestroy()
    {
        this.infoEntityList.clear();
        super.onDestroy();
    }
}