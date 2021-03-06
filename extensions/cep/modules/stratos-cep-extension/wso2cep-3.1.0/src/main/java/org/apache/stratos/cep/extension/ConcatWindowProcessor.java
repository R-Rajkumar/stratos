/*
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 */
package org.apache.stratos.cep.extension;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

@SiddhiExtension(namespace = "stratos", function = "concat")
public class ConcatWindowProcessor extends FunctionExecutor {
    Attribute.Type returnType = Attribute.Type.STRING;
    @Override
    public void init(Attribute.Type[] types, SiddhiContext siddhiContext) {
    }

    @Override
    protected Object process(Object obj) {
        if (obj instanceof Object[]) {
            StringBuffer sb=new StringBuffer();
            for (Object aObj : (Object[]) obj) {
                sb.append(aObj);
            }
            return sb.toString();
        } else {
            return obj.toString();
        }

    }

    @Override
    public void destroy() {
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }
}
