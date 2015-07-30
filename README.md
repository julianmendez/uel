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
  * [all versions](http://sourceforge.net/projects/uel/files/):


| version 	          | zip 	 | release date |	Java | OWL API |	Protégé       |
|:-------------------|:------|:-------------|:----:|:--------|:--------------|
| uel-1.4.0-SNAPSHOT |	      |              | 7    | 3.5.1   | 5.0.0-beta-17 |
| [uel-1.3.0](http://sourceforge.net/projects/uel/files/uel/1.3.0/plugin/de.tudresden.inf.lat.uel.jar/download)   | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.3.0/zip/uel-1.3.0.zip/download) |	2015-04-15  | 7    | 3.5.0   | 4.3           |
| [uel-1.2.0](http://sourceforge.net/projects/uel/files/uel/1.2.0/plugin/de.tudresden.inf.lat.uel.jar/download) 	 | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.2.0/zip/uel-1.2.0.zip/download) |	2012-04-30  | 6    | 3.2.4   | 4.1           |
| [uel-1.1.0](http://sourceforge.net/projects/uel/files/uel/1.1.0/plugin/de.tudresden.inf.lat.uel.jar/download) 	 | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.1.0/zip/uel-1.1.0.zip/download) |	2012-03-09  | 6    | 3.2.4   | 4.1           |
| [uel-1.0.0](http://sourceforge.net/projects/uel/files/uel/1.0.0/plugin/de.tudresden.inf.lat.uel.jar/download) 	 | [(zip)](http://sourceforge.net/projects/uel/files/uel/1.0.0/zip/uel-1.0.0.zip/download) |	2012-01-27  | 6    | 3.2.4   | 4.1           |



## Tutorial

[example: UEL as application](http://julianmendez.github.io/uel/example.html)

[example: UEL as Java library](http://julianmendez.github.io/uel/example-code.html)



## Installation and use

* as a **plugin**:
download [de.tudresden.inf.lat.uel.jar](http://sourceforge.net/projects/uel/files/uel/1.3.0/plugin/de.tudresden.inf.lat.uel.jar/download) and copy it into `Protege_4.3/plugins`.

* as a **library**:
download the [zip](http://sourceforge.net/projects/uel/files/uel/1.3.0/zip/uel-1.3.0.zip/download) containing `uel-1.3.0.jar` in directory uel/uel-library and use all of them as libraries.

* as a **standalone**:

To start the standalone application, you can use the following [script](http://julianmendez.github.io/uel/extra/start-uel.sh.txt).



## source code
clone the source code using Git
```
git clone https://github.com/julianmendez/uel.git
```

[browse repository](https://github.com/julianmendez/uel)
In [Eclipse](http://www.eclipse.org/), it requires the [m2e](http://www.eclipse.org/m2e-wtp/) plug-in.


## Build instructions
* **master** (trunk)
  to compile with Apache Maven (3.0.3+):
  ```
  mvn clean package
  ```
  to compile with Apache Ant (1.8.3+):
  ```
  ant
  ```
  release: `uel-build/target/uel-version.zip`

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


## Changes
* **v1.3.0**
 * added capability to handle dissubsumptions to SAT-based algorithm
 * added ASP-based algorithm (experimental)

* **v1.2.0**
 * added possibility to restrict SAT-based algorithm to minimal assignments

* **v1.1.0**
 * addded Rule-based algorithm

* **v1.0.0**
 * basic SAT-based algorithm



## [Support](http://lat.inf.tu-dresden.de/~mendez)


## Tutorial - UEL as Java library

This small tutorial gives an example of how to use UEL as a Java library. The class `AlternativeUelStarter` in the **uel-plugin** module provides a basic [OWL API](http://owlcs.github.io/owlapi/) interface to UEL. Its use is illustrated by the code of `AlternativeUelStarterTest` and is summarized by the following steps:

* Construct an `AlternativeUelStarter` with the background ontology (an `OWLOntology`) as argument. As of February 2015, only acyclic EL terminologies are supported.

* Call the method `modifyOntologyAndSolve` with the following arguments:

 * The subsumptions and equations of the unification problem that is to be solved (either as an `OWLOntology`, or a `Set<OWLSubClassOfAxiom>` and a `Set<OWLEquivalentClassesAxiom>`). Axioms of types other than `OWLSubClassOfAxiom` or `OWLEquivalentClassesAxiom` in the input ontology are ignored. Furthermore, all `OWLEquivalentClassesAxioms` should contain exactly two `OWLClassExpressions`.

 * The subsumptions and equations that are to be made false by the unifiers ("dissubsumptions" and "disequations"). The input format is the same as above. Dissubsumptions and disequations are currently only supported by the SAT processor (see #4 below).
 * A `Set<OWLClass>` containing all class names that are to be treated as variables for the unification.

 * A `String` designating the unification algorithm ("processor") to be used, as defined in `UelProcessorFactory`. There is an inefficient RULE_BASED_ALGORITHM, a more mature SAT_BASED_ALGORITHM (using the [Sat4j library](http://www.sat4j.org/)) with the option to only return "subset-minimal" solutions[1], and an ASP_BASED_ALGORITHM (using the ASP solver [Clingo](http://potassco.sourceforge.net/)) that as of February 2015 is still under development. Normal unification problems can already be solved by the ASP encoding, but dissubsumptions are not yet supported. If you want to try the ASP algorithm, we can send you more detailed information on how to install Clingo and set up UEL to use it.

* You get back an iterator that gives you unifiers in the form of `Set<OWLUelClassDefinition>` specifying a substitution for every variable. Each `OWLUelClassDefinition` can be converted into an `OWLEquivalentClassesAxiom`. It should be the case that the background ontology, extended by the `OWLEquivalentClassesAxioms` given by one unifier, entails all input subsumptions and does not entail any of the dissubsumptions.

`AlternativeUelStarter` also provides a simple command-line interface that can be accessed by starting Java directly on this class. The execution options are not documented yet, but can be found in the in the source code of the main method.

## References

[1] Franz Baader, Stefan Borgwardt, Julian Alfredo Mendez, and Barbara Morawska. UEL: Unification solver for EL. In Proc. DL 2012. [(PDF)](http://ceur-ws.org/Vol-846/paper_8.pdf)


