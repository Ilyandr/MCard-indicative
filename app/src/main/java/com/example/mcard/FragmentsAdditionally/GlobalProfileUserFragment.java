package com.example.mcard.FragmentsAdditionally;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.FunctionalInterfaces.CorrectInputTextAuth;
import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.GroupServerActions.SyncPersonalManager;
import com.example.mcard.R;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;

public final class GlobalProfileUserFragment extends Fragment implements View.OnClickListener
 , GeneralStructApp
 , CorrectInputTextAuth
{
    private View view;
    private AppCompatTextView btnSubscribe
            , btnSendMessage
            , btnListFriends
            , btnListPublicCards
            , btnAddBlackList
            , userId
            , allAccountUsers
            , dataRegister;

    private AppCompatImageButton btnBack;
    private UserIconLayout userIcon;

    private GlobalUserInfoEntity inputGlobalUserInfoEntity;
    private Animation animSelected;
    public static boolean globalListFragment;

    private GlobalDataFBManager globalDataFBManager;
    private SyncPersonalManager personalCardsManager;
    private BasicFireBaseManager basicFireBaseManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_user_global_profile, container, false);

        findObjects();
        drawableView();
        basicWork();

        return view;
    }

    @Override
    public void findObjects()
    {
        globalListFragment = true;
        this.btnSubscribe = view.findViewById(R.id.offer_subscribe);
        this.btnSendMessage = view.findViewById(R.id.send_message);
        this.btnListFriends = view.findViewById(R.id.list_friends);
        this.btnListPublicCards = view.findViewById(R.id.list_publicCards);
        this.btnAddBlackList = view.findViewById(R.id.add_blackList);
        this.btnBack = view.findViewById(R.id.btn_back);
        this.dataRegister = view.findViewById(R.id.user_dataRegister);
        this.userIcon = view.findViewById(R.id.user_photo);
        this.userId = view.findViewById(R.id.user_id);
        this.allAccountUsers = view.findViewById(R.id.users_count);

        this.basicFireBaseManager =
                new BasicFireBaseManager(requireContext());
        this.globalDataFBManager =
                new GlobalDataFBManager(requireContext());
        this.personalCardsManager =
                new SyncPersonalManager(requireContext());

        this.animSelected = AnimationUtils.loadAnimation(
                requireContext()
                , R.anim.select_object);

        this.inputGlobalUserInfoEntity = (GlobalUserInfoEntity) requireArguments()
                .getSerializable(GeneralRulesDB.TRANSACTION_KEY);
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

        themeAppController.setOptionsButtons(
                btnBack
                , btnSubscribe
                , btnSendMessage
                , btnListFriends
                , btnListPublicCards
                , btnAddBlackList);

        themeAppController.settingsText(view.findViewById(R.id.name_fragment)
                , "Профиль");
        themeAppController.settingsText(this.userId
                , "Аккаунт: " + inputGlobalUserInfoEntity.getUserGlobalID());
        themeAppController.settingsText(this.allAccountUsers
                , "Участники: " + this.inputGlobalUserInfoEntity
                .getNetworkActions());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void basicWork()
    {
        checkDataUser();

        this.btnSubscribe.setOnClickListener(this);
        this.btnBack.setOnClickListener(this);
        this.btnSendMessage.setOnClickListener(this);
        this.btnListFriends.setOnClickListener(this);
        this.btnListPublicCards.setOnClickListener(this);
        this.btnAddBlackList.setOnClickListener(this);

        this.globalDataFBManager.setGlobalUserPhoto(this.userIcon
                , this.inputGlobalUserInfoEntity.getUserGlobalID()
                , view.findViewById(R.id.loading));
        this.basicFireBaseManager
                .getInfoDataRegister(
                        this.inputGlobalUserInfoEntity.getUserGlobalUID()
                        , dataRegister);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v)
    {
        v.startAnimation(animSelected);

        switch (v.getId())
        {
            case R.id.offer_subscribe:
                setBtn_subscribe();
                break;

            case R.id.send_message:
                personalCardsManager.checkSubscriptionAccessRights(
                        inputGlobalUserInfoEntity.getUserGlobalID()
                        , this::setBtnSendMessage);
                break;

            case R.id.list_friends:
                setBtn_listFriends();
                break;

            case R.id.list_publicCards:
                personalCardsManager.checkSubscriptionAccessRights(
                        inputGlobalUserInfoEntity.getUserGlobalID()
                        , this::setBtn_list_publicCards);
                break;

            case R.id.add_blackList:
                setBtn_addBlackList();
                break;

            default:
                requireActivity().onBackPressed();
                break;
        }
    }

    private void setBtn_subscribe()
    {
        if (!btnSubscribe.getText().toString().equals("Отписаться"))
            this.personalCardsManager
                    .addNewFriend(this.inputGlobalUserInfoEntity.getUserGlobalID()
                    , this.inputGlobalUserInfoEntity.getUserGlobalUID()
                    , this.btnAddBlackList
                    , this.btnSubscribe);
        else
        {
            new CustomAppDialog(requireContext())
                    .buildEntityDialog(true)
                    .setTitle(getString(R.string.infoAcceptAction))
                    .setMessage("Вы точно хотите отменить подписку на аккаунт \""
                            + this.inputGlobalUserInfoEntity.getUserGlobalID() + "\"?", 2.75f)
                    .setPositiveButton("Да", (click) ->
                            this.personalCardsManager.removeUserFriend(
                                    this.inputGlobalUserInfoEntity.getUserGlobalID()
                                    , this.inputGlobalUserInfoEntity.getUserGlobalUID()
                                    , this.btnSubscribe))
                    .setNegativeButton("Отмена", null)
                    .show();
        }
    }

    public void setBtnSendMessage()
    {
        final AppCompatEditText userInputRequestText =
                new AppCompatEditText(requireContext());

        final AppCompatTextView textInfoCountInputSymbols =
                new AppCompatTextView(requireContext());

        textInfoCountInputSymbols.setGravity(Gravity.CENTER);
        textInfoCountInputSymbols.setTextSize(15f);
        textInfoCountInputSymbols.setTextColor(Color.WHITE);
        textInfoCountInputSymbols.setText(
                "Введено символов: 0.");
        userInputRequestText.setHint(R.string.messageRequestCardUser);
        userInputRequestText.setTextSize(15f);

        final TextWatcher changeTextController = new TextWatcher()
        {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence requestInfo, int i, int i1, int i2)
            {
                if (requestInfo.toString().length() > 20)
                    userInputRequestText.setText(
                            requestInfo.toString().substring(0, 19));

                textInfoCountInputSymbols.setText(
                        "Введено символов: "
                                + requestInfo.toString().length() + ".");
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) { }
        };

        userInputRequestText.addTextChangedListener(
                changeTextController);

        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle("Менеджер запросов карт")
                .setViews(userInputRequestText, textInfoCountInputSymbols)
                .setPositiveButton("Отправить", (click) ->
                {
                    if (this.checkRequestCardText(userInputRequestText
                            .getText()
                            .toString()))
                        basicFireBaseManager.sendGlobalOfferCard(
                                userInputRequestText.getText().toString()
                                , this.inputGlobalUserInfoEntity.getUserGlobalUID());
                    else
                        Toast.makeText(requireContext()
                                , requireContext().getString(R.string.errorRequestCardText)
                                , Toast.LENGTH_SHORT)
                                .show();

                    userInputRequestText.removeTextChangedListener(
                            changeTextController);
                })
                .setNegativeButton("Отмена", click ->
                        userInputRequestText.removeTextChangedListener(
                                changeTextController))
                .show();
    }

    private void setBtn_listFriends()
    {
      final UserSubscribersFragment userSubscribersFragment =
              new UserSubscribersFragment();
      userSubscribersFragment.setArguments(
              requireArguments());

       requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                .replace(R.id.main_linear, userSubscribersFragment)
                .commit();
    }

    private void setBtn_list_publicCards()
    {
        final SimilarCardsFragment similarCardsFragment =
                new SimilarCardsFragment(
                        SimilarCardsFragment.CODE_USER_PUBLIC_CARDS);
        similarCardsFragment.setArguments(requireArguments());

       requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                .replace(R.id.main_linear, similarCardsFragment)
                .commit();
    }

    private void setBtn_addBlackList()
    {
        final boolean itsLockUser = (this.btnAddBlackList
                .getText()
                .toString()
                .equals("Заблокировать"));

        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle(getString(R.string.infoAcceptAction))
                .setMessage("Вы точно хотите "
                        + (itsLockUser ? "заблокировать" : "разблокировать")
                        + " аккаунт \""
                        + this.inputGlobalUserInfoEntity.getUserGlobalID() + "\"?", 2.75f)
                .setPositiveButton("Да", (click) ->
                {
                    if (itsLockUser)
                        this.personalCardsManager.addInBlackList(
                                this.inputGlobalUserInfoEntity.getUserGlobalUID()
                                , this.inputGlobalUserInfoEntity.getUserGlobalID()
                                , btnAddBlackList
                                , btnSubscribe);
                    else
                        personalCardsManager.removeFromBlackList(
                                this.inputGlobalUserInfoEntity.getUserGlobalID()
                                , btnAddBlackList);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void checkDataUser()
    {
        this.personalCardsManager
                .checkUserInBlackListStageSecond(
                        this.inputGlobalUserInfoEntity.getUserGlobalID()
                , this.btnAddBlackList);

        this.personalCardsManager.checkSelectedUser(
                this.btnSubscribe
                , this.inputGlobalUserInfoEntity.getUserGlobalID());
    }

    public static class UserIconLayout extends AppCompatImageView
    {
        private int colorBorder = Color.parseColor("#000000");
        private float borderSize = 12f;
        private CardView additionalImageBorder;

        public UserIconLayout(@Nullable Context context)
        { super(context); }

        public UserIconLayout(@Nullable Context context, AttributeSet attrs)
        { super(context, attrs); }

        public UserIconLayout(@Nullable Context context, AttributeSet attrs, Integer defStyle)
        { super(context, attrs, defStyle); }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(@NonNull Canvas canvas)
        {
            final Path path = new Path();
            final RectF rect = new RectF(0f, 0f, this.getWidth(), this.getHeight());
            path.addRoundRect(rect, 20f, 20f, Path.Direction.CW);

            canvas.clipPath(path);
            super.onDraw(canvas);

            if (this.borderSize != 0f)
            {
                final Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(this.colorBorder);

                paint.setStrokeWidth(this.borderSize);
                canvas.drawPath(path, paint);
            }
            else if (this.additionalImageBorder != null)
                this.additionalImageBorder
                        .setCardBackgroundColor(this.colorBorder);

            path.close();
        }

        public void setColorBorder(int colorBorder)
        { this.colorBorder = colorBorder; }

        public void setBorderSize(float borderSize)
        { this.borderSize = borderSize; }

        public void setAdditionalImageBorder(CardView additionalImageBorder)
        { this.additionalImageBorder = additionalImageBorder; }
    }
}