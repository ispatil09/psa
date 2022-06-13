package com.psa.util;

import java.util.Scanner;

public class ScannerUtil {
	public static String getString(String msg) {
		System.out.print(msg);
		Scanner scanner = new Scanner(System.in);
		String string = scanner.next();
		return string;
	}
	public static String getCommand() {
		System.out.print("\nPSA > ");
		Scanner scanner = new Scanner(System.in);
		String string = scanner.nextLine();
		return string;
	}
}
