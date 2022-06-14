package com.example.mcard.UserActionsCard;

import static com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB.TRANSACTION_KEY;
import static com.example.mcard.GeneralInterfaceApp.MasterDesignCard.MODE_DESIGN_CHECK_DOWNLOAD;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.FragmentsAdditionally.SimilarCardsFragment;
import com.example.mcard.FragmentsAdditionally.PersonalProfileFragment;
import com.example.mcard.FragmentsAdditionally.GlobalProfileUserFragment;
import com.example.mcard.FunctionalInterfaces.SingleCardSettings;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView;
import com.example.mcard.GroupServerActions.SyncGlobalCardsManager;
import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.R;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralAnimations;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.SideFunctionality.ListViewComponent;
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;

import java.util.ArrayList;

public final class SingleGlobalCardFragment extends Fragment implements
        GeneralStructApp, SingleCardSettings
{
    private View view;
    private RelativeLayout barcodeForm;
    private CustomRealtiveLayout cardForm;

    private AppCompatImageButton btnBack;
    private AppCompatButton btnComplete;
    private AppCompatTextView btnImportCard
            , btnOpenUserProfile
            , btnOfferSimilarCards
            , cardName
            , cardNumber;
    private RoundRectCornerImageView designCard, cardBarcode;
    private boolean singleImportAction = false;

    private Animation animClick;
    private CardManagerDB cardManagerDB;

    private ArrayList<TextView> allTextView;
    private MasterDesignCard masterDesignCard;
    private DataInterfaceCard dataInterfaceCard;

    private BasicFireBaseManager basicFireBaseManager;
    private GlobalDataFBManager globalDataFBManager;
    private SyncGlobalCardsManager syncGlobalCardsManager;
    private SharedPreferencesManager sharedPreferencesManager;
    private CardInfoEntity transactionCardInfo;

    private final GeneralAnimations generalAnimations;
    private final ListViewComponent listViewComponent;

    public SingleGlobalCardFragment(GeneralAnimations generalAnimations
            , ListViewComponent listViewComponent)
    {
        this.listViewComponent = listViewComponent;
        this.generalAnimations = generalAnimations;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_single_global_card, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.btnBack = view.findViewById(R.id.btn_back);
        this.btnComplete = view.findViewById(R.id.btn_complete);
        this.btnImportCard = view.findViewById(R.id.import_card);
        this.btnOpenUserProfile = view.findViewById(R.id.globalUser_profile);
        this.btnOfferSimilarCards = view.findViewById(R.id.offer_similarCards);

        this.cardForm = view.findViewById(R.id.cardSize);
        this.barcodeForm = view.findViewById(R.id.surfaceForm);

        this.cardName = view.findViewById(R.id.name_card);
        this.cardNumber = view.findViewById(R.id.number_card);
        this.designCard = view.findViewById(R.id.color_card);
        this.cardBarcode = view.findViewById(R.id.barcode_card);
        this.animClick = AnimationUtils.loadAnimation(
                requireContext(), R.anim.select_object);

        this.cardManagerDB =
                new CardManagerDB(requireContext());
        this.basicFireBaseManager =
                new BasicFireBaseManager(requireContext());
        this.globalDataFBManager =
                new GlobalDataFBManager(requireContext());
        this.syncGlobalCardsManager =
                new SyncGlobalCardsManager(requireContext(), null);
        this.sharedPreferencesManager =
                new SharedPreferencesManager(requireContext());

        this.transactionCardInfo = requireArguments()
                .getParcelable(TRANSACTION_KEY);
        this.dataInterfaceCard =
                new DataInterfaceCard(requireContext());

        this.allTextView = new ArrayList<>();
        allTextView.add(this.cardName);
        allTextView.add(this.cardNumber);

        this.masterDesignCard = new MasterDesignCard(
                requireContext(), this.allTextView);

        this.cardName.setTypeface(Typeface.SERIF);
        this.cardNumber.setTypeface(Typeface.SERIF);
    }

    @Override
    public void drawableView()
    {
        this.settingsCard(this.dataInterfaceCard
                , this.cardForm
                , this.cardName
                , this.cardNumber);

        this.masterDesignCard.setCardDesignLocale(
                this.transactionCardInfo.getName(),
                this.transactionCardInfo.getColor()
                , this.designCard);

        if (masterDesignCard.availabilityDesign(
                transactionCardInfo.getName()
                , MODE_DESIGN_CHECK_DOWNLOAD) == null)
            this.allTextView.get(0)
                    .setText(this.transactionCardInfo.getName());

        this.allTextView.get(1)
                .setText(this.transactionCardInfo.getNumber());

        this.barcodeForm.setScaleX(0f);

        if (!this.transactionCardInfo.getBarcode().equals("-"))
        this.cardBarcode.setImageBitmap(
                MasterDesignCard.CardBarcodeManager.setCardBarcode(
                        CardInfoEntity.BarcodeEntity.serialaziableEntityBarcode(
                                this.transactionCardInfo.getBarcode())));

        final ThemeAppController themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.main_linear));
        themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , "Данные карты");

        themeAppController.setOptionsButtons(
                btnBack
                , btnImportCard
                , btnOpenUserProfile
                , btnOfferSimilarCards);
    }

    @Override
    public void basicWork()
    {
        this.btnComplete.setOnClickListener(v -> { v.startAnimation(animClick); requireActivity().onBackPressed(); });
        this.btnBack.setOnClickListener(v -> { v.startAnimation(animClick); requireActivity().onBackPressed(); });
        this.btnImportCard.setOnClickListener(v -> { v.startAnimation(animClick); setImport_card(); });
        this.btnOpenUserProfile.setOnClickListener(v -> { v.startAnimation(animClick); setBtn_openUserProfile(); });
        this.btnOfferSimilarCards.setOnClickListener(v -> { v.startAnimation(animClick); setBtn_offerSimilarCards(); });

        this.cardForm.setOnClickListener(v ->
                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.warningImportGlobalCard)
                        , Toast.LENGTH_LONG)
                        .show());
    }

    private void setImport_card()
    {
        this.globalDataFBManager.requestCardsActions(null
                , null
                , this.transactionCardInfo
                , GlobalDataFBManager.MODE_PUSH_IMPORT);
        this.singleImportAction = true;
    }

    private void setBtn_openUserProfile()
    {
        this.globalDataFBManager
                .getCardOwner(
                        this.transactionCardInfo.getUniqueIdentifier()
                        , inputObject ->
                                startOpenProfile((GlobalUserInfoEntity) inputObject
                                        , requireActivity()));
    }

    public void startOpenProfile(GlobalUserInfoEntity inputGlobalUserInfo
            , FragmentActivity thisFragment)
    {
        if (!new SharedPreferencesManager(thisFragment)
                .account_id(null)
                .equals(inputGlobalUserInfo.getUserGlobalID()))
        {
            final Bundle bundle = new Bundle();
            bundle.putSerializable(TRANSACTION_KEY, inputGlobalUserInfo);

            final GlobalProfileUserFragment globalProfileUserFragment =
                    new GlobalProfileUserFragment();
            globalProfileUserFragment.setArguments(bundle);

            thisFragment.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                    .replace(R.id.main_linear, globalProfileUserFragment)
                    .commit();
        }
        else
        {
            thisFragment.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                    .replace(R.id.main_linear, new PersonalProfileFragment())
                    .commit();
        }
    }

    private void setBtn_offerSimilarCards()
    {
        final SimilarCardsFragment similarCardsFragment =
                new SimilarCardsFragment(
                        SimilarCardsFragment.CODE_SIMILAR_CARD);
        final Bundle transactionOutputData = new Bundle();

        transactionOutputData.putParcelable(
                TRANSACTION_KEY, this.transactionCardInfo);
        similarCardsFragment.setArguments(transactionOutputData);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                .replace(R.id.global_linear_menu, similarCardsFragment)
                .commit();
    }

    @Override
    public void onDestroyView()
    {
        try
        {
            this.listViewComponent
                    .resumeCardAnim(this.generalAnimations);
        } catch (NullPointerException ignored) {}

        super.onDestroyView();
    }

    @Override
    public void onDestroy()
    {
        this.allTextView.clear();
        super.onDestroy();
    }

    @Override
    public void optionsViewByBarcodeCard() { }
}