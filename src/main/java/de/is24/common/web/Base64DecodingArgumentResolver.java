package de.is24.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class Base64DecodingArgumentResolver implements HandlerMethodArgumentResolver {
  private static final Logger LOGGER = LoggerFactory.getLogger(Base64DecodingArgumentResolver.class);
  public static final String STANDARD_ENCODING = StandardCharsets.ISO_8859_1.name();

  private final String encoding;

  public Base64DecodingArgumentResolver() {
    this.encoding = STANDARD_ENCODING;
  }

  public Base64DecodingArgumentResolver(String encoding) {
    this.encoding = encoding;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return findMethodAnnotation(DecodedUri.class, parameter) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    String parameterName = parameter.getParameterName();
    String parameterPayload = webRequest.getParameter(parameterName);
    if (parameterPayload == null) {
      DecodedUri methodAnnotation = findMethodAnnotation(DecodedUri.class, parameter);
      if (methodAnnotation.required()) {
        throw new MissingServletRequestParameterException(parameterName, "String");
      } else {
        return null;
      }
    }
    return parseUri(parameterPayload);
  }

  private URI parseUri(String possiblyEncodedUrl) throws ServletRequestBindingException {
    LOGGER.debug("Decoding possible encoded url. Payload: {}", possiblyEncodedUrl);
    try {
      return new URI(urlDecode(decodeBase64Url(possiblyEncodedUrl)));
    } catch (URISyntaxException | UnsupportedEncodingException e) {
      throw new ServletRequestBindingException("Failed to decode URL parameter!");
    }
  }

  private String urlDecode(String possiblyEncodedUrl) throws UnsupportedEncodingException {
    return URLDecoder.decode(possiblyEncodedUrl, encoding);
  }

  private String decodeBase64Url(String possiblyEncodedUrl) {
    try {
      byte[] decodedUrl = Base64.getDecoder().decode(possiblyEncodedUrl);
      return new String(decodedUrl);
    } catch (IllegalArgumentException e) {
      LOGGER.debug("Failed to decode Base64 URI. Maybe just URL encoded.");
      return possiblyEncodedUrl;
    }
  }

  private <T extends Annotation> T findMethodAnnotation(Class<T> annotationClass, MethodParameter parameter) {
    T annotation = parameter.getParameterAnnotation(annotationClass);
    return (annotation != null) ? annotation : searchForAnnotation(annotationClass, parameter);
  }

  private <T extends Annotation> T searchForAnnotation(Class<T> annotationClass, MethodParameter parameter) {
    T annotation;
    Annotation[] annotationsToSearch = parameter.getParameterAnnotations();
    for (Annotation toSearch : annotationsToSearch) {
      annotation = AnnotationUtils.findAnnotation(toSearch.annotationType(), annotationClass);
      if (annotation != null) {
        return annotation;
      }
    }
    return null;
  }
}
