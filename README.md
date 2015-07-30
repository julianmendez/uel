# [UEL](http://julianmendez.github.io/uel/)

[![Build Status](https://travis-ci.org/julianmendez/uel.png?branch=master)](https://travis-ci.org/julianmendez/uel)

UEL, Unifier for the [description logic](http://dl.kr.org/) EL, is a plug-in for [Protégé](http://protege.stanford.edu/) that uses the [OWL API](http://owlcs.github.io/owlapi/).


## Source code

To clone and compile the project:

~~~
$ git clone https://github.com/julianmendez/uel.git
$ cd uel
$ mvn clean install
~~~



## Authors

[Barbara Morawska](http://lat.inf.tu-dresden.de/~morawska/), [Stefan Borgwardt](http://lat.inf.tu-dresden.de/~stefborg/), [Julian Mendez](http://lat.inf.tu-dresden.de/~mendez/)


## License

[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt), [GNU Lesser General Public License version 3](http://www.gnu.org/licenses/lgpl-3.0.txt)


## [Download](http://sourceforge.net/projects/uel/files/uel/1.3.0/zip/uel-1.3.0.zip/download)

  * download [uel-1.3.0](http://sourceforge.net/projects/uel/files/uel/1.3.0/zip/uel-1.3.0.zip/download) (2015-04-15)
  * all versions:


| version 	         | zip 	 | release date |	Java | OWL API |	Protégé      |
|:-------------------|:------|:-------------|:----:|:--------|:--------------|
| uel-1.4.0-SNAPSHOT |			 |              | 7    | 3.5.1   | 5.0.0-beta-17 |
| [uel-1.3.0](http://sourceforge.net/projects/uel/files/uel/1.3.0/plugin/de.tudresden.inf.lat.uel.jar/download) 	   | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.3.0/zip/uel-1.3.0.zip/download) |	2015-04-15  | 7    | 3.5.0   | 4.3           |
| [uel-1.2.0](http://sourceforge.net/projects/uel/files/uel/1.2.0/plugin/de.tudresden.inf.lat.uel.jar/download) 	   | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.2.0/zip/uel-1.2.0.zip/download) |	2012-04-30  | 6    | 3.2.4   | 4.1           |
| [uel-1.1.0](http://sourceforge.net/projects/uel/files/uel/1.1.0/plugin/de.tudresden.inf.lat.uel.jar/download) 	   | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.1.0/zip/uel-1.1.0.zip/download) |	2012-03-09  | 6    | 3.2.4   | 4.1           |
| [uel-1.0.0](http://sourceforge.net/projects/uel/files/uel/1.0.0/plugin/de.tudresden.inf.lat.uel.jar/download) 	   | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.0.0/zip/uel-1.0.0.zip/download) |	2012-01-27  | 6    | 3.2.4   | 4.1           |


## [download area](http://sourceforge.net/projects/uel/files/)


## tutorial

[example: UEL as application](http://julianmendez.github.io/uel/example.html)

[example: UEL as Java library](http://julianmendez.github.io/uel/example-code.html)



installation and use
as a plugin:
download de.tudresden.inf.lat.uel.jar and copy it into Protege_4.3/plugins.

as a library:
download the zip containing uel-1.3.0.jar in directory uel/uel-library
and use all of them as libraries.

as a standalone:

To start the standalone application, you can use the following script.



## source code
clone the source code using Git
```
git clone https://github.com/julianmendez/uel.git
```

browse repository
In Eclipse, it requires the m2e plug-in.


## build instructions
* **master** (trunk)
  to compile with Apache Maven (3.0.3+):
  ```
  mvn clean package
  ```
  to compile with Apache Ant (1.8.3+):
  ```
  ant
  ```
  release: uel-build/target/uel-version.zip

* **v1.3.0**
  ```
  git checkout v1.3.0
  ```
  to compile with Apache Maven (3.0.3+):
  ```
  cd uel
  mvn clean package
  ```
  to compile with Apache Ant (1.8.3+):
  ```
  cd uel
  ant
  ```
  release: `uel/uel-build/target/uel-1.3.0.zip`

* **v1.2.0**
  ``` 
  git checkout v1.2.0
  cd uel/uel.distribution
  ```
  with Apache Ant:
  ```
  ant
  ```
  with Apache Maven:
  ```
  mvn clean package javadoc:javadoc source:jar
  ```
  release: `uel/uel.distribution/target/uel-1.2.0.zip`

* **v1.0.0** and **v1.1.0**
  ```
  git checkout v1.1.0      (or git checkout v1.0.0)
  cd uel/uel.plugin
  ant -buildfile build-bundle.xml
  ```
  release: `uel/uel.plugin/target/de.tudresden.inf.lat.uel.jar`

* **master** (trunk) (for offline development)
  to verify dependencies with Apache Maven before going offline:
  ```
  mvn dependency:go-offline
  ```
  to compile with Apache Maven:
  ```
  mvn --offline clean package
  ```
  release: `uel-build/target/uel-version.zip`


## changes
* **v1.3.0**
 * added capability to handle dissubsumptions to SAT-based algorithm
 * added ASP-based algorithm (experimental)

* **v1.2.0**
 * added possibility to restrict SAT-based algorithm to minimal assignments

* **v1.1.0**
 * addded Rule-based algorithm

* **v1.0.0**
 * basic SAT-based algorithm



## [support](http://lat.inf.tu-dresden.de/~mendez)


