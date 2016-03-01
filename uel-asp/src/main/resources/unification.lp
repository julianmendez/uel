
%
% Unification in EL
%
% ASP encoding based on the SAT encoding in 
% F. Baader and B. Morawska: SAT Encoding of Unification in EL

atom(A) :- eqhasatom(A,_,_).
atom(A) :- eqhasatom(exists(_,A),_,_).
nonvaratom(X) :- atom(X), X!=var(V):atom(var(V)).


triviallyfalse(cname(X),cname(Y)) :- atom(cname(X)), atom(cname(Y)), X!=Y.

triviallyfalse(exists(R1,C1),exists(R2,C2)) :- atom(exists(R1,C1)),
                                               atom(exists(R2,C2)), R1!=R2. 

triviallyfalse(cname(C),exists(R,C1)) :- atom(cname(C)), atom(exists(R,C1)).
triviallyfalse(exists(R,C1),cname(C)) :- atom(cname(C)), atom(exists(R,C1)).

subs(X,Y) :- subs(exists(R,X),exists(R,Y)).

subs(X,Z) :- subs(X,Y), subs(Y,Z).

:- subs(X,Y), triviallyfalse(X,Y).


1 { subs(Y,X):eqhasatom(Y,|P-1|,E),not triviallyfalse(Y,X) } :- eqhasatom(X,P,E), nonvaratom(X).

{ subs(X,C) } :- nonvaratom(C), eqhasatom(X,P,E), not eqhasatom(C,PX,E):eqhasatom(C,PX,E), X=var(V),
                 not triviallyfalse(X,C).

{ subs(X,C) } :- atom(X), nonvaratom(C), not triviallyfalse(X,C), X!=Y:eqhasatom(Y,P,E).

1 { subs(Y,C):eqhasatom(Y,|P-1|,E),not triviallyfalse(Y,C) } :- subs(X,C), eqhasatom(X,P,E),
                  X=var(V), nonvaratom(C), not eqhasatom(C,PX,E):eqhasatom(C,PX,E).


greater(X,Y) :- subs(var(X),exists(R,var(Y))).

:- greater(X,X).

greater(X,Z) :- greater(X,Y), greater(Y,Z).
