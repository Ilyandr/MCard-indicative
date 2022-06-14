package com.example.mcard.AdapersGroup;

import static com.example.mcard.GeneralInterfaceApp.MasterDesignCard.MODE_DESIGN_CHECK_DOWNLOAD;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mcard.GroupServerActions.GlobalDataFBManager;
import com.example.mcard.R;
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout;
import com.example.mcard.StorageAppActions.DataInterfaceCard;
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView;
import com.example.mcard.FunctionalInterfaces.SingleCardSettings;

import java.util.ArrayList;
import java.util.List;

public final class BasicCardManagerAdapter
 extends BaseAdapter
 implements SingleCardSettings
{
    private final LayoutInflater layoutInflater;
    private final Context context;

    private final int MODE_GET_OBJECT = 1;
    private final int MODE_GET_VALUE = 2;

    private final List<CardInfoEntity> mainInfoList;
    private final boolean itsGlobalCall;

    private String mainInfoMapSingle;

    private View view;
    private AppCompatTextView name, number;
    private RoundRectCornerImageView alternativeDesign;
    private CustomRealtiveLayout customRealtiveLayout;
    private SwipeRefreshLayout swipeLayoutStarter;

    @Override
    public int getCount()
    { return mainInfoList.size(); }

    @Override
    public Object getItem(int position)
    { return mainInfoList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        this.view = convertView;
        this.view = (this.view == null) ?
                layoutInflater.inflate(R.layout.single_adapter_card
                , parent
                , false) : convertView;

        this.view.startAnimation(
                AnimationUtils.loadAnimation(
                        context, R.anim.general_listview_anim));

        findObjects();
        drawableCard(position);
        offSwipeLayoutAnimation();
        return view;
    }

    public BasicCardManagerAdapter(
      @NonNull Context context
      , @NonNull List<CardInfoEntity> mainInfoList
      , boolean itsGlobalCall)
    {
        this.layoutInflater = (LayoutInflater) context
                .getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);

        this.mainInfoList = mainInfoList;
        this.context = context;
        this.itsGlobalCall = itsGlobalCall;
    }

    private void offSwipeLayoutAnimation()
    {
        if (this.swipeLayoutStarter != null)
            new Handler().postDelayed(() ->
                    this.swipeLayoutStarter.setRefreshing(
                            this.mainInfoList.size() == 0)
                    , 800);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void drawableCard(int positionCard)
    {
        final List<TextView> allTextView = new ArrayList<>();
        allTextView.add(this.name);
        allTextView.add(this.number);
        final CardInfoEntity cardInfoFinal =
                infoAdapterList(positionCard);

        if (this.itsGlobalCall)
            new GlobalDataFBManager(context)
                    .setGlobalUserPhoto(
                            this.view.findViewById(R.id.globalImage)
                            , cardInfoFinal.getCardOwner()
                            , this.view.findViewById(R.id.loading));
        else
            view.findViewById(
                    R.id.setRadiusView).setScaleX(0f);

        final MasterDesignCard masterDesignCard =
                new MasterDesignCard(
                        context, allTextView);

        masterDesignCard.setCardSize(this.customRealtiveLayout);
        masterDesignCard.setCardDesignLocale
                (cardInfoFinal.getName(),
                cardInfoFinal.getColor()
                        , this.alternativeDesign);

        this.settingsCard(new DataInterfaceCard(context)
                , null
                , name
                , number);

        if (masterDesignCard.availabilityDesign(
                cardInfoFinal.getName(), MODE_DESIGN_CHECK_DOWNLOAD)
                == null)
        this.name.setText(
                cardInfoFinal.getName());

        this.number.setText(
                cardInfoFinal.getNumber());
    }

    private void findObjects()
    {
        this.alternativeDesign = view.findViewById(R.id.cardDesign);
        this.name = view.findViewById(R.id.name_card);
        this.number = view.findViewById(R.id.number_card);
        this.customRealtiveLayout = view.findViewById(R.id.relativeContainer);
    }

    private CardInfoEntity infoAdapterList(int position)
    {
        return (CardInfoEntity)
                getItem(position);
    }

    public synchronized static String transliterateCardName(
            @NonNull String cardName)
    {
        StringBuilder check_regexStroke = new StringBuilder();
        char singleSymbol, removeSymbol;
        cardName += " ";

        for (int i = 0; i < cardName.length() - 1; i++)
        {
            removeSymbol = '-';
            singleSymbol = cardName.charAt(i);

            if ((i == 0))
            {
                check_regexStroke.append(
                        Character.toUpperCase(singleSymbol));
                removeSymbol = singleSymbol;
            }
            else if ((i > 1))
            {
                if ((cardName.charAt(i - 1) == ' ')
                        && !(cardName.charAt(i + 1) == ' ')
                        && (Character.isLowerCase(singleSymbol)))
                {
                    if (Character.isLowerCase(singleSymbol))
                        check_regexStroke.append(Character.toUpperCase(singleSymbol));
                    removeSymbol = singleSymbol;
                }
                else if ((cardName.charAt(i - 1) == ' ')
                        && !(cardName.charAt(i + 1) == ' ')
                        && !(Character.isLowerCase(singleSymbol)))
                {
                    check_regexStroke.append(singleSymbol);
                    removeSymbol = singleSymbol;
                }
            }

            if (removeSymbol == '-')
                check_regexStroke.append(
                        Character.toLowerCase(singleSymbol));
            else
                check_regexStroke.toString()
                        .replace(String.valueOf(removeSymbol), "");
        }

        cardName = check_regexStroke
                .toString()
                .replaceAll(" ", "");

        final char[] rus_symbol = {' ','а','б','в','г','д','е','ё', 'ж','з','и','й','к','л','м','н','о','п','р','с','т','у','ф','х', 'ц','ч', 'ш','щ','ъ','ы','ь','э', 'ю','я','А','Б','В','Г','Д','Е','Ё', 'Ж','З','И','Й','К','Л','М','Н','О','П','Р','С','Т','У','Ф','Х', 'Ц', 'Ч','Ш', 'Щ','Ъ','Ы','Ь','Э','Ю','Я','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
        final String[] lat_symbol = {" ","a","b","v","g","d","e","e","zh","z","i","y","k","l","m","n","o","p","r","s","t","u","f","h","ts","ch","sh","sch", "","i", "","e","ju","ja","A","B","V","G","D","E","E","Zh","Z","I","Y","K","L","M","N","O","P","R","S","T","U","F","H","Ts","Ch","Sh","Sch", "","I", "","E","Ju","Ja","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < cardName.length(); i++)
            for (int j = 0; j < rus_symbol.length; j++ )
                if (cardName.charAt(i) == rus_symbol[j])
                     builder.append(lat_symbol[j]);

        return builder.toString();
    }

    public static int percentageSimilarityResult(
            String recognitionObject, String basicObject)
    {
        if (basicObject.contains(
                recognitionObject))
            return 100;

        int percentageSimilarityResult = 0;
        final int singlePercentage =
                (100 / basicObject.length());

        for(int firstIndex = 0
            ; firstIndex < recognitionObject.length()
                ; firstIndex++)
        {
            for (int secondIndex = firstIndex
                 ; secondIndex < basicObject.length()
                    ; secondIndex++)
            {
                if (recognitionObject.charAt(firstIndex)
                        == Character.toLowerCase(basicObject.charAt(secondIndex))
                        || recognitionObject.charAt(firstIndex)
                        == Character.toUpperCase(basicObject.charAt(secondIndex))
                        && ((firstIndex - secondIndex == 0)))
                    percentageSimilarityResult += singlePercentage;
            }
        }
        return percentageSimilarityResult;
    }

    @Override
    public void optionsViewByBarcodeCard() { }

    public List<CardInfoEntity> getMainInfoList()
    { return mainInfoList; }
}
