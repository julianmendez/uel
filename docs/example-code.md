
![uel](https://github.com/julianmendez/uel/blob/master/docs/img/banner.png?raw=true)

This small tutorial gives an example of how to use UEL as a Java library.
The class [AlternativeUelStarter](https://github.com/julianmendez/uel/blob/master/uel-core/src/main/java/de/tudresden/inf/lat/uel/core/main/AlternativeUelStarter.java) in the <b>uel-plugin</b> module provides a basic [OWL API](http://owlapi.sourceforge.net/) interface to UEL.
Its use is illustrated by the code of [AlternativeUelStarterTest](https://github.com/julianmendez/uel/blob/master/uel-core/src/test/java/de/tudresden/inf/lat/uel/core/main/AlternativeUelStarterTest.java) and is summarized by the following steps:
* Construct an `AlternativeUelStarter` with the background ontology (an `OWLOntology`) as argument. As of February 2015, only acyclic EL terminologies are supported.
* Call the method `modifyOntologyAndSolve` with the following arguments:
  1. The subsumptions and equations of the unification problem that is to be solved (either as an `OWLOntology`, or a `Set`&lt;`OWLSubClassOfAxiom`&gt; and a `Set`&lt;`OWLEquivalentClassesAxiom`&gt;).
  Axioms of types other than `OWLSubClassOfAxiom` or `OWLEquivalentClassesAxiom` in the input ontology are ignored.
  Furthermore, all `OWLEquivalentClassesAxiom`s should contain exactly two `OWLClassExpression`s.
  2. The subsumptions and equations that are to be made false by the unifiers ("dissubsumptions" and "disequations"). The input format is the same as above. Dissubsumptions and disequations are currently only supported by the SAT processor (see #4 below).
  3. A `Set`&lt;`OWLClass`&gt; containing all class names that are to be treated as variables for the unification.
  4. A `String` designating the unification algorithm ("processor") to be used, as defined in [UnificationAlgorithmFactory](https://github.com/julianmendez/uel/blob/master/uel-core/src/main/java/de/tudresden/inf/lat/uel/core/processor/UnificationAlgorithmFactory.java).
  There is an inefficient *RULE_BASED_ALGORITHM*, a more mature *SAT_BASED_ALGORITHM* (using the [Sat4j library](http://www.sat4j.org/) ) with the option to only return "subset-minimal" solutions<sup>[ [1] ](#cite_note-1)</sup>, and an *ASP_BASED_ALGORITHM* (using the ASP solver [Clingo](http://potassco.sourceforge.net) ) that as of February 2015 is still under development.
  Normal unification problems can already be solved by the ASP encoding, but dissubsumptions are not yet supported.
    If you want to try the ASP algorithm, we can send you more detailed information on how to install Clingo and set up UEL to use it.
* You get back an iterator that gives you unifiers in the form of `Set`&lt;`OWLUelClassDefinition`&gt; specifying a substitution for every variable. Each `OWLUelClassDefinition` can be converted into an `OWLEquivalentClassesAxiom`.
It should be the case that the background ontology, extended by the `OWLEquivalentClassesAxiom`s given by one unifier, entails all input subsumptions and does not entail any of the dissubsumptions.

[AlternativeUelStarter](https://github.com/julianmendez/uel/blob/master/uel-core/src/main/java/de/tudresden/inf/lat/uel/core/main/AlternativeUelStarter.java) also provides a simple command-line interface that can be accessed by starting Java directly on this class.
The execution options are not documented yet, but can be found in the in the source code of the `main` method.


### References

* <a id="cite_note-1"/>Franz Baader, Stefan Borgwardt, Julian Alfredo Mendez, and Barbara Morawska. UEL: Unification solver for EL. In Proc. DL 2012. [(PDF)](http://ceur-ws.org/Vol-846/paper_8.pdf)


