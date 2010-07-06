@HEADER@

@INJARS@
@OUTJARS@
@LIBRARYJARS@

-overloadaggressively
-repackageclasses ''
-allowaccessmodification

-dontpreverify

-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

-dontwarn

#(org.xml.sax.EntityResolver)Class.forName(variable).newInstance()
-dontnote org.xml.sax.EntityResolver

-keep public class @MYAPP_PACKAGE@

