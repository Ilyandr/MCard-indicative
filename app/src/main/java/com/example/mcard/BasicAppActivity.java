package com.example.mcard;

import static com.example.mcard.GlobalListeners.NetworkListener.getStatusNetwork;
import static com.example.mcard.SideFunctionality.RotationCardMaster.getRotationCardActive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.CommercialAction.SubscribeManagerActivity;
import com.example.mcard.FragmentsAdditionally.BasicGlobalPlatformFragment;
import com.example.mcard.FragmentsAdditionally.FriendsListFragment;
import com.example.mcard.FragmentsAdditionally.HistoryOperationsFragment;
import com.example.mcard.FragmentsAdditionally.OptionsAppSyncFragment;
import com.example.mcard.FragmentsAdditionally.PersonalProfileFragment;
import com.example.mcard.FragmentsAdditionally.SettingsFragment;
import com.example.mcard.UserAuthorization.OfflineEntranceActions;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomPopupMenu;
import com.example.mcard.GroupServerActions.SyncGlobalCardsManager;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.GroupServerActions.SyncPersonalManager;
import com.example.mcard.GroupServerActions.SubscribeController;
import com.example.mcard.AdapersGroup.BasicCardManagerAdapter;
import com.example.mcard.FunctionalInterfaces.NetworkConnection;
import com.example.mcard.GlobalListeners.NetworkListener;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.SideFunctionality.GeneralAnimations;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.SideFunctionality.ListViewComponent;
import com.example.mcard.SideFunctionality.RotationCardMaster;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import com.example.mcard.UserActionsCard.CardAddActivity;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.UserActionsCard.CustomSortCardsManager;
import com.example.mcard.UserActionsCard.EditCardFragment;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.UserLocation.GeolocationFindStarter;
import com.example.mcard.databinding.ActivityGeneralAppBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public final class BasicAppActivity extends AppCompatActivity implements
 NetworkConnection
 , View.OnClickListener
 , AdapterView.OnItemClickListener
 , GeneralStructApp
 , ListViewComponent
 , OfflineEntranceActions
 , SwipeRefreshLayout.OnRefreshListener
 , NavigationView.OnNavigationItemSelectedListener
 , DrawerLayout.DrawerListener
{
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityGeneralAppBinding binding;

    private AppCompatImageButton btnLeftMenu
            , btnAdd
            , btnGeolocation;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GeneralAnimations generalAnimations;
    private ThemeAppController themeAppController;
    private SharedPreferencesManager sharedPreferencesManager;

    private Animation animForPlusBtn,
            animForMenuBtn,
            animMenuBackBtn,
            animDataListBtn;

    public static boolean updateTheme = false
            , animationsWithCards;

    private BasicFireBaseManager basicFireBaseManager;
    private SyncPersonalManager syncPersonalHybridMaster;
    private SyncGlobalCardsManager syncGlobalCardsManager;
    private GlobalDataFBManager global_dataFBManager;

    private SubscribeController subscribeController;
    private List<Fragment> fragmentsList;
    private NetworkListener networkListener;

    private Bundle givePosition;
    private CardManagerDB cardManagerDB;
    private CustomHeaderListView generalCardsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.binding = ActivityGeneralAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        findObjects();
        drawableView();
    }

    @Override
    public void findObjects()
    {
        this.btnLeftMenu =  findViewById(R.id.menu);
        this.btnAdd = findViewById(R.id.add);
        this.btnGeolocation = findViewById(R.id.btn_geolocation);
        this.generalCardsListView = findViewById(R.id.list_item);
        this.swipeRefreshLayout = findViewById(R.id.swipeGeneralView);

        this.animationsWithCards = new DataInterfaceCard(this)
                .actionAnimSelectCard(null);
        this.animForMenuBtn = AnimationUtils
                .loadAnimation(this, R.anim.to_button_animation_magic);
        this.animForPlusBtn = AnimationUtils
                .loadAnimation(this, R.anim.to_button_add_magic);
        this.animMenuBackBtn = AnimationUtils
                .loadAnimation(this, R.anim.back_button_magic);
        this.animDataListBtn = AnimationUtils
                .loadAnimation(this, R.anim.select_object);

        this.givePosition = new Bundle();
        this.fragmentsList = new ArrayList<>();
        this.cardManagerDB = new CardManagerDB(this);
        this.sharedPreferencesManager = new SharedPreferencesManager(this);

        this.generalAnimations = new GeneralAnimations(1f
                , 0f
                , -1f
                , 600
                , 150);
        this.swipeRefreshLayout.setColorScheme(
                R.color.main_color
                , R.color.green
                , R.color.yellow
                , R.color.red);
    }

    @Override
    public synchronized void drawableView()
    {
        this.themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(this));

        themeAppController.changeDesignIconBar(
                findViewById(R.id.control_panel));
        themeAppController.changeDesignDefaultView(
                findViewById(R.id.main_linear));
        themeAppController.settingsText(
                findViewById(R.id.main_text), "Список карт");

        themeAppController.setOptionsButtons(
                btnLeftMenu
                , btnAdd
                , btnGeolocation);

        if (this.checkOfflineEntrance(sharedPreferencesManager))
            GlobalDataFBManager.checkAvaibleAccount(
                    this, this::basicWork);
        else basicWork();
    }

    @Override
    public void basicWork()
    {
        networkListenerRegister();
        registerListView();

        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (this.checkOfflineEntrance(sharedPreferencesManager))
        {
            additionalFindObjects();

            this.subscribeController.checkSubscribe(
                    SubscribeController.MODE_GET, null, null, null, null);
            if (getIntent().getExtras() == null || !getStatusNetwork())
                this.basicFireBaseManager.checkSubscribeUsersCount();
            this.subscribeController.checkFindGEO(
                    SubscribeController.MODE_UPDATE_DATE, null);
            this.syncGlobalCardsManager
                    .basicSyncController(SyncGlobalCardsManager.MODE_INFINITE);
        }

        this.generalCardsListView
                .setAnimationAction(
                        this.swipeRefreshLayout, this);
        this.swipeRefreshLayout.setRefreshing(true);
        onRefresh();
        this.createItemInList(generalCardsListView);

        this.binding.drawerLayout.addDrawerListener(this);
        this.binding.navView.setNavigationItemSelectedListener(this);
        this.generalCardsListView.setOnItemClickListener(this);
        this.btnAdd.setOnClickListener(this);
        this.btnLeftMenu.setOnClickListener(this);
        this.btnGeolocation.setOnClickListener(this);
        this.swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void additionalFindObjects()
    {
        this.basicFireBaseManager = new BasicFireBaseManager(this);
        this.global_dataFBManager = new GlobalDataFBManager(this);
        this.syncPersonalHybridMaster = new SyncPersonalManager(this);
        this.syncGlobalCardsManager = new SyncGlobalCardsManager(this, this.generalCardsListView);
        this.subscribeController = new SubscribeController(this);
    }

    private void startCardAddActivity()
    {
        final ColorStateList textColor = ColorStateList.valueOf(
                Color.parseColor(
                        "#000044"));

        final AppCompatButton btnHaveBarcode =
                new AppCompatButton(this);
        final AppCompatButton btnNotHaveBarcode =
                new AppCompatButton(this);
        final AppCompatButton btnNumberPhone =
                new AppCompatButton(this);

        btnHaveBarcode.setHint(R.string.alertBtnHaveBarcodeText);
        btnNotHaveBarcode.setHint(R.string.alertBtnNotHaveBarcodeText);
        btnNumberPhone.setHint(R.string.alertBtnNumberPhoneText);

        btnHaveBarcode.setBackgroundResource(
                R.drawable.main_select_btn);
        btnNotHaveBarcode.setBackgroundResource(
                R.drawable.main_select_btn);
        btnNumberPhone.setBackgroundResource(
                R.drawable.main_select_btn);

        this.themeAppController.setOptionsButtons(
                btnHaveBarcode
                , btnNotHaveBarcode
                , btnNumberPhone);

        btnHaveBarcode.setOnClickListener(this::selectModeCardAdd);
        btnNotHaveBarcode.setOnClickListener(this::selectModeCardAdd);
        btnNumberPhone.setOnClickListener(this::selectModeCardAdd);

        new CustomAppDialog(this)
                .buildEntityDialog(true)
                .setTitle("Добавление новой карты")
                .setViews(btnHaveBarcode, btnNotHaveBarcode, btnNumberPhone)
                .setContainerParams(0, 0, 8, true)
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void selectModeCardAdd(@NonNull View view)
    {
        view.startAnimation(animDataListBtn);

        final boolean grantedPermissionCamera = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        final boolean grantedPermissionExStorage = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        if (grantedPermissionCamera && grantedPermissionExStorage)
        {
            final Intent transactionStart = new Intent(
                    BasicAppActivity.this, CardAddActivity.class);
            transactionStart.putExtra(CardAddActivity.SELECT_MODE
                    , ((AppCompatButton) view).getHint().toString());

            startActivity(transactionStart);
            finish();
        }
        else
            new CustomAppDialog(this)
                .buildEntityDialog(false)
                .setTitle("Предоставление разрешений")
                .setMessage((!grantedPermissionCamera && !grantedPermissionExStorage
                                ? R.string.messageActionDoubleCameraAndExStorage :
                                (!grantedPermissionCamera ? R.string.messageActionCamera :
                                        R.string.messageActionExStorage))
                        , 4.5f)
                .setPositiveButton("Далее", (click) -> ActivityCompat.requestPermissions(this
                        , (!grantedPermissionCamera && !grantedPermissionExStorage
                                ? new String[] {Manifest.permission.CAMERA
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE} :
                                (!grantedPermissionCamera ? new String[] {Manifest.permission.CAMERA} :
                                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE
                                                , Manifest.permission.WRITE_EXTERNAL_STORAGE}))
                        , (!grantedPermissionCamera && !grantedPermissionExStorage
                                ? PermissionCodes.ACCESS_CAMERA_AND_STORAGE :
                                (!grantedPermissionCamera ? PermissionCodes.ACCESS_CAMERA :
                                        PermissionCodes.ACCESS_STORAGE)))
                )
                    .setNegativeButton("Отмена", null)
                .show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void registerListView()
    {
        this.settingsListView(this.generalCardsListView
                , getDrawable(R.color.prism)
                , this.generalAnimations);
    }

    @Override
    public void createItemInList(@NonNull ListView listView)
    { this.generalAnimations.setCardList(this.generalCardsListView); }

    @Override
    public void onItemClick(
            AdapterView<?> parent, View view, int position, long id)
    {
       if (!this.generalAnimations.getActionResolution())
           return;

       this.givePosition.clear();
       final EditCardFragment fragmentEditCardFragment =
               new EditCardFragment(this.generalAnimations, this);
       fragmentEditCardFragment.setUpdateListView(
               this.generalCardsListView);

       final CardInfoEntity inputSingleCard = (CardInfoEntity) parent
               .getAdapter()
               .getItem(position);

       this.givePosition.putParcelable(
               GeneralRulesDB.TRANSACTION_KEY
               , inputSingleCard);
       fragmentEditCardFragment.setArguments(
               this.givePosition);

       this.selectCardAnim(this.generalAnimations
               , parent
               , view
               , () ->
                       getSupportFragmentManager()
                          .beginTransaction()
                          .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                          .replace(R.id.main_linear, fragmentEditCardFragment)
                          .commit()
               , position);
    }

    @Override
    public void onBackPressed()
    {
        if (updateTheme)
        {
            drawableView();
            updateTheme = !updateTheme;
        }

        if (getRotationCardActive())
        {
            RotationCardMaster.setRotationCardActive(
                    !getRotationCardActive());
            final WindowManager.LayoutParams displayParams =
                    this.getWindow().getAttributes();

            displayParams.screenBrightness =
                    RotationCardMaster.getPrevDisplayBright();
            this.getWindow().setAttributes(displayParams);
        }

        this.fragmentsList.clear();
        this.fragmentsList =
                getSupportFragmentManager().getFragments();

        if (this.fragmentsList.size() == 0)
            super.onBackPressed();
        else
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.start_test,R.anim.finish_test)
                    .remove(this.fragmentsList.get(
                            this.fragmentsList.size() - 1))
                    .commit();
    }

    @Override
    public void networkListenerRegister()
    {
        this.networkListener =
                new NetworkListener(this.global_dataFBManager);

        registerReceiver(this.networkListener
                , new IntentFilter(
                        ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public boolean checkNetwork() throws InterruptedException
    {
        if (!getStatusNetwork())
            Thread.sleep(1600);
        return getStatusNetwork();
    }

    @Override
    public void networkListenerUnregister()
    { unregisterReceiver(this.networkListener); }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v)
    {
        if (!this.generalAnimations.getActionResolution())
            return;
        v.startAnimation(animDataListBtn);

        switch (v.getId())
        {
            case R.id.btn_geolocation:
                cardOfferFunctionality(v);
                break;

            case R.id.add:
                if (!this.generalAnimations.getActionResolution())
                    return;

                v.startAnimation(animDataListBtn);
                startCardAddActivity();
                break;

            case R.id.menu:
                this.binding.drawerLayout.open();
                break;
        }
    }

    private void cardOfferFunctionality(View view)
    {
        new CustomPopupMenu(this, view)
                .createMenuForSelectCardSort(
                        this.sharedPreferencesManager
                        , () ->
                        {
                            this.swipeRefreshLayout.setRefreshing(true);
                            onRefresh();
                        });
    }

    public static synchronized void setListItemCards(
     @NonNull Context context
     , @NonNull CardManagerDB cardManagerDB
     , @NonNull CustomHeaderListView listView
     , @Nullable Boolean changeScale)
    {
        listView.checkEmptyList(true);
            if ((listView.getScaleX() == 0.0f
                    && changeScale != null
                    && changeScale)
                    || cardManagerDB.primaryStartApp())
                listView.setScaleX(1f);

            final List<CardInfoEntity> allCardsDataInput =
                    cardManagerDB.readAllCards();
            boolean updateListView = false;

            try
            {
                final List<CardInfoEntity> allCardsDataNow = ((BasicCardManagerAdapter)
                        listView.getAdapter())
                        .getMainInfoList();

                final int sizeListInput = allCardsDataInput.size()
                        , sizeListNow = allCardsDataNow.size();

                if (sizeListInput == sizeListNow)
                {
                    for (int i = 0; i < sizeListInput; i++)
                    {
                        for (int j = 0; j < sizeListNow; j++)
                        {
                            if (allCardsDataInput.get(i)
                                    .getUniqueIdentifier()
                                    .equals(allCardsDataNow.get(j)
                                            .getUniqueIdentifier()))
                            {
                                if (allCardsDataInput.get(i).hashCode()
                                        != allCardsDataNow.get(j).hashCode())
                                {
                                    updateListView = true;
                                    break;
                                }
                            }
                        }
                    }
                } else updateListView = true;
            }
            catch (Exception ignored)
            { updateListView = true; }

            if (updateListView || changeScale == null)
            {
                listView.removeAllViewsInLayout();
                listView.setAdapter(new BasicCardManagerAdapter(
                        context
                        , (changeScale == null)
                        ? CustomSortCardsManager.sortByInternalSystem(
                                allCardsDataInput
                        , new SharedPreferencesManager(context).sortedCardInfo(null)
                        , context, null) : allCardsDataInput
                        , false));
            }
    }

    @Override
    public AppCompatActivity getClassContext() {
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions
            , @NonNull int... grantResults)
    {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case PermissionCodes.ACCESS_LOCATION:
                for (int singleResult : grantResults)
                    if (singleResult != PackageManager.PERMISSION_GRANTED)
                        return;
                new CustomAppDialog(this)
                        .buildEntityDialog(false)
                        .setTitle("Управление данными")
                        .setMessage(
                                R.string.messageActionGeolocation,
                                3f)
                        .setPositiveButton("Включить", (click) ->
                        {
                            this.startActivity(new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                            this.generalCardsListView
                                    .getSwipeRefreshLayout()
                                    .setRefreshing(true);
                            GeolocationFindStarter.safeLaunchGeoFind(this);
                        }
                ).setNegativeButton("Назад", null).show();
                break;

            case PermissionCodes.ACCESS_CAMERA_AND_STORAGE:
            case PermissionCodes.ACCESS_CAMERA:
            case PermissionCodes.ACCESS_STORAGE:
                for (int singleResult : grantResults)
                    if (singleResult != PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this
                                , getString(R.string.warningAddNewCard)
                                , Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                Toast.makeText(this
                        , getString(R.string.successGetPermissions)
                        , Toast.LENGTH_LONG)
                        .show();
                break;
            case PermissionCodes.ACCESS_STORAGE_CARD_DESIGN:
            case PermissionCodes.ACCESS_STORAGE_IMAGE:
                for (int singleResult : grantResults)
                    if (singleResult != PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(this
                                , (requestCode == PermissionCodes.ACCESS_STORAGE_IMAGE
                                        ? R.string.warningAddImageProfile
                                        : R.string.warningAddImageCard)
                                , Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                Toast.makeText(this
                        , getString(R.string.successGetPermissions)
                        , Toast.LENGTH_LONG)
                        .show();
                break;
        }
    }

    @Override
    public void onRefresh()
    {
        if (!getStatusNetwork())
            this.generalCardsListView.offLoadingAnimations(false);

        this.generalCardsListView.setAdapter(
                new BasicCardManagerAdapter(
                        BasicAppActivity.this
                        , CustomSortCardsManager.sortByInternalSystem(
                        cardManagerDB.readAllCards()
                        , sharedPreferencesManager.sortedCardInfo(null)
                        , this
                        , this.generalCardsListView)
                        , false));
    }

    private synchronized void fragmentStarter(@NonNull Fragment fragmentLaunch)
    {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                .replace(R.id.main_linear, fragmentLaunch)
                .commit();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem inputSelectedItem)
    {
        this.binding.drawerLayout.close();
        switch (inputSelectedItem.getItemId())
        {
            case R.id.btnLocaleProfile:
                fragmentStarter(new PersonalProfileFragment());
            break;

            case R.id.btnSync:
                if (!inputSelectedItem.isChecked())
                    fragmentStarter(new OptionsAppSyncFragment(this.generalCardsListView));
                break;

            case R.id.btnSubscribe:
                if (!inputSelectedItem.isChecked())
                    fragmentStarter(new FriendsListFragment());
                break;

            case R.id.btnGlobalPlatform:
                if (!inputSelectedItem.isChecked() && getStatusNetwork())
                    fragmentStarter(new BasicGlobalPlatformFragment());
                break;

            case R.id.btnHistoryUsing:
                fragmentStarter(new HistoryOperationsFragment());
                break;

            case R.id.btnSettings:
                fragmentStarter(new SettingsFragment(this.generalCardsListView));
                break;

            case R.id.btnPaymentAction:
                if (!inputSelectedItem.isChecked() && getStatusNetwork())
                {
                    startActivity(new Intent(
                            BasicAppActivity.this
                            , SubscribeManagerActivity.class));
                    finish();
                }
                break;
        }
        return false;
    }

    private String getUserNicknameMenu(@Nullable String nameUserProfile)
    {
       return (nameUserProfile == null)
               ? "Пользователь" : nameUserProfile;
    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView)
    {
        ((AppCompatTextView) drawerView
                .findViewById(R.id.userNicknameMenu))
                .setText(getUserNicknameMenu(
                        sharedPreferencesManager.nameUserProfile(null)));

        PersonalProfileFragment.setUserIconImage(
                drawerView.findViewById(R.id.userProfileImage)
                , BasicAppActivity.this);

        if (!this.checkOfflineEntrance(sharedPreferencesManager))
            this.viewOffController(new Object[]
                    {
                            binding.navView.getMenu().getItem(3)
                            , binding.navView.getMenu().getItem(4)
                            , binding.navView.getMenu().getItem(5)
                            , binding.navView.getMenu().getItem(6)
                    }, it -> showOfferDialogRegistration(this));
    }


    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
    @Override
    public void onDrawerClosed(@NonNull View drawerView) {}
    @Override
    public void onDrawerStateChanged(int newState) {}

    public static final class PermissionCodes
    {
        public static final int ACCESS_LOCATION = 200;
        public static final int ACCESS_STORAGE = 201;
        public static final int ACCESS_STORAGE_IMAGE = 202;
        public static final int ACCESS_STORAGE_CARD_DESIGN = 203;
        public static final int ACCESS_CAMERA = 204;
        public static final int ACCESS_CAMERA_AND_STORAGE = 205;
    }

    public CustomHeaderListView getGeneralCardsListView() {
        return generalCardsListView;
    }

    @Override
    protected void onDestroy()
    {
        this.generalCardsListView.setScaleX(0f);
        super.onDestroy();
    }
}

