/*
 * Copyright 2013 Future TV, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package tv.icntv.common;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Johnson.Liu
 * <p/>
 * Author: Johnson.Liu
 * Date: 2013/10/22
 * Time: 11:42
 */
public class HttpClientHolder {

    private static HttpClientBuilder builder;
    private static CloseableHttpClient client;
    private static final int DEFAULT_MAX_NUM_PER_ROUTE = 20;
    private static final int DEFAULT_MAX_TOTAL_NUM = 50;
    private static final int MAX_EXECUTION_NUM = 3;

    private static void init() {
        // custom builder
        builder = HttpClients.custom();
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(DEFAULT_MAX_NUM_PER_ROUTE);
        manager.setMaxTotal(DEFAULT_MAX_TOTAL_NUM);
        builder.setConnectionManager(manager);
        builder.setMaxConnPerRoute(DEFAULT_MAX_NUM_PER_ROUTE);
        builder.setMaxConnTotal(DEFAULT_MAX_TOTAL_NUM);
        // headers
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg," +
                " application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint," +
                " application/msword, */*"));
//        headers.add(new BasicHeader("Content-Type","application/xml"));
        headers.add(new BasicHeader("Accept-Language", "zh-cn,en-us,zh-tw,en-gb,en;"));
        headers.add(new BasicHeader("Accept-Charset","gbk,gb2312,utf-8,BIG5,ISO-8859-1;"));
        headers.add(new BasicHeader("Connection","Close"));
        headers.add(new BasicHeader("Cache-Control","no-cache"));
        headers.add(new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; CIBA)"));
        headers.add(new BasicHeader("Accept-Encoding", "gzip,deflate,sdch"));
        headers.add(new BasicHeader("Connection", "close"));
        builder.setDefaultHeaders(headers);
        // retry handler
        builder.setRetryHandler(new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= MAX_EXECUTION_NUM) {
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    return false;
                }
                if (exception instanceof SSLException) {
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        });

        // build client
        client = builder.build();
    }

    /**
     * Get client
     * @return
     */
    public static synchronized CloseableHttpClient getClient() {
        if (builder == null) {
            init();
        }
        return client;
    }
}
