package com.github.danielfernandez.testspringbootreactivenettyoutput.web.controller;

import com.github.danielfernandez.testspringbootreactivenettyoutput.business.Item;
import com.github.danielfernandez.testspringbootreactivenettyoutput.business.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class MainController {

    private ItemRepository itemRepository = null;


    @Autowired
    public void setItemRepository(final ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    @RequestMapping("/")
    public String index() {
        return "Hello from Netty";
    }


    @RequestMapping("/items/{repetitions}")
    public Flux<Item> items(@PathVariable("repetitions") final int repetitions) {
        return this.itemRepository.findAllItems(Math.max(repetitions, 1));
    }

}
