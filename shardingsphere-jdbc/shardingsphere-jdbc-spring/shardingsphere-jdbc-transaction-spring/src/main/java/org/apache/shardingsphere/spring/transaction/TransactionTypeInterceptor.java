/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.spring.transaction;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shardingsphere.transaction.annotation.ShardingSphereTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Transaction type interceptor.
 */
public final class TransactionTypeInterceptor implements MethodInterceptor {
    
    @Override
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
        ShardingSphereTransactionType shardingSphereTransactionType = getAnnotation(methodInvocation);
        Objects.requireNonNull(shardingSphereTransactionType, "could not found transaction type annotation");
        TransactionType preTransactionType = TransactionTypeHolder.get();
        TransactionTypeHolder.set(shardingSphereTransactionType.value());
        try {
            return methodInvocation.proceed();
        } finally {
            TransactionTypeHolder.clear();
            if (null != preTransactionType) {
                TransactionTypeHolder.set(preTransactionType);
            }
        }
    }
    
    private ShardingSphereTransactionType getAnnotation(final MethodInvocation invocation) {
        Objects.requireNonNull(invocation.getThis());
        Class<?> targetClass = AopUtils.getTargetClass(invocation.getThis());
        ShardingSphereTransactionType result = getMethodAnnotation(invocation, targetClass);
        return null != result ? result : targetClass.getAnnotation(ShardingSphereTransactionType.class);
    }
    
    private ShardingSphereTransactionType getMethodAnnotation(final MethodInvocation invocation, final Class<?> targetClass) {
        Method specificMethod = ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
        Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        return userDeclaredMethod.getAnnotation(ShardingSphereTransactionType.class);
    }
}
