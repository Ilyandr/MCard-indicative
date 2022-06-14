package com.example.mcard.SideFunctionality;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;

public interface ListViewComponent
{
    void registerListView();
    void createItemInList(@NonNull ListView listView);

    @SuppressLint("ClickableViewAccessibility")
    default void settingsListView(@NonNull ListView listView
     , @NonNull Drawable res
     , @NonNull GeneralAnimations generalAnimations)
    {
        listView.setSelector(res);
        listView.setDivider(res);
        listView.setDividerHeight(-255);

        listView.setCacheColorHint(
                Color.parseColor("#00000000"));
        listView.setVerticalScrollBarEnabled(false);

        listView.setOnTouchListener((view1, motionEvent) ->
        {
            try
            {
                return !generalAnimations.getActionResolution()
                        && motionEvent.getAction()
                        == MotionEvent.ACTION_MOVE;
            }
            catch (NullPointerException nullPointerException) { return false; }
        });
    }

    default void selectCardAnim(
     @NonNull GeneralAnimations generalAnimations
     , @NonNull AdapterView<?> parent
     , @NonNull View selectedView
     , @NonNull Runnable actionAfter
     , int positionCard)
    {
        if (positionCard == 0 && parent.getCount() >= 2)
            generalAnimations.liftingCard(parent.getChildAt(positionCard)
                , parent.getChildAt(positionCard + 1)
                , actionAfter
                , parent.getChildAt(positionCard + 2) == null
                            ? (parent.getChildAt(positionCard + 1).getY()
                            - parent.getChildAt(positionCard).getY()) * 2
                            : parent.getChildAt(positionCard + 2).getY());
        else
            generalAnimations.liftingCard(
                parent.getSelectedView() == null
                        ? selectedView : parent.getSelectedView()
                , null
                , actionAfter
                , 1f);
    }

    default void resumeCardAnim(
            @NonNull GeneralAnimations generalAnimations)
    {
        generalAnimations.liftingCard(null
            , null
            ,null
            , null);
    }
}
