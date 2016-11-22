package com.github.danielfernandez.testspringbootreactivenettyoutput.business.repository;

import java.util.ArrayList;
import java.util.List;

import com.github.danielfernandez.testspringbootreactivenettyoutput.business.Item;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class ItemRepository {


    public Flux<Item> findAllItems(final int repetitions) {

        /*
         * We don't really have a reactive source for these items, so we will just create a list of 10, and then
         * repeat it many times to make our Flux publish quite a lot of data.
         */

        final List<Item> itemList = new ArrayList<>();
        // Some large things in the Solar System
        itemList.add(new Item(1, "Mercury"));
        itemList.add(new Item(2, "Venus"));
        itemList.add(new Item(3, "Earth"));
        itemList.add(new Item(4, "Mars"));
        itemList.add(new Item(5, "Ceres"));
        itemList.add(new Item(6, "Jupiter"));
        itemList.add(new Item(7, "Saturn"));
        itemList.add(new Item(8, "Uranus"));
        itemList.add(new Item(9, "Neptune"));
        itemList.add(new Item(10, "Pluto"));

        return Flux.fromIterable(itemList).repeat(repetitions);

    }


}
