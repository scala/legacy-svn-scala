DownloaderTest shows you how to create an application that downloads a set of
files from a web server and stores the files on the SD card. It is useful
for applications, like games, that use too much content to store it all in
the application apk.

What this does:

+ Ensures that a given set of files have been downloaded from the
web server and installed on the /sdcard before the rest of the
application runs.

+ Provides a simple progress UI for the download process.

+ Diagnoses and reports common errors (such as lack of network connectivity)
that may occur during the download process.

What this doesn't do:

+ Does not provide a web server for serving the application data. You must
provide this yourself. There is no special requirement for the web server.
Any web server should work.

+ Does not check the integrity of the data files each time the application is
 run. If data file integrity is important to your application, you will
 have to take extra steps to ensure it.

+ Does not check if newer versions of the data files are available on the
 web server. If you want to do this you will have to do it yourself.

+ Does not provide a way of automatically uninstalling the data files when
 the application is uninstalled.

Known Issues:

+ The current Android implementation of java.security.MessageDigest is slow.
+ The USER_AGENT string is not currently actually used in requests sent
  to the HTTP server.

Using Downloader in your own application

To use the downloader in your own application:

1) Copy the sources and resources to your project:

   + com.google.android.downloader.PreconditionActivityHelper.java
   + com.google.android.downloader.DownloaderActivity.java
   + Merge the res/values/strings.xml strings into your project.

2) Add this code to the start of your activity's onCreate method:

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (! DownloaderActivity.ensureDownloaded(this, FILE_CONFIG_URL,
                CONFIG_VERSION, DATA_PATH, USER_AGENT)) {
            return;
        }

    private final static String FILE_CONFIG_URL =
        "http://example.com/download.config";
    private final static String CONFIG_VERSION="1.0";
    private final static String DATA_PATH = "/sdcard/data/downloadTest";
    private final static String USER_AGENT = "MyApp Downloader";

3) Edit your application's AndroidManifest.xml file:
   a) Add the android.permission.INTERNET permission:

    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="YOUR_PACKAGE_HERE">
        <uses-permission android:name="android.permission.INTERNET" />
        ...
   b) Add the DownloaderActivity activity
         <application>
           <activity android:name="DownloaderActivity"
            android:label="@string/download_activity_title" />
           ....

4) Create a config file with the following structure:

<config version="1.0">
  <file src="url-of-source-file" dest="relative-path-on-sd-card"
      size="1234" md5="..." />
  <file dest="relative-path-on-sd-card">
    <part src="url-of-first-part-of-file" size="1234" md5="..." />
    <part src="url-of-second-part-of-file" />
    ...
  </file>
  ...
</config>

The "version" attribute should match the CONFIG_VERSION argument passed to
DownloaderActivity.ensureDownloaded().

The "src" attribute can be relative to the config.xml file's URL, or it can be
absolute. The "dest" attribute is relative to the DATA_PATH argument to
DownloaderActivity.ensureDownloaded.

The "size" attribute is optional, but if included will speed up downloading by
reducing the number of HTTP round-trips that will have to be made to download
the files.

The "md5" attribute is optional, but if included will be used to verify that
the data was received from the server and written to the SD-Card correctly.
On OS X you can compute an MD5 digest for a file by executing:

    openssl md5 filename

Note that the "file" tag can either be a single tag or contain child "part"
tags. Part tags allow hosting large files on web servers that have
restrictions on the size of individual files.

5) Publish the config file and the data files on your web server.

6) Make sure you have an SD card installed on your device.

7) If you are using an emulator, consult
the SDK documentation for the "mksdcard" tool and the emulator -sdcard
command-line option.

8) If you are using the emulator with the Eclipse Android plugin, you can
configure the emulator to use your sdcard by clicking on the little black
triangle to the right of the "Debug" icon, and then choosing the
"Debug Configurations..." menu item. This brings up a "Debug Configurations"
dialog box where you can choose "Android Application:Downloader", and then
choose the "Target" tab and edit the "Additional Emulator Command Line
Options" field. Enter "-sdcard full-path-to-sdcard1.iso".

9) While debugging, or while the device is connected to a computer, make sure
that "USB mass storage" is turned off. Otherwise the sdcard will be mounted
read-only. You can modify the USB mass storage setting using the
"Settings : SD card & phone storage : Use for USB storage" check-box, available
from the home-screen Settings menu item.

10) Compile and run the application.

Note: When run under the Eclipse debugger there seems to be an issue where
after the "Downloader" activity completes the Home screen is displayed rather
than the "Download Test" activity. If this happens, you can long-press on the
"Home" button to bring up a menu that lets you choose the "Download Test"
activity.
This behavior does not seem to occur on an actual device.

Appendix: Extra files stored in the download directory

The downloader activity writes three files to the data directory:

.downloadConfig_temp
This file holds the downloaded configuration file for the duration of the
download process.

.downloadConfig_filtered
This file is only present during the download process. Its presence
indicates that the download directory has been filtered to remove any
files left over from earlier versions of the download data.

.downloadConfig
This file holds the downloaded configuration file after the download
process completes successfully. It's checked each time the application
starts to make sure the version number of the downloaded data matches
the version number of the application.



.


