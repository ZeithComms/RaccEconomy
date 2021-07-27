package org.zeith.comms.c17racceconomy.utils;

public class NFU
{
	public static String balance2String(long balance)
	{
		return String.format("%,2d", balance).replaceAll("\\p{Z}", " ").trim();
	}
}