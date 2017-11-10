package com.monitor.bus.consts;

import android.provider.BaseColumns;

/**
 * 
 * 2013-10-28
 */
public final class DBUser {

	public static final class User implements BaseColumns {
		public static final String USERNAME = "username";
		public static final String PASSWORD = "password";
		public static final String IPADDRESS = "ip";
		public static final String PORT = "port"; 
		public static final String ISDOMAIN= "isdomain";
		public static final String ISSAVED = "issaved";
	}

}
