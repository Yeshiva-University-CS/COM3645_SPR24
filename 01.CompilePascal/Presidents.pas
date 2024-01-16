PROGRAM Presidents;

Uses sysutils;

CONST
    MAX_PRESIDENTS = 20;

TYPE
    { A record type that combines data about a U.S. president. }
    President = RECORD
                    start_year, end_year : integer;
                    first_name, initials, last_name : string
                END;
    
    { The array type of presidents. }
    PrezArray = ARRAY [1..MAX_PRESIDENTS] of President;
    
VAR
    roster      : PrezArray;  { the array of presidents }
    roster_size : integer;

{ Given the current character ch, read blanks up to the next
  nonblank character. Return that nonblank character. }
FUNCTION read_next_nonblank(ch : char) : char;

    BEGIN
        WHILE (ch = ' ') AND (NOT eoln) DO read(ch);
        read_next_nonblank := ch;
    END;

{ Read a name. If the name contained a period (because
  it's a middle initial), return true. Else return false.}
FUNCTION read_name(VAR name : string) : boolean;

    VAR
        ch : char;
        saw_period : boolean;

    BEGIN
        name := '';
        saw_period := false;

        read(ch);
        ch := read_next_nonblank(ch);

        { Read the name character by character. }
        REPEAT
            name := name + ch;
            
            IF ch = '.' THEN saw_period := true;                
            read(ch);
        WHILE eoln OR (ch = ' ');
        
        { If at end of line, append the last character. }
        IF eoln THEN name := name + ch;

        read_name := saw_period;  { Was there a period? }
    END;

{ Read the input data about the president prez.
  The start year st_yr has already been read. }
PROCEDURE read_one_president(st_yr : integer; VAR prez : President);

    VAR
        read_initials : boolean;

    BEGIN
        WITH prez DO BEGIN
            start_year := st_yr;
            read(end_year);

            read_name(first_name);
            read_initials := read_name(last_name);
            
            { The last name was actually the middle initials. }
            IF read_initials THEN BEGIN
                initials := last_name;
                read_name(last_name);
            END
            ELSE BEGIN
                initials := '';
            END;
        END;

        readln;
    END;

{ Read the roster of presidents. Return the roster size. }
FUNCTION read_president_roster : integer;

    VAR 
        i, year : integer;
    
    BEGIN
        i := 0;
        read(year);
        
        { Keep reading until the -1. }
        WHILE year > 0 DO BEGIN
            i := i + 1;
            read_one_president(year, roster[i]);
            read(year);
        END;
        
        readln;
        read_president_roster := i;
    END;
    
{ Sort the roster of presidents by starting year. }
PROCEDURE sort_president_roster;

    VAR
        i, j : integer;
        temp : President;
        
    BEGIN
        FOR i := 1 TO roster_size - 1 DO BEGIN
            FOR j := i + 1 TO roster_size DO BEGIN
                IF roster[i].start_year > roster[j].start_year THEN BEGIN
                    temp      := roster[i];
                    roster[i] := roster[j];
                    roster[j] := temp;
                END;
            END;
        END;
    END;

{ Print the data of a president prez. }
PROCEDURE print_president(VAR prez : President);

    BEGIN
        WITH prez DO BEGIN
            write('     FIRST NAME: '); write(first_name);  writeln;
            
            IF Length(initials) > 0 THEN BEGIN
                write('       INITIALS: '); write(initials);
                writeln;
            END;

            write('      LAST NAME: '); write(last_name); writeln;
            writeln('YEARS IN OFFICE:', start_year:5, end_year:5);
            writeln;
        END;
    END;

{ Print the roster of presidents. }
PROCEDURE print_president_roster;

    VAR
        i : integer;

    BEGIN
        writeln('Roster of Presidents');
        writeln;

        FOR i := 1 TO roster_size DO print_president(roster[i]);
    END;

{ Return the roster index of the president in office during a year.
  Start the search at start_index. Return -1 if no match. }
FUNCTION president_index(year, start_index : integer) : integer;

    VAR
        i : integer;
        
    BEGIN
        i := start_index;
        
        { Keep searching as long as the start year isn't -1 and the
          year isn't between a president's start year and the end year. }
        WHILE (i <= roster_size)
        AND   NOT (    (year >= roster[i].start_year) 
                   AND (year <= roster[i].end_year)) DO i := i + 1;

        { Either we return the index of a match, or -1 if no match. }
        IF roster[i].start_year <= 0 THEN president_index := -1
                                     ELSE president_index := i;
    END;

{ Search the roster of presidents for who was in office
  during each input year. }
PROCEDURE search_president_roster;

    VAR
        year, index : integer;
        
    BEGIN
        read(year);
        
        { Keep reading years until -1. }
        WHILE year > 0 DO BEGIN
            writeln('------------------------------');
            writeln;
            writeln('In office during the year ', year);
            writeln;
            
            { Get the index of a match for the year, or -1.
              The first search of each year starts at index 1. }
            index := president_index(year, 1);
            IF index > 0 THEN BEGIN
                print_president(roster[index]);
                
                { There may be another match. Start a second search
                  from the index returned from the first search. }
                index := president_index(year, index + 1);
                IF index > 0 THEN print_president(roster[index]);
            END
            ELSE BEGIN
                writeln('   No match.');
                writeln;
            END;
            
            read(year);
        END;
    END;

BEGIN { main }
    roster_size := read_president_roster;
    sort_president_roster;
    print_president_roster;
    search_president_roster;
    
    writeln('Done!');
END.
