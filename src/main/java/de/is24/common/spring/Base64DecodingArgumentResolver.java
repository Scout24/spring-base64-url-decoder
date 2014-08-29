package de.is24.common.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;


public class Base64DecodingArgumentResolver implements HandlerMethodArgumentResolver {
  private static final Logger LOGGER = LoggerFactory.getLogger(Base64DecodingArgumentResolver.class);

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return findMethodAnnotation(Base64DecodedUrl.class, parameter) != null;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    String parameterName = parameter.getParameterName();
    String parameterPayload = webRequest.getParameter(parameterName);
    if (parameterPayload == null) {
      throw new MissingServletRequestParameterException("Missing parameter {}.", parameterName);
    }
    return parseUrl(parameterPayload);
  }

  private URL parseUrl(String possiblyEncodedUrl) throws MalformedURLException {
    try {
      return new URL(possiblyEncodedUrl);
    } catch (MalformedURLException e) {
      try {
        byte[] decodedUrl = Base64.getDecoder().decode(possiblyEncodedUrl);
        return new URL(new String(decodedUrl));
      } catch (IllegalArgumentException iae) {
        LOGGER.warn("Failed to decode URL parameter with payload: {}", possiblyEncodedUrl);
        throw new MalformedURLException("Failed to decode URL parameter!");
      }
    }
  }

  private <T extends Annotation> T findMethodAnnotation(Class<T> annotationClass, MethodParameter parameter) {
    T annotation = parameter.getParameterAnnotation(annotationClass);
    if (annotation != null) {
      return annotation;
    }

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
