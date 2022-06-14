package com.example.mcard.RecognitionsFromCard;

import android.util.SparseArray;
import android.widget.TextView;

import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.CustomGraphicOverlay;
import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;

import static com.google.android.gms.vision.Detector.Detections;
import static com.google.android.gms.vision.Detector.Processor;

import androidx.annotation.NonNull;

public final class CardTextRecognition implements Processor
{
    private boolean work_class = true;
    public String getResult;
    private TextView textFind;
    private final GraphicOverlay<CustomGraphicOverlay> graphicOverlay;

    public CardTextRecognition(
            @NonNull GraphicOverlay<CustomGraphicOverlay> graphicOverlay)
    { this.graphicOverlay = graphicOverlay; }

    public void setWorkClass(boolean work)
    { work_class = work; }

    @Override
    public void release()
    { graphicOverlay.clear(); }

    @Override
    public void receiveDetections(@NonNull Detections detections)
    {
        this.graphicOverlay.clear();
        if (work_class)
        {
            try
            {
                final SparseArray<TextBlock> items =
                        detections.getDetectedItems();

                for (int i = 0; i < items.size(); i++)
                {
                    final TextBlock item = items.valueAt(i);
                    if ((item != null) && (item.getValue() != null))
                    {
                        this.graphicOverlay.add(
                                new CustomGraphicOverlay(
                                        graphicOverlay, item));
                        if (!correctGetText(item.getValue()).equals(""))
                        {
                            textFind.setText(correctGetText(item.getValue()));
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }
    public void setTextFind(TextView text_find)
    { this.textFind = text_find; }

    private synchronized String correctGetText(String getBadResult)
    {
        try
        {
            for (int i = 0; i < getBadResult.length(); i++)
                getBadResult =
                        getBadResult.replace(" ", "")
                                .replace("O", "0");

            if(getBadResult.matches("[0-9]\\d+"))
                getResult = getBadResult;
            else getResult = "";

            return getResult;
        }
        catch (Exception exception)
        {
            this.getResult = "";
            return getResult;
        }
    }
}
