package com.zfoo.net.base.http;

import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


@Ignore
public class AsyncHttpTest {


    @Test
    public void test() {
        HttpClient client = HttpClient.newBuilder()
                .executor(Executors.newSingleThreadExecutor())
                .build();

        HttpRequest request = HttpRequest
                .newBuilder(URI.create("https://www.baidu.com"))
                .GET()
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        CompletableFuture<HttpResponse<String>> sendAsync = client.sendAsync(request, responseBodyHandler);
        sendAsync.thenApply(t -> t.body()).thenAccept(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println(s);
            }
        });
        ThreadUtils.sleep(Integer.MAX_VALUE);
    }

}
