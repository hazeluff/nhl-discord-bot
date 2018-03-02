package com.hazeluff.discord.nhlbot.nhl;

import java.util.HashMap;
import java.util.Map;

public class GamePeriod {
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

		public String getId() {
			return id;
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

	public GamePeriod(int periodNum, Type type, String ordinalNum) {
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
		switch (type) {
		case REGULAR:
			return String.format("%s Period", ordinalNum);
		case OVERTIME:
			return String.format("%s Overtime", ordinalNum);
		case SHOOTOUT:
			return "Shootout";
		default:
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ordinalNum == null) ? 0 : ordinalNum.hashCode());
		result = prime * result + periodNum;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GamePeriod other = (GamePeriod) obj;
		if (ordinalNum == null) {
			if (other.ordinalNum != null)
				return false;
		} else if (!ordinalNum.equals(other.ordinalNum))
			return false;
		if (periodNum != other.periodNum)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NHLGamePeriod [periodNum=" + periodNum + ", type=" + type + ", ordinalNum=" + ordinalNum + "]";
	}
}
