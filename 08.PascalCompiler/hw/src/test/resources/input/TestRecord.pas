program TestRecord;

type
    MyRecord = record
        i : integer;
        r : real;
        b : boolean;
        s : string;
        c : char;
    end;

var
    data : myrecord;

begin
    data.i := 1;
    data.r := 2.0;
    data.b := TRUE;
    data.s := 'TestString';
    data.c := 'T';

    writeln('i = ', data.i, ', r = ', data.r:5:2, ', b = ', data.b, ', s = ', data.s, ', c = ', data.c);

end.
