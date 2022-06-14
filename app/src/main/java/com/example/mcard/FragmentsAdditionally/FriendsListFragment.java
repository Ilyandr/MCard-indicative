package com.example.mcard.FragmentsAdditionally;

import static com.example.mcard.GlobalListeners.NetworkListener.getStatusNetwork;

import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.GroupServerActions.SyncPersonalManager;
import com.example.mcard.AdapersGroup.GlobalUserInfoEntity;
import com.example.mcard.R;
import com.example.mcard.GroupServerActions.BasicFireBaseManager;
import com.example.mcard.StorageAppActions.SQLiteChanges.GeneralRulesDB;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.StorageAppActions.DataInterfaceCard;

import java.util.ArrayList;
import java.util.List;

public final class FriendsListFragment extends Fragment implements
 GeneralStructApp
 , AdapterView.OnItemClickListener
{
    private View view;
    private CustomHeaderListView listFriends;
    private AppCompatImageButton backBtn
            , btnSubscribe
            , btnSubscribers;
    private TextView tvSubscribe, tvSubscribers;

    private List<GlobalUserInfoEntity> zeroList;
    private Animation selectedAnimation;

    private BasicFireBaseManager basicFireBaseManager;
    private SyncPersonalManager syncPersonalCardsManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
       this.view = inflater.inflate(R.layout.fragment_list_friends, container, false);

       findObjects();
       drawableView();
       basicWork();

       return this.view;
    }

    @Override
    public void findObjects()
    {
        this.listFriends = view.findViewById(R.id.listview_friends);
        this.backBtn = view.findViewById(R.id.btn_back);
        this.btnSubscribe = view.findViewById(R.id.my_subscribe);
        this.btnSubscribers = view.findViewById(R.id.my_subscribers);
        this.tvSubscribe = view.findViewById(R.id.my_subscribe_text);
        this.tvSubscribers = view.findViewById(R.id.my_subscribers_text);
        this.selectedAnimation = AnimationUtils.loadAnimation(
                requireContext(), R.anim.light_selected_btn);

        this.basicFireBaseManager =
                new BasicFireBaseManager(requireContext());
        this.syncPersonalCardsManager =
                new SyncPersonalManager(requireContext());
        this.zeroList = new ArrayList<>();
    }

    @Override
    public void drawableView()
    {
        final ThemeAppController themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bottomPanel));
        themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , requireContext().getString(R.string.left_menu_list_friends));
        themeAppController.setOptionsButtons(
                backBtn
                , btnSubscribe
                , btnSubscribers);
    }

    @Override
    public void basicWork()
    {
        getSubscribeList();
        this.listFriends.setOnItemClickListener(this);
        this.backBtn.setOnClickListener(v -> requireActivity().onBackPressed());
        this.btnSubscribe.setOnClickListener(v -> getSubscribeList());
        this.btnSubscribers.setOnClickListener(v -> get_subscribersList());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        final GlobalProfileUserFragment globalProfileUser_fragment =
                new GlobalProfileUserFragment();
        final Bundle bundle = new Bundle();

        bundle.putSerializable(GeneralRulesDB.TRANSACTION_KEY
                , (GlobalUserInfoEntity) parent
                        .getAdapter()
                        .getItem(position));
        globalProfileUser_fragment.setArguments(bundle);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.menu_to_right, R.anim.menu_to_left)
                .replace(R.id.main_linear, globalProfileUser_fragment)
                .commit();
    }

    private void getSubscribeList()
    {
        this.listFriends.removeAllViewsInLayout();
        this.btnSubscribers.clearAnimation();
        this.tvSubscribers.clearAnimation();

        this.tvSubscribe.startAnimation(this.selectedAnimation);
        this.btnSubscribe.startAnimation(this.selectedAnimation);

        if (getStatusNetwork())
        this.syncPersonalCardsManager.getListFriends(this.listFriends
                , this.zeroList
                , false);
        else
            listFriends.checkEmptyList(false);
    }

    private void get_subscribersList()
    {
        this.listFriends.removeAllViewsInLayout();
        this.btnSubscribe.clearAnimation();
        this.tvSubscribe.clearAnimation();

        this.tvSubscribers.startAnimation(this.selectedAnimation);
        this.btnSubscribers.startAnimation(this.selectedAnimation);

        if (getStatusNetwork())
            this.syncPersonalCardsManager.getListFriends(
                    this.listFriends
                    , this.zeroList
                    , true);
        else
            listFriends.checkEmptyList(false);
    }

    @Override
    public void onDestroy()
    {
        this.zeroList.clear();
        super.onDestroy();
    }
}