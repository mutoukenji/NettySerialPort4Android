package tech.yaog.netty.serialportandroid;

import java.io.IOException;
import java.io.OutputStream;

public class SerialPortOutputStream extends OutputStream {

    private OutputStream target;

    public SerialPortOutputStream(OutputStream target) {
        this.target = target;
    }

    @Override
    public void write(int b) throws IOException {
        target.write(b);
    }

    @Override
    public void flush() throws IOException {
        target.flush();
        super.flush();
    }

    @Override
    public void close() throws IOException {
        target.close();
        super.close();
    }
}
