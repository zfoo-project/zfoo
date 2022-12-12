package com.zfoo.net.base.netty.nettyfileserver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author godotg
 * @version 1.0
 * @since 2017 05.31 09:58
 */
public class FileServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Server receive client : [" + msg + "]");
        File file = new File((String) msg);
        if (file.exists() && !file.isFile()) {
            ctx.writeAndFlush("not a file:" + file + System.getProperty("line.separator"));
            return;
        }
        ctx.write(file + " " + file.length());
        RandomAccessFile randomAccessFile = null;
        FileRegion region = null;
        try {
            randomAccessFile = new RandomAccessFile((String) msg, "r");
            region = new DefaultFileRegion(randomAccessFile.getChannel(), 0, randomAccessFile.length());
            ctx.write(region);
            ctx.writeAndFlush(System.getProperty("line.separator"));
            randomAccessFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
