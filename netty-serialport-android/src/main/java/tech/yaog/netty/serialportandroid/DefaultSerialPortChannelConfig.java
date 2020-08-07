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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;

import java.util.Map;

import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.BAUD_RATE;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.DATA_BITS;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.DTR;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.PARITY_BIT;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.READ_TIMEOUT;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.RTS;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.STOP_BITS;
import static tech.yaog.netty.serialportandroid.SerialPortChannelOption.WAIT_TIME;

/**
 * Default configuration class for RXTX device connections.
 *
 */
final class DefaultSerialPortChannelConfig extends DefaultChannelConfig implements SerialPortChannelConfig {

    private volatile int baudrate = 115200;
    private volatile boolean dtr;
    private volatile boolean rts;
    private volatile Stopbits stopbits = Stopbits.STOPBITS_1;
    private volatile Databits databits = Databits.DATABITS_8;
    private volatile Paritybit paritybit = Paritybit.NONE;
    private volatile int waitTime;
    private volatile int readTimeout = 1000;

    DefaultSerialPortChannelConfig(SerialPortChannel channel) {
        super(channel);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), BAUD_RATE, DTR, RTS, STOP_BITS, DATA_BITS, PARITY_BIT, WAIT_TIME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == BAUD_RATE) {
            return (T) Integer.valueOf(getBaudrate());
        }
        if (option == DTR) {
            return (T) Boolean.valueOf(isDtr());
        }
        if (option == RTS) {
            return (T) Boolean.valueOf(isRts());
        }
        if (option == STOP_BITS) {
            return (T) getStopbits();
        }
        if (option == DATA_BITS) {
            return (T) getDatabits();
        }
        if (option == PARITY_BIT) {
            return (T) getParitybit();
        }
        if (option == WAIT_TIME) {
            return (T) Integer.valueOf(getWaitTimeMillis());
        }
        if (option == READ_TIMEOUT) {
            return (T) Integer.valueOf(getReadTimeout());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == BAUD_RATE) {
            setBaudrate((Integer) value);
        } else if (option == DTR) {
            setDtr((Boolean) value);
        } else if (option == RTS) {
            setRts((Boolean) value);
        } else if (option == STOP_BITS) {
            setStopbits((Stopbits) value);
        } else if (option == DATA_BITS) {
            setDatabits((Databits) value);
        } else if (option == PARITY_BIT) {
            setParitybit((Paritybit) value);
        } else if (option == WAIT_TIME) {
            setWaitTimeMillis((Integer) value);
        } else if (option == READ_TIMEOUT) {
            setReadTimeout((Integer) value);
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    @Override
    public SerialPortChannelConfig setBaudrate(final int baudrate) {
        this.baudrate = baudrate;
        return this;
    }

    @Override
    public SerialPortChannelConfig setStopbits(final Stopbits stopbits) {
        this.stopbits = stopbits;
        return this;
    }

    @Override
    public SerialPortChannelConfig setDatabits(final Databits databits) {
        this.databits = databits;
        return this;
    }

    @Override
    public SerialPortChannelConfig setParitybit(final Paritybit paritybit) {
        this.paritybit = paritybit;
        return  this;
    }

    @Override
    public int getBaudrate() {
        return baudrate;
    }

    @Override
    public Stopbits getStopbits() {
        return stopbits;
    }

    @Override
    public Databits getDatabits() {
        return databits;
    }

    @Override
    public Paritybit getParitybit() {
        return paritybit;
    }

    @Override
    public boolean isDtr() {
        return dtr;
    }

    @Override
    public SerialPortChannelConfig setDtr(final boolean dtr) {
        this.dtr = dtr;
        return this;
    }

    @Override
    public boolean isRts() {
        return rts;
    }

    @Override
    public SerialPortChannelConfig setRts(final boolean rts) {
        this.rts = rts;
        return this;
    }

    @Override
    public int getWaitTimeMillis() {
        return waitTime;
    }

    @Override
    public SerialPortChannelConfig setWaitTimeMillis(final int waitTimeMillis) {
        if (waitTimeMillis < 0) {
            throw new IllegalArgumentException("Wait time must be >= 0");
        }
        waitTime = waitTimeMillis;
        return this;
    }

    @Override
    public SerialPortChannelConfig setReadTimeout(int readTimeout) {
        if (readTimeout < 0) {
            throw new IllegalArgumentException("readTime must be >= 0");
        }
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public SerialPortChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public SerialPortChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public SerialPortChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public SerialPortChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public SerialPortChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public SerialPortChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public SerialPortChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public SerialPortChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public SerialPortChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public SerialPortChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
        return this;
    }

    @Override
    public SerialPortChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
