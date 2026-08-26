package com.pears.pass.autofill.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema v2 record keys and dual-store shape. Mirrors
 * lockwright-lib-vault recordSchema / toAppRecord / projectRecordToV1
 * so the autofill plugin can javac this without Android SDK.
 */
public final class RecordStoreKeys {
    public static final int SCHEMA_V2 = 2;
    public static final String DEFAULT_URI_MATCH = "baseDomain";
    public static final String RECORD_V1_PREFIX = "record/";
    public static final String RECORD_V2_PREFIX = "record-v2/";

    private RecordStoreKeys() {}

    public static String recordKeyV1(String id) {
        return RECORD_V1_PREFIX + id;
    }

    public static String recordKeyV2(String id) {
        return RECORD_V2_PREFIX + id;
    }

    public static String fileKeyV1(String recordId, String fileId) {
        return RECORD_V1_PREFIX + recordId + "/file/" + fileId;
    }

    public static String fileKeyV2(String recordId, String fileId) {
        return RECORD_V2_PREFIX + recordId + "/file/" + fileId;
    }

    public static List<Map<String, String>> deriveUrisFromWebsites(
            List<String> websites, List<Map<String, String>> existingUris) {
        List<String> list = websites != null ? websites : new ArrayList<>();
        Map<String, Map<String, String>> byUri = new LinkedHashMap<>();
        if (existingUris != null) {
            for (Map<String, String> entry : existingUris) {
                if (entry == null) continue;
                String uri = entry.get("uri");
                if (uri != null) byUri.put(uri, entry);
            }
        }
        List<Map<String, String>> out = new ArrayList<>();
        for (String uri : list) {
            if (uri == null) continue;
            Map<String, String> prev = byUri.get(uri);
            Map<String, String> next = new LinkedHashMap<>();
            next.put("uri", uri);
            String match = prev != null ? prev.get("match") : null;
            next.put("match", match != null && !match.isEmpty() ? match : DEFAULT_URI_MATCH);
            out.add(next);
        }
        return out;
    }

    public static List<String> deriveWebsitesFromUris(
            List<Map<String, String>> uris, List<String> fallbackWebsites) {
        if (uris != null) {
            List<String> out = new ArrayList<>();
            for (Map<String, String> entry : uris) {
                if (entry == null) continue;
                String uri = entry.get("uri");
                if (uri != null) out.add(uri);
            }
            return out;
        }
        return fallbackWebsites != null ? new ArrayList<>(fallbackWebsites) : new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toAppRecord(Map<String, Object> record) {
        if (record == null) {
            throw new IllegalArgumentException("toAppRecord: record required");
        }
        Map<String, Object> out = shallowCopy(record);
        Map<String, Object> dataIn = record.get("data") instanceof Map
                ? (Map<String, Object>) record.get("data")
                : new LinkedHashMap<>();
        Map<String, Object> data = shallowCopy(dataIn);

        boolean hasUriShape = "login".equals(record.get("type"))
                || data.get("websites") instanceof List
                || data.get("uris") instanceof List;

        if (hasUriShape) {
            List<Map<String, String>> existingUris = coerceUriList(data.get("uris"));
            List<String> websites = coerceStringList(data.get("websites"));
            if (existingUris.isEmpty()) {
                data.put("uris", deriveUrisFromWebsites(websites, existingUris));
            } else {
                List<Map<String, String>> normalized = new ArrayList<>();
                for (Map<String, String> entry : existingUris) {
                    if (entry == null || entry.get("uri") == null) continue;
                    Map<String, String> next = new LinkedHashMap<>();
                    next.put("uri", entry.get("uri"));
                    String match = entry.get("match");
                    next.put("match", match != null && !match.isEmpty() ? match : DEFAULT_URI_MATCH);
                    normalized.add(next);
                }
                data.put("uris", normalized);
            }
            data.put("websites", deriveWebsitesFromUris(
                    (List<Map<String, String>>) data.get("uris"), websites));
        }

        out.put("data", data);
        out.put("schema", SCHEMA_V2);
        return out;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> projectRecordToV1(Map<String, Object> record) {
        if (record == null) {
            throw new IllegalArgumentException("projectRecordToV1: record required");
        }
        Map<String, Object> rest = shallowCopy(record);
        rest.remove("schema");
        rest.remove("previousData");
        rest.remove("skipV1Projection");

        Map<String, Object> dataIn = rest.get("data") instanceof Map
                ? (Map<String, Object>) rest.get("data")
                : new LinkedHashMap<>();
        Map<String, Object> dataRest = shallowCopy(dataIn);
        Object uris = dataRest.remove("uris");

        List<String> websites = deriveWebsitesFromUris(
                coerceUriList(uris),
                coerceStringList(dataRest.get("websites"))
        );
        dataRest.put("websites", websites);
        rest.put("data", dataRest);
        return rest;
    }

    private static Map<String, Object> shallowCopy(Map<String, Object> in) {
        return new LinkedHashMap<>(in);
    }

    @SuppressWarnings("unchecked")
    private static List<String> coerceStringList(Object value) {
        List<String> out = new ArrayList<>();
        if (!(value instanceof List)) return out;
        for (Object item : (List<?>) value) {
            if (item instanceof String) out.add((String) item);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, String>> coerceUriList(Object value) {
        List<Map<String, String>> out = new ArrayList<>();
        if (!(value instanceof List)) return out;
        for (Object item : (List<?>) value) {
            if (!(item instanceof Map)) continue;
            Map<?, ?> map = (Map<?, ?>) item;
            Object uri = map.get("uri");
            if (!(uri instanceof String)) continue;
            Map<String, String> next = new LinkedHashMap<>();
            next.put("uri", (String) uri);
            Object match = map.get("match");
            if (match instanceof String) next.put("match", (String) match);
            out.add(next);
        }
        return out;
    }
}
