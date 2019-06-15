public class Main {

    public static void main(String[] args)
    {
        Log.init();

        if(args.length == 1)
        {
            Pipeline pipeline = new Pipeline(args[0]);

            if(pipeline != null)
                pipeline.start();
            else
                Log.message("Pipeline == null.");

            Log.message("Process completed.");
        }
        else {
            Log.message("Pipeline config missed.");
        }

        Log.close();
    }
}
