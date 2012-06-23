/**
 * Copyright 2012 Ajay Kumar.N.S
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.anadathur.springdemo1.client.util;

import com.anadathur.springdemo1.client.ProductClient;
import com.anadathur.springdemo1.model.Product;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author Ajay Kumar.N.S
 * @since 1.0, 6/22/12, 9:48 PM
 */
public class RestClientProxy implements InvocationHandler{

    private final String serverBaseUrl;
    private final RestTemplate restTemplate;
    private final ConversionService conversionService = new DefaultConversionService();

    public RestClientProxy(String serverBaseUrl, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.serverBaseUrl = serverBaseUrl;
    }

    private String toString(Object obj){
        assertNotNull(obj, "Argument cannnot be null.");
        if (obj instanceof String){
            return (String)obj;
        } else {
            return this.conversionService.convert(obj, String.class);
        }
    }

    private String[] toStringArray(Object obj){
        assertNotNull(obj, "Argument cannnot be null.");
        if(obj.getClass().isArray()){
            Object [] arr = (Object[]) obj;
            String [] result = new String[arr.length];
            for(int i = 0; i < arr.length; ++i){
                result[i] = toString(arr[i]);
            }
            return result;
        } else {
            return new String[]{toString(obj)};
        }
    }

    private boolean isNotEmpty(Object value){
        String [] s = (String[])value;
        return s != null && s.length > 0;
    }

    private void appendPath(StringBuilder bldr, String path){
        if(path.startsWith("/")){
            path = path.substring(1);
        }

        if(bldr.charAt(bldr.length() - 1) == '/'){
            bldr.append(path);
        } else {
            bldr.append('/').append(path);
        }
    }


    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        //first extract parent class's base url.
        RequestMapping rm = AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequestMapping.class);
        StringBuilder url = new StringBuilder(serverBaseUrl);

        if(rm != null && isNotEmpty(rm.value())){
            appendPath(url, rm.value()[0]);
        }

        RequestMapping mapping = AnnotationUtils.getAnnotation(method, RequestMapping.class);
        assertNotNull(mapping, "@RequestMapping annotation not found on method: " + method);

        //next extract method's url.
        if(isNotEmpty(mapping.value())){
            if(mapping.value().length > 1){
                throw new IllegalArgumentException("Only single url mapping supported for clients");
            } else {
                appendPath(url, mapping.value()[0]);
            }
        }

        RequestMethod[] methods = mapping.method();
        Assert.notEmpty(methods, "No http methods specified! Please use 'method' attribute of @RequestMapping");
        Assert.isTrue(methods.length == 1, "Only 1 http method is supported.");

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url.toString());
        Map<String, String> pathParams = new HashMap<String, String>();
        Annotation [][] annotations = method.getParameterAnnotations();
        Object payload = null;

        Map<Class<? extends Annotation>, Annotation> annotationMap = new HashMap<Class<? extends Annotation>, Annotation>();

        for(int i = 0; i < objects.length; ++i){
            Object param = objects[i];

            if (annotations[i] == null || annotations[i].length == 0){
                continue;
            }

            for (Annotation a : annotations[i]) {
                annotationMap.put(a.annotationType(), a);
            }

            if(annotationMap.containsKey(PathVariable.class)){
                PathVariable pathVariable = (PathVariable) annotationMap.get(PathVariable.class);
                pathParams.put(pathVariable.value(), toString(param));
            } else if(annotationMap.containsKey(RequestParam.class)){
                RequestParam reqParam = (RequestParam) annotationMap.get(RequestParam.class);
                if(reqParam.required() && param == null){
                    throw new IllegalArgumentException("QueryParam [" + reqParam.value() + "] is required, but not provided!");
                }

                if(param != null){
                    uriComponentsBuilder.queryParam(reqParam.value(), toStringArray(param));
                }
            } else if(annotationMap.containsKey(RequestBody.class)){
                payload = param;
            }
            annotationMap.clear();
        }

        URI finalURI = uriComponentsBuilder.buildAndExpand(pathParams).toUri();
        ResponseEntity<?> response = null;
        switch(methods[0]){
            case GET:
                response = this.restTemplate.getForEntity(finalURI, method.getReturnType());
                //TODO: log response
                return response.getBody();
            case POST:
                assertNotNull(payload, "No candidate identified as payload. Did you forget to annotate your payload with @RequestBody?");
                if(method.getReturnType() != Void.TYPE){
                    response = this.restTemplate.postForEntity(finalURI, payload, method.getReturnType());
                    //TODO: log response
                    return response.getBody();
                } else {
                    return this.restTemplate.postForObject(finalURI, payload, method.getReturnType());
                }
            case PUT:
                this.restTemplate.put(finalURI, payload);
                break;
            case DELETE:
                this.restTemplate.delete(finalURI);
                break;
            default:
                throw new IllegalStateException("Only GET, POST, DELETE, PUT supported! Not supported = " + methods[0]);
        }
        return null;
    }

    public static <T> T createProxy(String serverBaseUrl, RestTemplate template, Class<T> clientClazz, Class<T> ...clazzes){
        Assert.isTrue(clientClazz != null, "clientClazz parameter cannot be null");
        Class<?> [] proxied = null;
        if(clazzes != null && clazzes.length > 0){
            proxied = new Class<?>[clazzes.length + 1];
            System.arraycopy(clazzes, 0, proxied, 1, clazzes.length);
        } else {
            proxied = new Class<?>[1];
        }

        proxied[0] = clientClazz;
        return (T) Proxy.newProxyInstance(clientClazz.getClassLoader(), proxied,
                new RestClientProxy(serverBaseUrl, template));
    }

    void assertNotNull(Object arg, String msg){
        Assert.isTrue(arg != null, msg);
    }

    public static void main(String[] args) {
        RestTemplate template = new RestTemplate();
        ProductClient productClient = createProxy("http://localhost:8080/test-server/", template, ProductClient.class);
        //System.out.println(productClient.withId(123));
        Random rand = new Random();
        for (int i = 0; i < 10; ++i) {
            Product p = new Product();
            p.setName("apple");
            p.setPrice(rand.nextFloat());
            p.setQuantity(Math.abs(rand.nextInt()));
            p = productClient.createProduct(p);
            System.out.println(p);
        }

        System.out.println(productClient.queryProducts(.75f, .9f, 99999999l));
    }
}
