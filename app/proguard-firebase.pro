
# Begin: Proguard rules for Firebase

# Authentication
-keepattributes *Annotation*

# Realtime database
-keepattributes Signature

-dontwarn com.google.firebase.iid.zzc.**
# End: Proguard rules for Firebase