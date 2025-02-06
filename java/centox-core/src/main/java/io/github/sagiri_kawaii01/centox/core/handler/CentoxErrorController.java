package io.github.sagiri_kawaii01.centox.core.handler;

import io.github.sagiri_kawaii01.centox.x.exception.ApiException;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CentoxErrorController extends BasicErrorController {
    public CentoxErrorController() {
        super(new DefaultErrorAttributes(), new ErrorProperties());
    }

    @RequestMapping
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            throw new ApiException(status.value(), status.getReasonPhrase());
        }
        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        String path = "path:" + body.get("path");
        String error = "error:" + body.get("error");
        throw new ApiException(status.value(), String.join(", ", path, error));
    }

}
