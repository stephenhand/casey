# Casey
<img src="http://orig08.deviantart.net/e677/f/2010/239/2/6/casey_junior_widescreen_by_736berkshire.jpg" width="400" height="200" />

Utility classes that help developers use Java 8 streams to implement memory efficient, lazily evaluated IO processing without sacradicing a clean API and strong separation of concerns.

#### Currently includes

* QueryStreamer: Queries datasources via JDBC and wraps the returned RecordSet cursor in a Java 8 stream API to permit memory efficient, performant processing of large data sets.

* StreamInputStream: An InputStream implementation that takes a Java 8 stream of byte arrays as its data source, allowing you to pipe the contents of a stream to any consumer of binary data that takes an InputStream.
