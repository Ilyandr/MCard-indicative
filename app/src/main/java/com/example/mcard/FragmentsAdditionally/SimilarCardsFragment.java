package com.example.mcard.FragmentsAdditionally;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.R;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.SideFunctionality.GeneralAnimations;
import com.example.mcard.SideFunctionality.ListViewComponent;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.UserActionsCard.SingleGlobalCardFragment;
import static com.example.mcard.FragmentsAdditionally.GlobalProfileUserFragment.globalListFragment;

public final class SimilarCardsFragment extends Fragment implements GeneralStructApp
{
    private View view;
    private CustomHeaderListView customHeaderListView;
    private AppCompatImageButton btnBack;

    private BasicFireBaseManager basicFireBaseManager;
    private GlobalDataFBManager globalDataFBManager;
    private Object inputObjectInfoEntity;

    private GeneralAnimations generalAnimations;
    private ListViewComponent listViewComponent;

    public final static char CODE_SIMILAR_CARD = 0;
    public final static char CODE_USER_PUBLIC_CARDS = 1;
    private final char inputCodeOperation;

    public SimilarCardsFragment(char inputCodeOperation)
    { this.inputCodeOperation = inputCodeOperation; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_get_similar_card, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.btnBack = view.findViewById(R.id.btn_back);
        this.customHeaderListView = view.findViewById(R.id.card_list);

        this.basicFireBaseManager = new BasicFireBaseManager(requireContext());
        this.globalDataFBManager = new GlobalDataFBManager(requireContext());

        this.inputObjectInfoEntity =
                (this.inputCodeOperation == CODE_SIMILAR_CARD)
                        ? requireArguments().getParcelable(GeneralRulesDB.TRANSACTION_KEY)
                        : requireArguments().getSerializable(GeneralRulesDB.TRANSACTION_KEY);

        this.generalAnimations = new GeneralAnimations(1f
                , 0f
                , -1f
                , 800
                , 200);

        this.listViewComponent = new ListViewComponent()
        {
            @Override
            @SuppressLint("UseCompatLoadingForDrawables")
            public void registerListView()
            {
                this.settingsListView(customHeaderListView
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
                view.findViewById(R.id.bar_icon));
        themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.main_linear_similar_card));

        if (this.inputCodeOperation == CODE_SIMILAR_CARD)
            themeAppController
                    .settingsText(
                            view.findViewById(R.id.name_fragment)
                            , "Похожие карты");
        else if (this.inputCodeOperation == CODE_USER_PUBLIC_CARDS)
            themeAppController
                    .settingsText(
                            view.findViewById(R.id.name_fragment)
                            , "Карты пользователя");
        themeAppController.setOptionsButtons(btnBack);
    }

    @Override
    public void basicWork()
    {
        globalListFragment = true;
        this.customHeaderListView.setScaleX(0f);

        this.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        this.customHeaderListView.setOnItemClickListener(((parent, view1, position, id) ->
        {
            final CardInfoEntity card_infoEntity = (CardInfoEntity)
                    parent.getAdapter().getItem(position);
            final Bundle output_dataSingleCard = new Bundle();

            output_dataSingleCard.putParcelable(GeneralRulesDB.TRANSACTION_KEY
                    , new CardInfoEntity(card_infoEntity.getNumber()
                            , card_infoEntity.getName()
                            , card_infoEntity.getBarcode()
                            , card_infoEntity.getColor()
                            , card_infoEntity.getCardOwner()
                            , card_infoEntity.getDateAddCard()
                            , card_infoEntity.getUniqueIdentifier()));

            final SingleGlobalCardFragment singleGlobalCardFragment
                    = new SingleGlobalCardFragment(null ,null);
            singleGlobalCardFragment.setArguments(output_dataSingleCard);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                    .replace(R.id.main_linear, singleGlobalCardFragment)
                    .commit();
        }));

        if (this.inputCodeOperation == CODE_SIMILAR_CARD)
            startFragmentSimilar();
        else if (this.inputCodeOperation == CODE_USER_PUBLIC_CARDS)
            startFragmentGetPublicCards();
    }

    private void startFragmentSimilar()
    {
        this.globalDataFBManager.find_similarCards(
                (CardInfoEntity) this.inputObjectInfoEntity
               , this.customHeaderListView
               , this.listViewComponent);
    }

    private void startFragmentGetPublicCards()
    {
        this.globalDataFBManager.get_publicUserCardList(
                ((GlobalUserInfoEntity) this.inputObjectInfoEntity)
                        .getUserGlobalID()
                , this.customHeaderListView
                , this.listViewComponent);
    }
}