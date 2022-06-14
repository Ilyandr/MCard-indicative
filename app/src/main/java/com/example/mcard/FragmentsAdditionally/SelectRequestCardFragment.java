package com.example.mcard.FragmentsAdditionally;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.example.mcard.AdapersGroup.BasicCardManagerAdapter;
import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.R;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.SideFunctionality.GeneralAnimations;
import com.example.mcard.SideFunctionality.ListViewComponent;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;

import static com.example.mcard.FragmentsAdditionally.GlobalProfileUserFragment.globalListFragment;

public final class SelectRequestCardFragment extends Fragment
        implements GeneralStructApp, ListViewComponent
{
    private View view;
    private AppCompatImageButton btnBack, btnConfirm;
    private CustomHeaderListView localeCardList;
    private View prevView;

    private Animation animSelected;
    private CardInfoEntity cardInfoEntity;
    private GeneralAnimations generalAnimations;

    private BasicFireBaseManager basicFireBaseManager;
    private GlobalDataFBManager globalDatafbmanager;
    private ThemeAppController themeAppController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_select_request_card, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.btnBack = view.findViewById(R.id.btn_back);
        this.btnConfirm = view.findViewById(R.id.btn_confirm);
        this.localeCardList = view.findViewById(R.id.listview_select);
        this.animSelected = AnimationUtils.loadAnimation(
                requireContext(), R.anim.light_selected_btn);

        this.basicFireBaseManager =
                new BasicFireBaseManager(requireContext());
        this.globalDatafbmanager =
                new GlobalDataFBManager(requireContext());
        this.themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        this.generalAnimations = new GeneralAnimations(1f
                , 0f
                , -1f
                , 800
                , 200);
    }

    @Override
    public void drawableView()
    {
        this.themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        this.themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.main_linear_select_card));
        this.themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , "Выбор карты");
        this.themeAppController.setOptionsButtons(btnBack);
    }

    @Override
    public void basicWork()
    {
        registerListView();
        this.localeCardList.setAdapter(
                new BasicCardManagerAdapter(
                        requireContext()
                        , new CardManagerDB(requireContext())
                        .readAllCards()
                        , false));

        this.localeCardList.checkEmptyList(false);
        createItemInList(this.localeCardList);

        this.btnBack.setOnClickListener((v -> requireActivity().onBackPressed()));
        this.localeCardList.setOnItemClickListener((parent, view, position, id) ->
        {
            try { prevView.clearAnimation(); }
            catch (Exception ignored) {}

            prevView = view;
            view.startAnimation(animSelected);
            cardInfoEntity = (CardInfoEntity) parent
                    .getAdapter()
                    .getItem(position);
        this.btnConfirm.setImageResource(
                R.drawable.ic_complete);

        this.btnConfirm.setOnClickListener(v ->
        {
            final GlobalUserInfoEntity globalUserInfoEntity =
                    (GlobalUserInfoEntity) requireArguments()
                    .getSerializable(GeneralRulesDB.TRANSACTION_KEY);

            this.globalDatafbmanager.requestCardsActions(
                    globalUserInfoEntity.getUserGlobalUID()
                    , globalUserInfoEntity.getUserGlobalID()
                    , cardInfoEntity
                    , GlobalDataFBManager.MODE_PUSH_REQUEST);

            globalListFragment = true;
            requireActivity().onBackPressed();
        });});
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void registerListView()
    {
        this.settingsListView(this.localeCardList
                , requireContext().getDrawable(R.color.prism)
                , generalAnimations);
    }

    @Override
    public void createItemInList(@NonNull ListView listView)
    { this.generalAnimations.setCardList(listView); }
}