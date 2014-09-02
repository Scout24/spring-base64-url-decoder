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
import java.net.MalformedURLException;
import java.net.URL;
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
    return findMethodAnnotation(DecodedUrl.class, parameter) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    String parameterName = parameter.getParameterName();
    String parameterPayload = webRequest.getParameter(parameterName);
    if (parameterPayload == null) {
      DecodedUrl methodAnnotation = findMethodAnnotation(DecodedUrl.class, parameter);
      if (methodAnnotation.required()) {
        throw new MissingServletRequestParameterException("Missing parameter {}.", parameterName);
      } else {
        return null;
      }
    }
    return parseUrl(parameterPayload);
  }

  private URL parseUrl(String possiblyEncodedUrl) throws ServletRequestBindingException {
    LOGGER.debug("Decoding possible encoded url. Payload: {}", possiblyEncodedUrl);
    try {
      return new URL(possiblyEncodedUrl);
    } catch (MalformedURLException e) {
      return decodeUrl(possiblyEncodedUrl);
    }
  }

  private URL decodeUrl(String possiblyEncodedUrl) throws ServletRequestBindingException {
    LOGGER.debug("Try to decode URL with URL decoder.");
    try {
      String decoded = URLDecoder.decode(possiblyEncodedUrl, encoding);
      return new URL(decoded);
    } catch (MalformedURLException | UnsupportedEncodingException e) {
      return decodeBase64Url(possiblyEncodedUrl);
    }
  }

  private URL decodeBase64Url(String possiblyEncodedUrl) throws ServletRequestBindingException {
    LOGGER.debug("Try to decode url with Base64 decoder.");
    try {
      byte[] decodedUrl = Base64.getDecoder().decode(possiblyEncodedUrl);
      return new URL(new String(decodedUrl));
    } catch (IllegalArgumentException | MalformedURLException iae) {
      LOGGER.debug("Failed to decode URL parameter.");
      throw new ServletRequestBindingException("Failed to decode URL parameter!");
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
