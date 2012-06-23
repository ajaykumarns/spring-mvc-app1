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
*/
package com.anadathur.springdemo1.client;

import com.anadathur.springdemo1.model.Product;
import com.anadathur.springdemo1.model.ProductList;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Ajay Kumar.N.S
 * @since 1.0, 6/22/12, 9:46 PM
 */
@RequestMapping("/api/product/")
public interface ProductClient {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Product withId(@PathVariable("id") int id);

    @RequestMapping(method = RequestMethod.POST)
    public Product createProduct(@RequestBody Product product);

    @RequestMapping(method = RequestMethod.PUT)
    public Product updateProduct(@RequestBody Product product);

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteProduct(@PathVariable("id") int id);

    @RequestMapping(value = "/query", method = GET)
    @ResponseBody
    public ProductList queryProducts(
            @RequestParam(value = "beginPrice", required = true) float beginPrice,
            @RequestParam(value = "endPrice", required = true) float endPrice,
            @RequestParam(value = "minQuantity", required = false) Long quantity
    );
}