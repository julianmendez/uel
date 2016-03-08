
%
% Local Disunification in EL
%
% ASP encoding based on the SAT encoding in
% F. Baader, S. Borgwardt, and B. Morawska: Dismatching and Local Disunification in EL

existsvaratom(X) :- atom(X), X=exists(R,var(V)).


subs(cname(X),cname(X)) :- atom(cname(X)).

subs(exists(R,X),exists(R,Y)) :- subs(X,Y), atom(exists(R,X)), atom(exists(R,Y)).


{ subs(X,C) } :- atom(X), nonvaratom(C), not triviallyfalse(X,C), X!=Y:hasatom(Y,P,E).

{ subs(X,C) } :- atom(X), nonvaratom(C), not triviallyfalse(X,C), hasatom(X,P,E), diseq(E).


1 { dissubs(0,E) ; dissubs(1,E) } :- diseq(E).
dissubs(1,E) :- dissubs(E).
expand(E) :- dissubs(P,E).

expandatom(A,P,E) :- hasatom(X,P,E), expand(E), X=var(V), subs(X,A), nonvaratom(A).
expandatom(X,P,E) :- hasatom(X,P,E), expand(E), nonvaratom(X).

expandatom(A,0,E) :- check(X,Y,E), subs(X,A), nonvaratom(A).
expandatom(A,1,E) :- check(X,Y,E), subs(Y,A), nonvaratom(A).
dissubs(1,E) :- check(X,Y,E).

ok1(P,E) :- dissubs(P,E), expandatom(Y,P,E), not existsvaratom(Y), not expandatom(Y,1-P,E),
            not subs(X,Y):expandatom(X,1-P,E).

{ ok2(P,E,Y) } :- dissubs(P,E), not ok1(P,E), expandatom(Y,P,E), existsvaratom(Y), 
                  not subs(X,Y):expandatom(X,1-P,E).

check(var(X'),var(Y'),id(X',Y')) :- ok2(P,E,Y), Y=exists(R,var(Y')), expandatom(X,1-P,E), X=exists(R,var(X')).

:- ok2(P,E,Y), expandatom(X,1-P,E), Y=exists(R,var(Y')), X=exists(R,cname(Z)),
   subs(cname(Z),V):subs(var(Y'),V), nonvaratom(V). 

ok(P,E) :- ok1(P,E).
ok(P,E) :- ok2(P,E,Y).
:- dissubs(P,E), not ok(P,E).




%% old encoding (for dissubs over variables)
%ok1(C,D)        :- dissubs(C,D), subs(D,D'), nonvaratom(D'), not existsvaratom(D'), 
%                   not subs(C,D'), not subs(C',D'):subs(C,C').
%{ ok2(C,D,D') } :- dissubs(C,D), not ok1(C,D), subs(D,D'), nonvaratom(D'), 
%                   existsvaratom(D'), not subs(C,D').
%dissubs(var(X),var(Y)):- ok2(C,D,D'), subs(C,C'), D'=exists(R,var(Y)), C'=exists(R,var(X)).
%:- ok2(C,D,D'), subs(C,C'), D'=exists(R,var(Y)), C'=exists(R,cname(Z)), 
%   subs(cname(Z),Y'):subs(var(Y),Y'),nonvaratom(Y').
%
%ok(C,D) :- ok1(C,D).
%ok(C,D) :- ok2(C,D,D').
%:- dissubs(C,D), not ok(C,D).
