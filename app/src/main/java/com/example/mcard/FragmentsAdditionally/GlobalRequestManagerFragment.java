package com.example.mcard.FragmentsAdditionally;


import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.R;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;

public final class GlobalRequestManagerFragment extends Fragment implements GeneralStructApp
{
    private View view;
    private CustomHeaderListView offer_list;
    private AppCompatImageButton btn_back;
    private BasicFireBaseManager basicFireBaseManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_global_request, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.offer_list = view.findViewById(R.id.listview_offer);
        this.btn_back = view.findViewById(R.id.btn_back);

        this.basicFireBaseManager =
                new BasicFireBaseManager(requireContext());
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
                , "Запросы карт");
        themeAppController.setOptionsButtons(btn_back);
    }

    @Override
    public void basicWork()
    {
        this.basicFireBaseManager
                .downloadInputCardOffer(offer_list);

        this.btn_back.setOnClickListener((v -> requireActivity().onBackPressed()));
        this.offer_list.setOnItemClickListener((parent, view, position, id) ->
        {
            final GlobalUserInfoEntity selectItemInfoOwner =
                    (GlobalUserInfoEntity) parent
                            .getAdapter()
                            .getItem(position);

            new CustomAppDialog(requireContext())
                    .buildEntityDialog(true)
                    .setTitle("Входящий запрос карты")
                    .setMessage("Пользователь \"" + selectItemInfoOwner.getUserGlobalID()
                            + "\" отправил вам следующий запрос: \""
                            + selectItemInfoOwner.getNetworkActions().split("<-_-->")[0]
                            + "\".\nЧтобы совершить действие - нажмите на одну из кнопок ниже.", 4f)
                    .setPositiveButton("Принять", (click) ->
                            acceptRequest(selectItemInfoOwner))
                    .setAdditionalButton("Отклонить", (click) ->
                            this.basicFireBaseManager
                            .rejectRequestAction(selectItemInfoOwner.getUserGlobalID()
                                    , requireActivity()))
                    .setNegativeButton("Назад", null)
                    .show();
        });
    }

    private void acceptRequest(GlobalUserInfoEntity globalUserInfoEntity)
    {
        final Bundle argument = new Bundle();
        final SelectRequestCardFragment selectRequestCardFragment =
                new SelectRequestCardFragment();

        argument.putSerializable(
                GeneralRulesDB.TRANSACTION_KEY
                , globalUserInfoEntity);
        selectRequestCardFragment.setArguments(argument);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                .replace(R.id.main_linear, selectRequestCardFragment)
                .commit();
    }
}