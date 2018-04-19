package com.wysong.id;

import org.testng.annotations.Test;

import java.math.BigInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class Base62CodecTest {

    @Test(expectedExceptions = NullPointerException.class)
    public void testEncodeNullPointerException() {
        Base62Codec.encode((BigInteger) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testEncodeIllegaArgumentException() {
        Base62Codec.encode(new BigInteger("-1"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDecodeBigIntegerIllegalArgumentException() {
        Base62Codec.decodeBigInteger(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDecodeBigIntegerIllegalArgumentException2() {
        Base62Codec.decodeBigInteger("  ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDecodeBigIntegerIllegalArgumentExceptionBadBase62String() {
        Base62Codec.decodeBigInteger("Not-Base-62");
    }

    @Test
    public void testDecodeBigIntegeString() {
        BigInteger bigInteger = Base62Codec.decodeBigInteger("Base62String");
        assertNotNull(bigInteger);
        assertEquals(bigInteger.compareTo(BigInteger.ZERO), 1);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testEncodeUuidNullPointerException() {
        Base62Codec.encode((java.util.UUID) null);
    }

    @Test
    public void testDecode() {
        Base62Codec.decode("Base62String");
        Base62Codec.decode("4bEt6D4W8NUR1sdlnz7O79");
    }
}