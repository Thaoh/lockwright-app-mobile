package com.pears.pass.autofill.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecordStoreKeysTest {
    private static int failures = 0;

    public static void main(String[] args) {
        expect("v2 record key", RecordStoreKeys.recordKeyV2("abc"), "record-v2/abc");
        expect("v1 record key", RecordStoreKeys.recordKeyV1("abc"), "record/abc");
        expect("v2 file key", RecordStoreKeys.fileKeyV2("r", "f"), "record-v2/r/file/f");
        expect("v1 file key", RecordStoreKeys.fileKeyV1("r", "f"), "record/r/file/f");

        List<String> sites = new ArrayList<>();
        sites.add("https://example.com");
        List<Map<String, String>> uris = RecordStoreKeys.deriveUrisFromWebsites(sites, null);
        expect("uris size", uris.size(), 1);
        expect("uris uri", uris.get(0).get("uri"), "https://example.com");
        expect("uris match", uris.get(0).get("match"), RecordStoreKeys.DEFAULT_URI_MATCH);

        Map<String, Object> data = new HashMap<>();
        data.put("title", "Site");
        data.put("websites", sites);

        Map<String, Object> record = new HashMap<>();
        record.put("id", "abc");
        record.put("type", "login");
        record.put("data", data);

        Map<String, Object> v2 = RecordStoreKeys.toAppRecord(record);
        expect("schema", v2.get("schema"), 2);
        @SuppressWarnings("unchecked")
        Map<String, Object> v2data = (Map<String, Object>) v2.get("data");
        expect("v2 has uris", v2data.get("uris") instanceof List, true);

        Map<String, Object> v1 = RecordStoreKeys.projectRecordToV1(v2);
        expect("v1 has no schema", v1.containsKey("schema"), false);
        @SuppressWarnings("unchecked")
        Map<String, Object> v1data = (Map<String, Object>) v1.get("data");
        expect("v1 has no uris", v1data.containsKey("uris"), false);
        expect("v1 websites kept", ((List<?>) v1data.get("websites")).get(0), "https://example.com");

        if (failures > 0) {
            System.err.println(failures + " RecordStoreKeys checks failed");
            System.exit(1);
        }
        System.out.println("ok");
    }

    private static void expect(String label, Object got, Object want) {
        if (got == null ? want != null : !got.equals(want)) {
            failures++;
            System.err.println("FAIL " + label + ": got " + got + ", want " + want);
        }
    }
}
