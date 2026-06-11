# App-specific R8 rules. Library rules (Coil, OkHttp, Apollo, Hilt, kotlinx.serialization)
# ship as consumer rules inside their own artifacts, so only project-specific keeps belong
# here.

# Navigation 3 back-stack keys are restored from SavedState via kotlinx.serialization.
# The serialization plugin's consumer rules keep the generated serializers, but R8 full
# mode may still strip the @Serializable key classes' synthetic members if nothing else
# references them — keep them explicitly.
-keep,includedescriptorclasses class com.baruckis.ainews.navigation.** { *; }
