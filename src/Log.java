import java.io.FileWriter;
import java.io.IOException;

public class Log {

    private static FileWriter writer;

    public static boolean init()
    {
        try {
            writer = new FileWriter("log.txt");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void message(String message)
    {
        try {
            writer.write(message + "\n");
            writer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void close()
    {
        try {
            writer.close();
        }
        catch (Exception e)
        {
            System.out.print("Log.close error\n");
            Log.message("Log.close error");
        }

    }
}
