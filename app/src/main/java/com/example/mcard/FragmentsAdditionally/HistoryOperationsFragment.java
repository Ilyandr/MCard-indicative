package com.example.mcard.FragmentsAdditionally;

import android.os.Bundle;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.mcard.AdapersGroup.HistoryManagerAdapter;
import com.example.mcard.AdapersGroup.HistoryUseInfoEntity;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView;
import com.example.mcard.R;
import com.example.mcard.StorageAppActions.SQLiteChanges.HistoryManagerDB;
import com.example.mcard.SideFunctionality.CustomAppDialog;
import com.example.mcard.SideFunctionality.GeneralStructApp;
import com.example.mcard.GeneralInterfaceApp.ThemeAppController;
import com.example.mcard.StorageAppActions.DataInterfaceCard;

import java.util.ArrayList;
import java.util.List;

public final class HistoryOperationsFragment extends Fragment implements
 GeneralStructApp
 , AdapterView.OnItemClickListener
{
    private View view;
    private AppCompatImageButton btnBack;
    private CustomHeaderListView listViewHistory;

    private HistoryManagerDB historyManagerDB;
    private List<HistoryUseInfoEntity> historyUseInfoEntities;
    private ThemeAppController themeAppController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.view = inflater.inflate(R.layout.fragment_history_operation, container, false);

        findObjects();
        drawableView();
        basicWork();

        return this.view;
    }

    @Override
    public void findObjects()
    {
        this.btnBack = view.findViewById(R.id.btn_back);
        this.listViewHistory = view.findViewById(R.id.listview_history);
        this.historyManagerDB = new HistoryManagerDB(requireContext());
    }

    @Override
    public void drawableView()
    {
        this.themeAppController =
                new ThemeAppController(
                        new DataInterfaceCard(requireContext()));

        themeAppController.changeDesignIconBar(
                view.findViewById(R.id.bar_icon));
        themeAppController.changeDesignDefaultView(
                view.findViewById(R.id.mainLinearLayout));
        themeAppController.settingsText(
                view.findViewById(R.id.name_fragment)
                , "История");
        themeAppController.setOptionsButtons(btnBack);
    }

    @Override
    public void basicWork()
    {
        this.historyUseInfoEntities = new ArrayList<>();
        this.historyManagerDB.readDBHistory(
                this.historyUseInfoEntities);

        this.listViewHistory.setAdapter(new HistoryManagerAdapter(
                requireContext()
                , this.themeAppController
                , this.historyUseInfoEntities));

        this.listViewHistory.checkEmptyList(false);
        this.listViewHistory.setOnItemClickListener(this);

        this.btnBack.setOnClickListener(v ->
                requireActivity().onBackPressed());
    }

    @Override
    public void onItemClick(AdapterView<?> parent
            , View view
            , int position
            , long id)
    {
        new CustomAppDialog(requireContext())
                .buildEntityDialog(true)
                .setTitle("Удаление записи")
                .setMessage(R.string.infoRemoveHistory, 3.25f)
                .setPositiveButton("Удалить", unused ->
                {
                    if (this.historyManagerDB.removeSingleActionHistory((
                            (HistoryUseInfoEntity) parent.getAdapter()
                                    .getItem(position)).getTimeAdd()))

                    this.historyUseInfoEntities.remove(
                            (HistoryUseInfoEntity) parent
                                    .getAdapter()
                                    .getItem(position));

                    this.listViewHistory.removeAllViewsInLayout();
                    this.listViewHistory.setAdapter(
                            new HistoryManagerAdapter(
                                    requireContext()
                                    , themeAppController
                                    , historyUseInfoEntities));
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onDestroy()
    {
        this.historyUseInfoEntities.clear();
        super.onDestroy();
    }
}