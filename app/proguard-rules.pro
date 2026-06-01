# Error Prone annotations - compile-only, not needed at runtime
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# Gson - keep @SerializedName annotated fields for export/import
-keepclassmembers class com.example.periodvibe.data.exportimport.** {
    <fields>;
}

# Keep line numbers for readable stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
