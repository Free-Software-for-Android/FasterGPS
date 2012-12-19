# FasterGPS

With FasterGPS you can select a NTP Server matching your region to speed up the process of getting a GPS fix.

For more information visit http://code.google.com/p/faster-gps/

# Build using Ant

1. Add a file ``local.properties`` in the folder ``FasterGPS`` and ``FasterGPS/android-libs/Donations`` with the following lines:
``sdk.dir=/opt/android-sdk``. Alter these lines to your locations of the Android SDK!
2. Execute ```ant clear```
3. Execute ```ant debug -Dtemplates=other```
4. To disable Flattr and PayPal (not allowed in Google Play), execute ```ant debug -Dtemplates=google```

# Contribute

Fork FasterGPS and do a Pull Request. I will merge your changes back into the main project.

# Libraries

All JAR-Libraries are provided in this repository under ``libs``, all Android Library projects are under ``android-libs``.

# Information about gps.conf

* http://forum.xda-developers.com/showthread.php?p=11342772
* http://forum.xda-developers.com/showthread.php?t=1498276
* http://forum.xda-developers.com/showthread.php?t=1493695

other gps configs:
* http://forum.xda-developers.com/showthread.php?p=25094918

# Translations

Translations are hosted on Transifex, which is configured by ``.tx/config``

1. To pull newest translations install transifex client (e.g. aptitude install transifex-client)
2. Config Transifex client with ``~/.transifexrc``
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
The file COPYING includes the full license text.

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

* HTMLCleaner  
  http://htmlcleaner.sourceforge.net/  
  BSD License

* HtmlSpanner  
  Apache License v2