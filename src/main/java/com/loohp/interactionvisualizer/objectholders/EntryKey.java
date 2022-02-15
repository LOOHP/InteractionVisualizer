package com.loohp.interactionvisualizer.objectholders;

import org.bukkit.plugin.Plugin;

import java.util.regex.Pattern;

public class EntryKey {

    public static final Pattern VALIDATE = Pattern.compile("^[a-z0-9_]*$");
    public static final String NATIVE = "interactionvisualizer";

    private final String namespace;
    private final String key;

    /**
     * <b>You are encouraged to use {@link EntryKey#EntryKey(Plugin, String)} for your own plugin.</b>
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
            throw new IllegalArgumentException("Invalid EntryKey, an EntryKey may only contain " + VALIDATE.pattern());
        }
    }

    /**
     * <b>You are encouraged to use {@link EntryKey#EntryKey(Plugin, String)} for your own plugin.</b>
     */
    public EntryKey(String namespace, String key) {
        this.namespace = namespace;
        this.key = key;
        if (!VALIDATE.matcher(namespace).matches() || !VALIDATE.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid EntryKey, an EntryKey may only contain " + VALIDATE.pattern());
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
        return this.namespace + ":" + this.key;
    }

    public String toSimpleString() {
        return isNative() ? key : toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.namespace.hashCode();
        hash = 47 * hash + this.key.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntryKey other = (EntryKey) obj;
        return this.namespace.equals(other.namespace) && this.key.equals(other.key);
    }

}