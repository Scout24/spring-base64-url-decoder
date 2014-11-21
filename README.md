Spring Base64 Url Decoder
=========================
[![Build Status](https://api.travis-ci.org/ImmobilienScout24/spring-base64-url-decoder.svg?branch=master)](https://travis-ci.org/ImmobilienScout24/spring-base64-url-decoder)

This library delivers a annotation and adds a [HandlerMethodArgumentResolver] to the [Spring] MVC framework.

Binaries
--------
Example for Maven:

```xml
    <dependency>
      <groupId>de.is24.spring</groupId>
      <artifactId>base64-url-decoder</artifactId>
      <version>1.0</version>
    </dependency>
```

Usage
-----

Annotate your parameters with the **@DecodedUri** annotation in a MVC Controller according to this example:
 
``` java
@Controller
public class TestController {
  @RequestMapping("/test")
  public String test(@DecodedUri URI uri) {
    return url.toString();
  }
}
```

And register the **Base64DecodingArgumentResolver** in to your MVC context:

``` java
@Configuration
public class Web extends WebMvcConfigurerAdapter {
  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new UrlResolvingHandlerMethodArgumentResolver());
  }
}
```

License
-------
MIT

[Spring]:http://spring.io/
[HandlerMethodArgumentResolver]:http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/method/support/HandlerMethodArgumentResolver.html
