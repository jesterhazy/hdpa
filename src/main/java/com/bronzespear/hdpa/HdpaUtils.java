package com.bronzespear.hdpa;

import java.util.concurrent.TimeUnit;

public class HdpaUtils {
	public static String repeatString(String s, int times) {
		if (times <= 0) { 
			return s;
		}
		
		int slen = s.length();
		StringBuilder sb = new StringBuilder(slen * times);
		for (int i = 0; i < times; i++) {
			sb.append(s);
		}
		
		return sb.toString();
	}

	public static String formatDuration(long duration) {
		return new StringBuilder()
				.append(TimeUnit.MILLISECONDS.toHours(duration)).append(":")
				.append(TimeUnit.MILLISECONDS.toMinutes(duration) % 60).append(":")
				.append(TimeUnit.MILLISECONDS.toSeconds(duration) % 60).append(".")
				.append(duration % 1000).toString();
	}
}
