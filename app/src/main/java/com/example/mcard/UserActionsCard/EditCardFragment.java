package com.example.mcard.UserActionsCard;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.CommercialAction.YandexADS.MediationNetworkEntity;
import com.example.mcard.CommercialAction.YandexADS.RewardedMobileMediationManager;
import com.example.mcard.FunctionalInterfaces.DelegateVoidInterface;
import com.example.mcard.FunctionalInterfaces.SingleCardSettings;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView;
import com.example.mcard.GroupServerActions.SubscribeController;
import com.example.mcard.GroupServerActions.SyncGlobalCardsManager;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.GroupServerActions.SyncPersonalManager;

import com.example.mcard.BasicAppActivity;
import com.example.mcard.FunctionalInterfaces.NetworkConnection;
import com.example.mcard.GlobalListeners.NetworkListener;
import com.example.mcard.GlobalListeners.OptionsRulesByNetwork;
import com.example.mcard.R;
import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.CameraController;
import com.example.mcard.RecognitionsFromCard.CardBarcodeRecognition;
import com.example.mcard.RecognitionsFromCard.GeneralRecognitionController;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.FunctionalInterfaces.CorrectInputTextAuth;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.SideFunctionality.GeneralAnimations;
import com.example.mcard.SideFunctionality.ListViewComponent;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.SideFunctionality.RotationCardMaster;

import com.example.mcard.StorageAppActions.SQLiteChanges.HistoryManagerDB;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import com.example.mcard.UserAuthorization.OfflineEntranceActions;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.mcard.GeneralInterfaceApp.MasterDesignCard.getOpposedColor;
import static com.example.mcard.SideFunctionality.RotationCardMaster.getRotationCardActive;
import static com.example.mcard.FragmentsAdditionally.PersonalProfileFragment.CODE_GET_IMAGE_FRAGMENT_INFO;
import static com.example.mcard.BasicAppActivity.setListItemCards;
import static com.example.mcard.GeneralInterfaceApp.MasterDesignCard.MODE_DESIGN_CHECK_DOWNLOAD;
import static com.example.mcard.GeneralInterfaceApp.MasterDesignCard.MODE_GET_PARAM_WIDTH;

import kotlin.Pair;

public final class EditCardFragment extends Fragment implements
 CameraController
 , ColorPickerDialogListener
 , NetworkConnection
 , View.OnClickListener
 , GeneralStructApp
 , SingleCardSettings
 , CorrectInputTextAuth
 , OfflineEntranceActions
{
    private View view, clearView;
    private CardInfoEntity inputSingleCard;
    private ObjectAnimator objectAnimatorCard;

    private AppCompatEditText name_card, number_card;
    private RoundRectCornerImageView barcodeCard;
    private SurfaceView cameraEditBarcode;
    private GeneralRecognitionController generalRecognitionController;
    private RoundRectCornerImageView designCard;

    private CardManagerDB cardManagerDB;
    private MasterDesignCard masterDesignCard;
    private RotationCardMaster rotationCardMaster;
    private DataInterfaceCard dataInterfaceCard;
    private SharedPreferencesManager sharedPreferencesManager;

    private final ListViewComponent listViewComponent;
    private final GeneralAnimations generalAnimations;
    private OptionsRulesByNetwork optionsRulesByNetwork;
    private CustomHeaderListView updateListView;

    private BasicFireBaseManager basicFireBaseManager;
    private SyncPersonalManager syncPersonalManager;
    private SyncGlobalCardsManager syncGlobalCardsManager;
    private GlobalDataFBManager globalDataFBManager;
    private SubscribeController subscribeController;

    private RelativeLayout layoutAnimation;
    private CustomRealtiveLayout cardSize;
    private CardView roundSurfaceView;
    private LinearLayout thisGeneralLayout, layoutAllBtn;

    private AppCompatTextView editColor
            , delete
            , editTextBtn
            , pushCard
            , btnEditBarcode
            , btnCustomDesign;
    private AppCompatButton finish, btnChangeFlashMode;
    private AppCompatImageButton btnBack, changeHistoryBtn;

    private Animation animationText
            , animationLightText
            , animationDisappearanceON
            , animationDisappearanceOFF
            , animationEmergence
            , animationHide;

    private boolean changeDefaultDesign = false
            , blockChangeName = false
            , accessStartBarcodeEdit = true;

    private String thisAccountID;
    private final String ADD_BARCODE = "Добавить штрих-код";
    private final String CHANGE_BARCODE = "Изменить штрих-код";

    private static final char MODE_NAME = 0, MODE_NUMBER = 1;
    private CardInfoEntity.BarcodeEntity barcodeEdit = null;

    public EditCardFragment(GeneralAnimations generalAnimations
            , ListViewComponent listViewComponent)
    {
        this.listViewComponent = listViewComponent;
        this.generalAnimations = generalAnimations;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_edit_card, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.designCard = view.findViewById(R.id.color_card);
        this.editTextBtn = view.findViewById(R.id.edit_text_card);
        this.name_card = view.findViewById(R.id.name_card);
        this.barcodeCard = view.findViewById(R.id.barcode_card);
        this.number_card = view.findViewById(R.id.number_card);
        this.finish = view.findViewById(R.id.editOkay);
        this.editColor = view.findViewById(R.id.btn_startEditColor);
        this.btnEditBarcode = view.findViewById(R.id.btn_editBarcode);
        this.delete = view.findViewById(R.id.btn_deleteCard);
        this.pushCard = view.findViewById(R.id.push_card);
        this.btnBack = view.findViewById(R.id.btn_back);
        this.btnChangeFlashMode = view.findViewById(R.id.btnControlFlashMode);
        this.btnCustomDesign = view.findViewById(R.id.btn_customDesign);
        this.cameraEditBarcode = view.findViewById(R.id.cameraEdit_barcode);
        this.roundSurfaceView = view.findViewById(R.id.roundSurfaceView);
        this.cardSize = view.findViewById(R.id.cardSize);
        this.layoutAnimation = view.findViewById(R.id.surfaceForm);
        this.thisGeneralLayout = view.findViewById(R.id.main_linear_reception);
        this.changeHistoryBtn = view.findViewById(R.id.changeHistoryBtn);
        this.layoutAllBtn = view.findViewById(R.id.layoutAllBtn);
        this.clearView = view.findViewById(R.id.clearView);

        this.cardManagerDB =
                new CardManagerDB(requireContext());
        this.dataInterfaceCard =
                new DataInterfaceCard(requireContext());
        this.sharedPreferencesManager =
                new SharedPreferencesManager(requireContext());

        if (this.checkOfflineEntrance(this.sharedPreferencesManager))
        {
            this.basicFireBaseManager =
                    new BasicFireBaseManager(requireContext());
            this.syncPersonalManager =
                    new SyncPersonalManager(requireContext());
            this.syncGlobalCardsManager = new SyncGlobalCardsManager(
                    requireContext(), this.updateListView);
            this.globalDataFBManager =
                    new GlobalDataFBManager(requireContext());
            this.subscribeController =
                    new SubscribeController(requireContext());
        }

        this.animationText =
                AnimationUtils.loadAnimation(
                        requireContext(), R.anim.select_object);
        this.animationLightText =
                AnimationUtils.loadAnimation(
                        requireContext(), R.anim.light_text);
        this.animationDisappearanceON =
                AnimationUtils.loadAnimation(
                        requireContext(), R.anim.animation_disappearance);
        this.animationDisappearanceOFF =
                AnimationUtils.loadAnimation(
                        requireContext(), R.anim.animation_disappearance_off);
        this.thisAccountID =
                new SharedPreferencesManager(
                        requireContext()).account_id(null);
    }

    @Override
    public void drawableView()
    {
        final ThemeAppController themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , "Данные карты");
        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.main_linear_reception));

        themeAppController.setOptionsButtons(
                this.btnBack
                , this.editColor
                , this.delete
                , this.editTextBtn
                , this.pushCard
                , this.btnEditBarcode
                , this.btnCustomDesign
                , this.changeHistoryBtn
                , this.finish);
    }

    private void initCardAnimator()
    {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        this.requireActivity()
                .getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        final int[] originalPos = new int[2];
        this.cardSize.getLocationOnScreen(originalPos);

        this.objectAnimatorCard = ObjectAnimator.ofFloat(
                        this.cardSize
                        , "y"
                        , displayMetrics.heightPixels / 3 + originalPos[1])
                .setDuration(800);
    }

    private boolean acceptActionChangeDataCard()
    {
        return (this.inputSingleCard.getCardOwner() != null
                && !NetworkListener.getStatusNetwork());
    }

    @Override
    public void basicWork()
    {
        this.settingsCard(this.dataInterfaceCard
                , this.cardSize
                , this.name_card
                , this.number_card);

        setChangeInputText(false);
        this.layoutAnimation.setScaleX(0f);

        this.inputSingleCard = requireArguments()
                .getParcelable(GeneralRulesDB.TRANSACTION_KEY);

        final ArrayList<TextView> allTextView = new ArrayList<>();
        allTextView.add(this.name_card);
        allTextView.add(this.number_card);

        this.masterDesignCard = new MasterDesignCard(
                requireContext(), allTextView);

        this.cardSize.getLayoutParams().width =
                masterDesignCard.calculateNormalSize(
                        MODE_GET_PARAM_WIDTH);

        invalidateCardDesign();
        if (masterDesignCard.availabilityDesign(inputSingleCard.getName()
                , MODE_DESIGN_CHECK_DOWNLOAD) != null)
        {
            this.name_card.setScaleX(0f);
            this.editColor.setHint(
                    requireContext().getString(
                            R.string.removeDesignCard));
        }

        this.number_card.setText(inputSingleCard.getNumber());
        this.name_card.setText(inputSingleCard.getName());
        optionsViewByBarcodeCard();
        initOptionBtnSetColor();
        initCardAnimator();

        this.finish.setOnClickListener(this);
        this.pushCard.setOnClickListener(this);
        this.editColor.setOnClickListener(this);
        this.delete.setOnClickListener(this);
        this.btnEditBarcode.setOnClickListener(this);
        this.editTextBtn.setOnClickListener(this);
        this.btnBack.setOnClickListener(this);
        this.btnCustomDesign.setOnClickListener(this);
        this.cardSize.setOnClickListener(this);
        this.changeHistoryBtn.setOnClickListener(this);

        this.rotationCardMaster = new RotationCardMaster(requireActivity()
                , null
                , this.masterDesignCard
                , this.designCard
                , this.cardSize
                , this.layoutAnimation
                , new Pair(this.name_card, this.name_card.getScaleX() != 0f)
                , this.number_card
                , this.inputSingleCard.getColor());

        if (this.checkOfflineEntrance(this.sharedPreferencesManager))
            this.globalDataFBManager
                    .checkHaveCardInFB(
                            this.inputSingleCard, this.pushCard);
        else
            this.viewOffController(
                    new View[] { pushCard, btnCustomDesign }
                    , click ->
                    {
                        if (!getRotationCardActive())
                            this.showOfferDialogRegistration(
                                    this.requireActivity());
                    });


        final List<AppCompatTextView> listBtn = new ArrayList<>();
        listBtn.add(this.pushCard);
        listBtn.add(this.editColor);
        listBtn.add(this.delete);
        listBtn.add(this.btnEditBarcode);
        listBtn.add(this.editTextBtn);

        this.optionsRulesByNetwork = new OptionsRulesByNetwork(listBtn
                , AnimationUtils.loadAnimation(
                        requireContext()
                , R.anim.anim_button_network),
                (this.inputSingleCard.getCardOwner() != null));
        this.optionsRulesByNetwork.registerListenerNetworkChange();
    }

    public void setColorEdit()
    {
        if (getRotationCardActive())
            this.rotationCardMaster.rotationAction();

        final ColorPickerDialog selectColorInterfaceApp =
                ColorPickerDialog.newBuilder()
                .setShowAlphaSlider(true)
                .setShowColorShades(true)
                .setColorShape(ColorShape.CIRCLE)
                .setDialogTitle(R.string.choose_color)
                .setColor(Color.WHITE)
                .setAllowCustom(true)
                .setAllowPresets(true)
                .setPresetsButtonText(R.string.presets_button_text)
                .setCustomButtonText(R.string.custom)
                .setSelectedButtonText(R.string.select)
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .create();

        selectColorInterfaceApp.setColorPickerDialogListener(this);
        selectColorInterfaceApp.show(requireFragmentManager()
                .beginTransaction()
                .attach(this), "");
    }

    @Override
    public boolean checkNetwork() {
        return NetworkListener.getStatusNetwork();
    }

    @Override
    public void networkListenerRegister() { }
    @Override
    public void networkListenerUnregister() { }

    @Override
    public void stopStream()
    {
        try
        {
            this.cameraEditBarcode.getHolder()
                    .removeCallback(
                            this.generalRecognitionController);

            this.generalRecognitionController
                    .surfaceDestroyed(
                            this.cameraEditBarcode.getHolder());

            this.barcodeCard.setScaleX(1f);
            this.btnChangeFlashMode.setScaleX(0f);
            this.accessStartBarcodeEdit = true;
        } catch (Exception ignored) {}
    }

    @Override
    public void startStream()
    {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(false)
                    .setTitle("Предоставление разрешений")
                    .setMessage(getString(R.string.messagePermissionCamera), 4f)
                    .setPositiveButton("Далее", (click) ->
                            ActivityCompat.requestPermissions(requireActivity()
                                    , (new String[] {Manifest.permission.CAMERA})
                                    , BasicAppActivity.PermissionCodes.ACCESS_CAMERA))
                    .setNegativeButton("Отмена", null)
                    .show();
            return;
        }

        this.barcodeEdit = null;
        this.btnChangeFlashMode.setScaleX(1f);

        setBtnEditBarcode();
        barcodeEditListener();
    }

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v)
        {
            v.startAnimation(animationText);

            if (v.getId() == R.id.btn_back)
                requireActivity().onBackPressed();
            else if (v.getId() == R.id.changeHistoryBtn)
                writeHistoryAction();
            else if (v.getId() == R.id.cardSize)
            {
                moveViewToScreenCenter(!getRotationCardActive());
                stopStream();
            }

            if (!getRotationCardActive())
            {
                if (v.getId() == R.id.btn_customDesign)
                    setBtn_customDesign();
                else if (v.getId() ==  R.id.editOkay)
                    setBtnFinish();
                else if (v.getId() == R.id.btn_startEditColor
                        && this.editColor.
                        getText()
                        .toString()
                        .equals(requireContext()
                                .getString(R.string.removeDesignCard)))
                    askUserRemoveDesign();
                else if (warningAcceptChangeGlobalCard())
                    Toast.makeText(requireContext()
                                    , requireContext().getString(R.string.dontAcceptChangeCard)
                                    , Toast.LENGTH_LONG)
                            .show();
                else switch (v.getId())
                    {
                        case R.id.btn_startEditColor:
                            setColorEdit();
                            break;

                        case R.id.btn_deleteCard:
                            setBtnDelete();
                            break;

                        case R.id.edit_text_card:
                            setEdit_textBtn();
                            break;

                        case R.id.push_card:
                            if (checkAccessChangeDataCard())
                                setBtnPushCard();
                            break;

                        case R.id.btn_editBarcode:
                            startStream();
                            break;
                    }
            }
        }

        private void writeHistoryAction()
        {
            new HistoryManagerDB(requireContext())
                    .writeNewHistoryDB(
                            inputSingleCard.getName()
                            , this.updateListView.getAddressShop(
                                    this.inputSingleCard));
            Toast.makeText(
                    requireContext()
                    , R.string.infoSuccessHistoryCard
                    , Toast.LENGTH_LONG)
                    .show();

            this.changeHistoryBtn.setClickable(false);
            this.changeHistoryBtn.setAnimation(
                    AnimationUtils.loadAnimation(
                            requireContext(), R.anim.light_text));
            this.changeHistoryBtn
                    .getAnimation()
                    .setAnimationListener(new Animation.AnimationListener()
                    {
                        @Override
                        public void onAnimationStart(Animation animation) {}
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation)
                        { changeHistoryBtn.setScaleX(0f); }
            });
        }

        private void askUserRemoveDesign()
        {
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(true)
                    .setTitle(getString(R.string.infoAcceptAction))
                    .setMessage(R.string.askRemoveDesignCard, 3.25f)
                    .setPositiveButton("Удалить", (click) ->
                        removeAltDesignData((checkAltDesign() ?
                                        inputSingleCard.getName()
                                        : name_card.getText().toString())
                                , true))
                    .setNegativeButton("Отмена", null)
                    .show();
        }

        private void invalidateCardDesign()
        {
            this.masterDesignCard
                    .setCardDesignLocale(
                            inputSingleCard.getName()
                            , inputSingleCard.getColor()
                            , designCard);
        }

    synchronized private void removeAltDesignData(String card_name
            , boolean actionReplaceFunctColor)
    {
        final String cardFullNameData = this.masterDesignCard
                .availabilityDesign(inputSingleCard.getName()
                        , MasterDesignCard.MODE_DESIGN_CHECK_ALL);

        if (actionReplaceFunctColor
                || (cardFullNameData != null))
        {
            if (new File(requireContext().getCacheDir()
                    , cardFullNameData)
                    .delete())
            {
                this.name_card.setText(inputSingleCard.getName());
                invalidateCardDesign();

                this.editColor.setText(R.string.changeColor);
                this.name_card.setScaleX(1f);
            }
        }

        if (actionReplaceFunctColor)
            this.editColor.setText(R.string.changeColor);
    }

    private void initOptionBtnSetColor()
    {
        String cardName = this.name_card
                .getText()
                .toString();

        if (cardName.equals(""))
        {
            this.blockChangeName = true;
            setChangeInputText(false);
            this.editColor.setText(R.string.removeDesignCard);
        }
        else if (this.masterDesignCard.availabilityDesign(
                cardName
                , MasterDesignCard.MODE_DESIGN_CHECK_CUSTOM) != null)
            this.editColor.setText(requireContext()
                    .getString(R.string.removeDesignCard));
    }

    private void setBtnEditBarcode()
    {
        if (!accessStartBarcodeEdit)
            return;
        else if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
        {
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(false)
                    .setTitle("Предоставление разрешений")
                    .setMessage(R.string.messageActionChangeBarcode, 5f)
                    .setPositiveButton("Далее", (click) ->
                            ActivityCompat.requestPermissions(requireActivity()
                                    , new String[] {Manifest.permission.CAMERA}
                                    , BasicAppActivity.PermissionCodes.ACCESS_CAMERA)
                    )
                    .setNegativeButton("Отмена", null)
                    .show();
            return;
        }

        if (!getRotationCardActive())
            moveViewToScreenCenter(!getRotationCardActive());

        this.roundSurfaceView.setScaleX(1f);
        this.barcodeCard.setScaleX(0f);
        accessStartBarcodeEdit = false;

        this.generalRecognitionController =
                new GeneralRecognitionController(this.cameraEditBarcode
                        , this::setBtnEditBarcode
                        , new kotlin.Pair(
                                requireContext(), new kotlin.Pair(
                                        this.btnChangeFlashMode, this.animationText))
                        , null
                        , null
                        , new BarcodeDetector.Builder(requireContext())
                        .setBarcodeFormats(Barcode.ALL_FORMATS)
                        .build()
                        , null
                );

        this.cameraEditBarcode.getHolder().addCallback(
                this.generalRecognitionController.setCameraSourceAndReturnCallback(
                        GeneralRecognitionController.ACTION_RECOGNITION_BARCODE
                        , requireContext()
                        ,     new CardBarcodeRecognition((inputEncodeBarcode) ->
                        {
                            this.barcodeEdit =
                                    ((CardInfoEntity.BarcodeEntity) inputEncodeBarcode);

                            this.cameraEditBarcode.setScaleX(0f);
                            this.barcodeCard.setScaleX(1f);

                            this.barcodeCard.setImageBitmap(
                                    MasterDesignCard.CardBarcodeManager.setCardBarcode(
                                            this.barcodeEdit));
                        }, this.view.findViewById(R.id.graphicOverlay))));
    }

    private boolean checkAltDesign()
    {
        return this.name_card
            .getText()
            .toString()
            .equals("");
    }

    private void setBtnFinish()
    {
        final String name, number;
        final int color;

        name = checkAltDesign() ? this.inputSingleCard.getName()
                : name_card.getText().toString();

        number = number_card.getText().toString();
        color = rotationCardMaster.getColorCard();

        if (userChangeDataCard(name, number, color))
        {
            if (!this.checkCorrectPassword(name
                    , MIN_BOUND_CARD_INFO
                    , MAX_BOUND_CARD_INFO))
            {
                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.errorInputNameCard)
                        , Toast.LENGTH_SHORT).show();
                return;
            }
            else if (!this.checkCorrectPassword(number
                    , MIN_BOUND_CARD_INFO
                    , MAX_BOUND_CARD_INFO))
            {
                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.errorInputNumberCard)
                        , Toast.LENGTH_SHORT).show();
                return;
            }
            else if (warningAcceptChangeGlobalCard())
            {
                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.dontAcceptChangeCard)
                        , Toast.LENGTH_LONG).show();
                return;
            }

            final CardInfoEntity updateDataCard = new CardInfoEntity(number
                    , name
                    , (barcodeEdit == null ? inputSingleCard.getBarcode()
                    : barcodeEdit.toString())
                    , color
                    , inputSingleCard.getCardOwner()
                    , inputSingleCard.getDateAddCard()
                    , inputSingleCard.getUniqueIdentifier());

            this.cardManagerDB.replaceObjectDB(
                    updateDataCard);

            setListItemCards(requireContext()
                    , this.cardManagerDB
                    , this.updateListView
                    , false);

            if (this.checkOfflineEntrance(this.sharedPreferencesManager))
                    this.syncGlobalCardsManager.saveCardInGlobalDB();
        }

       if(!name.equals(inputSingleCard.getName()))
           this.masterDesignCard.changeCardName(
                   inputSingleCard.getName(), name);
       requireActivity().onBackPressed();
    }

    private boolean warningAcceptChangeGlobalCard()
    {
       return (!NetworkListener.getStatusNetwork()
                && inputSingleCard.getCardOwner()
               != null);
    }

    private void setBtnDelete()
    {
        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle(getString(R.string.infoAcceptAction))
                .setMessage(R.string.messageRemoveCard, 4.75f)
                .setPositiveButton("Удалить", (click) ->
                {
                    removeAltDesignData(
                            inputSingleCard.getName()
                            , false);

                    this.cardManagerDB.removeObjectDB(
                            this.inputSingleCard.getUniqueIdentifier());

                    if (this.checkOfflineEntrance(this.sharedPreferencesManager))
                    {
                        this.globalDataFBManager.removeCardInFB(inputSingleCard
                                , operationRefreshCard()
                                , pushCard);
                            this.syncGlobalCardsManager.saveCardInGlobalDB();
                    }

                    setListItemCards(requireContext()
                            , this.cardManagerDB
                            , this.updateListView
                            , false);
                    requireActivity().onBackPressed();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private boolean checkAccessChangeDataCard()
    {
        if ((inputSingleCard.getCardOwner() != null)
                && (!thisAccountID.equals(
                        inputSingleCard.getCardOwner())))
        {
            Toast.makeText(requireContext()
                    , requireContext().getText(R.string.warningAccessChangeCard)
                            + " \"" + inputSingleCard.getCardOwner() + "\""
                    , Toast.LENGTH_LONG).show();
            return false;
        }
        else return true;
    }

    private void setEdit_textBtn()
    {
        if (getRotationCardActive())
            this.rotationCardMaster.rotationAction();

        this.name_card.startAnimation(this.animationLightText);
        this.number_card.startAnimation(this.animationLightText);

        setChangeInputText(true);
        this.name_card.setFocusableInTouchMode(true);
        this.number_card.setFocusableInTouchMode(true);
    }

    private void setChangeInputText(boolean edit_text)
    {
        this.name_card.setFocusable(!this.blockChangeName && edit_text);
        this.name_card.setLongClickable(!this.blockChangeName && edit_text);
        this.name_card.setCursorVisible(!this.blockChangeName && edit_text);

        this.number_card.setFocusable(edit_text);
        this.number_card.setLongClickable(edit_text);
        this.number_card.setCursorVisible(edit_text);
    }

    private void setBtnPushCard()
    {
        if (NetworkListener.getStatusNetwork())
        {
            if (pushCard.getText()
                    .equals("Поделиться"))
                publishCardDialog();
            else
                this.globalDataFBManager.removeCardInFB(this.inputSingleCard
                        , operationRefreshCard()
                        , pushCard);
        }
        else
            Toast.makeText(requireContext()
                , R.string.offlineNetworkMSG
                , Toast.LENGTH_SHORT)
                    .show();
    }

    private void publishCardDialog()
    {
        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle(getString(R.string.infoAcceptAction))
                .setMessage(R.string.InfoPushCard, 5.25f)
                .setPositiveButton("Согласен", (click) ->
                        globalDataFBManager.pushCardToFB(this.inputSingleCard
                                , operationRefreshCard()
                                , this.pushCard))
                .setNegativeButton("Отмена", null)
                .show();
    }

    private DelegateVoidInterface operationRefreshCard()
    {
        return (inputSingleCard) ->
        {
            this.cardManagerDB.replaceObjectDB(
                    (CardInfoEntity) inputSingleCard);
            this.globalDataFBManager
                    .updateLocaleOwnerInfo(
                            ((CardInfoEntity) inputSingleCard).getUniqueIdentifier());
            this.optionsRulesByNetwork
                    .updateDataOwner(
                            ((CardInfoEntity) inputSingleCard).getCardOwner());
            try
            {
                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.successGlobalSyncCard)
                        , Toast.LENGTH_SHORT)
                        .show();

                setListItemCards(requireContext()
                        , this.cardManagerDB
                        , this.updateListView
                        , false);
            } catch (IllegalStateException ignore) {}
        };
    }

    private void setBtn_customDesign()
    {
        if (getRotationCardActive())
            this.rotationCardMaster.rotationAction();

        if (!this.subscribeController.checkAddPersonalDesign())
            RewardedMobileMediationManager.buildAndShowAd(
                    requireContext()
                    , new MediationNetworkEntity(
                            R.string.warningAddDesignCard
                            , null
                            , null
                            , this::addPersonalDesignCard));
        else if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED))
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(false)
                    .setTitle("Предоставление разрешений")
                    .setMessage(R.string.messageActionExStorageImage, 4.5f)
                    .setPositiveButton("Далее", (click) ->
                            ActivityCompat.requestPermissions(requireActivity()
                                    , new String[] {
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                            , Manifest.permission.WRITE_EXTERNAL_STORAGE }
                                    , BasicAppActivity.PermissionCodes.ACCESS_STORAGE_CARD_DESIGN)
                    )
                    .setNegativeButton("Отмена", null)
                    .show();
        else addPersonalDesignCard();
    }

    private synchronized void addPersonalDesignCard()
    {
        final Intent intent = new Intent();
        intent.setType(requireContext()
                .getString(R.string.setImageProgram));
        intent.setAction(Intent.ACTION_PICK);

        this.startActivityForResult(Intent.createChooser(intent
                , requireContext().getString(R.string.selectPhotoDesignCard))
                , CODE_GET_IMAGE_FRAGMENT_INFO);
    }

    private boolean userChangeDataCard(
            String nameCard
            , String numberCard
            , int colorCard)
    {
       return (!nameCard.equals(inputSingleCard.getName())
               || !numberCard.equals(inputSingleCard.getNumber())
               || barcodeEdit != null
               || colorCard != inputSingleCard.getColor());
    }

    @Override
    public void onActivityResult(int requestCode
            , int resultCode
            , @Nullable @org.jetbrains.annotations.Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_GET_IMAGE_FRAGMENT_INFO
                && data != null)
        {
            removeAltDesignData(
                    name_card.getText().toString()
                    , false);

            this.masterDesignCard.loadNewPersonalCardDesign(
                    this.inputSingleCard.getName()
                    , data
                    , this.designCard
                    , this.layoutAnimation
                    , this.animationDisappearanceON
                    , this.animationDisappearanceOFF);
            this.editColor.setHint(requireContext()
                    .getString(R.string.removeDesignCard));
        }
    }

    public void setUpdateListView(CustomHeaderListView updateListView)
    { this.updateListView = updateListView; }

    public RotationCardMaster getRotationCardMaster()
    { return rotationCardMaster; }

    public void setChangeDefaultDesign(boolean changeDefaultDesign)
    { this.changeDefaultDesign = changeDefaultDesign; }

    public RoundRectCornerImageView getDesignCard()
    { return designCard; }

    @SuppressLint("StaticFieldLeak")
    private void barcodeEditListener()
    {
        new AsyncTask<Void, Void, Boolean>()
        {
            @Override
            protected Boolean doInBackground(Void... voids)
            {
                for (;;)
                    if (barcodeEdit != null)
                    {
                        stopStream();
                        return true;
                    }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean)
            {
                super.onPostExecute(aBoolean);
                if (aBoolean)
                {
                    stopStream();
                    Toast.makeText(requireContext()
                            , requireContext().getString(
                                    R.string.infoSuccessEditBarcode)
                            , Toast.LENGTH_LONG).show();

                    btnEditBarcode.setText(CHANGE_BARCODE);
                    btnChangeFlashMode.setScaleX(0f);
                    accessStartBarcodeEdit = true;
                }
            }
        }.execute();
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void optionsViewByBarcodeCard()
    {
        if (Objects.equals(
                inputSingleCard.getBarcode()
                , CardManagerDB.BARCODE_NONE))
            this.btnEditBarcode.setHint(this.ADD_BARCODE);
        else
        {
            this.barcodeCard.setImageBitmap(MasterDesignCard.CardBarcodeManager
                    .setCardBarcode(
                            CardInfoEntity
                                    .BarcodeEntity
                                    .serialaziableEntityBarcode(
                                            this.inputSingleCard.getBarcode())));

            this.barcodeCard.setColorFilter((
                            Color.red(inputSingleCard.getColor()) <= 160
                                    && Color.green(inputSingleCard.getColor()) <= 160
                                    && Color.blue(inputSingleCard.getColor()) <= 160)
                            ? Color.BLACK : getOpposedColor(
                            inputSingleCard.getColor())
                    , PorterDuff.Mode.ADD);
        }
    }

    @Override
    public void onColorSelected(int dialogId, int color)
    {
        final Bitmap setColor = Bitmap.createBitmap(1
                , 1
                , Bitmap.Config.ARGB_8888);

        setColor.setPixel(0, 0, color);
        getDesignCard().setImageBitmap(setColor);
        setChangeDefaultDesign(true);
        getRotationCardMaster().setColorCard(color);

        getRotationCardMaster()
                .getMasterDesignCard()
                .colorCardText(
                        setColor, true);
    }

    @Override
    public void onDialogDismissed(int dialogId) { }

    @Override
    public void onDestroyView()
    {
        this.listViewComponent
                .resumeCardAnim(this.generalAnimations);
        super.onDestroyView();
    }

    @Override
    public void onDestroy()
    {
        this.requireArguments().clear();
        stopStream();
        this.rotationCardMaster.rotationClearRAM();

        this.optionsRulesByNetwork
                .unRegisterListenerNetworkChange();
        barcodeEdit = null;
        super.onDestroy();
    }

    @Override
    public void onPause()
    {
        stopStream();
        super.onPause();
    }


    private void moveViewToScreenCenter(boolean animationMode)
    {
        this.rotationCardMaster.rotationAction();

        if (animationMode)
            this.objectAnimatorCard.start();
        else
            this.objectAnimatorCard.reverse();

        this.layoutAllBtn.startAnimation(
                AnimationUtils.loadAnimation(
                        requireContext()
                        , animationMode ? R.anim.fade_launch : R.anim.fade_break));
        this.clearView.startAnimation(
                AnimationUtils.loadAnimation(
                        requireContext()
                        , animationMode ? R.anim.fade_launch : R.anim.fade_break));
        this.layoutAllBtn
                .getAnimation()
                .setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                if (!animationMode)
                {
                    layoutAllBtn.setScaleX(1f);
                    clearView.setScaleY(1f);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                if (animationMode)
                {
                    layoutAllBtn.setScaleX(0f);
                    clearView.setScaleY(0f);
                }
            }
        });
    }
}