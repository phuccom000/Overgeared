package net.stirdrem.overgeared.config.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Minimal TOML reader/writer for the config files: tables (incl. quoted and dotted names), comments,
 * booleans, integers, floats, basic/literal strings and (nested, multi-line) arrays. Inline tables and arrays
 * of tables are not supported.
 */
final class TomlIO {
    private static final Pattern BARE_KEY = Pattern.compile("[A-Za-z0-9_-]+");

    private TomlIO() {
    }

    static final class ParseException extends Exception {
        ParseException(String message, int line) {
            super(message + " (line " + line + ")");
        }
    }

    @SuppressWarnings("unchecked")
    static Object lookup(Map<String, Object> root, List<String> path) {
        Object current = root;
        for (String key : path) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = ((Map<String, Object>) map).get(key);
        }
        return current;
    }

    static String rangeText(Number min, Number max, Number lowest, Number highest) {
        boolean noMin = min.equals(lowest);
        boolean noMax = max.equals(highest);
        if (noMin && noMax) return "any";
        if (noMax) return "> " + min;
        if (noMin) return "< " + max;
        return min + " ~ " + max;
    }

    // ---- Writing ----

    static String write(List<ConfigSpec.ConfigValue<?>> values) {
        StringBuilder out = new StringBuilder();
        List<String> currentTable = List.of();
        boolean first = true;

        for (ConfigSpec.ConfigValue<?> value : values) {
            List<String> table = value.path.subList(0, value.path.size() - 1);
            String indent = "\t".repeat(table.size());
            if (!table.equals(currentTable)) {
                currentTable = table;
                if (!table.isEmpty()) {
                    if (!first) out.append('\n');
                    out.append("\t".repeat(table.size() - 1)).append('[');
                    for (int i = 0; i < table.size(); i++) {
                        if (i > 0) out.append('.');
                        out.append(key(table.get(i)));
                    }
                    out.append("]\n");
                }
            }
            first = false;

            for (String line : value.comment) {
                out.append(indent).append('#').append(line).append('\n');
            }
            for (String line : value.extraComment()) {
                out.append(indent).append("# ").append(line).append('\n');
            }
            out.append(indent).append(key(value.path.getLast())).append(" = ");
            writeValue(out, value.get());
            out.append('\n');
        }
        return out.toString();
    }

    private static String key(String key) {
        return BARE_KEY.matcher(key).matches() ? key : quote(key);
    }

    private static void writeValue(StringBuilder out, Object value) {
        if (value instanceof String s) {
            out.append(quote(s));
        } else if (value instanceof List<?> list) {
            out.append('[');
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) out.append(", ");
                writeValue(out, list.get(i));
            }
            out.append(']');
        } else if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (Double.isInfinite(d)) out.append(d > 0 ? "inf" : "-inf");
            else out.append(d);
        } else {
            out.append(value); // booleans, integers
        }
    }

    private static String quote(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20 || c == 0x7F) out.append(String.format("\\u%04X", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }

    // ---- Reading ----

    static Map<String, Object> read(String text) throws ParseException {
        return new Reader(text).parse();
    }

    private static final class Reader {
        private final String s;
        private int pos;
        private int line = 1;

        Reader(String s) {
            this.s = s.startsWith("﻿") ? s.substring(1) : s;
        }

        Map<String, Object> parse() throws ParseException {
            Map<String, Object> root = new LinkedHashMap<>();
            Map<String, Object> table = root;

            while (true) {
                skipWhitespaceAndComments(true);
                if (pos >= s.length()) return root;

                if (peek() == '[') {
                    pos++;
                    if (peek() == '[') throw error("Arrays of tables are not supported");
                    skipInlineWhitespace();
                    List<String> keys = readKeyPath();
                    skipInlineWhitespace();
                    expect(']');
                    table = descend(root, keys);
                } else {
                    List<String> keys = readKeyPath();
                    skipInlineWhitespace();
                    expect('=');
                    skipInlineWhitespace();
                    Object value = readValue();
                    Map<String, Object> target = descend(table, keys.subList(0, keys.size() - 1));
                    target.put(keys.getLast(), value);
                }
                endOfLine();
            }
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> descend(Map<String, Object> from, List<String> keys) throws ParseException {
            Map<String, Object> current = from;
            for (String key : keys) {
                Object next = current.computeIfAbsent(key, k -> new LinkedHashMap<String, Object>());
                if (!(next instanceof Map<?, ?>)) throw error("Key '" + key + "' is not a table");
                current = (Map<String, Object>) next;
            }
            return current;
        }

        private List<String> readKeyPath() throws ParseException {
            List<String> keys = new ArrayList<>();
            while (true) {
                skipInlineWhitespace();
                keys.add(readKey());
                skipInlineWhitespace();
                if (pos < s.length() && peek() == '.') {
                    pos++;
                } else {
                    return keys;
                }
            }
        }

        private String readKey() throws ParseException {
            if (pos >= s.length()) throw error("Expected a key");
            char c = peek();
            if (c == '"') return readBasicString();
            if (c == '\'') return readLiteralString();
            int start = pos;
            while (pos < s.length() && isBareKeyChar(peek())) pos++;
            if (start == pos) throw error("Expected a key but found '" + c + "'");
            return s.substring(start, pos);
        }

        private Object readValue() throws ParseException {
            if (pos >= s.length()) throw error("Expected a value");
            char c = peek();
            if (c == '"') return readBasicString();
            if (c == '\'') return readLiteralString();
            if (c == '[') return readArray();
            if (c == '{') throw error("Inline tables are not supported");
            if (s.startsWith("true", pos) && !isBareKeyChar(charAt(pos + 4))) {
                pos += 4;
                return true;
            }
            if (s.startsWith("false", pos) && !isBareKeyChar(charAt(pos + 5))) {
                pos += 5;
                return false;
            }
            return readNumber();
        }

        private List<Object> readArray() throws ParseException {
            expect('[');
            List<Object> list = new ArrayList<>();
            while (true) {
                skipWhitespaceAndComments(true);
                if (pos >= s.length()) throw error("Unterminated array");
                if (peek() == ']') {
                    pos++;
                    return list;
                }
                list.add(readValue());
                skipWhitespaceAndComments(true);
                if (pos >= s.length()) throw error("Unterminated array");
                if (peek() == ',') {
                    pos++;
                } else if (peek() != ']') {
                    throw error("Expected ',' or ']' in array");
                }
            }
        }

        private Object readNumber() throws ParseException {
            int start = pos;
            while (pos < s.length() && "+-0123456789._eEinfa".indexOf(peek()) >= 0) pos++;
            String raw = s.substring(start, pos).replace("_", "");
            if (raw.isEmpty()) throw error("Expected a value");
            switch (raw) {
                case "inf", "+inf" -> {
                    return Double.POSITIVE_INFINITY;
                }
                case "-inf" -> {
                    return Double.NEGATIVE_INFINITY;
                }
                case "nan", "+nan", "-nan" -> {
                    return Double.NaN;
                }
            }
            try {
                if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
                    return Double.parseDouble(raw);
                }
                long l = Long.parseLong(raw.startsWith("+") ? raw.substring(1) : raw);
                return l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE ? (Object) (int) l : (Object) l;
            } catch (NumberFormatException e) {
                throw error("Invalid value '" + raw + "'");
            }
        }

        private String readBasicString() throws ParseException {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (true) {
                if (pos >= s.length() || peek() == '\n') throw error("Unterminated string");
                char c = s.charAt(pos++);
                if (c == '"') return out.toString();
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                if (pos >= s.length()) throw error("Unterminated string");
                char e = s.charAt(pos++);
                switch (e) {
                    case '"' -> out.append('"');
                    case '\\' -> out.append('\\');
                    case 'n' -> out.append('\n');
                    case 't' -> out.append('\t');
                    case 'r' -> out.append('\r');
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'u', 'U' -> {
                        int len = e == 'u' ? 4 : 8;
                        if (pos + len > s.length()) throw error("Invalid unicode escape");
                        try {
                            out.appendCodePoint(Integer.parseInt(s.substring(pos, pos + len), 16));
                        } catch (IllegalArgumentException ex) {
                            throw error("Invalid unicode escape");
                        }
                        pos += len;
                    }
                    default -> throw error("Invalid escape '\\" + e + "'");
                }
            }
        }

        private String readLiteralString() throws ParseException {
            expect('\'');
            int end = s.indexOf('\'', pos);
            int newline = s.indexOf('\n', pos);
            if (end < 0 || (newline >= 0 && newline < end)) throw error("Unterminated string");
            String value = s.substring(pos, end);
            pos = end + 1;
            return value;
        }

        private void skipWhitespaceAndComments(boolean newlines) {
            while (pos < s.length()) {
                char c = peek();
                if (c == ' ' || c == '\t' || c == '\r') {
                    pos++;
                } else if (c == '\n' && newlines) {
                    pos++;
                    line++;
                } else if (c == '#') {
                    while (pos < s.length() && peek() != '\n') pos++;
                } else {
                    return;
                }
            }
        }

        private void skipInlineWhitespace() {
            while (pos < s.length() && (peek() == ' ' || peek() == '\t')) pos++;
        }

        private void endOfLine() throws ParseException {
            skipWhitespaceAndComments(false);
            if (pos < s.length() && peek() != '\n') throw error("Unexpected '" + peek() + "' after value");
        }

        private void expect(char c) throws ParseException {
            if (pos >= s.length() || peek() != c) throw error("Expected '" + c + "'");
            pos++;
        }

        private char peek() {
            return s.charAt(pos);
        }

        private char charAt(int i) {
            return i < s.length() ? s.charAt(i) : '\0';
        }

        private static boolean isBareKeyChar(char c) {
            return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
        }

        private ParseException error(String message) {
            return new ParseException(message, line);
        }
    }
}
