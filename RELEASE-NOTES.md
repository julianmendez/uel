## Release notes


| version | release date | Java | OWL API | Protégé       |
|:--------|:-------------|:----:|:--------|:--------------|
| v1.3.1  |              | 7    | 3.5.1   | 5.0.0-beta-17 |
| v1.3.0  | 2015-04-15   | 7    | 3.5.0   | 4.3           |
| v1.2.0  | 2012-04-30   | 6    | 3.2.4   | 4.1           |
| v1.1.0  | 2012-03-09   | 6    | 3.2.4   | 4.1           |
| v1.0.0  | 2012-01-27   | 6    | 3.2.4   | 4.1           |


### v1.3.1
*(unreleased)*
* has new icons
* does not longer have [Apache Ant + Apache Ivy](http://ant.apache.org/ivy/) build files
* includes Maven POM files to be deployed in [Sonatype](https://oss.sonatype.org/)
* is available at [The Central Repository](https://repo1.maven.org/maven2/de/tu-dresden/inf/lat/uel/)
* build commands: 
```
$ mvn clean install
```
* release: `target/uel-1.3.1.zip`


### v1.3.0
*(2015-04-15)*
* added capability to handle dissubsumptions to SAT-based algorithm
* added ASP-based algorithm (experimental)
```
$ git checkout v1.3.0
```
to compile it with Apache Maven (3.0.3+):
```
$ cd uel
$ mvn clean package
```
to compile it with Apache Ant (1.8.3+):
```
$ cd uel
$ ant
```
release: `uel/uel-build/target/uel-1.3.0.zip`

### v1.2.0
*(2012-04-30)*
* added possibility to restrict SAT-based algorithm to minimal assignments
* build commands: 
``` 
$ git checkout v1.2.0
$ cd uel/uel.distribution
```
with Apache Ant:
```
$ ant
```
with Apache Maven:
```
$ mvn clean package javadoc:javadoc source:jar
```
release: `uel/uel.distribution/target/uel-1.2.0.zip`


### v1.1.0
*(2012-03-09)*
* addded Rule-based algorithm
* build commands: 
```
$ git checkout v1.1.0
$ cd uel/uel.plugin
$ ant -buildfile build-bundle.xml
```
* release: `uel/uel.plugin/target/de.tudresden.inf.lat.uel.jar`


### v1.0.0
*(2012-01-27)*
* basic SAT-based algorithm
* build commands: 
```
$ git checkout v1.0.0
$ cd uel/uel.plugin
$ ant -buildfile build-bundle.xml
```
* release: `uel/uel.plugin/target/de.tudresden.inf.lat.uel.jar`


