package com.example.mcard.UserAuthorization;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.R;
import com.example.mcard.FunctionalInterfaces.CorrectInputTextAuth;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.SideFunctionality.GeneralStructApp;

public final class RegisterAppFragment extends Fragment implements
 View.OnClickListener
 , GeneralStructApp
 , CorrectInputTextAuth
{
    private View view;
    private AppCompatEditText login
            , password
            , confirmPassword
            , accountNameET;
    private AppCompatButton btnRegister;
    private AppCompatImageButton btnLookPasswords;

    private Animation animSelected;
    private CustomAppDialog customAppDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment__register, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.btnRegister = view.findViewById(R.id.btn_complete);
        this.login = view.findViewById(R.id.login_reg);
        this.password = view.findViewById(R.id.password_reg);
        this.confirmPassword = view.findViewById(R.id.password_true);
        this.accountNameET = view.findViewById(R.id.accountName);
        this.btnLookPasswords = view.findViewById(R.id.btnLookPassword);

        this.animSelected = AnimationUtils.loadAnimation(
                requireContext()
                , R.anim.select_object);
        this.customAppDialog =
                new CustomAppDialog(requireContext());
    }

    @Override
    public void drawableView() { }

    @Override
    public void basicWork()
    {
        this.btnRegister.setOnClickListener(this);
        this.btnLookPasswords.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v)
    {
        v.startAnimation(this.animSelected);
        switch (v.getId())
        {
            case R.id.btn_complete:
                final String getLogin = this.removeSymbolsTab(
                        this.login.getText().toString());
                final String getPassword = this.removeSymbolsTab(
                        this.password.getText().toString());
                final String getAccountId = this.removeSymbolsTab(
                        this.accountNameET.getText().toString());

                if (!this.checkCorrectLogin(getLogin))
                    Toast.makeText(requireContext()
                            , requireContext().getString(R.string.errorCheckLogin)
                            , Toast.LENGTH_SHORT)
                            .show();
                else if (!getPassword.equals(confirmPassword.getText().toString()))
                    Toast.makeText(requireContext()
                            , requireContext().getString(R.string.errorCheckPasswordsRegister)
                            , Toast.LENGTH_SHORT)
                            .show();
                else if (!this.checkCorrectPassword(getPassword
                        , CorrectInputTextAuth.MIN_BOUND_PASSWORD
                        , CorrectInputTextAuth.MAX_BOUND_PASSWORD))
                    Toast.makeText(requireContext()
                            , requireContext().getString(R.string.errorCheckPassword)
                            , Toast.LENGTH_SHORT)
                            .show();
                else if (!this.checkCorrectPassword(getAccountId
                        , CorrectInputTextAuth.MIN_BOUND_ACCOUNT_ID
                        , CorrectInputTextAuth.MAX_BOUND_ACCOUNT_ID))
                    Toast.makeText(requireContext()
                            , requireContext().getString(R.string.errorCheckID)
                            , Toast.LENGTH_SHORT)
                            .show();
                else
                {
                    this.customAppDialog.showLoadingDialog(
                            true, "Проверка данных, ожидайте..");

                    new BasicFireBaseManager(
                            getLogin.trim()
                            , getPassword.trim()
                            , getAccountId.trim()
                            , requireContext()).registerInServer(
                                    this.customAppDialog
                            , BasicFireBaseManager.operation_code_write_inDB);
                } break;

            case R.id.btnLookPassword:
                this.password.setInputType(InputType.TYPE_CLASS_TEXT);
                this.confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT);

                new Handler().postDelayed(() ->
                {
                    this.password.setInputType(129);
                    this.confirmPassword.setInputType(129);
                }, 4000);
                break;
        }
    }
}