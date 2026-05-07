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

    // IP address without a port
    @Test
    public void inetAddressTest() throws UnknownHostException {
        // Get the local machine IP
        InetAddress ipLocal = InetAddress.getLocalHost();
        System.out.println("Local address info:");
        System.out.println("Local hostname: " + ipLocal.getHostName());
        System.out.println("Local IP: " + ipLocal.getHostAddress());

        System.out.println("**************************************************************************");

        // Get a remote IP address
        InetAddress ipBaidu = InetAddress.getByName("www.baidu.com");
        // If resolution fails, both name and address return the raw IP
        // InetAddress ipBaidu=InetAddress.getByName("115.239.211.112");
        System.out.println("Remote address info:");
        System.out.println("Remote hostname: " + ipBaidu.getHostName());
        System.out.println("Remote IP: " + ipBaidu.getHostAddress());
    }

    // IP address with a port
    @Test
    public void inetSocketAddressToInetAddressTest() {
        // localhost = 127.0.0.1, loopback
        InetSocketAddress address = new InetSocketAddress("www.baidu.com", 9999);
        System.out.println(address.getHostName());
        System.out.println(address.getPort());

        // Conversion between InetSocketAddress and InetAddress
        InetAddress ip = address.getAddress();
        System.out.println(ip.getHostName()); // hostname
        System.out.println(ip.getHostAddress()); // ip
    }

    @Test
    public void urlTest() {
        // URI (Uniform Resource Identifier) uniquely identifies a resource
        // URL (Uniform Resource Locator) is a specific type of URI
        // Composed of 4 parts: scheme, host, port, and resource path
        URL url = null;

        // Read a web resource
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            // Absolute path
            url = new URL("https://pic3.zhimg.com/v2-473b053a376f6752da5d5b06a31b7304_b.jpg");
            System.out.println(url.getProtocol());
            System.out.println(url.getHost());
            System.out.println(url.getPort());
            System.out.println(url.getFile()); // resource
            System.out.println(url.getPath()); // relative path
            System.out.println(url.getRef()); // anchor/fragment
            // fragment present => query is null; no fragment => returns the query string
            System.out.println(url.getQuery());
            inputStream = url.openStream();
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            bufferedReader = new BufferedReader(inputStreamReader);

            String message;
            while ((message = bufferedReader.readLine()) != null) {
                System.out.println(message); // print to console
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeIO(bufferedReader);
        }
    }
}
