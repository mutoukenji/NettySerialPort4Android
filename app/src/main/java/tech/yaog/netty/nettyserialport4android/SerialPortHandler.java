package tech.yaog.netty.nettyserialport4android;

import android.util.Log;

import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import tech.yaog.netty.serialportandroid.SerialPortChannel;
import tech.yaog.netty.serialportandroid.SerialPortChannelConfig;
import tech.yaog.netty.serialportandroid.SerialPortChannelOption;
import tech.yaog.netty.serialportandroid.SerialPortDeviceAddress;

public class SerialPortHandler {

    private static final String tag = SerialPortHandler.class.getName();

    public String getDevice() {
        return "/dev/ttyO2";
    }

    public void start() {
        Log.d(tag, "click");
        new Thread(new Runnable() {
            @Override
            public void run() {


                Bootstrap b = new Bootstrap();
                EventLoopGroup eventLoopGroup = new OioEventLoopGroup();
                b.group(eventLoopGroup);
                b.channel(SerialPortChannel.class);
                b.option(SerialPortChannelOption.BAUD_RATE, 19200);
                b.option(SerialPortChannelOption.STOP_BITS, SerialPortChannelConfig.Stopbits.STOPBITS_1);
                b.option(SerialPortChannelOption.READ_TIMEOUT, 30000);
                b.option(SerialPortChannelOption.DATA_BITS, SerialPortChannelConfig.Databits.DATABITS_8);
                b.option(SerialPortChannelOption.PARITY_BIT, SerialPortChannelConfig.Paritybit.EVEN);

                b.handler(new ChannelInitializer<SerialPortChannel>() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                        Log.e(tag, "Inactive!!!!!");
                        super.channelInactive(ctx);
                    }

                    @Override
                    public void initChannel(SerialPortChannel ch) {
                        //编码
                        ch.pipeline()
                                .addLast(new StringEncoder())
                                .addLast(new StringDecoder())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        Log.d(tag, "receive:"+msg);
                                        ctx.writeAndFlush(msg);
                                    }
                                })
                        ;
                    }

                });

                try {
                    ChannelFuture f = b.connect(new SerialPortDeviceAddress(getDevice())).sync();

                    Executors.newSingleThreadExecutor().execute(
                            new Runnable() {
                                ChannelFuture f;

                                Runnable channelFuture(ChannelFuture f) {
                                    this.f = f;
                                    return this;
                                }

                                @Override
                                public void run() {
                                    try {
                                        // Wait until the server socket is closed.
                                        // In this example, this does not happen, but you can do that to gracefully
                                        // shut down your server.
                                        //相当于在这里阻塞，直到serverchannel关闭
                                        f.channel().closeFuture().sync();
                                        Log.e(tag, "Sync is ending!!!!!");
                                    } catch (InterruptedException e) {
                                        Log.e(tag, "InterruptedException", e);
                                    } catch (Exception e) {
                                        Log.e(tag, "串口启动失败了", e);
                                    }
                                }
                            }.channelFuture(f)
                    );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
