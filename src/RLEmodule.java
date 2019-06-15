import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

public class RLEmodule {

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private Map<ConfigReader.PARAMS, String> paramsMap;

    public RLEmodule(BufferedInputStream in, BufferedOutputStream out, Map<ConfigReader.PARAMS, String> paramsMap){
        this.in = in;
        this.out = out;
        this.paramsMap = paramsMap;
    }

    public void executeRLE()
    {
        int codeMode = Integer.parseInt(paramsMap.get(ConfigReader.PARAMS.PROCESS_MODE));
        if (codeMode == 0)
            codeRLE();
        else
            decodeRLE();
    }

    public void codeRLE()
    {
        int c, num = 1, symbol, maxSymbol = 255;

        try{
            c = in.read();

            while (c != -1)
            {
                symbol = c;

                while (c != -1 ) {
                    c = in.read();

                    if(c == symbol && num < maxSymbol)
                        num++;
                    else
                        break;
                }

                try {
                    out.write(num);
                    out.write(symbol);
                    out.flush();
                }catch (IOException e) {
                    Log.message("Error writing");
                    return;
                }

                num = 1;
            }
        }
        catch (IOException e)
        {
            Log.message("Error buffered reading\n");
            return;
        }
    }

    public void decodeRLE()
    {
        int num, c;

        while(true) {
            try {
                num = in.read();

                if (num == -1)
                    break;

                c = in.read();
            }catch (IOException e)
            {
                Log.message("Error reading");
                return;
            }
            for (int i = 0; i < num; i++) {
                try {
                    out.write(c);
                    out.flush();
                } catch (IOException e) {
                    Log.message("Error writing to output file");
                    return;
                }
            }
        }

    }
}
