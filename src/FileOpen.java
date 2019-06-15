import java.io.*;

public class FileOpen {

    private FileInputStream fin;
    public BufferedInputStream in;
    private FileOutputStream fout;
    public BufferedOutputStream out;

    public FileOpen(String fileIn, String fileOut){

        if(fileIn != null && fileOut != null) {
            try {
                fin = new FileInputStream(fileIn);
            }
            catch (FileNotFoundException e)
            {
                Log.message("Error: input file not found");
                return;
            }
            in = new BufferedInputStream(fin);

            try {
                fout = new FileOutputStream(fileOut);
            }
            catch (FileNotFoundException e)
            {
                Log.message("Error: output file not found");
                return;
            }
            out = new BufferedOutputStream(fout);

            return;
        }
        else {
            Log.message("Error: file name missing\n");
            return;
        }
    }
}
