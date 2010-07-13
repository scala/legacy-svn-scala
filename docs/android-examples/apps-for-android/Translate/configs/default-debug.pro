@HEADER@

@INJARS@
@OUTJARS@
@LIBRARYJARS@

-dontobfuscate
-dontoptimize
-dontpreverify

-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

-dontwarn

#scala.Enumeration accesses a field 'MODULE$' dynamically
-dontnote scala.Enumeration

#(org.xml.sax.EntityResolver)Class.forName(variable).newInstance()
-dontnote org.xml.sax.EntityResolver

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keep public class @MYAPP_PACKAGE@

