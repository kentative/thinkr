package com.bytes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestUtil {

	public static void debugAsJson(Object obj) {
		Gson formatter = new GsonBuilder().setPrettyPrinting().create();
		System.out.println(formatter.toJson(obj));		
	}
}
