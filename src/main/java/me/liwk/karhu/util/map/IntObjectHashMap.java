package me.liwk.karhu.util.map;

import java.util.*;

public class IntObjectHashMap<V> implements IntObjectMap<V> {
    public static final int DEFAULT_CAPACITY = 8;
    public static final float DEFAULT_LOAD_FACTOR = 0.5F;
    private static final Object NULL_VALUE = new Object();
    private int maxSize;
    private final float loadFactor;
    private int[] keys;
    private V[] values;
    private int size;
    private int mask;
    private final Set<Integer> keySet = new IntObjectHashMap.KeySet();
    private final Set<Entry<Integer, V>> entrySet = new IntObjectHashMap.EntrySet();
    private final Iterable<PrimitiveEntry<V>> entries = new Iterable<PrimitiveEntry<V>>() {
        @Override
        public Iterator<PrimitiveEntry<V>> iterator() {
            return new PrimitiveIterator();
        }
    };

    public IntObjectHashMap() {
        this(8, 0.5F);
    }

    public IntObjectHashMap(int initialCapacity) {
        this(initialCapacity, 0.5F);
    }

    public IntObjectHashMap(int initialCapacity, float loadFactor) {
        if (!(loadFactor <= 0.0F) && !(loadFactor > 1.0F)) {
            this.loadFactor = loadFactor;
            int capacity = MapMathUtil.safeFindNextPositivePowerOfTwo(initialCapacity);
            this.mask = capacity - 1;
            this.keys = new int[capacity];
            V[] temp = (V[])(new Object[capacity]);
            this.values = temp;
            this.maxSize = this.calcMaxSize(capacity);
        } else {
            throw new IllegalArgumentException("loadFactor must be > 0 and <= 1");
        }
    }

    private static <T> T toExternal(T value) {
        assert value != null : "null is not a legitimate internal value. Concurrent Modification?";

        return value == NULL_VALUE ? null : value;
    }

    private static <T> T toInternal(T value) {
        return (T)(value == null ? NULL_VALUE : value);
    }

    @Override
    public V get(int key) {
        int index = this.indexOf(key);
        return index == -1 ? null : toExternal(this.values[index]);
    }

    @Override
    public V put(int key, V value) {
        int startIndex = this.hashIndex(key);
        int index = startIndex;

        while(this.values[index] != null) {
            if (this.keys[index] == key) {
                V previousValue = this.values[index];
                this.values[index] = toInternal(value);
                return toExternal(previousValue);
            }

            if ((index = this.probeNext(index)) == startIndex) {
                throw new IllegalStateException("Unable to insert");
            }
        }

        this.keys[index] = key;
        this.values[index] = toInternal(value);
        this.growSize();
        return null;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends V> sourceMap) {
        if (sourceMap instanceof IntObjectHashMap) {
            IntObjectHashMap<V> source = (IntObjectHashMap)sourceMap;

            for(int i = 0; i < source.values.length; ++i) {
                V sourceValue = source.values[i];
                if (sourceValue != null) {
                    this.put(source.keys[i], sourceValue);
                }
            }
        } else {
            for(Entry<? extends Integer, ? extends V> entry : sourceMap.entrySet()) {
                this.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public V remove(int key) {
        int index = this.indexOf(key);
        if (index == -1) {
            return null;
        } else {
            V prev = this.values[index];
            this.removeAt(index);
            return toExternal(prev);
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void clear() {
        Arrays.fill(this.keys, 0);
        Arrays.fill(this.values, null);
        this.size = 0;
    }

    @Override
    public boolean containsKey(int key) {
        return this.indexOf(key) >= 0;
    }

    @Override
    public boolean containsValue(Object value) {
        V v1 = toInternal((V)value);

        for(V v2 : this.values) {
            if (v2 != null && v2.equals(v1)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterable<PrimitiveEntry<V>> entries() {
        return this.entries;
    }

    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                return new Iterator<V>() {
                    final PrimitiveIterator iter = new PrimitiveIterator();

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public V next() {
                        return iter.next().value();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    public int hashCode() {
        int hash = this.size;

        for(int key : this.keys) {
            hash ^= hashCode(key);
        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof IntObjectMap)) {
            return false;
        } else {
            IntObjectMap other = (IntObjectMap)obj;
            if (this.size != other.size()) {
                return false;
            } else {
                for(int i = 0; i < this.values.length; ++i) {
                    V value = this.values[i];
                    if (value != null) {
                        int key = this.keys[i];
                        Object otherValue = other.get(key);
                        if (value == NULL_VALUE) {
                            if (otherValue != null) {
                                return false;
                            }
                        } else if (!value.equals(otherValue)) {
                            return false;
                        }
                    }
                }

                return true;
            }
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return this.containsKey(this.objectToKey(key));
    }

    @Override
    public V get(Object key) {
        return this.get(this.objectToKey(key));
    }

    public V put(Integer key, V value) {
        return this.put(this.objectToKey(key), value);
    }

    @Override
    public V remove(Object key) {
        return this.remove(this.objectToKey(key));
    }

    @Override
    public Set<Integer> keySet() {
        return this.keySet;
    }

    @Override
    public Set<Entry<Integer, V>> entrySet() {
        return this.entrySet;
    }

    private int objectToKey(Object key) {
        return (Integer)key;
    }

    private int indexOf(int key) {
        int startIndex = this.hashIndex(key);
        int index = startIndex;

        while(this.values[index] != null) {
            if (key == this.keys[index]) {
                return index;
            }

            if ((index = this.probeNext(index)) == startIndex) {
                return -1;
            }
        }

        return -1;
    }

    private int hashIndex(int key) {
        return hashCode(key) & this.mask;
    }

    private static int hashCode(int key) {
        return key;
    }

    private int probeNext(int index) {
        return index + 1 & this.mask;
    }

    private void growSize() {
        ++this.size;
        if (this.size > this.maxSize) {
            if (this.keys.length == Integer.MAX_VALUE) {
                throw new IllegalStateException("Max capacity reached at size=" + this.size);
            }

            this.rehash(this.keys.length << 1);
        }
    }

    private boolean removeAt(int index) {
        --this.size;
        this.keys[index] = 0;
        this.values[index] = null;
        int nextFree = index;
        int i = this.probeNext(index);

        for(V value = this.values[i]; value != null; value = this.values[i = this.probeNext(i)]) {
            int key = this.keys[i];
            int bucket = this.hashIndex(key);
            if (i < bucket && (bucket <= nextFree || nextFree <= i) || bucket <= nextFree && nextFree <= i) {
                this.keys[nextFree] = key;
                this.values[nextFree] = value;
                this.keys[i] = 0;
                this.values[i] = null;
                nextFree = i;
            }
        }

        return nextFree != index;
    }

    private int calcMaxSize(int capacity) {
        int upperBound = capacity - 1;
        return Math.min(upperBound, (int)((float)capacity * this.loadFactor));
    }

    private void rehash(int newCapacity) {
        int[] oldKeys = this.keys;
        V[] oldVals = this.values;
        this.keys = new int[newCapacity];
        V[] temp = (V[])(new Object[newCapacity]);
        this.values = temp;
        this.maxSize = this.calcMaxSize(newCapacity);
        this.mask = newCapacity - 1;

        for(int i = 0; i < oldVals.length; ++i) {
            V oldVal = oldVals[i];
            if (oldVal != null) {
                int oldKey = oldKeys[i];
                int index = this.hashIndex(oldKey);

                while(this.values[index] != null) {
                    index = this.probeNext(index);
                }

                this.keys[index] = oldKey;
                this.values[index] = oldVal;
            }
        }
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            return "{}";
        } else {
            StringBuilder sb = new StringBuilder(4 * this.size);
            sb.append('{');
            boolean first = true;

            for(int i = 0; i < this.values.length; ++i) {
                V value = this.values[i];
                if (value != null) {
                    if (!first) {
                        sb.append(", ");
                    }

                    sb.append(this.keyToString(this.keys[i])).append('=').append(value == this ? "(this Map)" : toExternal(value));
                    first = false;
                }
            }

            return sb.append('}').toString();
        }
    }

    protected String keyToString(int key) {
        return Integer.toString(key);
    }

    private final class EntrySet extends AbstractSet<Entry<Integer, V>> {
        private EntrySet() {
        }

        @Override
        public Iterator<Entry<Integer, V>> iterator() {
            return new MapIterator();
        }

        @Override
        public int size() {
            return IntObjectHashMap.this.size();
        }
    }

    /**
     * Set implementation for iterating over the keys.
     */
    private final class KeySet extends AbstractSet<Integer> {
        @Override
        public int size() {
            return IntObjectHashMap.this.size();
        }

        @Override
        public boolean contains(Object o) {
            return IntObjectHashMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return IntObjectHashMap.this.remove(o) != null;
        }

        @Override
        public boolean retainAll(Collection<?> retainedKeys) {
            boolean changed = false;
            for (Iterator<PrimitiveEntry<V>> iter = entries().iterator(); iter.hasNext();) {
                PrimitiveEntry<V> entry = iter.next();
                if (!retainedKeys.contains(entry.key())) {
                    changed = true;
                    iter.remove();
                }
            }
            return changed;
        }

        @Override
        public void clear() {
            IntObjectHashMap.this.clear();
        }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                private final Iterator<Entry<Integer, V>> iter = entrySet.iterator();

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Integer next() {
                    return iter.next().getKey();
                }

                @Override
                public void remove() {
                    iter.remove();
                }
            };
        }
    }

    /**
     * Iterator over primitive entries. Entry key/values are overwritten by each call to {@link #next()}.
     */
    private final class PrimitiveIterator implements Iterator<PrimitiveEntry<V>>, PrimitiveEntry<V> {
        private int prevIndex = -1;
        private int nextIndex = -1;
        private int entryIndex = -1;

        private void scanNext() {
            while (++nextIndex != values.length && values[nextIndex] == null) {
            }
        }

        @Override
        public boolean hasNext() {
            if (nextIndex == -1) {
                scanNext();
            }
            return nextIndex != values.length;
        }

        @Override
        public PrimitiveEntry<V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            prevIndex = nextIndex;
            scanNext();

            // Always return the same Entry object, just change its index each time.
            entryIndex = prevIndex;
            return this;
        }

        @Override
        public void remove() {
            if (prevIndex == -1) {
                throw new IllegalStateException("next must be called before each remove.");
            }
            if (removeAt(prevIndex)) {
                // removeAt may move elements "back" in the array if they have been displaced because their spot in the
                // array was occupied when they were inserted. If this occurs then the nextIndex is now invalid and
                // should instead point to the prevIndex which now holds an element which was "moved back".
                nextIndex = prevIndex;
            }
            prevIndex = -1;
        }

        // Entry implementation. Since this implementation uses a single Entry, we coalesce that
        // into the Iterator object (potentially making loop optimization much easier).

        @Override
        public int key() {
            return keys[entryIndex];
        }

        @Override
        public V value() {
            return toExternal(values[entryIndex]);
        }

        @Override
        public void setValue(V value) {
            values[entryIndex] = toInternal(value);
        }
    }


    /**
     * Iterator used by the {@link Map} interface.
     */
    private final class MapIterator implements Iterator<Entry<Integer, V>> {
        private final PrimitiveIterator iter = new PrimitiveIterator();

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Entry<Integer, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            iter.next();

            return new MapEntry(iter.entryIndex);
        }

        @Override
        public void remove() {
            iter.remove();
        }
    }

    /**
     * A single entry in the map.
     */
    final class MapEntry implements Entry<Integer, V> {
        private final int entryIndex;

        MapEntry(int entryIndex) {
            this.entryIndex = entryIndex;
        }

        @Override
        public Integer getKey() {
            verifyExists();
            return keys[entryIndex];
        }

        @Override
        public V getValue() {
            verifyExists();
            return toExternal(values[entryIndex]);
        }

        @Override
        public V setValue(V value) {
            verifyExists();
            V prevValue = toExternal(values[entryIndex]);
            values[entryIndex] = toInternal(value);
            return prevValue;
        }

        private void verifyExists() {
            if (values[entryIndex] == null) {
                throw new IllegalStateException("The map entry has been removed");
            }
        }
    }
}
