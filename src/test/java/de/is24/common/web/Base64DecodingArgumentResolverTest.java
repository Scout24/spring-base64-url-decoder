package de.is24.common.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Base64;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


public class Base64DecodingArgumentResolverTest {
  public static final String TEST_URL = "http://www.google.de?test=123&foo=bar";
  public static final String MALFORMED_URL = "<script>Some stupid script.</script>";

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = standaloneSetup(new TestController()).setCustomArgumentResolvers(new Base64DecodingArgumentResolver())
      .build();
  }

  @Test
  public void shouldDecodeUrlEncoded() throws Exception {
    String encodedUrl = URLEncoder.encode(TEST_URL, "UTF-8");
    mockMvc.perform(request(HttpMethod.GET, "/test?url=" + encodedUrl))
    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
    .andExpect(MockMvcResultMatchers.forwardedUrl(TEST_URL));
  }

  @Test
  public void shouldDecodeBase64Encoded() throws Exception {
    String encodedUrl = new String(Base64.getEncoder().encode(TEST_URL.getBytes()));
    mockMvc.perform(request(HttpMethod.GET, "/test?url=" + encodedUrl))
    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
    .andExpect(MockMvcResultMatchers.forwardedUrl(TEST_URL));
  }

  @Test(expected = MalformedURLException.class)
  public void shouldThrowExceptionWhenDecodingFails() throws Exception {
    mockMvc.perform(request(HttpMethod.GET, "/test?url=" + MALFORMED_URL));
  }
}
