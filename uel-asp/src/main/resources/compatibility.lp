

compatible(var(X)) :- atom(var(X)).

compatible(var(X),var(Y)) :- hasatom(var(X),P,E), hasatom(var(Y),P,E), hasatom(var(Z),1-P,E), not hasatom(W,1-P,E):hasatom(W,1-P,E).

hasatom(var(Z),P,E) ...