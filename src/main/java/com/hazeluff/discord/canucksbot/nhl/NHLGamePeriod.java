package com.hazeluff.discord.canucksbot.nhl;

import java.util.HashMap;
import java.util.Map;

public class NHLGamePeriod {
	private final int periodNum;
	private final Type type;
	private final String ordinalNum;

	public enum Type {
		REGULAR("REGULAR", "Regular"), OVERTIME("OVERTIME", "Overtime"), SHOOTOUT("SHOOTOUT", "Shootout");

		private final String id;
		private final String value;
		private static final Map<String, Type> VALUES_MAP = new HashMap<>();

		static {
			for (Type t : Type.values()) {
				VALUES_MAP.put(t.id, t);
			}
		}

		private Type(String id, String value) {
			this.id = id;
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public static Type parse(String id) {
			Type result = VALUES_MAP.get(id);
			if (result == null) {
				throw new IllegalArgumentException("No value exists for: " + id);
			}
			return result;
		}
	}

	public NHLGamePeriod(int periodNum, Type type, String ordinalNum) {
		this.periodNum = periodNum;
		this.type = type;
		this.ordinalNum = ordinalNum;
	}

	public int getPeriodNum() {
		return periodNum;
	}

	public Type getType() {
		return type;
	}

	public String getOrdinalNum() {
		return ordinalNum;
	}

	public String getDisplayValue() {
		return type == Type.SHOOTOUT ? type.value : String.format("%s %s", ordinalNum, type.value);
	}

	@Override
	public String toString() {
		return "NHLGamePeriod [periodNum=" + periodNum + ", type=" + type + ", ordinalNum=" + ordinalNum + "]";
	}
}
