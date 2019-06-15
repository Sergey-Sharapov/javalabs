import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Pipeline {

    private DataInputStream in;
    private DataOutputStream out;
    private int numWorkers;
    private final String nullStr = "";
    private final int iFirstWorker = 0;
    private final int numLexem = 2;
    private final int numRelLexem = 2;
    private final int numLexemSeqCfg = 3;

    private ArrayList<Executor> executors = new ArrayList<>();

    public enum PARAM_ID {INPUT_FILE, OUTPUT_FILE, CHUNK_SIZE, WORKERS_SEQUENCE, RELATIONS}
    public static Map<String, PARAM_ID> paramIdMap;
    public Map<PARAM_ID, String> paramsMap = new HashMap<>();

    public Map<Integer, String> workerCfg = new HashMap<>();
    public Map<Integer, String> workerClassName = new HashMap<>();
    public ArrayList<Pair<Integer, Integer>> relations = new ArrayList<>();


    static {
        paramIdMap = new HashMap<>();
        paramIdMap.put("IN_FILE", PARAM_ID.INPUT_FILE);
        paramIdMap.put("OUT_FILE", PARAM_ID.OUTPUT_FILE);
        paramIdMap.put("WORKERS_SEQUENCE", PARAM_ID.WORKERS_SEQUENCE);
        paramIdMap.put("RELATIONS", PARAM_ID.RELATIONS);
    }

    public Pipeline(String config){
        cfgInterp(config);
        cfgSeqInterp(paramsMap.get(PARAM_ID.WORKERS_SEQUENCE));
        cfgRelInterp(paramsMap.get(PARAM_ID.RELATIONS));

        String cfgFile;
        numWorkers = 0;

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

        if(connectWorkers() != 0)
            Log.message("Error connection workers");
    }

    public void start()
    {
        if(executors.get(iFirstWorker).run() != 0)
            Log.message("Pipeline crashed");

            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.message("Error in.close");
                }
            }
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.message("Error out.close");
                }
            }
    }

    private int connectWorkers()
    {
        Pair<Integer, Integer> rel;

        executors.get(iFirstWorker).setInput(in);
        executors.get(getEndConsumer() - 1).setOutput(out);

        for(int i = 0; i < relations.size(); i++) {
            rel = relations.get(i);
            if(executors.get(rel.getKey() - 1).setConsumer(executors.get(rel.getValue() - 1)) != 0)
                return -1;
        }
        return 0;
    }

    private int getEndConsumer()
    {
        int f = 1;

        for(int i = 0; i < relations.size(); i++) {
            for(int j = 0; j < relations.size(); j++) {
                if(relations.get(i).getValue() == relations.get(j).getKey())
                    f = 0;
            }
            if(f == 1)
                return relations.get(i).getValue();
            f = 1;
        }
        return -1;
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

    private boolean cfgRelInterp(String cfg)
    {
        CfgReader cfgReader = new CfgReader(cfg);
        int i;
        String[] lexems = {nullStr, nullStr};
        Pair<Integer, Integer> tmp;

        Log.message("RelConfig:");

        while (true) {
            lexems = cfgReader.nextParams(numRelLexem);
            if(lexems[0].isEmpty())
                break;

            if((i = Integer.parseInt(lexems[0])) >= 0) {
                tmp = new Pair<>(Integer.parseInt(lexems[0]), Integer.parseInt(lexems[1]));
                relations.add(tmp);
            }
            else {
                Log.message("Unknown lexem");
                return false;
            }
        }
        return true;
    }
}
