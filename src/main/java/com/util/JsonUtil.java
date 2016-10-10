package com.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

public class JsonUtil {

    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class.getName());
    private static final JsonUtil INS = new JsonUtil();
    private ObjectMapper mapper = null;

    public static JsonUtil getInstance() {
        return INS;
    }

    private JsonUtil() {
        mapper = new ObjectMapper();
    }

    public String convertObjectToJsonString(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        String ret = null;
        try (StringWriter w = new StringWriter()) {
            mapper.writeValue(w, obj);
            ret = w.toString();
        } catch (Exception e) {
            logger.error("convertObjectToJsonString, error", e);
        }
        return ret;
    }
	
	public static <T> List<T> readList(String fileName, Class<T> valueType) throws Exception{
		InputStream is = JsonUtil.class.getResourceAsStream(fileName);
		ObjectMapper mapper = new ObjectMapper();
		JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, valueType);
		List<T> list = mapper.readValue(is, javaType);
		return list;
	}
	
	public static <T> T readObject(String fileName, Class<T> valueType) throws Exception{
		InputStream is = JsonUtil.class.getResourceAsStream(fileName);
		ObjectMapper mapper = new ObjectMapper();
		T t = mapper.readValue(is, valueType);
		return t;
	}

}
