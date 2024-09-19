/*
 * Copyright (C) 2016 - 2024 by Stefan Rothe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.kinet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides many helper functions to perform common tasks. The methods of this class all accept null values
 * as parameters.
 */
public final class Util {

    private static final Map<String, String> FILE_NAME_REPLACE_MAP = createFileNameReplaceMap();
    private static final Map<String, String> NAME_REPLACE_MAP = createNameReplaceMap();
    private static final String NAME_ALLOWED_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz.-";
    private static final Collator COLLATOR = initCollator();
    private static final DecimalFormat CURRENCY_FORMAT = initCurrencyFormat();
    private static final String EMAIL_REGEX = "(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private Util() {
    }

    public static enum NullPosition {

        FIRST, LAST
    };

    /**
     * Replaces place holders in a string with the corresponding arguments. The message template may contain place
     * holders having the form <code>{N}</code> where <code>N</code> is a number. This method will replace the
     * placeholder <code>{0}</code> with the first argument after <code>message</code>, <code>{1}</code> with the second
     * argument and so on.
     *
     * @param message message template
     * @param args arguments to be inserted in the message template
     * @return resulting message
     */
    public static String args(String message, Object... args) {
        if (args == null) {
            return message;
        }

        String result = message;
        int i = 0;
        for (Object arg : args) {
            final String key = "{" + i + "}";
            result = result.replace(key, toString(arg, "null"));
            i = i + 1;
        }

        return result;
    }

    public static <T> int compare(Comparable<T> object1, T object2) {
        return compare(object1, object2, NullPosition.FIRST);
    }

    /**
     * Compares two comparable objects safely.
     *
     * @param object1
     * @param object2
     * @param nullPosition
     * @return -1, 1, or 0
     */
    public static <T> int compare(Comparable<T> object1, T object2, NullPosition nullPosition) {
        // checks if both objects are identical (also covers the case that both are null)
        if (object1 == object2) {
            return 0;
        }
        else if (object1 == null) {
            return nullResult(nullPosition);
        }
        else if (object2 == null) {
            return -nullResult(nullPosition);
        }
        else {
            return object1.compareTo(object2);
        }
    }

    public static int compare(String string1, String string2) {
        return compare(string1, string2, NullPosition.FIRST);
    }

    public static int compare(String string1, String string2, NullPosition nullPosition) {
        if (string1 == null && string2 == null) {
            return 0;
        }
        else if (string1 == null) {
            return nullResult(nullPosition);
        }
        else if (string2 == null) {
            return -nullResult(nullPosition);
        }
        else {
            return COLLATOR.compare(string1, string2);
        }
    }

    public static int compare(int int1, int int2) {
        return int1 - int2;
    }

    public static String concat(Iterable<?> elements, String separator) {
        assert elements != null;

        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Object element : elements) {
            if (first) {
                first = false;
            }
            else {
                result.append(separator);
            }
            result.append(element);
        }

        return result.toString();
    }

    public static boolean contains(String a, String b) {
        if (a == null || a.isEmpty() || b == null || b.isEmpty()) {
            return false;
        }
        return a.contains(b);
    }

    public static List<String> createList(String... elements) {
        final List<String> result = new ArrayList<>();
        for (final String element : elements) {
            result.add(element);
        }

        return result;
    }

    public static <T> Set<T> createSet(Stream<T> elements) {
        return elements.collect(Collectors.toSet());
    }

    public static Set<String> createSet(String... elements) {
        final Set<String> result = new HashSet<>();
        for (final String element : elements) {
            result.add(element);
        }

        return result;
    }

    public static boolean endsWith(String s, String suffix) {
        if (s == null) {
            return false;
        }
        else {
            return s.endsWith(suffix);
        }
    }

    public static <T> boolean equal(T a, T b) {
        if (a == null) {
            return b == null;
        }
        else {
            return a.equals(b);
        }
    }

    public static String formatCurrency(double value, String currencyCode) {
        final StringBuilder result = new StringBuilder();
        result.append(currencyCode);
        result.append(' ');
        result.append(CURRENCY_FORMAT.format(value));
        return result.toString();
    }

    public static String formatDayOfWeekLong(DayOfWeek dayOfWeek) {
        return dayOfWeek == null ? null : dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    public static String formatDayOfWeekShort(DayOfWeek dayOfWeek) {
        return dayOfWeek == null ? null : dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }

    public static String formatPhoneInternational(String number) {
        if (number == null) {
            return "";
        }
        else if (number.startsWith("+")) {
            // ok
            return number;
        }
        else if (number.startsWith("00")) {
            // convert country prefix
            return "+" + number.substring(2);
        }
        else if (number.startsWith("0")) {
            // add country prefix
            return "+41 " + number.substring(1);
        }
        else {
            // hmm, what now?
            return number;
        }
    }

    private static StringBuilder formatPhoneUriHelper(String value) {
        if (value == null) {
            return null;
        }

        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); ++i) {
            final char ch = value.charAt(i);
            if (Character.isDigit(ch)) {
                result.append(ch);
            }
        }

        return result;
    }

    public static String formatPhoneUri(String value) {
        return formatPhoneUriHelper(value).toString();
    }

    public static String formatPhoneUriIntl(String value) {
        if (value == null) {
            return null;
        }
        boolean intl = false;
        if (value.startsWith("+")) {
            intl = true;
        }

        final StringBuilder result = formatPhoneUriHelper(value);
        if (intl) {
            result.insert(0, "+");
        }
        else {
            result.deleteCharAt(0);
            result.insert(0, "+41");
        }

        return result.toString();
    }

    public static String hashSHA1(String text) {
        try {
            final MessageDigest sha = MessageDigest.getInstance("SHA-1");
            return hexEncode(sha.digest(text.getBytes()));
        }
        catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }

    public static boolean icontains(String a, String b) {
        return contains(toLower(a), toLower(b));
    }

    public static boolean iequal(String a, String b) {
        return equal(toLower(a), toLower(b));
    }

    public static boolean iStartsWith(String a, String b) {
        return a == null ? false : toLower(a).startsWith(toLower(b));
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static int length(String s) {
        if (s == null) {
            return 0;
        }
        else {
            return s.length();
        }
    }

    public static boolean parseBoolean(String s, boolean defaultValue) {
        s = s.toLowerCase();
        if (equal(s, "true")) {
            return true;
        }

        if (equal(s, "false")) {
            return false;
        }

        return defaultValue;
    }

    public static int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static String replaceAll(String value, Map<String, String> replaceMap) {
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            value = value.replaceAll(entry.getKey(), entry.getValue());
        }

        return value;
    }

    /**
     * Rounds a double value to a multiple of 0.05.
     *
     * @param value
     * @return the value rounded to a multiple of 0.05
     */
    public static double roundCurrency(double value) {
        double result = Math.round(value * 20.0) / 20.0;
        // avoid negative zero
        if (result < 0 && result > -0.025) {
            result = 0.0;
        }

        return result;
    }

    public static String sanitizeFileName(String value) {
        return replaceAll(Util.replaceAll(value, NAME_REPLACE_MAP), FILE_NAME_REPLACE_MAP).replaceAll("\\P{Print}", "");
    }

    /**
     * Sanitizes a string by converting to lower case, and removing accents and special characters. Can be used for
     * generating e-mail addresses and account names. The resulting string will only contain lowercase ASCII letters,
     * digits and the hyphen and point characters.
     *
     * @param text the string to be sanitized
     * @return the sanitized string
     */
    public static String sanitizeName(String text) {
        final StringBuilder result = new StringBuilder();
        text = text.toLowerCase();
        text = Util.replaceAll(text, NAME_REPLACE_MAP);
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (NAME_ALLOWED_CHARS.indexOf(ch) != -1) {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public static String[] split(String string, char separator) {
        final List<String> result = new ArrayList<>();
        int index = string.indexOf(separator);
        while (index != -1) {
            result.add(string.substring(0, index));
            string = string.substring(index + 1);
            index = string.indexOf(separator);
        }

        result.add(string);
        return result.toArray(new String[result.size()]);
    }

    public static boolean startsWith(String s, String suffix) {
        if (s == null) {
            return false;
        }
        else {
            return s.startsWith(suffix);
        }
    }

    /**
     * Checks if two strings are equal. An empty string is considered equal to <code>null</code>.
     *
     * @param a the first string
     * @param b the second string
     * @return <code>true</code> if the two strings are equal, otherwise false
     */
    public static boolean strEqual(String a, String b) {
        if (isEmpty(a)) {
            return isEmpty(b);
        }
        else {
            return a.equals(b);
        }
    }

    public static String toLower(String s) {
        if (s == null) {
            return null;
        }
        else {
            return s.toLowerCase();
        }
    }

    public static String toString(Object object) {
        if (object == null) {
            return null;
        }
        else {
            return object.toString();
        }
    }

    public static String toString(Object object, String nullValue) {
        if (object == null) {
            return nullValue;
        }
        else if (object instanceof Iterable<?>) {
            return "[" + concat((Iterable<?>) object, ", ") + "]";
        }
        else {
            return object.toString();
        }
    }

    public static String trim(String s) {
        if (s == null) {
            return null;
        }
        else {
            return s.trim();
        }
    }

    public static String trimToSize(String value, int size) {
        if (value == null) {
            return null;
        }
        else if (value.length() <= size) {
            return value;
        }
        else {
            final StringBuilder result = new StringBuilder();
            result.append(value.substring(0, size - 3));
            result.append("...");
            return result.toString();
        }
    }

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException ex) {
            return s;
        }
    }

    public static boolean isValidEmailAddress(String email) {
        if (isEmpty(email)) {
            return true;
        }

        return EMAIL_PATTERN.matcher(email).matches();
    }

    private static String hexEncode(byte[] byteArray) {
        final StringBuilder result = new StringBuilder();
        for (byte b : byteArray) {
            result.append(HEX_DIGITS[(b & 0xf0) >> 4]);
            result.append(HEX_DIGITS[b & 0x0f]);
        }

        return result.toString();
    }

    private static Collator initCollator() {
        final Collator original = Collator.getInstance();
        original.setStrength(Collator.TERTIARY);
        String rules = ((RuleBasedCollator) original).getRules();
        try {
            // Fix for sorting with spaces
            // https://stackoverflow.com/a/16567963/8365557
            return new RuleBasedCollator(rules.replaceAll("<'\u005f'", "<' '<'\u005f'"));
        }
        catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static DecimalFormat initCurrencyFormat() {
        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("###,##0.00", symbols);
    }

    private static Map<String, String> createFileNameReplaceMap() {
        final Map<String, String> result = new HashMap<>();
        result.put("/", "");
        result.put("\\\\", "");
        result.put("\\*", "");
        result.put(" ", "_");
        result.put(",", "_");
        return result;
    }

    private static Map<String, String> createNameReplaceMap() {
        HashMap<String, String> result = new HashMap<>();

        result.put("ä", "ae");
        result.put("á", "a");
        result.put("â", "a");
        result.put("à", "a");

        result.put("ë", "e");
        result.put("é", "e");
        result.put("ê", "e");
        result.put("è", "e");

        result.put("ï", "i");
        result.put("í", "i");
        result.put("î", "i");
        result.put("ì", "i");

        result.put("ö", "oe");
        result.put("ó", "o");
        result.put("ô", "o");
        result.put("ò", "o");
        result.put("õ", "o");

        result.put("š", "s");

        result.put("ü", "ue");
        result.put("ú", "u");
        result.put("û", "u");
        result.put("ù", "u");

        result.put("ç", "c");
        result.put("ñ", "n");
        return result;
    }

    private static int nullResult(NullPosition nullPosition) {
        switch (nullPosition) {
            case LAST:
                return 1;
            case FIRST:
            default:
                return -1;
        }
    }
}
