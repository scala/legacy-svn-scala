Unlocking Android - RestaurantFinder
------------------------------------
Android code example that uses the Google Base data feeds API
over HTTP to retrieve and display restaurant reviews on 
the Android platform. 

Note that this example currently uses a direct url.openStream
call to make a network request.  This works most of the time, but
is flaky in Android 1.0 for small requests (you may see socket errors
in logcat). This will probably be upgraded to using HttpClient in the future
(which doesn't seem to suffer the same issues) - just something to be 
aware of (still works as is 98% of the time). 

--------------------------------------

Checkout:
svn co http://unlocking-android.googlecode.com/svn/chapter3/trunk/RestaurantFinder/


Eclipse:
Setup a SVN repository for the UAD code project (http://unlocking-android.googlecode.com/svn). 
Then checkout chapter3/trunk/RestaurantFinder as an Eclipse project. 