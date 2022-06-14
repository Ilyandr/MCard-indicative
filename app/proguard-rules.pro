-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile
-keep class com.google.firebase.** {*;}
-keep class com.google.android.** { *; }
-keep class retrofit2.** {*;}
-keep interface retrofit2.** {*;}
-keepclasseswithmembers class com.google.firebase.FirebaseException

-keep class com.example.mcard.AdapersGroup.CardInfoEntity { *; }
-keep class com.example.mcard.GroupServerActions.GeoFindEntity { *; }
-keep class com.example.mcard.UserLocation.ResponseAPIEntity.** { *; }


