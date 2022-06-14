package com.example.mcard.AdapersGroup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.mcard.R;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;

import java.util.List;

public final class HistoryManagerAdapter extends BaseAdapter
{
    private final List<HistoryUseInfoEntity> historyUseInfoEntities;
    private final LayoutInflater layoutInflater;
    private final Context context;
    private final ThemeAppController themeAppController;

    private HistoryUseInfoEntity infoAdapter;
    private View view;
    private TextView shopName
            , shopAddress
            , dateItemAdd;

    @Override
    public int getCount()
    { return historyUseInfoEntities.size(); }

    @Override
    public Object getItem(int element_id)
    { return historyUseInfoEntities.get(element_id); }

    @Override
    public long getItemId(int element_id)
    { return element_id; }

    @Override
    public View getView(int position, View new_view, ViewGroup viewGroup)
    {
        this.view = new_view;
        if (this.view == null)
            this.view = layoutInflater.inflate(R.layout.get_history_data
                , viewGroup
                , false);

        this.view.startAnimation(
                AnimationUtils.loadAnimation(
                        context, R.anim.animation_for_global_users_items));

        findObjects();
        infoAdapter = infoAdapter(position);

        this.themeAppController.settingsTextAdapter(
                this.shopName
                , this.infoAdapter.getShopName());
        this.themeAppController.settingsTextAdapter(
                this.shopAddress
                , this.infoAdapter.getShopAddress());
        this.themeAppController.settingsTextAdapter(
                this.dateItemAdd
                , this.infoAdapter.getTimeAdd());
        return view;
    }

    private HistoryUseInfoEntity infoAdapter(int position)
    { return (HistoryUseInfoEntity) getItem(position); }

    public HistoryManagerAdapter(
     @NonNull Context context
     , @NonNull ThemeAppController themeAppController
     , @NonNull List<HistoryUseInfoEntity> historyUseInfoEntities)
    {
        this.historyUseInfoEntities = historyUseInfoEntities;
        this.context = context;
        this.themeAppController = themeAppController;
        this.layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    private void findObjects()
    {
        this.shopName = view.findViewById(R.id.shop_name_TV);
        this.shopAddress = view.findViewById(R.id.shop_address_TV);
        this.dateItemAdd = view.findViewById(R.id.time_add_TV);
    }
}
