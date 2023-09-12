package com.garganttua.server.bundles.maven.plugin;

import java.io.File;

public enum DependencyDestination {
	
	bin("bin"), binlibs("binlibs"), libs("libs"), deploy("deploy"), conf("conf"), none("none");
	
	private String string;

	DependencyDestination(String string) {
		this.string = string;
	}

	public String toString() {
		switch(this) {
		case bin:
			return "bin";
		case binlibs:
			return "bin"+File.separator+"libs";
		case conf:
			return "conf";
		case deploy:
			return "deploy";
		case libs:
			return "libs";
		default:
		case none:

		}
		return "";
	}

	static DependencyDestination fromString(String str) {
		switch(str) {
		case "bin":
			return bin;
		case "binlibs":
			return binlibs;
		case "conf":
			return conf;
		case "deploy":
			return deploy;
		case "libs":
			return libs;
		default:
		case "none":
			return none;
		}
	}

	String getString() {
		return this.string;
	}

}
