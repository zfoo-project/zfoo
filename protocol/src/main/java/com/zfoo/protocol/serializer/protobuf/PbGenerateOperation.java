/*
 * Copyright 2021 The edap Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.serializer.protobuf;

public class PbGenerateOperation {

    private String protoPath;
    private String outputPath;
    private String javaPackage;
    /**
     * Whether generated class is record
     */
    private boolean recordClass;
    /**
     * All protocol files are generated in a single protocol file.
     */
    private boolean oneProtocol;

    public String getProtoPath() {
        return protoPath;
    }

    public void setProtoPath(String protoPath) {
        this.protoPath = protoPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getJavaPackage() {
        return javaPackage;
    }

    public void setJavaPackage(String javaPackage) {
        this.javaPackage = javaPackage;
    }

    public boolean isRecordClass() {
        return recordClass;
    }

    public void setRecordClass(boolean recordClass) {
        this.recordClass = recordClass;
    }

    public boolean isOneProtocol() {
        return oneProtocol;
    }

    public void setOneProtocol(boolean oneProtocol) {
        this.oneProtocol = oneProtocol;
    }
}
