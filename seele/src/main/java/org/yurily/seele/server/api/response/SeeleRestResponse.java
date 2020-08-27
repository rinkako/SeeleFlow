/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.yurily.seele.server.api.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.yurily.seele.server.GDP;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class : SeeleRestResponse
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
public class SeeleRestResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_EXCEPTION = 500;

    private int code;

    private String message;

    private String seeleId;

    private String timestamp;

    private Object payload;

    public SeeleRestResponse(int code) {
        this(code, null);
    }

    public SeeleRestResponse(int code, String message) {
        this(code, message, null);
    }

    public SeeleRestResponse(int code, String message, Object payload) {
        this.code = code;
        this.message = message;
        this.payload = payload;
        this.timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        this.seeleId = GDP.SeeleId;
    }

    public static SeeleRestResponse ok() {
        return new SeeleRestResponse(CODE_SUCCESS);
    }

    public static SeeleRestResponse ok(Object payload) {
        return new SeeleRestResponse(CODE_SUCCESS, null, payload);
    }

    public static SeeleRestResponse exception(String message) {
        return new SeeleRestResponse(CODE_EXCEPTION, message);
    }
}
