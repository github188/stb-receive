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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;

import org.apache.http.*;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Johnson.Liu
 * <p/>
 * Author: Johnson.Liu
 * Date: 2013/10/18
 * Time: 15:02
 */
public class HttpClientUtil {

    private static final int CONNECTION_REQUEST_TIMEOUT = 5 * 60 * 1000;
    private static final int SOCKET_TIMEOUT = 10 * 60 * 1000;
    private static final String DEFAULT_ENCODING = "utf8";

    public static String getContent(String url, Charset charset) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build());
        CloseableHttpClient client = HttpClientHolder.getClient();
        CloseableHttpResponse response = null;

        response = client.execute(request);
        return EntityUtils.toString(response.getEntity(), charset);

    }

    /**
     * Get content by url as string
     *
     * @param url original url
     * @return page content
     * @throws java.io.IOException
     */
    public static String getContent(String url) throws IOException {
        // construct request
        HttpGet request = new HttpGet(url);
	    request.setHeader(new BasicHeader("Content-Type",
			    "application/x-www-form-urlencoded; charset=UTF-8"));

	    request.setConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build());
        // construct response handler
        ResponseHandler<String> handler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws IOException {
                StatusLine status = response.getStatusLine();
                // status
                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    throw new HttpResponseException(status.getStatusCode(), status.getReasonPhrase());
                }
                // get encoding in header
                String encoding = getPageEncoding(response);
                boolean encodingFounded = true;
                if (Strings.isNullOrEmpty(encoding)) {
                    encodingFounded = false;
                    encoding = "iso-8859-1";
                }
                // get content and find real encoding
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }
                // get content
                byte[] contentBytes = EntityUtils.toByteArray(entity);
                if (contentBytes == null) {
                    return null;
                }
                // found encoding
                if (encodingFounded) {
                    return new String(contentBytes, encoding);
                }
                // attempt to discover encoding
                String rawContent = new String(contentBytes, DEFAULT_ENCODING);
                Matcher matcher = PATTERN_HTML_CHARSET.matcher(rawContent);
                if (matcher.find()) {
                    String realEncoding = matcher.group(1);
                    if (!encoding.equalsIgnoreCase(realEncoding)) {
                        // bad luck :(
                        return new String(rawContent.getBytes(encoding), realEncoding);
                    }
                }
                // not found right encoding :)
                return rawContent;
            }
        };
        // execute
        CloseableHttpClient client = HttpClientHolder.getClient();
        return client.execute(request, handler);
    }


	
    private static final Pattern PATTERN_HTML_CHARSET = Pattern.compile("(?i)\\w+/\\w+;\\s*charset=([a-zA-Z0-9\\-]+)");

    private static String getPageEncoding(HttpResponse response) {
        Header contentType = response.getEntity().getContentType();
        if (contentType == null || Strings.isNullOrEmpty(contentType.getValue())) {
            return null;
        }
        Matcher matcher = PATTERN_HTML_CHARSET.matcher(contentType.getValue());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    public static void main(String[] args) throws IOException {
           String url="http://58.214.17.70:8081/tsop-api/api/getChannelDetails.json?channelId=9";
       String content=getContent("http://master1:50070/jmx?qry=Hadoop:service=NameNode,name=FSNamesystem");
       System.out.println(content);
       String test=((JSONArray)JSON.parseObject(content).get("beans")).get(0).toString();
       System.out.println(JSON.parseObject(test).get("tag.HAState"));
    }
}
