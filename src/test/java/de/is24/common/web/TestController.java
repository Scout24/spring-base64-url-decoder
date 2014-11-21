package de.is24.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import java.net.URI;


@Controller
public class TestController {
  @RequestMapping("/test")
  public String test(@DecodedUri URI uri) {
    return uri.toString();
  }

  @RequestMapping("/testNotRequiredParameter")
  public String testNotRequiredParameter(@DecodedUri(required = false) URI uri) {
    return (uri != null) ? uri.toString() : "missing URI";
  }

}
