package com.zfoo.protocol.serializer;

import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.registration.ProtocolRegistration;

import java.io.IOException;
import java.util.List;

/**
 * Test workflow after modifying a protocol:
 * 1. Test backward compatibility between old and new Java protocols; NormalObject/ObjectA/ObjectB have compatible fields
 * 2. Test parsing accuracy under bytecode enhancement, CutDown, and reflection
 * 3. Test 5 compatibility scenarios in resource/compatible
 * 4. Steps 1/2/3 produce 2*3*5=30 test cases
 * <p>
 * Test workflow after modifying protocol generation:
 * 1. Test compatibility; NormalObject/ObjectA/ObjectB have compatible fields
 * 2. Test parsing accuracy under normal generation and CutDown
 * 3. Test three generation modes: merge, fold, and default
 * 4. Test 5 compatibility scenarios in resource/compatible
 * 5. Steps 1/2/3/4 produce 2*2*3*5=60 test cases
 * 6. Simple test: enable compat fields in NormalObject/ObjectA; directly test non-CutDown fold
 *
 * @author godotg
 */
public interface ICodeGenerate {

    void init(GenerateOperation generateOperation);

    /**
     * Generate all protocol files under a package into a single file
     */
    void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException;

    /**
     * Fold protocols; generated files mirror the Java source directory structure
     */
    void foldProtocol(List<ProtocolRegistration> registrations) throws IOException;

    /**
     * Default: place all protocol files in the same root directory; no further action
     */
    void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException;

}
