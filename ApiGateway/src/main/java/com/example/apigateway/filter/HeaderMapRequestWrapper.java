package com.example.apigateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> headerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public HeaderMapRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void addHeader(String name, String value) {
        headerMap.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    }

    @Override
    public String getHeader(String name) {
        if (headerMap.containsKey(name)) {
            return headerMap.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (headerMap.containsKey(name)) {
            return Collections.enumeration(Collections.singleton(headerMap.get(name)));
        }
        return super.getHeaders(name);
    }

//    @Override
//    public Enumeration<String> getHeaderNames() {
//        if (headerMap.isEmpty()) {
//            return super.getHeaderNames();
//        }
//
//        Set<String> headerNames = new LinkedHashSet<>();
//        Enumeration<String> originalHeaderNames = super.getHeaderNames();
//        while (originalHeaderNames.hasMoreElements()) {
//            headerNames.add(originalHeaderNames.nextElement());
//        }
//        headerNames.addAll(headerMap.keySet());
//        return Collections.enumeration(headerNames);
//    }
}
