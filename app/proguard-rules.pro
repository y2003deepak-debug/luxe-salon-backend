# ============================================================
# LUXE SALON — PRODUCTION PROGUARD / R8 RULES
# SECURITY FIX: VULN-06 — Code Obfuscation & Hardening
# ============================================================

# --- Kotlin Metadata (required for reflection-based libs) ---
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

# --- Room Entities: must preserve field names for DB column mapping ---
-keep class com.example.data.** { *; }

# --- Retrofit & OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# --- Moshi JSON serialization ---
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepnames class com.example.data.api.** { *; }

# --- Coil image loading ---
-dontwarn coil.**

# --- Compose (Jetpack Compose does not need special rules with R8 full mode) ---
-dontwarn androidx.compose.**

# --- SECURITY: Strip ALL Android Log calls from release builds ---
# This prevents any PII, credentials, or internal state from leaking via Logcat.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(java.lang.String, java.lang.String);
    public static int v(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int d(java.lang.String, java.lang.String);
    public static int d(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int i(java.lang.String, java.lang.String);
    public static int i(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int w(java.lang.String, java.lang.String);
    public static int w(java.lang.String, java.lang.String, java.lang.Throwable);
    public static int e(java.lang.String, java.lang.String);
    public static int e(java.lang.String, java.lang.String, java.lang.Throwable);
}

# --- SECURITY: Aggressive obfuscation ---
# Repackage all obfuscated classes into a single package 'a' to make
# class hierarchy analysis harder for reverse engineers.
-repackageclasses 'a'
-allowaccessmodification

# --- Stack trace preservation for crash reporting (Crashlytics/Firebase) ---
# Preserve line numbers but hide original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Keep BuildConfig so BuildConfig.DEBUG guard works at runtime ---
-keep class com.example.BuildConfig { *; }

# --- Kotlin coroutines ---
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
