package com.wysong.id;


import com.wysong.util.StringUtils;

import java.math.BigInteger;
import java.util.UUID;

/**
 * A Base62 Codec to assist with encoding UUIDs into Base62 Strings and decoding Base62 Strings into UUIDs.
 *
 */
public class Base62Codec {
    public static final BigInteger BASE = BigInteger.valueOf(62);
    public static final String DIGITS_AND_LETTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Encodes a number using Base62 encoding.
     *
     * @param  number a positive {@link BigInteger}
     * @return a Base62 string
     * @throws IllegalArgumentException if <code>number</code> is a negative integer.
     * @throws NullPointerException if <code>number</code> is null.
     */
    public static String encode(BigInteger number) {
        if (number == null) {
            throw new NullPointerException("number cannot be null.");
        }
        if (number.compareTo(BigInteger.ZERO) == -1) { // number < 0
            throw new IllegalArgumentException("number must not be negative");
        }
        StringBuilder result = new StringBuilder();
        while (number.compareTo(BigInteger.ZERO) == 1) { // number > 0
            BigInteger[] divmod = number.divideAndRemainder(BASE);
            number = divmod[0];
            int digit = divmod[1].intValue();
            result.insert(0, DIGITS_AND_LETTERS.charAt(digit));
        }
        return (result.length() == 0) ? DIGITS_AND_LETTERS.substring(0, 1) : result.toString();
    }

    /**
     * Decodes a string using Base62 encoding.
     *
     * @param  string a Base62 string
     * @return a positive integer
     * @throws IllegalArgumentException if <code>string</code> is null, empty, or is not a valid Base62 string.
     */
    public static BigInteger decodeBigInteger(final String string) {
        validateBase62String(string);
        BigInteger result = BigInteger.ZERO;
        int digits = string.length();
        for (int index = 0; index < digits; index++) {
            int digit = DIGITS_AND_LETTERS.indexOf(string.charAt(digits - index - 1));
            result = result.add(BigInteger.valueOf(digit).multiply(BASE.pow(index)));
        }
        return result;
    }

    /**
     * Convenience method to ensure the provided string is a proper Base62 string.
     *
     * @param string presumably a Base62 string.
     * @throws IllegalArgumentException if <code>string</code> is null, empty, or is not a valid Base62 string.
     */
    private static void validateBase62String(String string) {
        if (!StringUtils.hasText(string)) {
            throw new IllegalArgumentException("string must not be null or empty");
        }
        for (char c : string.toCharArray()) {
            if (!DIGITS_AND_LETTERS.contains(String.valueOf(c))) {
                throw new IllegalArgumentException("string must be a valid Base62 string.");
            }
        }
    }

    /**
     * Encodes an UUID using Base62 encoding.
     *
     * @param  uuid an immutable universally unique identifier
     * @return a Base62 string
     * @throws NullPointerException if <code>uuid</code> is null.
     */
    public static String encode(UUID uuid) {
        if (uuid == null) {
            throw new NullPointerException("uuid must not be null");
        }
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        //create the byte array of actual uuid.
        byte[] buffer = new byte[16];
        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (msb >>> 8 * (7 - i));
        }
        for (int i = 8; i < 16; i++) {
            buffer[i] = (byte) (lsb >>> 8 * (7 - i));
        }
        //pad the byte array with leading zero to ensure positive numbers due to BigInteger's two's complement representation.
        byte[] positive = new byte[17];
        byte[] zeroBit = new byte[]{0};

        System.arraycopy(zeroBit, 0, positive, 0, 1);
        System.arraycopy(buffer, 0, positive, 1, buffer.length);
        return encode(new BigInteger(positive));
    }

    /**
     * Decodes a string using Base62 encoding.
     *
     * @param  string a Base62 string
     * @return a positive integer
     * @throws IllegalArgumentException if <code>string</code> is null, empty, or an invalid Base62 string.
     */
    public static UUID decode(final String string) {
        //validation of string occurs in decodeBigInteger
        BigInteger bigInteger = decodeBigInteger(string);
        //since we encoded the byte array with padding, we have to remove that smut when decoding.
        byte[] paddedBytes = bigInteger.toByteArray();
        byte[] actualBytes = new byte[16];
        if( paddedBytes.length < 17) {
            int difference = 16 - paddedBytes.length;
            byte[] zeroBit = new byte[]{0};
            for (int i = 0; i < difference; i++) {
                System.arraycopy(zeroBit, 0, actualBytes, i, 1);
            }
            System.arraycopy(paddedBytes, 0, actualBytes, difference, paddedBytes.length);
        } else {
            System.arraycopy(paddedBytes, 1, actualBytes, 0, actualBytes.length);
        }
        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++) {
            msb = (msb << 8) | (actualBytes[i] & 0xff);
        }
        for (int i=8; i<16; i++) {
            lsb = (lsb << 8) | (actualBytes[i] & 0xff);
        }

        return new UUID(msb, lsb);
    }
}
