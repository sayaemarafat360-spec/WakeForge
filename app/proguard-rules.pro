# ================================================================
# WakeForge ProGuard Rules
# ================================================================

# ================================================================
# Room - Keep all entities and data classes
# ================================================================
-keep class com.wakeforge.app.data.database.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Room @Entity annotated classes and all their fields
-keep @androidx.room.Entity class * { *; }

# Keep enum types used in Room entities
-keepclassmembers enum com.wakeforge.app.data.database.entity.** { *; }

# Keep Room column info and type converters
-keepclassmembers class * {
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.ForeignKey <fields>;
    @androidx.room.Embedded <fields>;
    @androidx.room.Ignore <init>(...);
}

# Keep Room TypeConverters
-keep @androidx.room.TypeConverter class * { *; }
-keepclassmembers class * { @androidx.room.TypeConverter *; }

# Keep Room DAO interfaces
-keep interface com.wakeforge.app.data.database.dao.** { *; }

# ================================================================
# Hilt - Dependency Injection
# ================================================================

# Keep @HiltAndroidApp annotated Application classes
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep @AndroidEntryPoint annotated classes
-keep class * { @dagger.hilt.android.AndroidEntryPoint *; }

# Keep Hilt module classes
-keep @dagger.Module class * { *; }
-keepclassmembers class * { @dagger.Module *; }

# Keep Hilt entry point accessors
-keepclassmembers class * {
    @dagger.hilt.InstallIn <methods>;
}

# Prevent obfuscation of generated Hilt code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep @Inject constructors
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep @AssistedInject constructors
-keepclassmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}

# Keep qualifier annotations
-keep @interface dagger.hilt.android.qualifiers.*
-keep @interface javax.inject.*

# ================================================================
# AdMob - Google Mobile Ads
# ================================================================
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keepattributes *Annotation*
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.ads.**

# ================================================================
# WakeForge Enums
# ================================================================
-keepclassmembers enum com.wakeforge.app.domain.models.** {
    **[] values();
    public *;
}

-keep enum com.wakeforge.app.domain.models.MissionType { *; }
-keep enum com.wakeforge.app.domain.models.MissionDifficulty { *; }
-keep enum com.wakeforge.app.domain.models.DayOfWeek { *; }
-keep enum com.wakeforge.app.domain.models.ThemeMode { *; }

# Keep all enum values() and valueOf() methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================================================================
# Kotlin Coroutines
# ================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ================================================================
# AndroidX & Jetpack Compose
# ================================================================

# Keep Compose navigation arguments
-keepclassmembers class * {
    *** navigationArguments(...);
}

# ================================================================
# Suppress warnings for third-party libraries
# ================================================================
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn com.google.protobuf.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.eclipse.jdt.annotation.**

# ================================================================
# General best practices
# ================================================================
# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep custom exceptions
-keep class * extends java.lang.Throwable {
    <init>(...);
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep Parcelable and Serializable (scoped to app package)
-keepclassmembers class com.wakeforge.app.** implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class com.wakeforge.app.** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
