# ProGuard rules for Math Solitaire

# Kotlin Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# Preserve Data Models used in game session serialization
-keep class com.math.solitaire.data.model.** { *; }

# In-App Billing Services (Google Play, Bazaar, Myket)
-keep interface com.android.vending.billing.** { *; }
-keep class com.android.vending.billing.** { *; }
-keep class com.math.solitaire.data.billing.** { *; }

# Tapsell Ads & Mediation SDK
-keep class ir.tapsell.** { *; }
-dontwarn ir.tapsell.**

# Google Mobile Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**

# Android Jetpack & Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
