package com.example.mcard.FragmentsAdditionally;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GroupServerActions.SyncPersonalManager;
import com.example.mcard.BasicAppActivity;
import com.example.mcard.FunctionalInterfaces.NetworkConnection;
import com.example.mcard.GlobalListeners.NetworkListener;
import com.example.mcard.R;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.FunctionalInterfaces.CorrectInputTextAuth;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.GeneralInterfaceApp.SettingsInterfaceFragment;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import com.example.mcard.UserAuthorization.OfflineEntranceActions;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;

import static com.example.mcard.FragmentsAdditionally.PersonalProfileFragment.getUserName;
import static com.example.mcard.SideFunctionality.CustomAppDialog.DEFAULT_MESSAGE_SIZE;

import kotlin.Pair;

@SuppressLint("NonConstantResourceId")
public final class SettingsFragment extends Fragment implements GeneralStructApp
 , View.OnClickListener
 , NetworkConnection
 , CorrectInputTextAuth
 , ColorPickerDialogListener
 , OfflineEntranceActions
{
    private View view;
    private Animation animClick;
    private AppCompatTextView changeLoginBtn
            , changePasswordBtn
            , btnAlternativeDesign
            , btnSettingsCards
            , btnSettingsInterfaceApp;
    private final CustomHeaderListView changeListView;

    private AppCompatImageButton btnBack;
    private SharedPreferencesManager sharedPreferencesManager;
    private DataInterfaceCard dataInterfaceCard;
    private BasicFireBaseManager basicFireBaseManager;
    private SyncPersonalManager syncPersonalManager;
    private CardManagerDB cardManagerDB;
    private ThemeAppController themeAppController;
    private CustomAppDialog customAppDialogForInterface
            , getCustomAppDialogForLoading;

    private static final int DIALOG_INTERFACE_COLOR = -1;
    private static final int DIALOG_TEXT_COLOR = 1;

    public SettingsFragment(CustomHeaderListView customHeaderListView)
    { this.changeListView = customHeaderListView; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_settings, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.btnSettingsInterfaceApp = view.findViewById(R.id.changeAppInterfaceBtn);
        this.btnSettingsCards = view.findViewById(R.id.settingsCards);
        this.changeLoginBtn = view.findViewById(R.id.update_login);
        this.changePasswordBtn = view.findViewById(R.id.update_password);
        this.btnAlternativeDesign = view.findViewById(R.id.all_alternativeDesign);
        this.btnBack = view.findViewById(R.id.btn_back);
        this.animClick = AnimationUtils.loadAnimation(
                requireContext(), R.anim.select_object);

        this.cardManagerDB =
                new CardManagerDB(requireContext());
        this.sharedPreferencesManager =
                new SharedPreferencesManager(requireContext());
        this.dataInterfaceCard =
                new DataInterfaceCard(requireContext());

        if (this.checkOfflineEntrance(this.sharedPreferencesManager))
        {
            this.basicFireBaseManager =
                    new BasicFireBaseManager(requireContext());
            this.syncPersonalManager =
                    new SyncPersonalManager(requireContext());
            this.getCustomAppDialogForLoading =
                    new CustomAppDialog(requireContext());
        }
    }

    @Override
    public void drawableView()
    {
        this.themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        PersonalProfileFragment.setUserIconImage(
                view.findViewById(R.id.user_photo)
                , requireContext());

        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.main_linear));
        themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , "Настройки");

        themeAppController.setOptionsButtons(
                btnBack
                , btnAlternativeDesign
                , changeLoginBtn
                , btnSettingsCards
                , btnSettingsInterfaceApp
                , changePasswordBtn
                , view.findViewById(R.id.btn_complete));

        this.themeAppController.settingsText(
                view.findViewById(R.id.user_info)
                , getUserName(requireContext()));
    }

    @Override
    public void basicWork()
    {
        this.btnSettingsInterfaceApp.setOnClickListener(this);
        this.btnSettingsCards.setOnClickListener(this);
        this.btnBack.setOnClickListener(this);
        this.changeLoginBtn.setOnClickListener(this);
        this.changePasswordBtn.setOnClickListener(this);
        this.btnAlternativeDesign.setOnClickListener(this);
        this.view.findViewById(R.id.btn_complete).setOnClickListener(this);

        if (!this.checkOfflineEntrance(this.sharedPreferencesManager))
            this.viewOffController(
                    new AppCompatTextView[] { changeLoginBtn, changePasswordBtn }
                    , click -> this.showOfferDialogRegistration(
                            requireActivity()));
    }

    @Override
    public void onClick(View v)
    {
        v.startAnimation(this.animClick);
        switch (v.getId())
        {
            case R.id.update_login:
                this.syncPersonalManager.checkUserRoleForPermissions(
                        R.string.roleAdmin
                        , R.string.warningChangeAccountLogin
                        , this::updateLoginBtn
                        , this.getCustomAppDialogForLoading);
                break;

            case R.id.update_password:
                this.syncPersonalManager.checkUserRoleForPermissions(
                        R.string.roleSmallAdmin
                        , R.string.warningChangeAccountPassword
                        , this::updatePasswordBtn
                        , this.getCustomAppDialogForLoading);
                break;

            case R.id.all_alternativeDesign:
                setBtnAlternativeDesign();
                break;

            case R.id.settingsCards:
                startSettingsCards();
                break;

            case R.id.changeAppInterfaceBtn:
                generalInterfaceAppSettings();
                break;

            default:
                requireActivity().onBackPressed();
                break;
        }
    }

    private void generalInterfaceAppSettings()
    {
        this.customAppDialogForInterface = new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle("Интерфейс приложения")
                .setViews(CustomAppDialog.singleDialogItemBuilder(
                        requireContext()
                        , R.string.dialogSettingsText0
                        , R.drawable.personal_design_icon
                        , click ->
                        {
                            click.startAnimation(animClick);
                            additionalBtnChangeColorApp(DIALOG_INTERFACE_COLOR);
                        })
                        , CustomAppDialog.singleDialogItemBuilder(
                                requireContext()
                                , R.string.dialogSettingsText1
                                , R.drawable.edit_color_icon
                                , click ->
                                {
                                    click.startAnimation(animClick);
                                    additionalBtnChangeColorApp(DIALOG_TEXT_COLOR);
                                })
                        ,  CustomAppDialog.singleDialogItemBuilder(
                                requireContext()
                                , R.string.dialogSettingsText2
                                , R.drawable.design_remove_icon
                                , click ->
                                {
                                    click.startAnimation(animClick);
                                    additionalBtnDefaultSettingsInterfaceApp();
                                }))
                .setContainerParams(0, 0, 8, true)
                .setNegativeButton("Назад", null);
        this.customAppDialogForInterface.show();
    }

    private void additionalBtnChangeColorApp(int dialogID)
    {
        final ColorPickerDialog selectColorInterfaceApp =  ColorPickerDialog.newBuilder()
                .setShowAlphaSlider(true)
                .setShowColorShades(true)
                .setColorShape(ColorShape.SQUARE)
                .setDialogTitle(dialogID == DIALOG_INTERFACE_COLOR
                        ? R.string.titleSelectColorApp
                        : R.string.messageSelectTextColorApp)
                .setColor(Color.WHITE)
                .setAllowCustom(true)
                .setAllowPresets(true)
                .setPresetsButtonText(R.string.presets_button_text)
                .setCustomButtonText(R.string.custom)
                .setSelectedButtonText(R.string.select)
                .setDialogId(dialogID)
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .create();

        selectColorInterfaceApp.setColorPickerDialogListener(this);
        selectColorInterfaceApp.show(requireFragmentManager()
                .beginTransaction()
                .attach(this), "");
    }

    private void additionalBtnDefaultSettingsInterfaceApp()
    {
        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle("Подтверждение действия")
                .setMessage(R.string.messageConfirmDefaultInterfaceApp, DEFAULT_MESSAGE_SIZE)
                .setPositiveButton("Сброс", click ->
                {
                    dataInterfaceCard.actionDefaultSettings(true);

                    this.customAppDialogForInterface
                            .invalidateBackgroundDialog();
                    this.customAppDialogForInterface
                            .invalidateIconsCustomDialog(themeAppController);
                    this.customAppDialogForInterface
                            .invalidateTextColorDialog(themeAppController);
                    drawableView();
                    BasicAppActivity.updateTheme = true;
                    Toast.makeText(requireContext()
                            , getString(R.string.messageSuccessDefaultInterfaceApp)
                            , Toast.LENGTH_LONG)
                            .show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void startSettingsCards()
    {
        SettingsFragment.this
                .getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                .replace(R.id.main_linear, new SettingsInterfaceFragment())
                .commit();
    }

   private void updateLoginBtn()
   {
       final AppCompatEditText old_login =
               new AppCompatEditText(requireContext());
       final AppCompatEditText new_login =
               new AppCompatEditText(requireContext());

       old_login.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
       old_login.setHint("Введите старый логин");

       new_login.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
       new_login.setHint("Введите новый логин");

       new CustomAppDialog(requireContext())
               .buildEntityDialog(true)
               .setTitle("Изменение логина (Email)")
               .setViews(old_login, new_login)
               .setPositiveButton("Готово", (click) ->
               {
                   final String old_login_get =
                           this.removeSymbolsTab(old_login.getText().toString());
                   final String new_login_get =
                           this.removeSymbolsTab(new_login.getText().toString());

                   if (this.checkCorrectLogin(old_login_get)
                           && this.checkCorrectLogin(new_login_get))
                   {
                       this.getCustomAppDialogForLoading.showLoadingDialog(
                               true,"Загрузка изменений..");

                       this.basicFireBaseManager.changeLogin(
                               this.getCustomAppDialogForLoading
                               , old_login_get
                               , new_login_get);
                   }
                   else
                       Toast.makeText(requireContext()
                           , requireContext().getString(R.string.errorCheckLogin)
                           , Toast.LENGTH_SHORT)
                               .show();
               })
               .setNegativeButton("Отмена", null)
               .show();
   }

    private void updatePasswordBtn()
    {
        final AppCompatEditText oldPassword =
                new AppCompatEditText(requireContext());
        final AppCompatEditText newPassword =
                new AppCompatEditText(requireContext());

        oldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        oldPassword.setHint("Введите старый пароль");

        newPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        newPassword.setHint("Введите новый пароль");

        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle("Изменение пароля")
                .setViews(oldPassword, newPassword)
                .setPositiveButton("Готово", (click) ->
                {
                    final String getOldPassword =
                            this.removeSymbolsTab(oldPassword.getText().toString());
                    final String  getNewPassword =
                            this.removeSymbolsTab(newPassword.getText().toString());

                    if (this.checkCorrectPassword(getOldPassword
                            , MIN_BOUND_PASSWORD
                            , MAX_BOUND_PASSWORD)
                            && this.checkCorrectPassword(getNewPassword
                            , MIN_BOUND_PASSWORD
                            , MAX_BOUND_PASSWORD))
                    {
                        this.getCustomAppDialogForLoading.showLoadingDialog(
                                true,"Загрузка изменений..");

                        this.basicFireBaseManager.changePassword(
                                this.getCustomAppDialogForLoading
                                , getOldPassword
                                , getNewPassword);
                    }
                    else
                        Toast.makeText(requireContext()
                            , requireContext().getString(R.string.errorCheckPassword)
                            , Toast.LENGTH_SHORT)
                                .show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

   private void setBtnAlternativeDesign()
   {
       if (checkNetwork())
       {
         SyncPersonalManager.downloadAllAltDesignCards(
                 cardManagerDB.allCardDefaultName()
                 , changeListView
                 , cardManagerDB);

           Toast.makeText(requireContext()
                   , requireContext().getString(R.string.successAltDesign)
                   , Toast.LENGTH_LONG)
                   .show();
           requireActivity().onBackPressed();
       }
       else
           Toast.makeText(requireContext()
               , requireContext().getString(R.string.offlineNetworkMSG)
               , Toast.LENGTH_LONG)
                   .show();
   }

    @Override
    public boolean checkNetwork() {
        return NetworkListener.getStatusNetwork();
    }

    @Override
    public void onColorSelected(int dialogId, int selectedUserColor)
    {
        if (Color.red(selectedUserColor) >= 221
                && Color.green(selectedUserColor) >= 221
                && Color.blue(selectedUserColor) >= 221)
        {
            Toast.makeText(requireContext()
                    , getString(R.string.warningChangeInterfaceApp)
                    , Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (dialogId == DIALOG_INTERFACE_COLOR)
        {
            this.dataInterfaceCard.actionGeneralInterfaceAppColor(
                    new Pair(selectedUserColor, false));
            BasicAppActivity.updateTheme = true;

            drawableView();
            this.customAppDialogForInterface
                    .invalidateBackgroundDialog();

            Toast.makeText(
                    requireContext()
                    , R.string.messageSuccessUpdateColorApp
                    , Toast.LENGTH_SHORT)
                    .show();
        }
        else if (dialogId == DIALOG_TEXT_COLOR)
        {
            this.dataInterfaceCard.actionGeneralTextAppColor(
                    selectedUserColor);
            drawableView();
            BasicAppActivity.updateTheme = true;
            this.customAppDialogForInterface.invalidateTextColorDialog(
                    this.themeAppController);

            Toast.makeText(requireContext()
                    , R.string.successChangeAppColorText
                    , Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) { }
}