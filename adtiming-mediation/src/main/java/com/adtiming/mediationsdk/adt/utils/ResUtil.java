// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.adtiming.mediationsdk.adt.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.adtiming.mediationsdk.utils.Constants;
import com.adtiming.mediationsdk.utils.DeveloperLog;
import com.adtiming.mediationsdk.utils.IOUtil;
import com.adtiming.mediationsdk.utils.cache.Cache;
import com.adtiming.mediationsdk.utils.crash.CrashUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ResUtil {
    private static final Pattern[] REGEX = {
            Pattern.compile("<link.+href=['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\s+src\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("url\\(['\"]?([^'\")]+)['\"]?\\)", Pattern.CASE_INSENSITIVE),
    };
    private static final Pattern NO_PRELOAD = Pattern.compile("nopreload=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern REDIRECT_200 = Pattern.compile("http\\-equiv\\s*=\\s*\"Refresh\"\\s+content\\s*=\\s*\"\\d+;url=([^\"]+)\"", Pattern.CASE_INSENSITIVE);

    private static final String[] RES_TYPE = new String[]{".jpg", ".jepg", ".png", ".webp", ".mp4",
            ".mp3", ".avi", ".rmvb", ".3gp", ".flv", ".ogg", ".wav", ".flac", ".ape"};

    public static boolean loadRes(Context context, List<String> resources) throws Exception {
        if (resources == null || resources.isEmpty()) {
            return false;
        }

        Set<URI> pendingRes = new HashSet<>();
        for (String resource : resources) {
            if (TextUtils.isEmpty(resource)) {
                continue;
            }

            final Set<URI> resSet = getHtmlRes(context, resource, 1);
            DeveloperLog.LogD("resSet:" + resSet.toString());
            //Traverse to get all relevant cached assets
            for (URI u : resSet) {
                if (Cache.existCache(context, u.toString())) {
                    File file = Cache.getCacheFile(context, u.toString(), null);
                    FileInputStream inputStream = IOUtil.getFileInputStream(file);
                    if (inputStream == null) {
                        continue;
                    }
                    Set<URI> s = getResourceURI(context, u, inputStream);
                    DeveloperLog.LogD("res uri is : " + u.toURL().toString() + " s:" + s.toString());
                    pendingRes.addAll(s);
                    IOUtil.closeQuietly(inputStream);
                }
                pendingRes.add(u);
            }
        }

        if (pendingRes.isEmpty()) {
            return false;
        } else {
            DeveloperLog.LogD("resSet:" + pendingRes.toString());
            return ResDownloader.downloadFile(pendingRes);
        }
    }

    /**
     * Parses a given url's all child links' and returns their assets 
     */
    private static Set<URI> getHtmlRes(Context context, final String url, int reqCount) throws Exception {
        if (reqCount > 5) {
            return new HashSet<>();
        }

        final Set<URI> resSet = new HashSet<>();
        //if not cached, download it first then parse
        File file;
        if (!Cache.existCache(context, url)) {
            file = ResDownloader.downloadFile(url);
        } else {
            file = Cache.getCacheFile(context, url, null);
        }

        if (file != null) {
            DeveloperLog.LogD("file:" + file.getName());
            //checks redirect until finds a HTTP 200
            String nextPage = getNextPage(context, url);
            //checks if the next page has http-equiv
            String refreshPage = getRefreshPage(context, nextPage);
            if (!TextUtils.equals(nextPage, refreshPage)) {
                resSet.addAll(getHtmlRes(context, refreshPage, reqCount + 1));
            }

            if (!TextUtils.isEmpty(nextPage)) {
                URI uri = new URI(nextPage);
                resSet.add(uri);
                resSet.addAll(getCss(context, uri, file));
            }
        }
        return resSet;
    }

    /**
     * Recursively parses a given uri to get its all child elements
     */
    private static Set<URI> getCss(Context context, URI uri, File file) {
        Set<URI> cssResult = new HashSet<>();
        try {
            FileInputStream fileInputStream = IOUtil.getFileInputStream(file);
            if (fileInputStream == null) {
                return cssResult;
            }
            Set<URI> set = getResourceURI(context, uri, fileInputStream);
            for (final URI u : set) {
                if (u.toURL().getFile().endsWith("css")) {
                    if (Cache.existCache(context, u.toURL().toString())) {
                        cssResult.add(u);
                        cssResult.addAll(getCss(context, u, Cache.getCacheFile(context, u.toString(), null)));
                    } else {
                        DeveloperLog.LogD("download css file URI:" + u.toString());
                        File cssFile = ResDownloader.downloadFile(u.toURL().toString());
                        if (cssFile != null) {
                            cssResult.add(u);
                            cssResult.addAll(getCss(context, u, cssFile));
                        }
                    }
                }
            }
            return cssResult;
        } catch (Exception e) {
            DeveloperLog.LogD("getHtmlRes error", e);
            return cssResult;
        }
    }

    private static String getRefreshPage(Context context, String url) {
        FileInputStream inputStream = null;
        try {
            File file = Cache.getCacheFile(context, url, null);
            inputStream = IOUtil.getFileInputStream(file);
            if (inputStream == null) {
                return url;
            }
            String html = IOUtil.toString(inputStream, "utf-8");
            Matcher m = REDIRECT_200.matcher(html);
            if (m.find()) {
                String s = m.group(1);
                if (s.startsWith("data:")) {
                    return url;
                }
                int i = s.indexOf('#');
                if (i != -1) {
                    s = s.substring(0, i);
                }
                URL u = new URL(new URL(url), s);
                DeveloperLog.LogD("u:" + u.toString());
                return u.toString();
            }
        } catch (Exception e) {
            DeveloperLog.LogD("getRefreshPage error", e);
        } finally {
            IOUtil.closeQuietly(inputStream);
        }
        return url;
    }

    private static String getNextPage(Context context, String url) {
        //Checks HTTP 302 
        try {
            File header = Cache.getCacheFile(context, url, Constants.FILE_HEADER_SUFFIX);
            if (!header.exists()) {
                return url;
            }
            String location = Cache.getValueFromFile(header, Constants.KEY_LOCATION);
            if (!TextUtils.isEmpty(location)) {
                URL u = new URL(new URL(url), location);
                return getNextPage(context, u.toString());
            } else {
                File file = Cache.getCacheFile(context, url, null);
                if (file == null || !file.exists()) {
                    return url;
                }
                String contentType = Cache.getValueFromFile(header, Constants.KEY_CONTENT_TYPE);

                if (TextUtils.equals(contentType, "text/html")) {
                    return url;
                }
            }
        } catch (Exception e) {
            DeveloperLog.LogD("getHtmlRes error", e);
        }
        return url;
    }

    private static Set<URI> getResourceURI(Context context, URI uri, InputStream inStream) throws
            IOException, URISyntaxException {
        return getResourceURI(context, uri, IOUtil.toString(inStream, Charset.forName("UTF-8")));
    }

    private static Set<URI> getResourceURI(Context context, URI uri, String html) throws
            MalformedURLException, URISyntaxException {
        Set<URI> resources = new LinkedHashSet<>();
        if (shouldNotTraversals(uri.toURL().toString())) {
            resources.add(uri);
            return resources;
        }
        if (TextUtils.isEmpty(html)) {
            return resources;
        }

        String locationUri = getRedirectUrl(context, uri.toURL().toString());
        if (!TextUtils.isEmpty(locationUri) && !TextUtils.equals(locationUri, uri.toString())) {
            DeveloperLog.LogD("URI:" + uri.toString());
            DeveloperLog.LogD("locationURI:" + locationUri);
            uri = new URI(locationUri);
        }

        Set<URI> notNeeded = new HashSet<>();
        match(uri, html, NO_PRELOAD, notNeeded, null);

        for (Pattern p : REGEX) {
            match(uri, html, p, resources, notNeeded);
        }
        return resources;
    }

    private static void match(URI context, String html, Pattern
            p, Set<URI> result, Set<URI> excludes) {
        Matcher m = p.matcher(html);
        while (m.find()) {
            String s = m.group(1);
            if (!TextUtils.isEmpty(s)) {
                s = s.trim();
                if (s.startsWith("data:") || s.startsWith("javascript:")) {
                    continue;
                }
                int i = s.indexOf('#');
                if (i != -1) {
                    s = s.substring(0, i);
                }
                try {
                    URL u = new URL(context.toURL(), s);
                    URI uri = new URI(u.getProtocol(), u.getHost(), u.getPath(), u.getQuery(), null);
                    if (excludes == null || !excludes.contains(uri)) {
                        result.add(uri);
                    }
                } catch (Exception e) {
                    CrashUtil.getSingleton().saveException(e);
                    DeveloperLog.LogD("res match error", e);
                }
            }
        }
    }

    private static String getRedirectUrl(Context context, String url) {
        try {
            File header = Cache.getCacheFile(context, url, Constants.FILE_HEADER_SUFFIX);
            if (header.exists()) {
                String location = Cache.getValueFromFile(header, Constants.KEY_LOCATION);
                if (TextUtils.isEmpty(location)) {
                    return url;
                } else {
                    return getRedirectUrl(context, location);
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            DeveloperLog.LogD("getRedirectUrl error", e);
            CrashUtil.getSingleton().saveException(e);
            return "";
        }
    }

    /**
     * Checks if a css file needs traversal
     */
    private static boolean shouldNotTraversals(String url) throws MalformedURLException {
        URL u = new URL(url);
        String file = u.getFile();
        boolean result = false;
        for (String s : RES_TYPE) {
            if (file.endsWith(s)) {
                result = true;
            }
        }
        return result;
    }

    public static WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        try {
            DeveloperLog.LogD(url);
            int i = url.indexOf('#');
            if (i != -1) url = url.substring(0, i);

            //Check redirect location
            File headerFile = Cache.getCacheFile(view.getContext(), url, Constants.FILE_HEADER_SUFFIX);
            if (!headerFile.exists()) {
                return null;
            } else {
                String location = Cache.getValueFromFile(headerFile, Constants.KEY_LOCATION);
                if (!TextUtils.isEmpty(location)) {
                    return null;
                }
            }

            if (Cache.existCache(view.getContext(), url)) {
                DeveloperLog.LogD("exist:" + url);
                try {
                    File header = Cache.getCacheFile(view.getContext().getApplicationContext(), url, Constants.FILE_HEADER_SUFFIX);
                    String mime_type = Cache.getValueFromFile(header, Constants.KEY_CONTENT_TYPE);

                    if (TextUtils.isEmpty(mime_type)) {//checks the mimeType obtained from the header
                        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                        if (TextUtils.equals("js", extension.toLowerCase())) {
                            mime_type = "application/x-javascript";
                        } else {
                            mime_type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        }
                    }

                    if (!TextUtils.isEmpty(mime_type)) {//re-checks the mimeType in order to not load pure text files
                        InputStream input = IOUtil.getFileInputStream(Cache.getCacheFile(view.getContext().getApplicationContext(),
                                url, null));
                        if (input == null) {
                            return null;
                        }
                        WebResourceResponse response = new WebResourceResponse(mime_type, null, input);
                        Map<String, String> headers = new HashMap<>();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            headers.put("Access-Control-Allow-Origin", "*");
                            response.setResponseHeaders(headers);
                        }
                        return response;
                    }
                } catch (Exception e) {
                    DeveloperLog.LogD("ResUtil", e);
                    CrashUtil.getSingleton().saveException(e);
                }
            }
        } catch (Exception e) {
            DeveloperLog.LogD("ResUtil", e);
            CrashUtil.getSingleton().saveException(e);
        }
        return null;
    }
}
