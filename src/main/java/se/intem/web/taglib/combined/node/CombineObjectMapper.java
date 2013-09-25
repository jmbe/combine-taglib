package se.intem.web.taglib.combined.node;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CombineObjectMapper extends ObjectMapper {

    public CombineObjectMapper() {
        configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

}