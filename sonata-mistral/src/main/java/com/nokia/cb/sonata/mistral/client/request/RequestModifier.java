/*
 * (c) 2017 Nokia Proprietary
 *
 * This software is licensed under the Apache 2 license, quoted below.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.nokia.cb.sonata.mistral.client.request;

import com.nokia.cb.sonata.mistral.client.exception.ServiceClientHttpException;
import com.nokia.cb.sonata.mistral.client.request.filters.ServiceAuthFilter;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by smendel on 2017.
 */
public class RequestModifier implements AutoCloseable{


    private static ThreadLocal<MultivaluedMap<String, Object>> headers = new ThreadLocal<MultivaluedMap<String, Object>>() {
        @Override
        protected MultivaluedMap<String, Object> initialValue() {
            return new MultivaluedHashMap<>();
        }
    };

    public RequestModifier() {
    }

    /**
     * @return All headers (or an empty map if no headers were added)
     */
    public static MultivaluedMap<String, Object> getHeaders() {
        return headers.get();
    }

    /**
     * Gets the first header value that matches the header name
     *
     * @param name The name of the header
     * @return Returns the value or null if no such header exist
     */
    public static Object getFirstHeader(String name) {
        return headers.get().getFirst(name);
    }

    /**
     * Gets the values of header named <code>name</code>
     *
     * @param name The name of the header
     * @return Returns the values or null if no such header exist
     */
    public static List<Object> getHeaders(String name) {
        return headers.get().get(name);
    }

    public RequestModifier addHeader(String name, Object value) {
        MultivaluedMap<String, Object> multivaluedMap = RequestModifier.headers.get();
        multivaluedMap.add(name, value);
        return this;
    }

    private RequestModifier addHeaderSingleValue(String name, Object value) {
        MultivaluedMap<String, Object> multivaluedMap = RequestModifier.headers.get();
        Object existingValue = multivaluedMap.getFirst(name);
        if (null == existingValue) {
            multivaluedMap.putSingle(name, value);
        } else {
            if (!existingValue.equals(value)) {
                throw new ServiceClientHttpException("Auth token mismatch.", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        }
        return this;
    }

    public RequestModifier addAuthorizationHeader(String accessToken) {
        return addHeaderSingleValue(ServiceAuthFilter.AUTHORIZATION, ServiceAuthFilter.BEARER_AUTH_TOKEN_TYPE_PREFIX + accessToken);
    }

    @Override
    public void close() {
        // Clear all headers from the thread
        // so they will not be used in future requests
        RequestModifier.headers.get().clear();
    }
}