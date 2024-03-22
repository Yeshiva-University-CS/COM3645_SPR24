PROGRAM TestFunction;

FUNCTION cube(x : integer): integer;
    BEGIN
        cube := x * x * x;
    END;

BEGIN
    writeln('3 cubed, cubed = ', cube(cube(3)));
END.