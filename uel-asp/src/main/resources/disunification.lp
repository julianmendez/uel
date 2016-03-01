
%
% Local Disunification
%

% definitions
atom(A) :- diseqhasatom(A,_,_).
atom(A) :- diseqhasatom(exists(_,A),_,_).
existsvaratom(X) :- atom(X), X=exists(R,var(V)).


{ lefttoright(E) } :- diseqhasatom(_,_,E).

1 { chosen(Y,E):diseqhasatom(Y,1,E) } :- diseqhasatom(_,_,E), lefttoright(E).
1 { chosen(Y,E):diseqhasatom(Y,0,E) } :- diseqhasatom(_,_,E), not lefttoright(E).

dissubs(X,Y) :- diseqhasatom(X,0,E), diseqhasatom(Y,1,E), chosen(Y,E), lefttoright(E).
dissubs(X,Y) :- diseqhasatom(X,1,E), diseqhasatom(Y,0,E), chosen(Y,E), not lefttoright(E).

subs(cname(X),cname(X)) :- atom(cname(X)).

subs(exists(R,X),exists(R,Y)) :- subs(X,Y), atom(exists(R,X)), atom(exists(R,Y)).


ok1(C,D)        :- dissubs(C,D), subs(D,D'), nonvaratom(D'), not existsvaratom(D'), 
                   not subs(C,D'), not subs(C',D'):subs(C,C').
{ ok2(C,D,D') } :- dissubs(C,D), not ok1(C,D), subs(D,D'), nonvaratom(D'), 
                   existsvaratom(D'), not subs(C,D').
dissubs(var(X),var(Y)):- ok2(C,D,D'), subs(C,C'), D'=exists(R,var(Y)), C'=exists(R,var(X)).
:- ok2(C,D,D'), subs(C,C'), D'=exists(R,var(Y)), C'=exists(R,cname(Z)), 
   subs(cname(Z),Y'):subs(var(Y),Y'),nonvaratom(Y').

ok(C,D) :- ok1(C,D).
ok(C,D) :- ok2(C,D,D').
:- dissubs(C,D), not ok(C,D).
