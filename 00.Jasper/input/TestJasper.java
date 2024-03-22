public class TestJasper 
{
    private static float test()
    {
        float alpha  =  0; //* slot #0
        float beta   = 10; //* slot #1
        float gamma  = 20; //* slot #2
        int   thirty = 30; //* slot #3
        int   forty  = 40; //* slot #4
        int   fifty  = 50; //* slot #5

        if (forty == fifty) 
        {
            return alpha + 3/(beta - gamma) + 5;
        }
        else 
        {
            return alpha + thirty/(beta - gamma) + fifty;
        }
    }

    public static void main(String args[]) {
        System.out.println(TestJasper.test());
    }
}