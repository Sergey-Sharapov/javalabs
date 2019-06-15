import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

interface Executor {

    int setInput(DataInputStream input);

    int setOutput(DataOutputStream output);

    int setConsumer(Executor consumer);

    void setConfig(String config);

    int put(Object obj);

    int run()throws IOException;
}