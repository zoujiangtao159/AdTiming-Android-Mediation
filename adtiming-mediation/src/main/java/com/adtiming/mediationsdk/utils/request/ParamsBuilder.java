// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.utils.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ParamsBuilder {
    private List<Param> ps;

    public ParamsBuilder() {
        this.ps = new ArrayList<>();
    }

    public ParamsBuilder(int initialCapacity) {
        this.ps = new ArrayList<>(initialCapacity);
    }

    public ParamsBuilder(List<Param> ps) {
        this.ps = ps;
    }

    public ParamsBuilder p(String name, Object value) {
        ps.add(new Param(name, value));
        return this;
    }

    public String format(String charset) {
        return format(ps, charset);
    }

    public static String format(List<Param> ps, String charset) {
        try {
            final StringBuilder result = new StringBuilder();
            for (Param p : ps) {
                final String encodedName = URLEncoder.encode(p.name, charset);
                final String encodedValue = URLEncoder.encode(p.value, charset);
                if (result.length() > 0) {
                    result.append('&');
                }
                result.append(encodedName);
                if (encodedValue != null) {
                    result.append('=').append(encodedValue);
                }
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String format() {
        return format("UTF-8");
    }

    private static class Param {
        private String name, value;

        private Param(String name, Object value) {
            this.name = name;
            this.value = value == null ? "" : value.toString();
        }
    }
}
