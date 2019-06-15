import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RLE {

    //public static Map<ConfigReader.PARAMS, String> paramsMap = new HashMap<>();

    public static void  main(String[] args) {

        RLEmodule rle;
        FileOpen fopen;
        ConfigReader configReader;

        Log.init();

        if (args.length == 1) {

            configReader = new ConfigReader(args[0]);
            fopen = new FileOpen(configReader.paramsMap.get(ConfigReader.PARAMS.INPUT_FILE), configReader.paramsMap.get(ConfigReader.PARAMS.OUTPUT_FILE));
            rle = new RLEmodule(fopen.in, fopen.out, configReader.paramsMap);

            rle.executeRLE();

            Log.message("Processing completed successfully.");
        } else {
            Log.message("Configuration file missing");
        }

        Log.close();
    }

}
