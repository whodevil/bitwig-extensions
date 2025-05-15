Bitwig MPD 24 script extension
=============================

Early days. I want to integrate my workflow closer with the mpd, 
and I'd  also like to learn the bitwig api.

Experiments abound. 

### Developing
Dependencies
- Java 21 or newer
- Set the environment variable `BITWIG_EXTENSIONS_LOCATION` to the location bitwig expects the
extensions to be installed. On Windows, by default, this is `~/Documents/Bitwig\ Studio/Extensions`.

All the extensions ship in a single package, so hitting the install target will put the package where it needs to go
to be picked up by Bitwig.
- `./gradlew install`
