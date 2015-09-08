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
* build: 
```
$ mvn clean install
```
* zip: `target/uel-1.3.1.zip`


### v1.3.0
*(2015-04-15)*
* added capability to handle dissubsumptions to SAT-based algorithm
* added ASP-based algorithm (experimental)


### v1.2.0
*(2012-04-30)*
* added possibility to restrict SAT-based algorithm to minimal assignments


### v1.1.0
*(2012-03-09)*
* addded Rule-based algorithm


### v1.0.0
*(2012-01-27)*
* basic SAT-based algorithm


