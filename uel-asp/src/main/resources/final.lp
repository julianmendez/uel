
relsubs(var(X),C) :- subs(var(X),C), relevant(X), nonvaratom(C).

%#minimize{ relsubs(X,Y) }.

%#hide.
%#show subs/2.
#show relsubs/2.
%#show nonvaratom/1.
%#show dissubs/2.
%#show eqhasatom/3.
%#show diseqhasatom/3.
%#show greater/2.
%#show counterexample/3.
