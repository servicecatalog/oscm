/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.vo;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Verifies that VOs do not have cyclic references.
 * 
 * @author hoffmann
 */
public class CyclicReferencesTest {

    @Test
    public void testCyclesInVOs() throws IOException {
        for (final String name : getVOClassNames()) {
            assertNoCycles(name);
        }
    }

    protected List<String> getVOClassNames() throws IOException {
        final URL voUrl = BaseVO.class.getResource("BaseVO.class");
        final File folder = new File(voUrl.getFile()).getParentFile();
        final File[] voClassFiles = folder.listFiles((dir, name) -> name.startsWith("VO") && name.endsWith(".class"));
        final List<String> result = new ArrayList<String>();
        for (final File classfile : voClassFiles) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(classfile);
                final ClassReader reader = new ClassReader(in);
                result.add(Type.getObjectType(reader.getClassName())
                        .getClassName());
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return result;
    }

    protected void assertNoCycles(final String type) {
        final List<String> empty = Collections.emptyList();
        assertNoCycles(type, empty);
    }

    protected void assertNoCycles(final String type, final List<String> path) {
        final int seenBefore = path.indexOf(type);
        if (seenBefore != -1) {
            fail("cyclic reference: " + path.subList(seenBefore, path.size()));
            return;
        }
        final List<String> extendedPath = new ArrayList<String>(path);
        extendedPath.add(type);
        for (final String ref : getReferencedTypes(type)) {
            // Ignore references to Java types:
            if (!ref.startsWith("java")) {
                assertNoCycles(ref, extendedPath);
            }
        }
    }

    private final Map<String, Set<String>> referencedTypesCache = new HashMap<String, Set<String>>();

    /**
     * returns all types directly referenced by the given type and its super
     * classes.
     * 
     * @param type
     * @param result
     */
    protected Set<String> getReferencedTypes(final String type) {
        Set<String> result = referencedTypesCache.get(type);
        if (result == null) {
            result = new HashSet<String>();
            getReferencedTypes(type, result);
            referencedTypesCache.put(type, result);
        }
        return result;
    }

    /**
     * Finds all types directly referenced by the given type and its super
     * classes and adds them to the result set.
     *
     * @param type
     * @param result
     */
    protected void getReferencedTypes(final String type,
            final Set<String> result) {
        final ClassReader reader;
        try {
            reader = new ClassReader(type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public void visit(int version, int access, String name,
                    String signature, String superName, String[] interfaces) {
                if (superName != null) {
                    getReferencedTypes(superName, result);
                }
            }

            @Override
            public FieldVisitor visitField(int access, String name,
                    String desc, String signature, Object value) {
                if ((access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC)) == 0) {
                    if (signature == null) {
                        signature = desc;
                    }
                    getTypesFromSignature(signature, result);
                }
                return null;
            }
        }, 0);
    }

    /**
     * Finds all type references in the given signature and adds them to the
     * result set.
     * 
     * @param signature
     * @param result
     */
    protected void getTypesFromSignature(final String signature,
            final Set<String> result) {
        new SignatureReader(signature).acceptType(new SignatureVisitor(Opcodes.ASM5) {
            @Override
            public void visitClassType(String vmname) {
                final Type type = Type.getObjectType(vmname);
                result.add(type.getClassName());
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return this;
            }

            @Override
            public SignatureVisitor visitSuperclass() {
                return this;
            }

            @Override
            public SignatureVisitor visitReturnType() {
                return this;
            }

            @Override
            public SignatureVisitor visitParameterType() {
                return this;
            }

            @Override
            public SignatureVisitor visitInterfaceBound() {
                return this;
            }

            @Override
            public SignatureVisitor visitInterface() {
                return this;
            }

            @Override
            public SignatureVisitor visitExceptionType() {
                return this;
            }

            @Override
            public SignatureVisitor visitClassBound() {
                return this;
            }

            @Override
            public SignatureVisitor visitArrayType() {
                return this;
            }

            @Override
            public void visitBaseType(char arg0) {
            }

            @Override
            public void visitInnerClassType(String name) {
            }

            @Override
            public void visitTypeArgument() {
            }

            @Override
            public void visitFormalTypeParameter(String arg0) {
            }

            @Override
            public void visitTypeVariable(String name) {
            }

            @Override
            public void visitEnd() {
            }
        });
    }

}
