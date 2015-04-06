// Copyright 2011 i-MD. All rights reserved.

package com.wy.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * @author  (yuanwang)
 */
public class HttpAgent {
  private static final int CON_TIME_OUT = 10000;
  private static final int SO_TIME_OUT = 10000;
  private static final int MAX_TOTAL_CONNECTIONS = 10;
  private static final int MAX_ROUTE_CONNECTIONS = 10;
  private static final int WAIT_TIMEOUT = 10000;
  private static HttpClient httpClient;
  

  public HttpAgent() {
	  createHttpClient();
  }
  public HttpResponse doDelete(String url, String params) {
    HttpDelete httpDelete = new HttpDelete(url + params);
    HttpResponse response = null;
    try {
      response = httpClient.execute(httpDelete);
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return response;
  }
  public HttpResponse doPut(String url, List<NameValuePair> formparams) {
    HttpPut httpPut = new HttpPut(url);
    UrlEncodedFormEntity entity;
    try {
      entity = new UrlEncodedFormEntity(formparams, "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
    httpPut.setEntity(entity);
    HttpResponse response = null;
    try {
      response = httpClient.execute(httpPut);
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return response;
  }
  public String doGet(String url, String params) {
	String result = "";
    HttpGet httpGet = new HttpGet(url + params);
    addHeader(httpGet);
    HttpResponse response = null;
    try {
      response = httpClient.execute(httpGet);
      if (response != null) {
		result = toJson(response);
      }
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
  public String doPost(String url, String params) {

		String result = "";
		HttpPost httpPost = new HttpPost(url);
		addHeader(httpPost);
		try {
			httpPost.setEntity(new StringEntity(params, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		}
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			if (response != null) {
				result = toJson(response);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
  }
  public String toJson(HttpResponse response) {
		String json = "";
		try {
			if (response != null) {
				int code = response.getStatusLine().getStatusCode();
				if (code == 200) {
					HttpEntity resEntity = response.getEntity();
					json = EntityUtils.toString(resEntity, HTTP.UTF_8);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
  }
  public HttpResponse doPost(String url, List<NameValuePair> formparams) {
    HttpPost httpPost = new HttpPost(url);
    addHeader(httpPost);
//    request.setHeader("cookie", "imdToken=\"" + token + "\"");
    // request.setHeader("User-Agent", IApp.USER_AGENT);
    UrlEncodedFormEntity entity;
    try {
      entity = new UrlEncodedFormEntity(formparams, "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
    httpPost.setEntity(entity);
    HttpResponse response = null;
    try {
      response = httpClient.execute(httpPost);
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return response;
//  if (httpResponse.getStatusLine().getStatusCode() == 200)
  }
  /*public void createHttpClient() {
    httpClient = new DefaultHttpClient();
    HttpParams httpParams = httpClient.getParams();
    httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1 );
    httpParams.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
    httpParams.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
//  HttpConnectionParams.setConnectionTimeout(params, CON_TIME_OUT);
//  HttpConnectionParams.setSoTimeout(params, SO_TIME_OUT);
    String proxyHost = android.net.Proxy.getDefaultHost();
    int proxyPort = android.net.Proxy.getDefaultPort();
    //Set Proxy params of client, if they are not the standard
    if (proxyHost != null && proxyPort > 0) {
      HttpHost proxy = new HttpHost(proxyHost, proxyPort);
      httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }
  }*/
  public static synchronized HttpClient createHttpClient() {
    if (null == httpClient) {
      HttpParams httpParams = new BasicHttpParams();
      HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
      HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
      HttpProtocolParams.setHttpElementCharset(httpParams, "UTF-8");
      HttpConnectionParams.setConnectionTimeout(httpParams, CON_TIME_OUT);
      HttpConnectionParams.setSoTimeout(httpParams, SO_TIME_OUT);
//      HttpProtocolParams.setUserAgent(httpParams, "");
      // 设置获取连接的最大等待时间
      ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);
      // 设置最大连接数
      ConnManagerParams.setMaxTotalConnections(httpParams, MAX_TOTAL_CONNECTIONS);
      // 设置每个路由最大连接数
      ConnPerRouteBean connPerRoute = new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS);
      ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);
      // 设置我们的HttpClient支持HTTP和HTTPS两种模式
      SchemeRegistry schReg = new SchemeRegistry();
      schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
      schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
      ClientConnectionManager conMgr = new ThreadSafeClientConnManager(httpParams, schReg);
//      conMgr.closeExpiredConnections();
//      conMgr.closeIdleConnections(idletime, tunit)
      httpClient = new DefaultHttpClient(conMgr, httpParams);
    }
    return httpClient;
  }
  protected void addHeader(HttpGet httpGet) {
  }
  protected void addHeader(HttpPost httpPost) {
  }
}

