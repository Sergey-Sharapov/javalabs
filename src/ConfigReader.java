import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigReader {

    public enum PARAMS {INPUT_FILE, OUTPUT_FILE, PARAM_FILE, PROCESS_MODE, MIN_LENGTH}
    public Map<String, PARAMS> paramNamesMap;

   /* static{

        paramNamesMap = new HashMap<>();
        paramNamesMap.put("IN_FILE", PARAMS.INPUT_FILE);
        paramNamesMap.put("OUT_FILE", PARAMS.OUTPUT_FILE);
        paramNamesMap.put("PARAM", PARAMS.PARAM_FILE);
        paramNamesMap.put("PROCESS_MODE", PARAMS.PROCESS_MODE);
        paramNamesMap.put("MIN_LENGTH", PARAMS.MIN_LENGTH);
    }*/

    public Map<ConfigReader.PARAMS, String> paramsMap = new HashMap<>();

    public ConfigReader(String config) {

        paramNamesMap = new HashMap<>();
        paramNamesMap.put("IN_FILE", PARAMS.INPUT_FILE);
        paramNamesMap.put("OUT_FILE", PARAMS.OUTPUT_FILE);
        paramNamesMap.put("PARAM", PARAMS.PARAM_FILE);
        paramNamesMap.put("PROCESS_MODE", PARAMS.PROCESS_MODE);
        paramNamesMap.put("MIN_LENGTH", PARAMS.MIN_LENGTH);

        Read(config);
    }

    private void Read(String config)
    {
        File file;
        FileReader confReader;
        BufferedReader confBufReader;
        String buf = "";
        String[] lexems;
        PARAMS paramID;

        try {
            file = new File(config);
            confReader = new FileReader(file);
            confBufReader = new BufferedReader(confReader);
        }
        catch (IOException e)
        {
            Log.message("Can't open file: " + config);
            return;
        }
        try {
            buf = confBufReader.readLine();
        }
        catch (IOException e) {
            Log.message("Error buffered reading while confreading");
            return;
        }
        while(buf != null)
        {
            buf = buf.trim();
            if(buf.isEmpty())
                continue;

            lexems = buf.split(":", 2);

            lexems[0] = lexems[0].trim();
            lexems[1] = lexems[1].trim();

            if((paramID = paramNamesMap.get(lexems[0])) != null)
                paramsMap.put(paramID, lexems[1]);
            else {
                Log.message("Unknown lexem");
                return;
            }

            try {
                buf = confBufReader.readLine();
            }
            catch (IOException e) {
                Log.message("Error buffered reading");
                return;
            }
        }

        if(!config.equals(paramsMap.get(PARAMS.PARAM_FILE))) {
            Read(paramsMap.get(PARAMS.PARAM_FILE));
            return;
        }

        return;
    }

}
