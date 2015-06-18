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

# Google Play Services
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

# Android Support Library
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

-keep class com.uoscs09.theuos2.annotation.**

-keepclassmembernames  @com.uoscs09.theuos2.annotation.KeepName class * {
     *;
}

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Library Jars
#-libraryjars libs/jericho-html-3.3.jar
#-libraryjars libs/asyncexcutor.jar

-keepattributes SourceFile,LineNumberTable

-dontwarn com.haarman.listviewanimations.**
-dontwarn net.htmlparser.jericho.**
-dontwarn com.google.android.gms.**