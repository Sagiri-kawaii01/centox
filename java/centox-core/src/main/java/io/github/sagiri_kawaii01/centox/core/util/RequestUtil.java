package io.github.sagiri_kawaii01.centox.core.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="https://github.com/Sagiri-kawaii01>Sagiri-kawaii01</a>
 * @since 1.0
 */
public class RequestUtil {

    @NotNull
    public static JSONObject getJsonObject(ContentCachingRequestWrapper request) throws Exception {
        byte[] cache = request.getContentAsByteArray();
        JSONObject jsonObject;
        try {
            if (cache.length == 0) {
                jsonObject = getOriginJson(request);
            } else {
                jsonObject = JSONObject.parseObject(new String(cache, request.getCharacterEncoding()));
            }
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException("Json parse error", new ServletServerHttpRequest(request));
        }
        return jsonObject;
    }

    private static JSONObject getOriginJson(HttpServletRequest request) throws Exception {
        ServletInputStream is = request.getInputStream();
        byte[] bytes = new byte[10240];
        int len ;
        BufferedInputStream bi = new BufferedInputStream(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        while ((len = bi.read(bytes)) != -1) {
            os.write(bytes, 0, len);
        }
        return JSON.parseObject(new String(os.toByteArray(), StandardCharsets.UTF_8));
    }
}
