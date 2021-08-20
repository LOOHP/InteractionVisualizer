package com.loohp.interactionvisualizer.objectholders;

import java.util.Objects;
import java.util.regex.Pattern;

import org.bukkit.plugin.Plugin;

public class EntryKey {
	
	public static final Pattern VALIDATE = Pattern.compile("^[a-z1-9_]*$");
	public static final String NATIVE = "interactionvisualizer";

	private String namespace;
	private String key;

	/**
	 * <b>You are encouraged to use {@link EntryKey#EntryKey(Plugin, String)}.</b>
	 */
	public EntryKey(String namespacedKey) {
		int index = namespacedKey.indexOf(":");
		if (index >= 0) {
			this.namespace = namespacedKey.substring(0, index);
			this.key = namespacedKey.substring(index + 1);
		} else {
			this.namespace = NATIVE;
			this.key = namespacedKey;
		}
		if (!VALIDATE.matcher(namespace).matches() || !VALIDATE.matcher(key).matches()) {
			throw new IllegalArgumentException("Invalid EntryKey, an EntryKey can only contain " + VALIDATE.pattern());
		}
	}

	/**
	 * <b>You are encouraged to use {@link EntryKey#EntryKey(Plugin, String)}.</b>
	 */
	public EntryKey(String namespace, String key) {
		this.namespace = namespace;
		this.key = key;
		if (!VALIDATE.matcher(namespace).matches() || !VALIDATE.matcher(key).matches()) {
			throw new IllegalArgumentException("Invalid EntryKey, an EntryKey can only contain " + VALIDATE.pattern());
		}
	}
	
	public EntryKey(Plugin plugin, String key) {
		this.namespace = plugin.getName().toLowerCase();
		this.key = key;
		if (!VALIDATE.matcher(namespace).matches() || !VALIDATE.matcher(key).matches()) {
			throw new IllegalArgumentException("Invalid EntryKey, an EntryKey can only contain " + VALIDATE.pattern());
		}
	}
	
	public String getNamespace() {
		return namespace;
	}

	public String getKey() {
		return key;
	}
	
	public boolean isNative() {
		return namespace.equals(NATIVE);
	}

	@Override
	public String toString() {
		return namespace + ":" + key;
	}
	
	public String toSimpleString() {
		if (isNative()) {
			return key;
		} else {
			return toString();
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof EntryKey) {
			EntryKey other = (EntryKey) obj;
			return Objects.equals(other.namespace, this.namespace) && Objects.equals(other.key, this.key);
		}
		return false;
	}
}