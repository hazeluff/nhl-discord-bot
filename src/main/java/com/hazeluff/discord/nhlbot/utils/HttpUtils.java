package com.hazeluff.discord.nhlbot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.Config;

public class HttpUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

	public static String get(URI uri) throws HttpException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(uri);
		HttpResponse response = null;
		int retries = Config.HTTP_REQUEST_RETRIES;
		int httpStatusCode = -1;
		do {
			try {
				response = client.execute(request);
				httpStatusCode = response == null ? -1 : response.getStatusLine().getStatusCode();
			} catch (IOException e) {
				LOGGER.error("Failed to request page [" + uri.toString() + "]", e);
			}
		} while ((response == null || httpStatusCode != 200) && retries-- > 0);
		if ((response == null || httpStatusCode != 200) && retries <= 0) {
			String message = "Failed to get page after (" + Config.HTTP_REQUEST_RETRIES + ") retries.";
			LOGGER.error(message);
			throw new HttpException(message);
		}

		BufferedReader rd;
		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		} catch (UnsupportedOperationException | IOException e) {
			LOGGER.error("Error reading response");
			throw new HttpException(e);
		}
	}

	public static String getAndRetry(URI uri, int retries, long sleepMs, String description) throws HttpException {
		try {
			return Utils.getAndRetry(() -> get(uri), retries, sleepMs, description);
		} catch (TimeoutException e) {
			throw new HttpException(e);
		}
	}
}
