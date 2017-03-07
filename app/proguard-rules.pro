# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keepclasseswithmembernames @android.support.annotation.keep class * {
    *;
}

-keep class com.uoscs09.theuos2.annotation.**


-keepclassmembers @com.uoscs09.theuos2.annotation.KeepName class * {
   public <init>(...);
}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}



##### Android Support Library
-keep class android.support.** { *; }
-keep interface android.support.** { *; }
#-keep class !android.support.v7.internal.view.menu.**, ** { *; }

##### glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

#### xml
-keep class mj.android.utils.xml.**

-keepclassmembernames @mj.android.utils.xml.* class * {
    *;
}
-keepclassmembers @mj.android.utils.xml.* class * {
   public <init>(...);
}



# Begin: Proguard rules for Firebase

# Authentication
-keepattributes *Annotation*

# Realtime database
-keepattributes Signature

# End: Proguard rules for Firebase



#### Butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}



# Begin: Proguard rules for okhttp3

-dontwarn okhttp3.**
-dontwarn okio.**

-dontnote okhttp3.**

# End: Proguard rules for okhttp3


#### Google Play Services
#
# Keep SafeParcelable value, needed for reflection. This is required to support backwards
# compatibility of some classes.
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

# Keep the names of classes/members we need for client functionality.
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# Needed for Parcelable/SafeParcelable Creators to not get stripped
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-dontwarn com.google.android.gms.**



# Begin: Proguard rules for retrofit2

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

-dontwarn retrofit2.adapter.rxjava.CompletableHelper$**

# End: Proguard rules for retrofit2



#### Retrolambda
-dontwarn java.lang.invoke.*
-keepclassmembernames  @com.uoscs09.theuos2.annotation.KeepName class * {
     *;
}




-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

-dontwarn rx.internal.**
-dontnote rx.internal.**



-keepattributes SourceFile,LineNumberTable

-dontwarn com.haarman.listviewanimations.**
-dontwarn net.htmlparser.jericho.**
