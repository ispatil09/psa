package com.psa.util;

import java.util.ResourceBundle;

public class ResourceBundleUtil {
	public static String getString(String propertyFileName , String key) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(propertyFileName);
		String message = resourceBundle.getString(key);
		return message;
	}
}
