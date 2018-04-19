package com.wysong.id;


import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;

public class Base62IdGeneratorTest {

    @Test
    public void testGenerateIdUniqueness() {
        final int ITERATIONS = 50000;
        Base62IdGenerator base62IdGenerator = new Base62IdGenerator();
        List<String> generatedIds = new ArrayList<>();
        for (int i = 0; i < ITERATIONS; i++) {
            generatedIds.add(base62IdGenerator.generateId());
        }
        Set<String> comeToJesusSet = new HashSet<>();
        comeToJesusSet.addAll(generatedIds);
        assertEquals(comeToJesusSet.size(), generatedIds.size());
    }

    @Test
    public void testGenerate() {
        Base62IdGenerator base62IdGenerator = new Base62IdGenerator();
        String uuid = base62IdGenerator.generateId();
        assertNotNull(uuid);
        assertTrue(22 >= uuid.length());
    }

    @Test
    public void testGetId() {
        String uuid = Base62IdGenerator.getId();
        assertNotNull(uuid);
        assertTrue(22 >= uuid.length());
    }
}
