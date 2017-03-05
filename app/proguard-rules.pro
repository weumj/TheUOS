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


-keepattributes SourceFile,LineNumberTable

-dontwarn com.haarman.listviewanimations.**
-dontwarn net.htmlparser.jericho.**
