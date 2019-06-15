import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pipeline {

    private DataInputStream in;
    private DataOutputStream out;
    private int numWorkers;
    private ArrayList<Executor> executors;
    private final String nullStr = "";
    private final int iFirstWorker = 0;
    private final int numLexem = 2;
    private final int numLexemSeqCfg = 3;

    public enum PARAM_ID {INPUT_FILE, OUTPUT_FILE, CHUNK_SIZE, WORKERS_SEQUENCE}
    public static Map<String, PARAM_ID> paramIdMap;
    public Map<PARAM_ID, String> paramsMap = new HashMap<>();

    public Map<Integer, String> workerCfg = new HashMap<>();
    public Map<Integer, String> workerClassName = new HashMap<>();

    static {
        paramIdMap = new HashMap<>();
        paramIdMap.put("IN_FILE", PARAM_ID.INPUT_FILE);
        paramIdMap.put("OUT_FILE", PARAM_ID.OUTPUT_FILE);
        paramIdMap.put("WORKERS_SEQUENCE", PARAM_ID.WORKERS_SEQUENCE);
    }

    public Pipeline(String config){
        cfgInterp(config);
        cfgSeqInterp(paramsMap.get(PARAM_ID.WORKERS_SEQUENCE));

        String cfgFile;
        numWorkers = 0;
        executors = new ArrayList<>();

        for (int num = 1; ;num++) {
            if ((cfgFile = workerCfg.get(num)) != null) {
                try {
                    Executor worker = (Executor) Class.forName(workerClassName.get(num)).getDeclaredConstructor().newInstance();
                    executors.add(worker);
                    executors.get(num - 1).setConfig(cfgFile);
                }catch (ReflectiveOperationException e) {
                    Log.message("ReflectiveOperationException");
                }
            }
            else
                break;
        }

/*
        String className = "Translater";
        try {
            Executor tmp = (Executor) Class.forName(className).getDeclaredConstructor().newInstance();
            executors.add(tmp);
            executors.get(executors.size() - 1).setConfig("first.txt");
        }catch (Exception e)
        {
            Log.message("error with translator");
        }
*/

        numWorkers = executors.size();

        try {
            in = new DataInputStream(new FileInputStream(paramsMap.get(PARAM_ID.INPUT_FILE)));
            out = new DataOutputStream(new FileOutputStream(paramsMap.get(PARAM_ID.OUTPUT_FILE)));
        }catch (IOException e)
        {
            Log.message("Input/Output file not found");
            return;
        }
        //Log.message(Integer.toString(executors.size()));

        connectWorkers();
    }

    public void start()
    {
       try {
           while(executors.get(iFirstWorker).run() == 0);

       }catch (IOException e) {
            Log.message("Pipeline crashed");
       }
    }

    private void connectWorkers()
    {
        executors.get(iFirstWorker).setInput(in);
        if(numWorkers > 1) {
            for (int i = iFirstWorker; i < numWorkers - 1; i++)
                executors.get(i).setConsumer(executors.get(i + 1));
        }
        executors.get(numWorkers - 1).setOutput(out);
    }

    private boolean cfgInterp(String cfg)
    {
        CfgReader cfgReader = new CfgReader(cfg);
        PARAM_ID paramID;
        String[] lexems = {nullStr,nullStr};

        Log.message("PipeConfig:");

        while (true) {
            lexems = cfgReader.nextParams(numLexem);

            if(lexems[0].isEmpty())
                break;

            if ((paramID = paramIdMap.get(lexems[0])) != null)
                paramsMap.put(paramID, lexems[1]);
            else {
                Log.message("Unknown lexem");
                return false;
            }
        }
        return true;
    }

    private boolean cfgSeqInterp(String cfg)
    {
        CfgReader cfgReader = new CfgReader(cfg);
        Integer workerId;
        String[] lexems = {nullStr, nullStr, nullStr};

        Log.message("SeqConfig:");

        while (true) {
            lexems = cfgReader.nextParams(numLexemSeqCfg);
            if(lexems[0].isEmpty())
                break;

            if((workerId = Integer.parseInt(lexems[0])) > 0) {
                workerCfg.put(workerId, lexems[1]);
                workerClassName.put(workerId, lexems[2]);
            }
            else {
                Log.message("Unknown lexem");
                return false;
            }
        }
        return true;
    }
}
