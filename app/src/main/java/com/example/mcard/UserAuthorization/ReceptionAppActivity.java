package com.example.mcard.UserAuthorization;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.example.mcard.BasicAppActivity;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.FunctionalInterfaces.NetworkConnection;
import com.example.mcard.GlobalListeners.NetworkListener;
import com.example.mcard.R;
import com.example.mcard.FunctionalInterfaces.CorrectInputTextAuth;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public final class ReceptionAppActivity extends AppCompatActivity implements NetworkConnection
 , View.OnClickListener
 , GeneralStructApp
 , CorrectInputTextAuth
{
    private AppCompatButton btnRegister
            , btnRestoreAccount
            , btnStartWork
            , btnOfflineEntrance;
    private AppCompatImageButton btnLookPassword;
    private AppCompatEditText login, password;
    private List<Fragment> fragmentList;
    private Animation animSelected;

    private RegisterAppFragment registerAppFragment;
    private CustomAppDialog customAppDialog;
    private RestoreManagerFragment restoreManagerFragment;
    private BasicFireBaseManager basicFireBaseManager;
    private NetworkListener networkListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);

       findObjects();
       drawableView();
       basicWork();
    }

    @Override
    public void findObjects()
    {
        this.btnStartWork = findViewById(R.id.btn_startApp);
        this.btnRegister = findViewById(R.id.btn_register);
        this.btnRestoreAccount = findViewById(R.id.restoreAccount);
        this.login = findViewById(R.id.login);
        this.password = findViewById(R.id.password);
        this.btnLookPassword = findViewById(R.id.btnLookPassword);
        this.btnOfflineEntrance = findViewById(R.id.btnOfflineEntrance);
        this.animSelected = AnimationUtils.loadAnimation(
                this, R.anim.select_object);

        this.fragmentList = new ArrayList<>();
        this.registerAppFragment = new RegisterAppFragment();
        this.restoreManagerFragment = new RestoreManagerFragment();
        this.customAppDialog = new CustomAppDialog(this);
    }

    @Override
    public void drawableView() { }

    @Override
    public void basicWork()
    {
        networkListenerRegister();
        this.btnRestoreAccount.setOnClickListener(this);
        this.btnStartWork.setOnClickListener(this);
        this.btnRegister.setOnClickListener(this);
        this.btnLookPassword.setOnClickListener(this);
        this.btnOfflineEntrance.setOnClickListener(this);
    }

    @Override
    public void networkListenerRegister()
    {
        this.networkListener = new NetworkListener();

        registerReceiver(this.networkListener
                , new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View inputView)
    {
        inputView.startAnimation(this.animSelected);

        if (inputView.getId() == R.id.btnOfflineEntrance)
        {
            actionOfflineEntrance();
            return;
        }

        if (checkNetwork())
            switch (inputView.getId())
            {
                case R.id.btn_register:
                    open_register(); break;
                case R.id.btn_startApp:
                    authentication(); break;
                case R.id.restoreAccount:
                    actionRestoreAccount(); break;
                case R.id.btnLookPassword:
                    timeLookPassword(); break;
                case R.id.btnOfflineEntrance:
            }
        else
            Toast.makeText(this
                , getString(R.string.ErrorNetwork)
                , Toast.LENGTH_SHORT)
                .show();
    }

    private void timeLookPassword()
    {
        this.password.setInputType(InputType.TYPE_CLASS_TEXT);
        new Handler().postDelayed(() ->
                this.password.setInputType(129), 4000);
    }

    private void open_register()
    {
      getSupportFragmentManager()
                .beginTransaction()
              .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                .replace(R.id.main_linear_reception, registerAppFragment)
                .commit();
    }

    private void authentication()
    {
        final boolean checkLogin =
                this.checkCorrectLogin(this.removeSymbolsTab(
                        this.login.getText().toString()));
        final boolean checkPassword =
                this.checkCorrectPassword(this.removeSymbolsTab(
                        this.password.getText().toString())
                        , CorrectInputTextAuth.MIN_BOUND_PASSWORD
                        , CorrectInputTextAuth.MAX_BOUND_PASSWORD);

        if (checkLogin && checkPassword)
          connectionFirebase(this.removeSymbolsTab(
                  this.login.getText().toString().trim())
                  , this.removeSymbolsTab(
                          this.password.getText().toString().trim()));
      else if (!checkLogin)
          Toast.makeText(this
                  , this.getString(R.string.errorCheckLogin)
                  , Toast.LENGTH_SHORT).show();
      else if (!checkPassword)
          Toast.makeText(this
                  , this.getString(R.string.errorCheckPassword)
                  , Toast.LENGTH_SHORT).show();
    }

    private void connectionFirebase(
            String get_login, String get_password)
    {
        this.basicFireBaseManager = new BasicFireBaseManager(
                get_login
                , get_password
                , null
                , ReceptionAppActivity.this);

        if (this.customAppDialog.getAcceptLoading())
        {
            this.customAppDialog.showLoadingDialog(
                    true, "Загрузка данных аккаунта..");

            this.basicFireBaseManager.authenticationInServer(
                    this.customAppDialog
                    , BasicFireBaseManager.operation_code_read_fromDB
                    , this);
        }
        else
            Toast.makeText(this
                    , R.string.messageWaitReception
                    , Toast.LENGTH_SHORT)
                    .show();
    }

    private void actionRestoreAccount()
    {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                .replace(R.id.main_linear_reception, this.restoreManagerFragment)
                .commit();
    }

    private void actionOfflineEntrance()
    {
        new CustomAppDialog(this)
                .buildEntityDialog(true)
                .setTitle(getString(R.string.titleDialogReception))
                .setMessage(getString(R.string.infoDialogOfflineEntrance), 10f)
                .setPositiveButton("Продолжить", (click) ->
                {
                    new SharedPreferencesManager(this)
                            .setUserData("-", "-");
                    Toast.makeText(
                            this
                            , R.string.infoSuccessEntrance
                            , Toast.LENGTH_LONG)
                            .show();

                    this.startActivity(new Intent(
                            ReceptionAppActivity.this
                            , BasicAppActivity.class));
                    this.finish();
                })
                .setNegativeButton("Назад", null)
                .show();
    }

    @Override
    public void onBackPressed()
    {
        this.fragmentList.clear();
        this.fragmentList = getSupportFragmentManager().getFragments();
        final int removeFragment = this.fragmentList.size();

        if (removeFragment == 0) super.onBackPressed();
        else
        {
            try
            {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                        .remove(this.fragmentList.get(removeFragment - 1))
                        .commit();
            }
            catch (ArrayIndexOutOfBoundsException ignored) { }
        }
    }

    @Override
    protected void onDestroy()
    {
        networkListenerUnregister();
        this.fragmentList.clear();
        super.onDestroy();
    }
}