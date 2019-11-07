// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request.network;

import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Params {

    public static Builder newBuilder() {
        return new Builder();
    }

    private final Map<String, List<Object>> mMap;

    private Params(Builder builder) {
        this.mMap = builder.mMap;
    }

    /**
     * Get parameters by key.
     *
     * @param key key.
     * @return if the key does not exist, it may be null.
     */
    public List<Object> get(String key) {
        return mMap.get(key);
    }

    /**
     * Gets the first value of the key.
     *
     * @param key key.
     * @return if the key does not exist, it may be null.
     */
    public Object getFirst(String key) {
        List<Object> values = mMap.get(key);
        if (values != null && values.size() > 0)
            return values.get(0);
        return null;
    }

    /**
     * Gets {@link Set} view of the parameters.
     *
     * @return a set view of the mappings.
     * @see Map#entrySet()
     */
    public Set<Map.Entry<String, List<Object>>> entrySet() {
        return mMap.entrySet();
    }

    /**
     * Gets {@link Set} view of the keys.
     *
     * @return a set view of the keys.
     * @see Map#keySet()
     */
    public Set<String> keySet() {
        return mMap.keySet();
    }

    /**
     * No parameters.
     *
     * @return true if there are no key-values pairs.
     * @see Map#isEmpty()
     */
    public boolean isEmpty() {
        return mMap.isEmpty();
    }

    /**
     * Parameters contain the key.
     *
     * @param key key.
     * @return true if there contains the key.
     */
    public boolean containsKey(String key) {
        return mMap.containsKey(key);
    }

    /**
     * ReBuilder.
     */
    public Builder builder() {
        Map<String, List<Object>> map = new LinkedHashMap<>();
        for (Map.Entry<String, List<Object>> entry : mMap.entrySet()) {
            map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return new Builder(map);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Set<String> keySet = keySet();
        for (String key : keySet) {
            List<Object> values = get(key);
            for (Object value : values) {
                if (value instanceof CharSequence) {
                    String textValue = Uri.encode(value.toString());
                    builder.append("&").append(key).append("=").append(textValue);
                }
            }
        }
        if (builder.length() > 0) builder.deleteCharAt(0);
        return builder.toString();
    }

    public static class Builder {

        private Map<String, List<Object>> mMap;

        private Builder() {
            this.mMap = new LinkedHashMap<>();
        }

        private Builder(Map<String, List<Object>> map) {
            this.mMap = map;
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, int value) {
            return add(key, Integer.toString(value));
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, long value) {
            return add(key, Long.toString(value));
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, boolean value) {
            return add(key, Boolean.toString(value));
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, char value) {
            return add(key, String.valueOf(value));
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, double value) {
            return add(key, Double.toString(value));
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, float value) {
            return add(key, Float.toString(value));
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, short value) {
            return add(key, Integer.toString(value));
        }

        /**
         * Adds parameter.
         */
        public Builder add(String key, CharSequence value) {
            return add(key, (Object) value);
        }

        /**
         * Adds parameters.
         */
        public Builder add(String key, List<String> values) {
            for (String value : values) add(key, value);
            return this;
        }

        /**
         * Adds parameter.
         */
        private Builder add(String key, Object value) {
            if (!TextUtils.isEmpty(key)) {
                if (!mMap.containsKey(key)) {
                    mMap.put(key, new ArrayList<>(1));
                }
                if (value == null) value = "";
                mMap.get(key).add(value);
            }
            return this;
        }

        /**
         * Adds parameters.
         */
        public Builder add(Params params) {
            for (Map.Entry<String, List<Object>> entry : params.entrySet()) {
                String key = entry.getKey();
                List<Object> valueList = entry.getValue();
                for (Object value : valueList) add(key, value);
            }
            return this;
        }

        /**
         * Adds parameters.
         */
        public Builder set(Params params) {
            return clear().add(params);
        }

        /**
         * Removes parameters by key.
         */
        public Builder remove(String key) {
            mMap.remove(key);
            return this;
        }

        /**
         * Removes all parameters.
         */
        public Builder clear() {
            mMap.clear();
            return this;
        }

        public Params build() {
            return new Params(this);
        }
    }
}