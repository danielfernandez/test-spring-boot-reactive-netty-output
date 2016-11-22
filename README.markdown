Test repository for Spring Web Reactive + Netty output behaviour
----------------------------------------------------------------

This repository is aimed at testing the differences in output behaviour (specifically time-to-first-output)
between Spring Boot + Spring Web Reactive applications running on Netty and on other supported servers.

The idea to be tested is that Netty might not be sending any results to the browser until the entire output is
available, which shouldn't happen.


## How to test

Compile and run each of the five Spring Boot applications, which are identical except for the fact that
they use a different server:

```
cd spring-boot-reactive-netty-output-{server}
mvn -U clean compile spring-boot:run
```

All five applications can be run at the same time (in fact, it is the recommended setup for testing). Each
application will listen in a different port:

   * `http://localhost:8081` — Jetty
   * `http://localhost:8082` — Netty
   * `http://localhost:8083` — RxNetty
   * `http://localhost:8084` — Tomcat
   * `http://localhost:8085` — Undertow

All aplications offer a `/items/{repetitions}` URL which returns a JSON array of very simple data items. These
items actually come from a `Flux<Item>` object returned by the controller, and are created at the
`ItemRepository` class from a `List<Item>` and then repeated the amount of times specified at the URL,
by means of the following code:


```java
    public Flux<Item> findAllItems(final int repetitions) {

        /*
         * We don't really have a reactive source for these items, so
         * we will just create a list of 10, and then repeat it many
         * times to make our Flux publish quite a lot of data.
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
```

## Observed Results

After starting all the servers, this is the behaviour obtained for each server. Note *intro* is hit
every 5 seconds so that we can better observe the evolution of the data transfer over time.

All `out.{server}` output files have been verified to be the equal (for the servers that actually finished executing).


**Jetty**

Finished OK, 31 seconds, started returning data right away as expected.

```
$ curl http://localhost:8081/items/10000 > out.jetty
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  400k    0  400k    0     0  73855      0 --:--:--  0:00:05 --:--:-- 76756
100  752k    0  752k    0     0  74798      0 --:--:--  0:00:10 --:--:-- 75892
100 1136k    0 1136k    0     0  76268      0 --:--:--  0:00:15 --:--:-- 79320
100 1520k    0 1520k    0     0  76949      0 --:--:--  0:00:20 --:--:-- 79032
100 1905k    0 1905k    0     0  77142      0 --:--:--  0:00:25 --:--:-- 77921
100 2289k    0 2289k    0     0  77345      0 --:--:--  0:00:30 --:--:-- 78372
100 2421k    0 2421k    0     0  77823      0 --:--:--  0:00:31 --:--:-- 82026
```


**Netty**

Finished OK, 33 seconds, didn't return any data until the last moment **contrary to expected**.

```
$ curl http://localhost:8082/items/10000 > out.netty
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:--  0:00:05 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:00:10 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:00:15 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:00:20 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:00:25 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:00:30 --:--:--     0
100 2421k    0 2421k    0     0  74041      0 --:--:--  0:00:33 --:--:--  579k
```


**RxNetty**

Cancelled after 7 minutes, didn't return any data. Seems to have problems when `repetitions > 100`.

```
$ curl http://localhost:8083/items/10000 > out.rxnetty
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
  0     0    0     0    0     0      0      0 --:--:--  0:00:15 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:00:30 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:00:45 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:01:00 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:01:15 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:01:30 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:01:45 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:02:00 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:02:15 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:02:30 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:02:45 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:03:00 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:03:16 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:03:30 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:03:45 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:04:00 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:04:15 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:04:30 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:04:45 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:05:00 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:05:15 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:05:30 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:05:45 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:06:00 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:06:15 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:06:30 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:06:45 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:07:00 --:--:--     0
  0     0    0     0    0     0      0      0 --:--:--  0:07:03 --:--:--     0
^C (Cancelled, didn't finish)
```


**Tomcat**

Finished OK, 7 seconds, started returning data right away as expected.

```
$ curl http://localhost:8084/items/10000 > out.tomcat
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 1959k    0 1959k    0     0   334k      0 --:--:--  0:00:05 --:--:--  340k
100 2421k    0 2421k    0     0   336k      0 --:--:--  0:00:07 --:--:--  340k
```


**Undertow**

Finished OK, 9 seconds, started returning data right away as expected.

```
$ curl http://localhost:8085/items/10000 > out.undertow
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 1353k    0 1353k    0     0   232k      0 --:--:--  0:00:05 --:--:--  249k
100 2421k    0 2421k    0     0   245k      0 --:--:--  0:00:09 --:--:--  265k
```
