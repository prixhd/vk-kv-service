package org.example.repository;

import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.mapping.TarantoolResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class TarantoolKvRepository {

    private final TarantoolBoxClient client;

    public TarantoolKvRepository(String host, int port, String user, String password) {
        try {
            this.client = TarantoolFactory.box()
                    .withHost(host)
                    .withPort(port)
                    .withUser(user)
                    .withPassword(password)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed connect to Tarantool", e);
        }
    }

    private List<?> call(String function, Object... args) {
        try {
            List<Object> argList = new ArrayList<>(Arrays.asList(args));

            TarantoolResponse<List<?>> response = client.call(function, argList).get();
            return response.get();

        } catch (Exception e) {
            throw new RuntimeException("Error функции: " + function, e);
        }
    }


    private byte[] extractValue(List<?> tuple) {
        if (tuple.size() > 1 && tuple.get(1) != null) {
            return (byte[]) tuple.get(1);
        }
        return null;
    }

    public void put(String key, byte[] value) {
        call("kv_put", key, value);
    }

    public KvResult get(String key) {
        List<?> result = call("kv_get", key);

        if (result.isEmpty() || result.get(0) == null) {
            return KvResult.notFound();
        }

        List<?> tuple = (List<?>) result.get(0);
        String foundKey = (String) tuple.get(0);

        byte[] foundValue = extractValue(tuple);

        return KvResult.found(foundKey, foundValue);
    }

    public boolean delete(String key) {
        List<?> result = call("kv_delete", key);
        return !result.isEmpty() && Boolean.TRUE.equals(result.get(0));
    }

    public long count() {
        List<?> result = call("kv_count");
        return result.isEmpty() ? 0 : ((Number) result.get(0)).longValue();
    }

    public void range(String from, String to, Consumer<KvResult> onEach) {
        List<?> result = call("kv_range", from, to);

        for (Object item : result) {
            if (!(item instanceof List)) continue;

            List<?> tuple = (List<?>) item;
            if (tuple.isEmpty()) continue;

            Object keyObj = tuple.get(0);
            String key;

            if (keyObj instanceof String) {
                key = (String) keyObj;
            } else if (keyObj instanceof List) {
                List<?> keyList = (List<?>) keyObj;
                if (keyList.isEmpty() || !(keyList.get(0) instanceof String)) continue;
                key = (String) keyList.get(0);
            } else {
                continue;
            }

            byte[] value = extractValue(tuple);

            onEach.accept(KvResult.found(key, value));
        }
    }

    public void close() {
        try {
            client.close();
        } catch (Exception e) {
            throw new RuntimeException("Error close connection", e);
        }
    }

    public static class KvResult {

        private final boolean found;
        private final String key;
        private final byte[] value;

        private KvResult(boolean found, String key, byte[] value) {
            this.found = found;
            this.key = key;
            this.value = value;
        }

        public static KvResult found(String key, byte[] value) {
            return new KvResult(true, key, value);
        }

        public static KvResult notFound() {
            return new KvResult(false, null, null);
        }

        public boolean isFound() { return found; }
        public String getKey()   { return key; }
        public byte[] getValue() { return value; }
    }
}