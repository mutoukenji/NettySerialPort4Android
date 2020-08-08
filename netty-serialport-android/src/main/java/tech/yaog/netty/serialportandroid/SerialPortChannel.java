/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package tech.yaog.netty.serialportandroid;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.OioByteStreamChannel;
import tech.yaog.hardwares.serialport.SerialPort;

import java.io.File;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.BAUD_RATE;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.DATA_BITS;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.PARITY_BIT;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.RTS;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.STOP_BITS;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.WAIT_TIME;

/**
 * A channel to a serial device using the RXTX library.
 *
 */
public class SerialPortChannel extends OioByteStreamChannel {

    private static final SerialPortDeviceAddress LOCAL_ADDRESS = new SerialPortDeviceAddress("localhost");

    private final SerialPortChannelConfig config;

    private boolean open = true;
    private SerialPortDeviceAddress deviceAddress;
    private SerialPort serialPort;

    public SerialPortChannel() {
        super(null);

        config = new DefaultSerialPortChannelConfig(this);
    }

    @Override
    public SerialPortChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new RxtxUnsafe();
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        deviceAddress = (SerialPortDeviceAddress) remoteAddress;
    }

    protected void doInit() throws Exception {
        File device = new File(deviceAddress.value());
        serialPort = new SerialPort(device,
                config().getOption(BAUD_RATE),
                config().getOption(DATA_BITS).value(),
                config().getOption(PARITY_BIT).value(),
                config().getOption(STOP_BITS).value(),
                config().getOption(RTS),
                false,
                0
                );

        activate(new SerialPortInputStream(serialPort.getInputStream()), new SerialPortOutputStream(serialPort.getOutputStream()));
    }

    @Override
    public SerialPortDeviceAddress localAddress() {
        return (SerialPortDeviceAddress) super.localAddress();
    }

    @Override
    public SerialPortDeviceAddress remoteAddress() {
        return (SerialPortDeviceAddress) super.remoteAddress();
    }

    @Override
    protected SerialPortDeviceAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SerialPortDeviceAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
           super.doClose();
        } finally {
            if (serialPort != null) {
                serialPort.close();
                serialPort = null;
            }
        }
    }

    @Override
    protected boolean isInputShutdown() {
        return !open;
    }

    @Override
    protected ChannelFuture shutdownInput() {
        return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
    }

    private final class RxtxUnsafe extends AbstractUnsafe {
        @Override
        public void connect(
                final SocketAddress remoteAddress,
                final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            try {
                final boolean wasActive = isActive();
                doConnect(remoteAddress, localAddress);

                int waitTime = config().getOption(WAIT_TIME);
                if (waitTime > 0) {
                    eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doInit();
                                safeSetSuccess(promise);
                                if (!wasActive && isActive()) {
                                    pipeline().fireChannelActive();
                                }
                            } catch (Throwable t) {
                                safeSetFailure(promise, t);
                                closeIfClosed();
                            }
                        }
                   }, waitTime, TimeUnit.MILLISECONDS);
                } else {
                    doInit();
                    safeSetSuccess(promise);
                    if (!wasActive && isActive()) {
                        pipeline().fireChannelActive();
                    }
                }
            } catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
            }
        }
    }
}
