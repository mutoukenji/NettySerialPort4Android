package tech.yaog.netty.serialportandroid;

import java.io.IOException;
import java.io.InputStream;

public class SerialPortInputStream extends InputStream {

    private InputStream source;

    @Override
    public int read() throws IOException {
        int res = -1;
        while (!Thread.interrupted() && (res = source.read()) == -1) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return res;
    }

    @Override
    public int available() throws IOException {
        int available = source.available();
        if (available < 0) {
            return 0;
        }
        return available;
    }

    public SerialPortInputStream() {
    }

    public SerialPortInputStream(InputStream is) {
        source = is;
    }

    public void setSource(InputStream source) {
        this.source = source;
    }

    @Override
    public void close() throws IOException {
        source.close();
        super.close();
    }

    @Override
    public long skip(long n) throws IOException {
        return source.skip(n);
    }

    @Override
    public synchronized void reset() throws IOException {
        source.reset();
    }

    @Override
    public boolean markSupported() {
        return source.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        source.mark(readlimit);
    }
}
