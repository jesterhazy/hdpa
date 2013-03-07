package com.bronzespear.hdpa;

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
}
