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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.R;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralAnimations;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.SideFunctionality.ListViewComponent;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.UserActionsCard.SingleGlobalCardFragment;
import static com.example.mcard.GlobalListeners.NetworkListener.getStatusNetwork;

public final class BasicGlobalPlatformFragment extends Fragment
        implements View.OnClickListener, GeneralStructApp
{
    private View view;
    private AppCompatImageButton requestGlobal
            , findGlobal
            , faqGlobal
            , backBtn;
    private CustomHeaderListView listCardGlobal;

    private Animation animForPlusBtn;
    private BasicFireBaseManager basicFireBaseManager;
    private GlobalDataFBManager globalDataFBManager;

    private GeneralAnimations generalAnimations;
    private ListViewComponent listViewComponent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_global_cards, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.requestGlobal = view.findViewById(R.id.request_global);
        this.findGlobal = view.findViewById(R.id.find_global);
        this.backBtn = view.findViewById(R.id.btnBack);
        this.faqGlobal = view.findViewById(R.id.btnFAQ);
        this.listCardGlobal = view.findViewById(R.id.listCard_global);
        this.animForPlusBtn = AnimationUtils.loadAnimation(requireContext(), R.anim.select_object);

        this.basicFireBaseManager = new BasicFireBaseManager(requireContext());
        this.globalDataFBManager = new GlobalDataFBManager(requireActivity());

        this.generalAnimations = new GeneralAnimations(1f
             , 0f
             , -1f
             , 600
             , 150);

        this.listViewComponent = new ListViewComponent()
        {
            @Override
            @SuppressLint("UseCompatLoadingForDrawables")
            public void registerListView()
            {
                this.settingsListView(listCardGlobal
                        , requireContext().getDrawable(R.color.prism)
                        , generalAnimations);
            }

            @Override
            public void createItemInList(@NonNull ListView listView)
            { generalAnimations.setCardList(listView); }
        };
    }

    @Override
    public void drawableView()
    {
        final ThemeAppController themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_global));
        themeAppController.settingsText(
                view.findViewById(R.id.name_fragment), requireContext().getString(R.string.nameGlobalFragment));

        themeAppController.setOptionsButtons(
                requestGlobal
                , findGlobal
                , backBtn
                , faqGlobal);
        this.basicFireBaseManager.checkInputRequestCards(
                this.requestGlobal);
    }

    @Override
    public void basicWork()
    {
        this.listCardGlobal.setScaleX(0f);
        this.backBtn.setOnClickListener(this);

        if (getStatusNetwork())
        {
            this.listViewComponent.registerListView();
            this.globalDataFBManager.getGlobalCardList(
                    this.listCardGlobal, this.listViewComponent);
        }
        else
        {
            this.listCardGlobal.checkEmptyList(false);
            Toast.makeText(requireContext()
                    , requireContext().getString(R.string.warningLoadGlobalCards),
                    Toast.LENGTH_LONG).show();
            return;
        }

        this.findGlobal.setOnClickListener(this);
        this.requestGlobal.setOnClickListener(this);
        this.faqGlobal.setOnClickListener(this);

        this.listCardGlobal.setOnItemClickListener((parent
                , view
                , position
                , id) -> openSingleCard(parent, view, position));
    }

    @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v)
        {
            if (!this.generalAnimations
                    .getActionResolution()) return;
            v.startAnimation(animForPlusBtn);

            switch (v.getId())
            {
                case R.id.find_global:
                    requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                        .replace(R.id.global_linear_menu, new GlobalSearchUsersFragment())
                        .commit();
                    break;

                case R.id.request_global:
                    requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                        .replace(R.id.global_linear_menu, new GlobalRequestManagerFragment())
                        .commit();
                    break;

                case R.id.btnFAQ:
                    showFAQGlobalInfo();
                    break;

                default:
                    requireActivity().onBackPressed();
                    break;
            }
        }

        private void showFAQGlobalInfo()
        {
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(true)
                    .setTitle("Дополнительная информация")
                    .setMessage(R.string.messageFAQGlobal, 6.75f)
                    .setPositiveButton("Подробнее", (click) ->
                    {

                    })
                    .setNegativeButton("Назад", null)
                    .show();
        }

    private void openSingleCard(AdapterView<?> data_card
            , View view
            , int card_position)
    {
        if (!this.generalAnimations.getActionResolution())
            return;

        final CardInfoEntity cardInfoEntity =
                (CardInfoEntity) data_card.getAdapter()
                        .getItem(card_position);
        final Bundle dataSingleCard = new Bundle();

        dataSingleCard.putParcelable(GeneralRulesDB.TRANSACTION_KEY
                , new CardInfoEntity(cardInfoEntity.getNumber()
                        , cardInfoEntity.getName()
                        , cardInfoEntity.getBarcode()
                        , cardInfoEntity.getColor()
                        , cardInfoEntity.getCardOwner()
                        , cardInfoEntity.getDateAddCard()
                        , cardInfoEntity.getUniqueIdentifier()));

        final SingleGlobalCardFragment singleGlobalCardFragment =
                new SingleGlobalCardFragment(
                        this.generalAnimations, this.listViewComponent);
        singleGlobalCardFragment.setArguments(dataSingleCard);

        this.listViewComponent.selectCardAnim(this.generalAnimations
                , data_card
                , view
                , () ->
                        requireActivity()
                                .getSupportFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                                .replace(R.id.global_linear_menu, singleGlobalCardFragment)
                                .commit()
                , card_position);
    }
}