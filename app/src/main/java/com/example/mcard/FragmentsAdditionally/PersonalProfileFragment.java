package com.example.mcard.FragmentsAdditionally;

import static com.example.mcard.SideFunctionality.CustomAppDialog.DEFAULT_MESSAGE_SIZE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.mcard.BasicAppActivity;
import com.example.mcard.UserAuthorization.OfflineEntranceActions;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.GroupServerActions.SyncPersonalManager;
import com.example.mcard.GlobalListeners.NetworkListener;
import com.example.mcard.R;
import com.example.mcard.StorageAppActions.SQLiteChanges.HistoryManagerDB;
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.FunctionalInterfaces.AnimationsController;
import com.example.mcard.FunctionalInterfaces.CorrectInputTextAuth;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView;
import com.example.mcard.UserAuthorization.ReceptionAppActivity;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;
import com.example.mcard.UserAuthorization.RegisterAppFragment;
import com.squareup.picasso.Picasso;
import java.io.File;

public final class PersonalProfileFragment extends Fragment implements
 GeneralStructApp
 , OfflineEntranceActions
 , View.OnClickListener
 , CorrectInputTextAuth
 , AnimationsController
{
    private View view;
    private AppCompatTextView changeImageUser
            , changeNameUser
            , appInfo
            , removeAccount
            , exitAccount;

    private RoundRectCornerImageView userIconImage;
    private AppCompatEditText userInfoText;
    private AppCompatImageButton completeBtn;

    private BasicFireBaseManager basicFireBaseManager;
    private GlobalDataFBManager globalDataFBManager;
    private SyncPersonalManager syncPersonalManager;
    private SharedPreferencesManager sharedPreferencesManager;
    private ThemeAppController themeAppController;

    public static int CODE_GET_IMAGE_FRAGMENT_INFO = 100;
    private Boolean typeLoadPhoto = false;
    private Animation anim_selected;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_profile, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.changeImageUser = view.findViewById(R.id.change_photo_profile);
        this.changeNameUser = view.findViewById(R.id.user_name_change);
        this.appInfo = view.findViewById(R.id.app_info);
        this.removeAccount = view.findViewById(R.id.remove_account);
        this.exitAccount = view.findViewById(R.id.exit_account);
        this.userIconImage = view.findViewById(R.id.user_photo);
        this.userInfoText = view.findViewById(R.id.user_info);
        this.completeBtn = view.findViewById(R.id.btn_back);

        this.anim_selected =
                AnimationUtils.loadAnimation(
                        requireContext(), R.anim.select_object);
        this.sharedPreferencesManager =
                new SharedPreferencesManager(requireContext());

        if (this.checkOfflineEntrance(this.sharedPreferencesManager))
        {
            this.basicFireBaseManager =
                    new BasicFireBaseManager(requireContext());
            this.globalDataFBManager =
                    new GlobalDataFBManager(requireContext());
            this.syncPersonalManager =
                    new SyncPersonalManager(requireContext());
        }
        this.themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));
    }

    @Override
    public void drawableView()
    {
        this.themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        this.themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.main_linear_reception));
        this.themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , "Мой профиль");

        this.themeAppController.setOptionsButtons(
                completeBtn
                , changeImageUser
                , changeNameUser
                , appInfo
                , removeAccount
                , exitAccount);

        this.themeAppController.settingsText(view.findViewById(R.id.user_id)
                , "Id: " + sharedPreferencesManager.account_id(null));
        this.themeAppController.settingsText(
                userInfoText
                , getUserName(requireContext()));
    }

    @Override
    public void basicWork()
    {
        this.changeImageUser.setOnClickListener(this);
        this.changeNameUser.setOnClickListener(this);
        this.removeAccount.setOnClickListener(this);
        this.exitAccount.setOnClickListener(this);
        this.appInfo.setOnClickListener(this);
        this.completeBtn.setOnClickListener(this);

        if (!this.checkOfflineEntrance(this.sharedPreferencesManager))
            this.viewOffController(
                new View[]{ removeAccount }
                , (click) ->
                {
                    click.startAnimation(anim_selected);
                    this.showOfferDialogRegistration(requireActivity());
                });
        setChangeNameUser(false);
        setUserIconImage(this.userIconImage, requireContext());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v)
    {
        v.startAnimation(anim_selected);
        switch (v.getId())
        {
            case R.id.change_photo_profile:
                changeUserPhoto();
                break;

            case R.id.user_name_change:
                setChangeNameUser(true);
                userInfoText.setFocusableInTouchMode(true);
                userInfoText.startAnimation(
                        AnimationUtils.loadAnimation(
                                requireContext(), R.anim.light_text));
                break;

            case R.id.remove_account:
                this.syncPersonalManager.checkUserRoleForPermissions(
                        R.string.roleAdmin
                        , R.string.warningRemoveAccount
                        , this::setRemoveAccount
                        , new CustomAppDialog(requireContext()));
                break;

            case R.id.exit_account:
                setExitAccount();
                break;

            case R.id.app_info:
                setAppInfo();
                break;

            default:
                requireActivity().onBackPressed();
                break;
        }
    }

    private void startPrimaryInstruction()
    {

    }

    private void changeUserPhoto()
    {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                && (ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED))
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(true)
                    .setTitle(getString(R.string.infoTypeAddImage))
                    .setMessage(R.string.messageTypeAddImage, 4f)
                    .setPositiveButton("Личная", (click) ->
                    {
                        typeLoadPhoto = false;
                        startPhotoChange();
                    })
                    .setAdditionalButton("Общая", (click) ->
                    {
                        if (!this.checkOfflineEntrance(sharedPreferencesManager))
                        {
                            this.showOfferDialogRegistration(
                                    requireActivity());
                            return;
                        }

                        typeLoadPhoto = null;
                        startPhotoChange();
                    })
                    .setNegativeButton("Глобальная", (click) ->
                    {
                        if (!this.checkOfflineEntrance(sharedPreferencesManager))
                        {
                            this.showOfferDialogRegistration(
                                    requireActivity());
                            return;
                        }

                        typeLoadPhoto = true;
                        startPhotoChange();
                    })
                    .show();
        else
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(false)
                    .setTitle("Предоставление разрешений")
                    .setMessage(R.string.messageActionExStorageImage, DEFAULT_MESSAGE_SIZE)
                    .setPositiveButton("Далее", (click) ->
                            ActivityCompat.requestPermissions(requireActivity()
                            , new String[] {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                    , Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            , BasicAppActivity.PermissionCodes.ACCESS_STORAGE_IMAGE)
                    )
                    .setNegativeButton("Отмена", null)
                    .show();
    }

    private void startPhotoChange()
    {
        final Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent
                , "Выберите фотографию")
                , CODE_GET_IMAGE_FRAGMENT_INFO);
    }

    @Override
    public void onActivityResult(int requestCode
            , int resultCode
            , @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODE_GET_IMAGE_FRAGMENT_INFO
                && data != null)
        {
            final Cursor query = requireContext()
                    .getContentResolver()
                    .query(data.getData(), null, null, null, null);
            query.moveToFirst();

            @SuppressLint("Range")
            final String getPath = query.getString(
                    query.getColumnIndex(
                            MediaStore.MediaColumns.DATA));

            if (getPath == null)
                return;
            else if (!NetworkListener.getStatusNetwork()
                    && (this.typeLoadPhoto == null
                    || this.typeLoadPhoto))
            {
                this.sharedPreferencesManager.push_data_profile(
                        data.getDataString());
                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.warningUpdateGlobalPhoto)
                        , Toast.LENGTH_LONG)
                        .show();
                return;
            }

            final CustomAppDialog loadingDialog =
                    new CustomAppDialog(requireContext());

            if (this.checkOfflineEntrance(this.sharedPreferencesManager))
                loadingDialog.showLoadingDialog(
                        true, getString(R.string.infoSyncImageProfile));

            if (this.typeLoadPhoto == null || this.typeLoadPhoto)
            {
                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.infoUploadPhotoProfile)
                        , Toast.LENGTH_SHORT)
                        .show();

                MasterDesignCard.lowAbstractImageOptions(loadingDialog
                        , requireContext()
                        , getPath
                        , (this.typeLoadPhoto == null ? userIconImage : null)
                        , this.globalDataFBManager
                        , (this.typeLoadPhoto == null ? sharedPreferencesManager : null));
            }
            else
            {
                MasterDesignCard.lowAbstractImageOptions(loadingDialog
                        , requireContext()
                        , getPath
                        , userIconImage
                        , null
                        , this.sharedPreferencesManager);

                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.LocaleProfileUpdate)
                        , Toast.LENGTH_SHORT)
                        .show();
            }
            query.close();
        }
    }

    private void setChangeNameUser(boolean edit_text)
    {
        this.userInfoText.setFocusable(edit_text);
        this.userInfoText.setLongClickable(edit_text);
        this.userInfoText.setCursorVisible(edit_text);

        if (edit_text) changeNameUser();
    }

    private void changeNameUser()
    {
        final AppCompatImageButton timeFireBtn =
                view.findViewById(R.id.time_fire_btn);
        timeFireBtn.setImageResource(R.drawable.ic_complete);

        timeFireBtn.startAnimation(
                AnimationUtils.loadAnimation(
                        requireContext(), R.anim.to_text_light));
        timeFireBtn.startAnimation(
                AnimationUtils.loadAnimation(
                        requireContext(), R.anim.light_selected_btn));

        timeFireBtn.setOnClickListener(v ->
        {
            if (!userInfoText.getText().toString().equals(""))
            {
                this.sharedPreferencesManager.nameUserProfile(
                        userInfoText.getText().toString());

                if (this.checkOfflineEntrance(sharedPreferencesManager))
                    this.basicFireBaseManager.updateLocaleUserNickname(
                            userInfoText.getText().toString());

                Toast.makeText(requireContext()
                        , requireContext().getString(R.string.LocaleProfileUpdate)
                        , Toast.LENGTH_SHORT)
                        .show();
            }
           setChangeNameUser(false);
           timeFireBtn.setScaleX(0f);
           timeFireBtn.setOnClickListener(null);
        });
    }

    private void setRemoveAccount()
    {
        final AppCompatEditText confirmPasswordET =
                new AppCompatEditText(requireContext());
        confirmPasswordET.setInputType(
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordET.setHint("Введите текущий пароль");

        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle("Удаление аккаунта")
                .setViews(confirmPasswordET)
                .setSizeMainContainer(3f)
                .setPositiveButton("Удалить", (click) ->
                {
                    if (this.checkCorrectPassword(
                            this.removeSymbolsTab(confirmPasswordET.getText().toString())
                            , CorrectInputTextAuth.MIN_BOUND_PASSWORD
                            , CorrectInputTextAuth.MAX_BOUND_PASSWORD))
                        this.basicFireBaseManager.removeAccount(
                                this.removeSymbolsTab(confirmPasswordET.getText().toString())
                                , requireActivity());
                    else
                        Toast.makeText(requireContext()
                            , requireContext().getString(R.string.errorCheckPassword)
                            , Toast.LENGTH_SHORT
                        ).show();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void setExitAccount()
    {
        if (this.checkOfflineEntrance(this.sharedPreferencesManager))
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(true)
                    .setTitle(getString(R.string.infoAcceptAction))
                    .setMessage(R.string.messageExitAccountRegistration, DEFAULT_MESSAGE_SIZE)
                    .setPositiveButton("Выход", (click) -> actionRemoveAccountFinally())
                    .setNegativeButton("Отмена", null)
                    .show();
        else
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(true)
                    .setTitle(getString(R.string.infoAcceptAction))
                    .setMessage(R.string.messageExitAccountNoRegistration, DEFAULT_MESSAGE_SIZE)
                    .setPositiveButton("Выход", click-> actionRemoveAccountFinally())
                    .setNegativeButton("Отмена", null)
                    .setAdditionalButton("Регистрация", click ->
                            this.getChildFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                                    .replace(R.id.main_linear_reception, new RegisterAppFragment())
                                    .commit())
                    .show();
    }

    private void actionRemoveAccountFinally()
    {
        new CardManagerDB(requireContext())
                .removeDatabase();
        new HistoryManagerDB(
                requireContext()).removeDb();
        this.sharedPreferencesManager
                .removeAllData();
        this.sharedPreferencesManager
                .primaryLoadApp(true);
        this.setAnimationsCard();

        startActivity(new Intent(requireActivity()
                , ReceptionAppActivity.class));
        requireActivity().finish();
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    private void setAppInfo()
    {
        final AppCompatButton btnOpenDeveloperLink =
                new AppCompatButton(requireContext())
                , btnOpenPoliticalConfLink =
                new AppCompatButton(requireContext());
        final AppCompatTextView tvGeneralBuildAppInfo =
                new AppCompatTextView(requireContext())
                , tvPeopleInfo =
                new AppCompatTextView(requireContext());
        final int whiteColor =
                Color.parseColor("#FFFFFF");

        btnOpenDeveloperLink.setHint(R.string.infoDevUserApp);
        btnOpenDeveloperLink.setBackgroundResource(R.color.prism);
        btnOpenPoliticalConfLink.setHint(R.string.btnInfoPoliticalConf);
        btnOpenPoliticalConfLink.setBackgroundResource(R.color.prism);

        tvPeopleInfo.setText(R.string.peopleAppInfo);
        tvPeopleInfo.setTextSize(14f);
        tvPeopleInfo.setTextColor(whiteColor);
        tvPeopleInfo.setTypeface(Typeface.MONOSPACE);
        tvPeopleInfo.setGravity(Gravity.CENTER);

        btnOpenDeveloperLink.setTextSize(14f);
        btnOpenDeveloperLink.setHintTextColor(whiteColor);
        btnOpenDeveloperLink.setTypeface(Typeface.MONOSPACE);
        btnOpenDeveloperLink.setGravity(Gravity.CENTER);

        btnOpenPoliticalConfLink.setTextSize(14f);
        btnOpenPoliticalConfLink.setHintTextColor(whiteColor);
        btnOpenPoliticalConfLink.setTypeface(Typeface.MONOSPACE);
        btnOpenPoliticalConfLink.setGravity(Gravity.CENTER);

        tvGeneralBuildAppInfo.setText(R.string.infoGeneralBuildApp);
        tvPeopleInfo.setTextSize(14f);
        tvPeopleInfo.setTextColor(whiteColor);
        tvPeopleInfo.setTypeface(Typeface.MONOSPACE);
        tvPeopleInfo.setGravity(Gravity.CENTER);

        btnOpenDeveloperLink.setOnClickListener((inputView) ->
            startActivity(new Intent(
                    Intent.ACTION_VIEW
                    , Uri.parse("https://vk.com/id_cs_sourse"))));
        btnOpenPoliticalConfLink.setOnClickListener((inputView) ->
                startActivity(new Intent(
                        Intent.ACTION_VIEW
                        , Uri.parse("https://pages.flycricket.io/mcard/privacy.html"))));

       new CustomAppDialog(requireContext())
               .buildEntityDialog(true)
               .setTitle("Дополнительная информация")
               .setViews(btnOpenDeveloperLink
                       , tvPeopleInfo
                       , btnOpenPoliticalConfLink
                       , tvGeneralBuildAppInfo)
               .setContainerParams(75, 75, 10, true)
               .setAdditionalButton("", null)
               .setNegativeButton("Назад", null)
               .show();
    }

    synchronized public static void setUserIconImage(
     @NonNull RoundRectCornerImageView imageView
     , @NonNull Context context)
    {
        try
        {
             Picasso.get().load(new File(
                     new SharedPreferencesManager(context)
                             .path_userIconProfile(null)))
                     .into(imageView);
            imageView.setScaleType(
                    ImageView.ScaleType.CENTER_CROP);
        } catch (NullPointerException e)
        {
            imageView.setBorderRadius(0f);
            imageView.setImageResource(R.drawable.user_icon);
        }
    }

    public synchronized static String getUserName(Context context)
    {
        final String userNickname = new SharedPreferencesManager(context)
                .nameUserProfile(null);
        return userNickname == null
                ? "Пользователь" : userNickname;
    }

    @Override
    public boolean setAnimationsCard()
    {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N)
           new DataInterfaceCard(requireContext())
                    .actionAnimSelectCard(false);
        return true;
    }
}
