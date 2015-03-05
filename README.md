# No longer in active development

**FasterGPS is no longer in active development. If you like to take over the maintaining, simply fork it and implement fixes. I will only do basic maintenance like merging pull requests and releasing new versions.**

# FasterGPS

With FasterGPS you can select a NTP Server matching your region to speed up the process of getting a GPS fix.

For more information visit http://sufficientlysecure.org/android-apps/

# Build with Gradle

1. Have Android SDK "tools", "platform-tools", and "build-tools" directories in your PATH (http://developer.android.com/sdk/index.html)
2. Open the Android SDK Manager (shell command: ``android``). Expand the Extras directory and install "Android Support Repository"
3. Export ANDROID_HOME pointing to your Android SDK
4. Execute ``./gradlew build``

## More build information

Two productFlavors are build with gradle. One for Google Play (without Paypal and Flattr Donations) and one for F-Droid (without Google Play Donations).

# Contribute

Fork FasterGPS and do a Pull Request. I will merge your changes back into the main project.

## Development

I am using the newest [Android Studio](http://developer.android.com/sdk/installing/studio.html) for development. Development with Eclipse is currently not possible because I am using the new [project structure](http://developer.android.com/sdk/installing/studio-tips.html).

1. Clone the project from github
2. From Android Studio: File -> Import Project -> Select the cloned top folder
3. Import project from external model -> choose Gradle

# Information about gps.conf

* http://forum.xda-developers.com/showthread.php?p=11342772
* http://forum.xda-developers.com/showthread.php?t=1498276
* http://forum.xda-developers.com/showthread.php?t=1493695
* http://forum.xda-developers.com/showthread.php?p=25094918

# Translations

Translations are hosted on Transifex, which is configured by ".tx/config"

1. To pull newest translations install transifex client (e.g. ``apt-get install transifex-client``)
2. Config Transifex client with "~/.transifexrc"
3. Go into root folder of git repo
4. execute ```tx pull``` (```tx pull -a``` to get all languages)

see http://help.transifex.net/features/client/index.html#user-client

# Coding Style

## Code
* Indentation: 4 spaces, no tabs
* Maximum line width for code and comments: 100
* Opening braces don't go on their own line
* Field names: Non-public, non-static fields start with m.
* Acronyms are words: Treat acronyms as words in names, yielding !XmlHttpRequest, getUrl(), etc.

See http://source.android.com/source/code-style.html

## XML
* XML Maximum line width 999
* XML: Split multiple attributes each on a new line (Eclipse: Properties -> XML -> XML Files -> Editor)
* XML: Indent using spaces with Indention size 4 (Eclipse: Properties -> XML -> XML Files -> Editor)

See http://www.androidpolice.com/2009/11/04/auto-formatting-android-xml-files-with-eclipse/

# Licenses
FasterGPS is licensed under the GPLv3+.  
The file LICENSE includes the full license text.

## Details
FasterGPS is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FasterGPS is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FasterGPS.  If not, see <http://www.gnu.org/licenses/>.

## Libraries
* Android Donations Lib  
  https://github.com/dschuermann/android-donations-lib  
  Apache License v2

* RootCommands  
  https://github.com/dschuermann/root-commands  
  Apache License v2

* HtmlTextView  
  https://github.com/dschuermann/html-textview  
  Apache License v2
  
## Images

* icon.svg  
  Based on GPS Navigation by shokunin and Stopwatch (no shading) by dtjohnnymonkey  
  http://openclipart.org/detail/76297/gps-navigation-by-shokunin  
  http://openclipart.org/detail/3977/stopwatch-(no-shading)-by-dtjohnnymonkey  
  Public Domain
