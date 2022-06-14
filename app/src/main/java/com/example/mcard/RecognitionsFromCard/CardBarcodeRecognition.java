package com.example.mcard.RecognitionsFromCard;

import android.util.SparseArray;
import androidx.annotation.NonNull;
import com.example.mcard.AdapersGroup.CardInfoEntity;
import com.example.mcard.FunctionalInterfaces.DelegateVoidInterface;
import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.CustomGraphicOverlay;
import com.example.mcard.RecognitionsFromCard.GraphicRecognitions.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Detector.Processor;
import com.google.android.gms.vision.barcode.Barcode;

public final class CardBarcodeRecognition implements Processor
{
  private Barcode resultBarcodeData;
  private final DelegateVoidInterface actionPostEncodeBarcode;
  private final GraphicOverlay<CustomGraphicOverlay> graphicOverlay;

  public CardBarcodeRecognition(
   @NonNull DelegateVoidInterface actionPostEncodeBarcode
   , @NonNull GraphicOverlay<CustomGraphicOverlay> graphicOverlay)
  {
    this.actionPostEncodeBarcode =
            actionPostEncodeBarcode;
    this.graphicOverlay = graphicOverlay;
  }

  @Override
  public void receiveDetections(Detector.Detections detections)
  {
    this.graphicOverlay.clear();
    this.resultBarcodeData = ((SparseArray<Barcode>)
            detections.getDetectedItems()).valueAt(0);
    try
    {
      this.graphicOverlay.add(
              new CustomGraphicOverlay(
                      graphicOverlay, this.resultBarcodeData));

      this.actionPostEncodeBarcode.delegateFunction(
              new CardInfoEntity.BarcodeEntity(
                      resultBarcodeData.rawValue
                      , resultBarcodeData.format));
    } catch (Exception ignored) { }
  }

  @Override
  public void release()
  { graphicOverlay.clear(); }
}
