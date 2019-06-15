import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CfgReader {

    private File file;
    private FileReader confReader;
    private BufferedReader confBufReader;
    private final String regex = ":";
    private final String nullStr = "";

    public CfgReader(String fname)
    {
        try {
            file = new File(fname);
            confReader = new FileReader(file);
            confBufReader = new BufferedReader(confReader);
        }
        catch (IOException e)
        {
            Log.message("Can't open file: " + fname);
            return;
        }
    }

    public String[] nextParams(int num)
    {
        String[] lexems = new String[num];
        String buf;

        for (int i = 0; i < num; i++)
            lexems[i] = nullStr;

        try {
            buf = confBufReader.readLine();

            //Writing to Log
            if(buf != null)
                Log.message(buf);
            else
                Log.message("\n");

            if(buf != null) {
                buf = buf.trim();
                if (buf.isEmpty())
                    return lexems;

                lexems = buf.split(regex, num);

                for(int i = 0; i < lexems.length; i++)
                    lexems[i] = lexems[i].trim();
            }
        }
        catch (IOException e) {
            Log.message("Error buffered reading");
        }
        return lexems;
    }
}
