import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkItem implements Executor {

    private DataInputStream in;
    private DataOutputStream out;
    private int chunkSize;
    private byte[] data;
    private final String nullStr = "";
    private final int numLexem = 2;

    public Executor consumer;

    public enum PARAM_ID {PROCESS_MODE, CHUNK_SIZE}
    public Map<PARAM_ID, String> paramsMap = new HashMap<>();
    public static Map<String, PARAM_ID> paramIdMap;

    static {
        paramIdMap = new HashMap<>();
        paramIdMap.put("PROCESS_MODE", PARAM_ID.PROCESS_MODE);
        paramIdMap.put("CHUNK_SIZE", PARAM_ID.CHUNK_SIZE);
    }

    public WorkItem() {
    }

   /* private void cfgInterp(String fname)
    {
        CfgReader cfgReader = new CfgReader(fname);
        PARAM_ID paramID;
        String[] lexems = twoNullStr;

        while (true) {
            lexems = cfgReader.nextParam();
            if(lexems[0].isEmpty())
                break;

            if ((paramID = paramIdMap.get(lexems[0])) != null)
                paramsMap.put(paramID, lexems[1]);
            else {
                Log.message("Unknown lexem");
                return;
            }
        }
    }
*/
    public int setInput(DataInputStream input){
        in = input;
        return 0;
    }

    public int setOutput(DataOutputStream output) {
        out = output;
        return 0;
    }

    public int setConsumer(Executor consumer) {
        this.consumer = consumer;
        return 0;
    }

    public void setConfig(String cfg)
    {
        //cfgInterp(cfg);

        CfgReader cfgReader = new CfgReader(cfg);
        PARAM_ID paramID;
        String[] lexems = {nullStr, nullStr};

        while (true) {
            lexems = cfgReader.nextParams(numLexem);
            if(lexems[0].isEmpty())
                break;

            if ((paramID = paramIdMap.get(lexems[0])) != null)
                paramsMap.put(paramID, lexems[1]);
            else {
                Log.message("Unknown lexem");
                //return 1;
            }
        }
        chunkSize = Integer.parseInt(paramsMap.get(PARAM_ID.CHUNK_SIZE));
        //return 0;
    }

    public int put(Object obj)
    {
        if(obj != null) {
            ArrayList<Byte> res = (ArrayList<Byte>) obj;
            data = new byte[res.size()];

            for (int i = 0; i < res.size(); i++)
                data[i] = res.get(i);
        }
        else
            data = null;

        return 0;
    }

    public int run()throws IOException
    {
        int numByte;
        byte[] buf = new byte[chunkSize];
        ArrayList<Byte> result = null;

        if(in != null)
        {
            try{
                numByte = in.read(buf,0, chunkSize);
                if(numByte > 0)
                    result = executeRLE(buf);

            }catch (IOException e)
            {
                Log.message("Error reading from DataInputStream.");
                return -1;
            }
        }
        else //read from 'data'
        {
            if(data != null)
                result = executeRLE(data);
        }

        if(out != null)
        {
            try {
                if(result == null || result.size() == 0) {
                    out.close();
                    return 1;
                }
                for(int i = 0; i < result.size() ;i++) {
                    out.write(result.get(i));
                    out.flush();
                }
            }catch (IOException e)
            {
                Log.message("Error writting to DataOutputStream.");
                return -1;
            }
        }
        else if(consumer != null)
        {
            consumer.put(result);
           return consumer.run();
        }

        return 0;
    }

    public ArrayList<Byte> executeRLE(byte[] array)
    {
        int codeMode = Integer.parseInt(paramsMap.get(PARAM_ID.PROCESS_MODE));
        if (codeMode == 0)
            return codeRLE(array);
        else
            return decodeRLE(array);
    }

    public ArrayList<Byte> codeRLE(byte[] array)
    {
        byte c,  symbol;
        int  num = 1, maxSymbol = 255, i = 0;
        ArrayList<Byte> result = new ArrayList<>();

        if(array != null) {

                while (i < array.length) {
                    c = array[i++];
                    symbol = c;

                    while (i < array.length) {
                        c = array[i];

                        if (c == symbol && num < maxSymbol) {
                            num++;
                            i++;
                        }
                        else
                            break;
                    }
                    result.add((byte) num);
                    result.add(symbol);
                    num = 1;

                }
        }else
            return null;

        return result;
    }

    public ArrayList<Byte> decodeRLE(byte[] array)
    {
        int num;
        byte c;
        ArrayList<Byte> result = new ArrayList<>();

        for(int i = 0; i < array.length; i++) {
            num = (int)array[i++];

            c = array[i];

            for (int j = 0; j < num; j++)
                result.add(c);
        }
        return result;
    }
}
