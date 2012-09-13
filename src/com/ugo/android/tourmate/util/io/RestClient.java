package com.ugo.android.tourmate.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class RestClient {
	
	private static String convertStreamToString(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	private ArrayList<NameValuePair> params;

	private ArrayList<NameValuePair> headers;
	
	private StringEntity postEntity;

	private String url;
	private int responseCode;

	private String message;
	private String response;
	private InputStream responseStream;
	/**
	 * The timeout in milliseconds until a connection is established.
	 */
	private int timeoutConnection;

	/**
	 * The socket timeout in milliseconds which is the time for waiting for a
	 * reply after connection.
	 */
	private int timeoutSocket;

	public RestClient(String url) {
		this.url = url;
		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
		timeoutConnection = 5000;
		timeoutSocket = 5000;
	}

	public void addHeader(String name, String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	public void addParam(String name, String value) {
		params.add(new BasicNameValuePair(name, value));
	}
	
	public void setPostStringEntity(StringEntity data) {
		this.postEntity = data;
	}

	public void execute(RequestMethod method)
			throws UnsupportedEncodingException, ConnectTimeoutException, UnknownHostException {
		switch (method) {
		case GET: {
			HttpGet request = new HttpGet(this.getFullUrl());

			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}

			executeRequest(request, url);
			break;
		}
		case POST: {
			HttpPost request = new HttpPost(url);

			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}

			/*if (!params.isEmpty()) {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}*/
			if (postEntity != null) {
				request.setEntity(postEntity);
			}

			executeRequest(request, url);
			break;
		}
		}
	}

	public String getErrorMessage() {
		return message;
	}

	public String getFullUrl() throws UnsupportedEncodingException {
		return url + getUrlParametersString();
	}

	public String getResponse() {
		return response;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public InputStream getResponseStream() {
		return responseStream;
	}

	/**
	 * Gets the timeout in milliseconds until a connection is established.
	 * 
	 * @return the timeout in milliseconds
	 */
	public int getTimeoutConnection() {
		return timeoutConnection;
	}

	/**
	 * Gets the socket timeout in milliseconds which is the time for waiting for
	 * a reply after connection.
	 * 
	 * @return the timeout in milliseconds
	 */
	public int getTimeoutSocket() {
		return timeoutSocket;
	}

	/**
	 * Sets the timeout in milliseconds until a connection is established.
	 * 
	 * @param timeoutConnection
	 *            the timeout to set in milliseconds
	 */
	public void setTimeoutConnection(int timeoutConnection) {
		this.timeoutConnection = timeoutConnection;
	}

	/**
	 * Sets the socket timeout in milliseconds which is the time for waiting for
	 * a reply after connection
	 * 
	 * @param timeoutSocket
	 *            the timeout to set in milliseconds
	 */
	public void setTimeoutSocket(int timeoutSocket) {
		this.timeoutSocket = timeoutSocket;
	}

	private void executeRequest(HttpUriRequest request, String url)
			throws ConnectTimeoutException, UnknownHostException {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams
				.setConnectionTimeout(httpParams, timeoutConnection);
		HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);

		HttpClient client = new DefaultHttpClient(httpParams);

		HttpResponse httpResponse;

		try {
			httpResponse = client.execute(request);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			message = httpResponse.getStatusLine().getReasonPhrase();

			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {

				responseStream = entity.getContent();
				response = convertStreamToString(responseStream);

				// Closing the input stream will trigger connection release
				responseStream.close();
			}

		} catch (ClientProtocolException e) {
			client.getConnectionManager().shutdown();

		} catch (UnknownHostException e) {
			client.getConnectionManager().shutdown();
			throw e;
		} catch (IOException e) {
			client.getConnectionManager().shutdown();
		}
	}

	private String getUrlParametersString() throws UnsupportedEncodingException {
		// add parameters
		String combinedParams = "";
		if (!params.isEmpty()) {
			combinedParams += "?";
			for (NameValuePair p : params) {
				String paramString = p.getName() + "="
						+ URLEncoder.encode(p.getValue(), "UTF-8");
				if (combinedParams.length() > 1) {
					combinedParams += "&" + paramString;
				} else {
					combinedParams += paramString;
				}
			}
		}
		return combinedParams;
	}

	public enum RequestMethod {
		GET, POST
	}

}
