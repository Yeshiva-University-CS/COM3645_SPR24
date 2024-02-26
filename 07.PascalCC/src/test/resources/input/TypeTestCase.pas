PROGRAM TestCase;

VAR
    i, j, even, odd, prime : integer;
    ch, str : char;
    os : string;

BEGIN
    i := 3;  ch := 'b';
    even := -990; odd := -999; prime := 0;
    os := 'windows';

    CASE i+1 OF
        1:       j := i;
        -8.0:      j := 8*i;
        5, 1, 4: j := 574*i;
    END;
    
    writeln('j = ', j);

    CASE ch OF
        'c', 'b' : str := 'p';
        'a'      : str := 'q'
    END;

    writeln('str = ''', str, '''');

    case os of
        'dos', 'windows' : writeln('Windows OS');
        'linux' : writeln('Linux OS');
    end;

    FOR i := -5 TO 15 DO BEGIN
        CASE i OF
            2: prime := i;
            -4, -2, 0, 4, 6, 8, 10, 12: even := i;
            -3, -1, 1, 3, 5, 7, 9, 11:  CASE i OF
                                            -3, -1, 1, 9:   odd := i;
                                            2, 3, 5, 7, 11: prime := i;
                                        END
        END;

        writeln('i = ', i, ', even = ', even, ', odd = ', odd,
                ', prime = ', prime);
    END
END.
