package com.bytes.fmk.service.persistence.impl;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OffsetDateTimeSerializer implements JsonSerializer<OffsetDateTime> {

	@Override
	public JsonElement serialize(OffsetDateTime src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
	}

}