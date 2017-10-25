/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.vo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Tests for the logic in {@link CyclicReferencesTest}.
 * 
 * @author hoffmann
 */
public class CyclicReferencesTestTest {

    private CyclicReferencesTest cyclicRefsTest;

    @Before
    public void setup() {
        cyclicRefsTest = new CyclicReferencesTest();
    }

    @Test
    public void testGetTypesFromSignature1() throws IOException {
        class Target {
            @SuppressWarnings("unused")
            private int i;
        }
        assertFiledTypes(Target.class);
    }

    @Test
    public void testGetTypesFromSignature2() throws IOException {
        class Target {
            @SuppressWarnings("unused")
            private String s;
        }
        assertFiledTypes(Target.class, "java.lang.String");
    }

    @Test
    public void testGetTypesFromSignature3() throws IOException {
        class Target {
            @SuppressWarnings("unused")
            private Integer[][] arr;
        }
        assertFiledTypes(Target.class, "java.lang.Integer");
    }

    @Test
    public void testGetTypesFromSignature4() throws IOException {
        class Target {
            @SuppressWarnings("unused")
            private List<Integer> list;
        }
        assertFiledTypes(Target.class, "java.util.List", "java.lang.Integer");
    }

    @Test
    public void testGetTypesFromSignature5() throws IOException {
        class Target {
            @SuppressWarnings("unused")
            private Map<String, Integer[]> list;
        }
        assertFiledTypes(Target.class, "java.util.Map", "java.lang.Integer",
                "java.lang.String");
    }

    private void assertFiledTypes(final Class<?> type, String... expected)
            throws IOException {
        final ClassReader reader = new ClassReader(type.getName());
        final Set<String> actual = new HashSet<String>();
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public FieldVisitor visitField(int access, String name,
                    String desc, String signature, Object value) {
                if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
                    if (signature == null) {
                        signature = desc;
                    }
                    cyclicRefsTest.getTypesFromSignature(signature, actual);
                }
                return null;
            }
        }, 0);
        assertEquals(new HashSet<String>(Arrays.asList(expected)), actual);
    }

    @Test
    public void testGetReferencedTypes1() {
        class Target {
            @SuppressWarnings("unused")
            private int i;
        }
        assertReferencedTypes(Target.class);
    }

    @Test
    public void testGetReferencedTypes2() {
        @SuppressWarnings("unused")
        class Target {
            private String s;
            private Integer i;
        }
        assertReferencedTypes(Target.class, "java.lang.String",
                "java.lang.Integer");
    }

    @Test
    public void testGetReferencedTypes3() {
        @SuppressWarnings("unused")
        class SuperTarget {
            private Long id;
        }
        @SuppressWarnings("unused")
        class Target extends SuperTarget {
            private List<Character> list;
        }
        assertReferencedTypes(Target.class, "java.lang.Long",
                "java.lang.Character", "java.util.List");
    }

    private void assertReferencedTypes(final Class<?> type, String... expected) {
        final Set<String> actual = cyclicRefsTest.getReferencedTypes(type
                .getName());
        assertEquals(new HashSet<>(Arrays.asList(expected)), actual);
    }

}
