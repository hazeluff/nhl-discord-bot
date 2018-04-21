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
	public void getShouldReturnString() throws URISyntaxException, ClientProtocolException, IOException {
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
	public void getShouldRetryWhenStatusIsNot200() throws URISyntaxException, ClientProtocolException, IOException {
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
	public void getShouldRetryWhenResponseIsNull() throws URISyntaxException, ClientProtocolException, IOException {
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
			throws URISyntaxException, ClientProtocolException, IOException {
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

	@Test(expected = RuntimeException.class)
	public void getShouldThrowRuntimeExceptionWhenRetriesExceededAndStatusIsNot200()
			throws URISyntaxException, ClientProtocolException, IOException {
		LOGGER.info("getShouldThrowRuntimeExceptionWhenRetriesExceeded");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(500);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		HttpUtils.get(mockURI);
	}

	@Test(expected = RuntimeException.class)
	public void getShouldThrowRuntimeExceptionWhenRetriesExceededAndResponseIsNull()
			throws URISyntaxException, ClientProtocolException, IOException {
		LOGGER.info("getShouldThrowRuntimeExceptionWhenRetriesExceededAndResponseIsNull");
		when(mockClient.execute(mockRequest)).thenReturn(null);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		HttpUtils.get(mockURI);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RuntimeException.class)
	public void getShouldThrowRuntimeExceptionWhenRetriesExceededAndIOExceptionIsThrown()
			throws URISyntaxException, ClientProtocolException, IOException {
		LOGGER.info("getShouldThrowRuntimeExceptionWhenRetriesExceededAndIOExceptionIsThrown");
		when(mockClient.execute(mockRequest)).thenThrow(IOException.class);
		when(mockBufferedReader.readLine()).thenReturn("{", "key:value", "}", null);

		HttpUtils.get(mockURI);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RuntimeException.class)
	public void getShouldThrowRuntimeExceptionWhenReadLineThrowsIOException()
			throws ClientProtocolException, IOException {
		LOGGER.info("getShouldThrowRuntimeExceptionWhenReadLineThrowsIOException");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockBufferedReader.readLine()).thenThrow(IOException.class);

		HttpUtils.get(mockURI);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RuntimeException.class)
	public void getShouldThrowRuntimeExceptionWhenGetContentThrowsUnsupportedOperationException()
			throws ClientProtocolException, IOException {
		LOGGER.info("getShouldThrowRuntimeExceptionWhenGetContentThrowsUnsupportedOperationException");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenThrow(UnsupportedOperationException.class);

		HttpUtils.get(mockURI);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RuntimeException.class)
	public void getShouldThrowRuntimeExceptionWhenGetContentThrowsIOException()
			throws ClientProtocolException, IOException {
		LOGGER.info("getShouldThrowRuntimeExceptionWhenGetContentThrowsIOException");
		when(mockClient.execute(mockRequest)).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
		when(mockStatusLine.getStatusCode()).thenReturn(200);
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockEntity.getContent()).thenThrow(IOException.class);

		HttpUtils.get(mockURI);
	}
}
