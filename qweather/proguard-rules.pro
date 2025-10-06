# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in project.properties.
# You can edit this file to add your own keepers.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflection to access classes in your shrinking code, you have to
# specify with '-keep' options which classes to keep. For example:
# -keep public class mypackage.MyClass

# If you're using retrofit, you may need to add the following lines to your
# proguard-rules.pro file.
# See https://square.github.io/retrofit/#rest-methods
-dontwarn retrofit2.Platform$Java8
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature
-keepattributes *Annotation*