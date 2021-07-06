package com.zfoo.net.base.bio;

import com.zfoo.protocol.util.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;


@Ignore
public class UrlTest {

    // 不包含端口的ip地址
    @Test
    public void inetAddressTest() throws UnknownHostException {
        // 获取本机的ip
        InetAddress ipLocal = InetAddress.getLocalHost();
        System.out.println("本机地址相关信息：");
        System.out.println("本机计算机名称：" + ipLocal.getHostName());
        System.out.println("本机ip：" + ipLocal.getHostAddress());

        System.out.println("**************************************************************************");

        // 获取远程ip地址
        InetAddress ipBaidu = InetAddress.getByName("www.baidu.com");
        // 如果解析不到name和address都为ip
        // InetAddress ipBaidu=InetAddress.getByName("115.239.211.112");
        System.out.println("远程地址相关信息：");
        System.out.println("远程计算机名称：" + ipBaidu.getHostName());
        System.out.println("远程ip：" + ipBaidu.getHostAddress());
    }

    // 包含端口的ip地址
    @Test
    public void inetSocketAddressToInetAddressTest() {
        // localhost=127.0.0.1,本机ip
        InetSocketAddress address = new InetSocketAddress("www.baidu.com", 9999);
        System.out.println(address.getHostName());
        System.out.println(address.getPort());

        // InetSocketAddress和InetAddress的相互转换
        InetAddress ip = address.getAddress();
        System.out.println(ip.getHostName());// 计算机名称
        System.out.println(ip.getHostAddress());// ip
    }

    @Test
    public void urlTest() {
        // URI统一资源标识符，用来唯一的标识一个资源
        // URL统一资源定位符，一种具体的URI
        // 四部分组成：协议 存放资源的主机名称 端口 资源文件名称
        URL url = null;

        // 读取网页的资源
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            // 绝对路径
            url = new URL("https://pic3.zhimg.com/v2-473b053a376f6752da5d5b06a31b7304_b.jpg");
            System.out.println(url.getProtocol());
            System.out.println(url.getHost());
            System.out.println(url.getPort());
            System.out.println(url.getFile());// 资源
            System.out.println(url.getPath());// 相对路径
            System.out.println(url.getRef());// 锚点
            System.out.println(url.getQuery());// 存在锚点，参数为null；如果不存在锚点，返回正确

            inputStream = url.openStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            String message;
            while ((message = bufferedReader.readLine()) != null) {
                System.out.println(message);// 输出到控制台
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeIO(bufferedReader);
        }
    }
}
