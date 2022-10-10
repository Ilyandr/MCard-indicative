-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile
-keep class com.google.firebase.** {*;}
-keep class com.google.android.** { *; }
-keep class retrofit2.** {*;}
-keep class org.slf4j.** {*;}
-keep class org.bouncycastle.jsse.** {*;}
-keep class org.openjsse.** {*;}
-keep class okhttp3.** {*;}
-keep interface retrofit2.** {*;}
-keepclasseswithmembers class com.google.firebase.FirebaseException
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn org.slf4j.impl.**
-dontwarn com.example.mcard.repository.models.storage.**

-keep class com.example.mcard.repository.models.storage.CardEntity { *; }
-keep class com.example.mcard.repository.models.other.GeoFindEntity { *; }
-keep class com.example.mcard.repository.models.storage.** { *; }
-keep class com.example.mcard.repository.features.rest.geolocation.ResponseAPIEntity.** { *; }


