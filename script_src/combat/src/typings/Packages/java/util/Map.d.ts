
declare module Packages.java.util {

    interface Map<K, V> {

        entrySet(): Set<Map.Entry<K, V>>;

        get(key: K): V;

        keySet(): Set<K>;

        values(): Collection<V>;
    }
}