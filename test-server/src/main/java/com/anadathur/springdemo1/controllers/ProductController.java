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
package com.anadathur.springdemo1.controllers;

import com.anadathur.springdemo1.model.Product;
import com.anadathur.springdemo1.model.ProductList;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ajay Kumar.N.S
 * @since 1.0, 6/22/12, 8:19 PM
 */
@Controller
@RequestMapping("/api/product/")
public class ProductController {
    private final ConcurrentHashMap<Integer, Product> productMap = new ConcurrentHashMap<Integer, Product>();
    private final AtomicInteger atomicInteger = new AtomicInteger();

    @RequestMapping(value="/{id}", method = GET)
    @ResponseBody
    public Product withId(@PathVariable("id") int id){
        return this.productMap.get(id);
    }

    @RequestMapping(value = "/", method = POST)
    @ResponseBody
    public Product createProduct(@RequestBody Product product){
        product.setId(atomicInteger.incrementAndGet());
        this.productMap.put(product.getId(), product);
        return product;
    }

    @RequestMapping(method = PUT)
    public void updateProduct(@RequestBody Product product){
        this.productMap.replace(product.getId(), product);
    }

    @RequestMapping(value="/{id}", method = DELETE)
    public void deleteProduct(@PathVariable("id") int id){
        this.productMap.remove(id);
    }

    @RequestMapping(value = "/query", method = GET)
    @ResponseBody
    public ProductList queryProducts(
        @RequestParam(value = "beginPrice", required = true) float beginPrice,
        @RequestParam(value = "endPrice", required = true) float endPrice,
        @RequestParam(value = "minQuantity", required = false) Long quantity
    ){
        List<Product> result = new ArrayList<Product>();
        for(Product p : productMap.values()){
            if(p.getPrice() > beginPrice && p.getPrice() < endPrice){
                result.add(p);
            }
        }

        if (quantity != null) {
            for (Iterator<Product> iter = result.iterator(); iter.hasNext(); ) {
                Product p = iter.next();
                if(p.getQuantity() < quantity){
                    iter.remove();
                }
            }
        }
        return new ProductList(result);
    }

}
