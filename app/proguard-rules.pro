# App-specific R8 rules. Library rules (Coil, OkHttp, Apollo, Hilt, kotlinx.serialization)
# ship as consumer rules inside their own artifacts, so only project-specific keeps belong
# here.

# Navigation 3 back-stack keys (the @Serializable NavKey types in the navigation package)
# are restored from SavedState through a reflective serializer lookup. R8 full mode strips
# the generated serializer entry points when nothing references them directly, so keep
# exactly those — the kotlinx.serialization consumer rules cover the rest of the runtime,
# and no other member of the package is kept.

# The generated serializer class of each @Serializable key (e.g. ArticleDetail$$serializer).
-if @kotlinx.serialization.Serializable class com.baruckis.ainews.navigation.**
-keep,includedescriptorclasses class com.baruckis.ainews.navigation.<1>$$serializer { *; }

# serializer() on the Companion of @Serializable key classes (e.g. ArticleDetail).
-if @kotlinx.serialization.Serializable class com.baruckis.ainews.navigation.** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# serializer() on @Serializable key objects (e.g. the NewsList data object).
-if @kotlinx.serialization.Serializable class com.baruckis.ainews.navigation.** {
    public static ** INSTANCE;
}
-keepclassmembers class com.baruckis.ainews.navigation.<1> {
    public static com.baruckis.ainews.navigation.<1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
