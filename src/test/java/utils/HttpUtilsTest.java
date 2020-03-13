package utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazeluff.discord.nhlbot.utils.HttpException;
import com.hazeluff.discord.nhlbot.utils.HttpUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpUtils.class, HttpClientBuilder.class })
public class HttpUtilsTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtilsTest.class);
	
	@Mock
	URI mockURI;
	@Mock
	HttpGet mockRequest;
	@Mock
	CloseableHttpResponse mockResponse;
	@Mock
	StatusLine mockStatusLine;
	@Mock
	HttpEntity mockEntity;
	@Mock
	HttpClientBuilder mockClientBuilder;
	@Mock
	CloseableHttpClient mockClient;
	@Mock
	BufferedReader mockBufferedReader;
	@Mock
	InputStreamReader mockInputStreamReader;

	@Before
	public void setup() throws Exception {
		mockStatic(HttpClientBuilder.class);
		when(HttpClientBuilder.create()).thenReturn(mockClientBuilder);
		when(mockClientBuilder.build()).thenReturn(mockClient);
		whenNew(HttpGet.class).withArguments(mockURI).thenReturn(mockRequest);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(mockBufferedReader);
		whenNew(InputStreamReader.class).withAnyArguments().thenReturn(mockInputStreamReader);
	}

	@Test
	public void getShouldReturnString() throws URISyntaxException, ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldReturnString");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		String result = HttpUtils.get(mockURI);

		assertEquals("{key:value}", result);
		
		verify(mockClient).execute(mockRequest);
	}

	@Test
	public void getShouldRetryWhenStatusIsNot200()
			throws URISyntaxException, ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldRetryWhenStatusIsNot200");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(500, 500, 500, 500, 200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		String result = HttpUtils.get(mockURI);

		assertEquals("{key:value}", result);

		verify(mockClient, times(5)).execute(mockRequest);
	}

	@Test
	public void getShouldRetryWhenResponseIsNull()
			throws URISyntaxException, ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldRetryWhenStatusIsNot200");
		when(mockClient.execute(mockRequest)).thenReturn(null, null, null, null, mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		String result = HttpUtils.get(mockURI);

		assertEquals("{key:value}", result);

		verify(mockClient, times(5)).execute(mockRequest);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getShouldRetryWhenIOExceptionIsThrown()
			throws URISyntaxException, ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldRetryWhenStatusIsNot200");
		when(mockClient.execute(mockRequest)).thenThrow(IOException.class, IOException.class, IOException.class,
				IOException.class).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		String result = HttpUtils.get(mockURI);

		assertEquals("{key:value}", result);

		verify(mockClient, times(5)).execute(mockRequest);
	}

	@Test(expected = HttpException.class)
	public void getShouldThrowHttpExceptionWhenRetriesExceededAndStatusIsNot200()
			throws URISyntaxException, ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldThrowHttpExceptionWhenRetriesExceeded");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(500);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		HttpUtils.get(mockURI);
	}

	@Test(expected = HttpException.class)
	public void getShouldThrowHttpExceptionWhenRetriesExceededAndResponseIsNull()
			throws URISyntaxException, ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldThrowHttpExceptionWhenRetriesExceededAndResponseIsNull");
		when(mockClient.execute(mockRequest)).thenReturn(null);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		HttpUtils.get(mockURI);
	}

	@Test(expected = HttpException.class)
	public void getShouldThrowHttpExceptionWhenRetriesExceededAndIOExceptionIsThrown()
			throws URISyntaxException, ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldThrowHttpExceptionWhenRetriesExceededAndIOExceptionIsThrown");
		when(mockClient.execute(mockRequest)).thenThrow(IOException.class);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		HttpUtils.get(mockURI);
	}

	@Test(expected = HttpException.class)
	public void getShouldThrowHttpExceptionWhenReadLineThrowsIOException()
			throws ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldThrowHttpExceptionWhenReadLineThrowsIOException");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenThrow(IOException.class);

		HttpUtils.get(mockURI);
	}

	@Test(expected = HttpException.class)
	public void getShouldThrowHttpExceptionWhenGetContentThrowsUnsupportedOperationException()
			throws ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldThrowHttpExceptionWhenGetContentThrowsUnsupportedOperationException");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenThrow(UnsupportedOperationException.class);

		HttpUtils.get(mockURI);
	}

	@Test(expected = HttpException.class)
	public void getShouldThrowHttpExceptionWhenGetContentThrowsIOException()
			throws ClientProtocolException, IOException, HttpException {
		LOGGER.info("getShouldThrowHttpExceptionWhenGetContentThrowsIOException");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenThrow(IOException.class);

		HttpUtils.get(mockURI);
	}
}
