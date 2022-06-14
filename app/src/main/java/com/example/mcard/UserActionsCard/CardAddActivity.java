package com.example.mcard.UserActionsCard;

import static com.example.mcard.GroupServerActions.SubscribeController.realDate;
import static com.example.mcard.GroupServerActions.SubscribeController.toDate;
import static com.example.mcard.SideFunctionality.RotationCardMaster.getRotationCardActive;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.GroupServerActions.SyncGlobalCardsManager;
import com.example.mcard.GroupServerActions.SyncPersonalManager;
import com.example.mcard.FunctionalInterfaces.NetworkConnection;
import com.example.mcard.GlobalListeners.NetworkListener;
import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.CameraController;
import com.example.mcard.RecognitionsFromCard.ImageColourManager;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.FunctionalInterfaces.CorrectInputTextAuth;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard;
import com.example.mcard.BasicAppActivity;
import com.example.mcard.R;
import com.example.mcard.RecognitionsFromCard.CardBarcodeRecognition;
import com.example.mcard.RecognitionsFromCard.CardTextRecognition;
import com.example.mcard.RecognitionsFromCard.GeneralRecognitionController;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.RotationCardMaster;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import com.example.mcard.UserAuthorization.OfflineEntranceActions;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.text.TextRecognizer;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;

public final class CardAddActivity extends AppCompatActivity implements TextWatcher
 , View.OnClickListener
 , ColorPickerDialogListener
 , NetworkConnection
 , CameraController
 , GeneralStructApp
 , CorrectInputTextAuth
 , OfflineEntranceActions
{
    private AppCompatButton btnAlternativeDesign
     , btnCardColor
     , btnControlFlashMode
     , btnBack;
    private AppCompatImageButton topBtnBack;

    private EditText cardName;
    private TextView numberUserCard, textBarcode;
    private TextView name_cardComplete, number_cardComplete;
    private AppCompatTextView messageActionInfoTV;

    private RoundRectCornerImageView barcodeCardComplete, cardDesign;
    private ImageView barcodeImage;
    private SurfaceView preview;
    private ColorStateList color_prism;
    private RelativeLayout surfaceFormLayout;
    private View clearView, clearViewRound;

    private GeneralRecognitionController generalRecognitionController;
    private NetworkListener networkListener;
    private MasterDesignCard masterDesignCard;
    private SharedPreferencesManager sharedPreferencesManager;

    private BasicFireBaseManager basicFireBaseManager;
    private SyncPersonalManager syncPersonalManager;
    private SyncGlobalCardsManager syncGlobalCardsManager;

    private RotationCardMaster rotationCardMaster;
    private Animation animSelected;
    private CardInfoEntity.BarcodeEntity encodeBarcodeCard;

    public static final String TEXT_BTN_FINISH_STAGE_2 = "Добавить карту";
    private final String TEXT_BTN_FINISH_COLOR = "Получить цвет";
    public static final String SELECT_MODE = "mode";

    private boolean oneOperation = true
     , accessFindBarcode = true
     , accessColorCard = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_add);

        findObjects();
        drawableView();
        basicWork();
    }

    @Override
    public void findObjects()
    {
        this.cardName = findViewById(R.id.name_card);
        this.preview = findViewById(R.id.camera);
        this.numberUserCard = findViewById(R.id.text_card);
        this.textBarcode = findViewById(R.id.barcode_result);
        this.btnBack = findViewById(R.id.btn_true);
        this.topBtnBack = findViewById(R.id.top_back);
        this.barcodeImage = findViewById(R.id.set_complete);
        this.btnAlternativeDesign = findViewById(R.id.alternative_design);
        this.btnCardColor = findViewById(R.id.color_signal);
        this.btnControlFlashMode = findViewById(R.id.btnControlFlashMode);

        this.clearView = findViewById(R.id.clear);
        this.clearViewRound = findViewById(R.id.roundSurfaceView);
        this.surfaceFormLayout = findViewById(R.id.surfaceForm);

        this.cardDesign = findViewById(R.id.color_card_complete);
        this.name_cardComplete = findViewById(R.id.NameTV);
        this.number_cardComplete = findViewById(R.id.NumberTV);
        this.barcodeCardComplete = findViewById(R.id.barcode_card);
        this.messageActionInfoTV = findViewById(R.id.messageActionInfo);

        this.animSelected = AnimationUtils.loadAnimation(
                CardAddActivity.this, R.anim.select_object);
        this.color_prism = ColorStateList.valueOf(
                Color.parseColor("#00000000"));
        initModeAdd(getIntent().getStringExtra(SELECT_MODE));
        this.sharedPreferencesManager =
                new SharedPreferencesManager(this);

        if (this.checkOfflineEntrance(this.sharedPreferencesManager))
        {
            this.basicFireBaseManager =
                    new BasicFireBaseManager(this);
            this.syncPersonalManager =
                    new SyncPersonalManager(this);
            this.syncGlobalCardsManager =
                    new SyncGlobalCardsManager(this, null);
        }

        this.generalRecognitionController =
                new GeneralRecognitionController(this.preview
                        , this::errorStage
                        , (!accessColorCard && !accessFindBarcode && !accessColorCard)
                        ? null : new kotlin.Pair(
                                this, new kotlin.Pair(
                                        this.btnControlFlashMode, this.animSelected))
                        , this.btnCardColor
                        , new TextRecognizer.Builder(this).build()
                        , new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.ALL_FORMATS).build()
                        , this::setReadyCard
                );
    }

    @Override
    public void drawableView()
    {
        final ThemeAppController themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(this));

        themeAppController.changeDesignDefaultView(
                findViewById(R.id.main_linear));

        themeAppController.setOptionsButtons(
                btnAlternativeDesign
                , topBtnBack
                , btnBack
                , messageActionInfoTV);
    }

    @Override
    public void basicWork()
    {
        networkListenerRegister();
        this.messageActionInfoTV.setAnimation(
                AnimationUtils.loadAnimation(
                        this, R.anim.light_selected_btn));

        if (this.accessColorCard)
        {
            this.numberUserCard.addTextChangedListener(this);
            startStream();
        }
        else setReadyCard();

        if (this.accessFindBarcode)
            this.textBarcode.addTextChangedListener(this);

        this.btnBack.setOnClickListener(this);
        this.topBtnBack.setOnClickListener(this);
        this.btnAlternativeDesign.setOnClickListener(this);
        this.btnCardColor.setOnClickListener(this);
    }

    private void startBarcodeDetector()
    {
        if (!this.accessFindBarcode)
        {
            actionAskColor();
            return;
        }

        this.messageActionInfoTV.setText(
                R.string.stageScanBarcodeCard);

        this.preview.getHolder().addCallback(
                this.generalRecognitionController.setCameraSourceAndReturnCallback(
                        GeneralRecognitionController.ACTION_RECOGNITION_BARCODE
                        , this
                        , new CardBarcodeRecognition((inputEncodeBarcode) ->
                        {
                            this.encodeBarcodeCard =
                                    ((CardInfoEntity.BarcodeEntity) inputEncodeBarcode);

                            this.barcodeImage.setBackgroundResource(
                                    R.drawable.ic_complete);
                            this.textBarcode.setText(
                                    this.encodeBarcodeCard.getBarcodeDataString());

                            if (this.encodeBarcodeCard != null)
                                this.barcodeCardComplete.setImageBitmap(
                                        MasterDesignCard.CardBarcodeManager.setCardBarcode(
                                                this.encodeBarcodeCard));
                        }, findViewById(R.id.graphicOverlay))));
    }

    private void startTextRecognition()
    {
        this.messageActionInfoTV.setText(
                R.string.stageScanNumberCard);

        final CardTextRecognition cardTextRecognition =
                new CardTextRecognition(
                        findViewById(R.id.graphicOverlay));
        cardTextRecognition.setTextFind(this.numberUserCard);

        this.preview.getHolder().addCallback(
                this.generalRecognitionController.setCameraSourceAndReturnCallback(
                        GeneralRecognitionController.ACTION_RECONGNITION_TEXT
                        , this
                        , cardTextRecognition));
    }

    private void errorStage()
    {
        Toast.makeText(this
                , getString(R.string.ErrorStage)
                , Toast.LENGTH_LONG)
                .show();
        onBackPressed();
    }

    private void setReadyCard()
    {
        changeVisibleItem(0f);
        this.btnControlFlashMode.setScaleX(0f);
        this.btnBack.setHint(TEXT_BTN_FINISH_STAGE_2);

        colorAction(false
                , generalRecognitionController.getColor());
        this.btnCardColor.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.light_text));

        if (!this.accessFindBarcode)
        {
            this.btnBack.setHint(TEXT_BTN_FINISH_STAGE_2);
            this.barcodeCardComplete.setScaleX(0f);
            this.numberUserCard.setInputType(InputType.TYPE_CLASS_PHONE);
        }

        this.number_cardComplete.setText(
                numberUserCard.getText().toString());
        this.name_cardComplete.setText(
                cardName.getText().toString());

        final ArrayList<TextView> containerTransactionView =
                new ArrayList<>();
        final CustomRealtiveLayout formCard =
                findViewById(R.id.cardSize);

        containerTransactionView.add(this.name_cardComplete);
        containerTransactionView.add(this.number_cardComplete);
        this.name_cardComplete.setTypeface(Typeface.SERIF);
        this.number_cardComplete.setTypeface(Typeface.SERIF);

        this.rotationCardMaster = new RotationCardMaster(null
                , CardAddActivity.this
                , new MasterDesignCard(this
                , containerTransactionView)
                , this.cardDesign
                , formCard
                , this.surfaceFormLayout
                , new Pair(this.name_cardComplete, this.name_cardComplete.getScaleX() != 0f)
                , this.number_cardComplete
                , generalRecognitionController.getColor());

        formCard.setOnClickListener(v ->
                this.rotationCardMaster.rotationAction());

        if (this.accessColorCard)
        Toast.makeText(this
                , this.getString(R.string.ReadyCard)
                , Toast.LENGTH_LONG)
                .show();

        this.cardName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            { name_cardComplete.setText(cardName.getText().toString()); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        this.numberUserCard.removeTextChangedListener(this);
        this.numberUserCard.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            { number_cardComplete.setText(numberUserCard.getText().toString()); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

            this.messageActionInfoTV.setText(
                    (!accessColorCard && !accessFindBarcode)
                            ?  R.string.stageScanManual : R.string.stageScanFinaly);
    }

    private void changeVisibleItem(float scaleArg)
    {
        this.clearView.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.to_text_light));
        this.clearView.setScaleX(scaleArg);

        this.preview.setScaleX(scaleArg);
        this.surfaceFormLayout.setScaleX(scaleArg);
    }

    private void setAlternativeDesign()
    {
        if (checkCorrectData() && checkNetwork())
        {
            final ArrayList<TextView> viewInCardSend = new ArrayList<>();
            viewInCardSend.add(this.name_cardComplete);
            viewInCardSend.add(this.number_cardComplete);

            SyncPersonalManager.setRealDesignCard(
                    this.cardName.getText().toString()
                    , this.cardDesign
                    , this.preview
                    , viewInCardSend
                    , this.btnBack);
        }
        else if (!checkCorrectData())
            Toast.makeText(this
                , getString(R.string.errorFindAltDesign)
                , Toast.LENGTH_LONG)
                .show();
        else
            Toast.makeText(this
                    , getString(R.string.offlineNetworkMSG)
                    , Toast.LENGTH_LONG)
                    .show();
    }

    private void finishActivityMaster()
    {
        if (Objects.equals(this.btnBack.getHint().toString()
                , TEXT_BTN_FINISH_COLOR))
        {
            this.generalRecognitionController
                    .controlGetColor();
            this.btnBack.setHint(TEXT_BTN_FINISH_STAGE_2);

            Executors.newSingleThreadScheduledExecutor()
                    .schedule(() ->
                    {
                        if (!this.accessColorCard)
                        {
                            colorAction(false
                                    , this.generalRecognitionController.getColor());
                            changeVisibleItem(0f);
                        } else setReadyCard();
                    }
                    , 500, TimeUnit.MILLISECONDS);
            setReadyCard();
        }
        else if (checkCorrectData())
        {
            if (checkStageReadyCard())
                    new CardManagerDB(this).saveAndFlashDB(new CardInfoEntity(
                            numberUserCard.getText().toString()
                            , cardName.getText().toString()
                            , (this.accessFindBarcode) ? this.encodeBarcodeCard.toString() : CardManagerDB.BARCODE_NONE
                            , generalRecognitionController.getColor()
                            , null
                            , toDate(realDate())
                            , null), () ->
                    {
                        if (this.checkOfflineEntrance(this.sharedPreferencesManager))
                            this.syncGlobalCardsManager.saveCardInGlobalDB();
                        finishActivity();
                    });
            else if (this.accessColorCard)
            {
                setReadyCard();
                this.btnBack.setHint(TEXT_BTN_FINISH_STAGE_2);
            }
        }
        else Toast.makeText(this
                , R.string.errorBuildMCard
                , Toast.LENGTH_LONG)
                    .show();
    }

    private boolean checkCorrectData()
    {
        return (!cardName.getText().toString().equals("")
                && !numberUserCard.getText().toString().equals("")
                && (!textBarcode.getText().toString().equals("")
                || !this.accessFindBarcode));
    }

    private void finishActivity()
    {
        final Intent startActivity = new Intent(this
                , BasicAppActivity.class);

        if (!NetworkListener.getStatusNetwork())
            startActivity.putExtra("", "");

        startActivity(startActivity);
        finish();
    }

    private void colorAction(boolean SET_MODE, int color)
    {
        final Bitmap setColor = Bitmap
                .createBitmap(
                        1
                        , 1
                        , Bitmap.Config.ARGB_8888);

        if (SET_MODE)
        {
            this.generalRecognitionController.setColor(color);
            this.btnCardColor.setBackgroundTintList(
                    ColorStateList.valueOf(color));
        }

        setColor.setPixel(0, 0, color);
        int opposedColor = MasterDesignCard.getOpposedColor(color);

        this.name_cardComplete.setTextColor(opposedColor);
        this.number_cardComplete.setTextColor(opposedColor);

        this.cardDesign.setScaleX(1f);
        this.cardDesign.setImageBitmap(setColor);
    }

    private void colorCardChange()
    {
        if ((this.accessColorCard
                && this.name_cardComplete.getScaleX() != 0f
                && checkStageReadyCard())
                || Objects.equals(getIntent()
                .getStringExtra(SELECT_MODE)
                , getString(R.string.alertBtnNumberPhoneText)))
            new CustomAppDialog(this)
                    .buildEntityDialog(true)
                    .setTitle(getString(R.string.infoChanheCardAddColor))
                    .setSizeMainContainer(0f)
                    .setPositiveButton("Ручной", click -> userChangeColorCard())
                    .setAdditionalButton("Камера", click ->
                    {
                        actionAskColor();
                        changeVisibleItem(1f);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
       else if (!checkStageReadyCard())
           Toast.makeText(this
                  , getString(R.string.ErrorChangeColor)
                  , Toast.LENGTH_SHORT).show();
       else Toast.makeText(this
                , getString(R.string.warningChangeCardAddColor)
                , Toast.LENGTH_LONG).show();
    }

    private void actionAskColor()
    {
        this.messageActionInfoTV.setText(
                R.string.stageScanColorCard);

        ImageColourManager.getMessage(this);
        this.btnBack.setHint(TEXT_BTN_FINISH_COLOR);

        this.preview.getHolder().addCallback(
                this.generalRecognitionController.setCameraSourceAndReturnCallback(
                        GeneralRecognitionController.ACTION_RECOGNITION_ADDITIONAL_COLOR
                        , this
                        , new Detector.Processor()
                        {
                            @Override
                            public void release() { }
                            @Override
                            public void receiveDetections(@NonNull Detector.Detections detections) { }
                        }));
    }

    private boolean checkStageReadyCard()
    {
        return this.btnBack.getHint()
            .toString()
            .equals(TEXT_BTN_FINISH_STAGE_2);
    }

    private void initModeAdd(String inputUserMode)
    {
        if (Objects.equals(inputUserMode, getString(R.string.alertBtnNotHaveBarcodeText)))
            this.accessFindBarcode = !accessFindBarcode;
        else if (Objects.equals(inputUserMode, getString(R.string.alertBtnNumberPhoneText)))
        {
            this.btnControlFlashMode.setScaleX(0f);
            this.accessColorCard = !accessColorCard;
            this.accessFindBarcode = !accessFindBarcode;
        }
        initOptionsByModeActivity();
    }

    private void userChangeColorCard()
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
        selectColorInterfaceApp.show(
                getSupportFragmentManager(), "");
    }

    private void initOptionsByModeActivity()
    {
        if (!this.accessFindBarcode || !this.accessColorCard)
        {
            final ColorStateList shadowColor = ColorStateList
                    .valueOf(Color.parseColor("#807C7B7B"));

            this.textBarcode.setHint(R.string.infoNotBarcodeCardAdd);
            this.textBarcode.setBackgroundTintList(shadowColor);

            findViewById(R.id.barcode_set)
                    .setBackgroundTintList(shadowColor);

            if (!this.accessColorCard)
            {
                ((TextView) findViewById(R.id.infoChangeColor))
                        .setHint(R.string.infoNotAutoColorCardAdd);

                this.numberUserCard
                        .setHint(R.string.infoNumberPhoneCardOwner);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s
            , int start
            , int before
            , int count)
    {
        if (this.numberUserCard.getText().length() >= 6
                && this.oneOperation)
        {
            this.oneOperation = !oneOperation;
            startBarcodeDetector();
        }

        if (textBarcode.getText()
                .toString()
                .length() > 0)
            this.generalRecognitionController
                    .controlGetColor();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View getView)
    {
        getView.startAnimation(this.animSelected);
        switch (getView.getId())
        {
            case R.id.alternative_design:
                setAlternativeDesign(); break;
            case R.id.top_back:
                finishActivity(); break;
            case R.id.color_signal:
                colorCardChange(); break;
            default: finishActivityMaster();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override
    public void afterTextChanged(Editable s) { }
    @Override
    public void onDialogDismissed(int dialogId) { }

    @Override
    public void onColorSelected(int dialogId, int newColor)
    {
        try
        {
            this.rotationCardMaster.setColorCard(newColor);
            colorAction(true, newColor);

            final Bitmap setColor = Bitmap.createBitmap(1
                    , 1
                    , Bitmap.Config.ARGB_8888);
            setColor.setPixel(0, 0, newColor);

            this.rotationCardMaster
                    .getMasterDesignCard()
                    .colorCardText(setColor, true);
        }
        catch (NullPointerException ignored)
        {
            Toast.makeText(this
                    , getString(R.string.warningChangeCardAddColor)
                    , Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        finishActivity();
    }

    @Override
    protected void onDestroy()
    {
        networkListenerUnregister();
        stopStream();
        this.textBarcode.removeTextChangedListener(this);

        if (this.rotationCardMaster != null)
            rotationCardMaster.rotationClearRAM();
        super.onDestroy();
    }

    @Override
    public void networkListenerRegister()
    {
        this.networkListener = new NetworkListener();
        registerReceiver(this.networkListener
                , new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public boolean checkNetwork()
    {
        if (!NetworkListener.getStatusNetwork())
        {
            try { Thread.sleep(1600); }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
        return NetworkListener.getStatusNetwork();
    }

    @Override
    public void networkListenerUnregister()
    { unregisterReceiver(this.networkListener); }

    @Override
    public void stopStream()
    {
        try
        {
            this.preview.getHolder()
                    .removeCallback(this.generalRecognitionController);
            this.generalRecognitionController
                    .surfaceDestroyed(this.preview.getHolder());
        } catch (Exception ignored) {}
    }

    @Override
    public void startStream()
    { startTextRecognition(); }
}



