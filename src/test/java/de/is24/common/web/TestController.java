package de.is24.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import java.net.URL;


@Controller
public class TestController {
  @RequestMapping("/test")
  public String test(@DecodedUrl URL url) {
    return url.toExternalForm();
  }

  @RequestMapping("/testNotRequiredParameter")
  public String testNotRequiredParameter(@DecodedUrl(required = false) URL url) {
    return (url != null) ? url.toExternalForm() : "missing URL";
  }

}
