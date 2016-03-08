
%
% Unification in EL
%
% ASP encoding based on the SAT encoding in 
% F. Baader and B. Morawska: SAT Encoding of Unification in EL

atom(A) :- hasatom(A,_,_).
atom(A) :- hasatom(exists(_,A),_,_).
nonvaratom(X) :- atom(X), X!=var(V):atom(var(V)).


triviallyfalse(cname(X),cname(Y)) :- atom(cname(X)), atom(cname(Y)), X!=Y.

triviallyfalse(exists(R1,C1),exists(R2,C2)) :- atom(exists(R1,C1)),
                                               atom(exists(R2,C2)), R1!=R2. 

triviallyfalse(cname(C),exists(R,C1)) :- atom(cname(C)), atom(exists(R,C1)).
triviallyfalse(exists(R,C1),cname(C)) :- atom(cname(C)), atom(exists(R,C1)).

subs(X,Y) :- subs(exists(R,X),exists(R,Y)).

subs(X,Z) :- subs(X,Y), subs(Y,Z).

:- subs(X,Y), triviallyfalse(X,Y).


1 { subs(Y,X):hasatom(Y,1-P,E),not triviallyfalse(Y,X) } :- hasatom(X,P,E), nonvaratom(X), eq(E).

{ subs(X,C) } :- nonvaratom(C), hasatom(X,P,E), eq(E), X=var(V),
                 not hasatom(C,PX,E):hasatom(C,PX,E).

1 { subs(Y,C):hasatom(Y,1-P,E),not triviallyfalse(Y,C) } :- subs(X,C), hasatom(X,P,E), eq(E),
                  X=var(V), nonvaratom(C), not hasatom(C,PX,E):hasatom(C,PX,E).


greater(X,Y) :- subs(var(X),exists(R,var(Y))).

:- greater(X,X).

greater(X,Z) :- greater(X,Y), greater(Y,Z).
