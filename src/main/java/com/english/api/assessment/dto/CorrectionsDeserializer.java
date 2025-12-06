package com.english.api.assessment.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CorrectionsDeserializer extends JsonDeserializer<List<CorrectionItem>> {

    private static final TypeReference<List<CorrectionItem>> TYPE_REF = new TypeReference<>() {};

    @Override
    public List<CorrectionItem> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText();
            if (text == null || text.isBlank() || text.equals("[]")) {
                return new ArrayList<>();
            }
            ObjectMapper mapper = (ObjectMapper) p.getCodec();
            return mapper.readValue(text, TYPE_REF);
        }

        if (p.currentToken() == JsonToken.START_ARRAY) {
            return p.readValueAs(TYPE_REF);
        }

        return new ArrayList<>();
    }
}
