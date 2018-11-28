package com.bytes.fmk.service.persistence.impl;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class OffsetDateTimeDeserializer implements JsonDeserializer<OffsetDateTime>  {

	@Override
	public OffsetDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return OffsetDateTime.parse(json.getAsJsonPrimitive().getAsString());
	}
}