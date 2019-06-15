import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;

public class WorkItem implements Executor {

    public class AdapterByte implements ByteAdapterInterface{
        private int startInd;
        private int end;
        private int curInd;

        public void setMetrics(int start, int end) {
            this.startInd = start;
            this.end = end;
            curInd = start;
        }

        public Byte getNextByte()
        {
            try {
                if(curInd <= end) {
                    Byte next = data[curInd];
                    curInd++;
                    return next;
                }
                else
                    return null;
            } catch(Exception e) {
                Log.message("Error getNextByte");
                e.printStackTrace();
                return 0;
            }
        }
    }

    public class AdapterChar implements CharAdapterInterface{
        private int startInd;
        private int end;
        private int curInd;

        public void setMetrics(int start, int end) {
            this.startInd = start;
            this.end = end;
            curInd = start;
        }

        public Character getNextChar()
        {
            try {
                if(curInd < end) {
                    char next = (char)data[curInd];
                    curInd++;
                    return next;
                }
                else
                    return null;
            } catch(Exception e) {
                Log.message("Error getNextChar");
                return 0;
            }
        }
    }

    public class AdapterDouble implements DoubleAdapterInterface{
        private int startInd;
        private int end;
        private int curInd;

        public void setMetrics(int start, int blockSize) {
            this.startInd = start;
            this.end = end;
            curInd = start;
        }

        public Double getNextDouble()
        {
            try {
                if(curInd < end) {
                    double next = (double)data[curInd];
                    curInd++;
                    return next;
                }
                else
                    return null;
            } catch(Exception e) {
                Log.message("Error getNextDouble");
                return 0.0;
            }
        }
    }

    private DataInputStream in;
    private DataOutputStream out;
    private int chunkSize;
    public int dataSize;
    private byte[] data;
    private final String nullStr = "";
    private final int numLexem = 2;
    private final int firstAppType = 0;

    public Map<Executor, Pair<APPROPRIATE_TYPES, Object>> adapterMap;
    public Map<Executor, Pair<APPROPRIATE_TYPES, Object>> consumers;

    public enum PARAM_ID {PROCESS_MODE, CHUNK_SIZE, TYPE}

    public Map<PARAM_ID, String> paramsMap = new HashMap<>();
    public static Map<String, PARAM_ID> paramIdMap;

    static {
        paramIdMap = new HashMap<>();
        paramIdMap.put("PROCESS_MODE", PARAM_ID.PROCESS_MODE);
        paramIdMap.put("CHUNK_SIZE", PARAM_ID.CHUNK_SIZE);
        paramIdMap.put("TYPE", PARAM_ID.TYPE);
    }

    @Override
    public void setAdapter(Executor provider, Object adapter, Executor.APPROPRIATE_TYPES type) {

        if(adapterMap == null)
            adapterMap = new HashMap<>();

        Pair<APPROPRIATE_TYPES, Object> pair = new Pair<>(type, adapter);

        adapterMap.put(provider, pair);
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
    @Override
    public void setInput(DataInputStream input){
        in = input;
    }

    @Override
    public void setOutput(DataOutputStream output) {
        out = output;
    }

    @Override
    public int setConsumer(Executor consumer) {
        Object adapter;
        Pair<APPROPRIATE_TYPES, Object> tmp = null;
        APPROPRIATE_TYPES[] AppTypes = consumer.getConsumedTypes();

        if(consumers == null)
            consumers = new HashMap<>();

        if(AppTypes != null) {
            switch (AppTypes[firstAppType]) {
                case BYTE:
                    adapter = (Object) new AdapterByte();
                    consumer.setAdapter(this, adapter, APPROPRIATE_TYPES.BYTE);
                    tmp = new Pair<>(APPROPRIATE_TYPES.BYTE, adapter);
                    break;
                case CHAR:
                    adapter = (Object) new AdapterChar();
                    consumer.setAdapter(this, adapter, APPROPRIATE_TYPES.CHAR);
                    tmp= new Pair<>(APPROPRIATE_TYPES.CHAR, adapter);
                    break;
                case DOUBLE:
                    adapter = (Object) new AdapterDouble();
                    consumer.setAdapter(this, adapter, APPROPRIATE_TYPES.DOUBLE);
                    tmp= new Pair<>(APPROPRIATE_TYPES.DOUBLE, adapter);
                    break;
            }
            consumers.put(consumer, tmp);
        }
        else
        {
            Log.message("Didn't find appropriate types");
            return -1;
        }


        return 0;
    }

    @Override
    public int setConfig(String cfg)
    {
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
                return -1;
            }
        }
        chunkSize = Integer.parseInt(paramsMap.get(PARAM_ID.CHUNK_SIZE));
        dataSize = 0;
        adapterMap = new HashMap<>();
        return 0;
    }

    @Override
    public APPROPRIATE_TYPES[] getConsumedTypes() {
        APPROPRIATE_TYPES[] type = new APPROPRIATE_TYPES[1];
        //

        switch(paramsMap.get(PARAM_ID.TYPE)) {
            case "BYTE":
                type[0] = APPROPRIATE_TYPES.BYTE;
                break;
            case "CHAR":
                type[0] = APPROPRIATE_TYPES.CHAR;
                break;
            case "DOUBLE":
                type[0] = APPROPRIATE_TYPES.DOUBLE;
                break;
        }
        return type;
    }

    @Override
    public int put(Executor provider)
    {
         int bufferSize = 2 * Integer.parseInt(paramsMap.get(PARAM_ID.CHUNK_SIZE));
         dataSize = 0;

        switch(adapterMap.get(provider).getKey())
        {
            case BYTE:
                ByteAdapterInterface adapterByte = (ByteAdapterInterface)adapterMap.get(provider).getValue();
                Byte b = null;
                byte [] bufferByte = new byte[bufferSize];

                for(int i = 0; i < bufferSize; i++) {
                    b = adapterByte.getNextByte();
                    if(b == null) {
                        dataSize = i;
                        break;
                    }
                    else {
                        bufferByte[i] = b;
                    }
                }
                data = new byte[dataSize];
                for(int i = 0; i < dataSize; i++)
                    data[i] = (byte)bufferByte[i];

                break;
            case CHAR:
                CharAdapterInterface adapterChar = (CharAdapterInterface)adapterMap.get(provider).getValue();
                Character c = null;
                char [] bufferChar = new char[bufferSize];

                for(int i = 0; i < bufferSize; i++) {
                     c = adapterChar.getNextChar();
                    if(c == null) {
                        dataSize = i + 1;
                        break;
                    }
                    else
                        bufferChar[i] = c;
                }
                data = new byte[dataSize];
                for(int i = 0; i < dataSize; i++)
                    data[i] = (byte)bufferChar[i];
                break;
            case DOUBLE:
                DoubleAdapterInterface adapterDouble = (DoubleAdapterInterface)adapterMap.get(provider).getValue();
                Double d = null;
                double [] bufferDouble = new double[bufferSize];

                for(int i = 0; i < bufferSize; i++) {
                    d = adapterDouble.getNextDouble();
                    if(d == null) {
                        dataSize = i + 1;
                        break;
                    }
                    else
                        bufferDouble[i] = d;
                }
                data = new byte[dataSize];
                for(int i = 0; i < dataSize; i++)
                    data[i] = (byte)bufferDouble[i];
                break;
        }

        return 0;
    }

    private byte[] read()
    {
        byte[] buffer = new byte[chunkSize];
        byte[] result = null, b = new byte[1];
        int isEnd = 0, i;

        for(i = 0; i < chunkSize ;i++)
        {
            try{
                isEnd = in.read(b);
            }catch (IOException e) {
                Log.message("Error reading from DataInputStream.");
                return null;
            }
            if(isEnd != -1)
                buffer[i] = b[0];
            else
                break;

        }

        if(i != 0) {
            result = new byte[i];
            for (int j = 0; j < i; j++)
                result[j] = buffer[j];
        }

        return result;
    }

    private int write()
    {
        if(out != null)//write to file
        {
            if(data == null) {
                try {
                    out.close();
                }catch (Exception e) {
                    Log.message("Error data == null");
                    return -1;
                }
            }else if(data.length == 0) {
                try {
                    out.close();
                } catch (Exception e) {
                    Log.message("Error data.length == 0");
                    return -1;
                }
            }

            for(int i = 0; i < data.length ;i++) {
                try {
                    out.write(data[i]);
                    out.flush();
                }catch (Exception e)
                {
                    Log.message("Error writting to DataOutputStream.");
                    return -1;
                }
            }
        }
        else if(consumers != null)//put to the consumers
        {
            int i = 0, numCons = consumers.size();
            int blockSize = data.length / numCons;
            Pair<Integer, Integer> adaptInf = null;

            for(Executor cons: consumers.keySet())
            {
                //calculation metrics
                if(i + 1 == consumers.size())//last consumer
                    adaptInf = new Pair<>(blockSize * i, data.length - 1);
                else
                    adaptInf = new Pair<>(blockSize * i, blockSize * (i + 1) - 1);

                //Setting start index to consumers adapter
                switch(consumers.get(cons).getKey())
                {
                    case BYTE:
                        ByteAdapterInterface adapterByte = (ByteAdapterInterface) consumers.get(cons).getValue();
                        adapterByte.setMetrics(adaptInf.getKey(), adaptInf.getValue());
                        break;
                    case CHAR:
                        CharAdapterInterface adapterChar = (CharAdapterInterface) consumers.get(cons).getValue();
                        adapterChar.setMetrics(adaptInf.getKey(), adaptInf.getValue());
                        break;
                    case DOUBLE:
                        DoubleAdapterInterface adapterDouble = (DoubleAdapterInterface) consumers.get(cons).getValue();
                        adapterDouble.setMetrics(adaptInf.getKey(), adaptInf.getValue());
                        break;

                }
                cons.put(this);
                if(cons.run() != 0)
                    return -1;
                i++;
            }
        }else
        {
            Log.message("Consumer == null, out == null");
            return -1;
        }

        return  0;
    }

    @Override
    public int run()
    {
        if(in != null)//read from file
        {
            while((data = read()) != null)
            {
                data = executeRLE(data);
                print();

                if(write() != 0)
                    return -1;
            }
        }
        else //read from 'data'
        {
            if(data != null) {
                print();
                data = executeRLE(data);
                //print();
            }
            else {
                Log.message("Error data == null");
                return -1;
            }
        }

        if (in == null && write() != 0)
            return -1;

        return 0;
    }

    private void print()
    {
        //Printing to console
        for (int i = 0; i < data.length; i++) {
            if (Integer.parseInt(paramsMap.get(PARAM_ID.PROCESS_MODE)) == 0) {
                if (i % 2 != 0)
                    System.out.print((char) data[i]);
                else
                    System.out.print(Integer.toString((int) data[i]));
            }
            else
                System.out.print((char) data[i]);
        }
        System.out.print('\n');
    }

    public byte[] executeRLE(byte[] array)
    {
        int codeMode = Integer.parseInt(paramsMap.get(PARAM_ID.PROCESS_MODE));
        if (codeMode == 0)
            return codeRLE(array);
        else
            return decodeRLE(array);
    }

    public byte[] codeRLE(byte[] array)
    {
        byte c,  symbol;
        byte[] result;
        int  num = 1, maxSymbol = 255, i = 0;
        ArrayList<Byte> resBuffer = new ArrayList<>();

        if(array != null) {

                while (i < array.length) {
                    c = array[i++];
                    symbol = c;
                    //num  = 1;

                    while (i < array.length) {
                        c = array[i];

                        if (c == symbol && num < maxSymbol) {
                            num++;
                            i++;
                        }
                        else
                            break;
                    }
                    resBuffer.add((byte) num);
                    resBuffer.add(symbol);
                    num = 1;

                }
        }else
            return null;

        result = new byte[resBuffer.size()];
        for (int j = 0; j < resBuffer.size(); j++)
            result[j] = resBuffer.get(j);

        return result;
    }

    public byte[] decodeRLE(byte[] array)
    {
        int num;
        byte c;
        byte[] result;
        ArrayList<Byte> resBuffer = new ArrayList<>();

        for(int i = 0; i < array.length - (array.length % 2); i++) {
            num = (int)array[i++];
            c = array[i];

            for (int j = 0; j < num; j++)
                resBuffer.add(c);
        }

        if(array.length % 2 != 0)
            Log.message("Error decode array % 2 != 0");

        result = new byte[resBuffer.size()];
        for (int j = 0; j < resBuffer.size(); j++)
            result[j] = resBuffer.get(j);

        return result;
    }
}
