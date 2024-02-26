PROGRAM HelloWorld;

TYPE
    week = (sun, mon, tue, wed, thu, fri, sat, sun);
    arr = ARRAY[1.4..mon] OF INTEGER;
    arr2 = ARRAY['z'..'a'] OF INTEGER;

VAR
    i, j, k, i : integer;
    w : week;
    a : arr;
    
    
BEGIN
    i := 1;
    j := 2;
    k := 3;
    w := mon;
    a['c'] := 1;
    writeln(i, j, k, w);
END.