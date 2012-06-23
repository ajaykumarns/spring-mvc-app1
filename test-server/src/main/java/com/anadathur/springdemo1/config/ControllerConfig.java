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
package com.anadathur.springdemo1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author Ajay Kumar.N.S
 * @since 1.0, 6/22/12, 8:21 PM
 */
@Configuration
@EnableWebMvc
@ComponentScan("com.anadathur.springdemo1.controllers")
public class ControllerConfig extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        super.configureMessageConverters(converters);
        converters.add(mappingJacksonHttpMessageConverter());
        converters.add(jaxb2RootElementHttpMessageConverter());
    }

    @Bean
    public MappingJacksonHttpMessageConverter mappingJacksonHttpMessageConverter(){
        return new MappingJacksonHttpMessageConverter();
    }

    @Bean
    public Jaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter(){
        return new Jaxb2RootElementHttpMessageConverter();
    }

}
