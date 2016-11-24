Test repository for Spring Web Reactive + Netty output behaviour
----------------------------------------------------------------

This repository is aimed at testing the differences in output behaviour
between Spring Boot + Spring Web Reactive applications running on Netty and on other supported servers.

The issues to be tested are:

   1. **Netty** might not be sending any results to the browser until the entire output is available,
     which shouldn't happen.
   2. **RxNetty** is not sending results at all when outputting large amounts of data.
   3. There seem to be important differences in performance between *normal* and **SSE** output (SSE is much slower).
   4. **Jetty** is significantly slower than Tomcat and Undertow.



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

## Observed Results (non-SSE)

After starting all the servers, this is the behaviour obtained for each server. Note *intro* is hit
every 5 seconds so that we can better observe the evolution of the data transfer over time.

All `out.{server}` output files have been verified to be equal (for the servers that actually finished executing).


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

## Observed Results (SSE)

After starting all the servers, this is the behaviour obtained for each server. Note *intro* is hit
every 15 seconds so that we can better observe the evolution of the data transfer over time.

All `outsse.{server}` output files have been verified to be equal.

Note in general, all these results are much slower than the equivalent non-SSE ones above.

**Jetty (SSE)**

Finished OK, 4 minutes 8 seconds, started returning data right away. **Slower than non-SSE**.

```
$ curl -H "Accept:text/event-stream" http://localhost:8081/items/10000 > outsse.jetty
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  170k    0  170k    0     0  10967      0 --:--:--  0:00:15 --:--:-- 12063
100  352k    0  352k    0     0  11669      0 --:--:--  0:00:30 --:--:-- 12596
100  535k    0  535k    0     0  11935      0 --:--:--  0:00:45 --:--:-- 12428
100  718k    0  718k    0     0  12070      0 --:--:--  0:01:00 --:--:-- 12541
100  900k    0  900k    0     0  12149      0 --:--:--  0:01:15 --:--:-- 12521
100 1084k    0 1084k    0     0  12210      0 --:--:--  0:01:30 --:--:-- 12500
100 1267k    0 1267k    0     0  12251      0 --:--:--  0:01:45 --:--:-- 12493
100 1450k    0 1450k    0     0  12279      0 --:--:--  0:02:00 --:--:-- 12462
100 1631k    0 1631k    0     0  12289      0 --:--:--  0:02:15 --:--:-- 12473
100 1814k    0 1814k    0     0  12311      0 --:--:--  0:02:30 --:--:-- 12525
100 1996k    0 1996k    0     0  12324      0 --:--:--  0:02:45 --:--:-- 12345
100 2179k    0 2179k    0     0  12337      0 --:--:--  0:03:00 --:--:-- 12553
100 2362k    0 2362k    0     0  12348      0 --:--:--  0:03:15 --:--:-- 12372
100 2545k    0 2545k    0     0  12356      0 --:--:--  0:03:30 --:--:-- 12535
100 2728k    0 2728k    0     0  12365      0 --:--:--  0:03:45 --:--:-- 12449
100 2910k    0 2910k    0     0  12371      0 --:--:--  0:04:00 --:--:-- 12498
100 3007k    0 3007k    0     0  12375      0 --:--:--  0:04:08 --:--:-- 12495
```


**Netty (SSE)**

Finished OK, 2 minutes 26 seconds, started returning data right away (which didn't happen when not using SSE). **Slower than non-SSE**.

```
$ curl -H "Accept:text/event-stream" http://localhost:8082/items/10000 > outsse.netty
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  295k    0  295k    0     0  19403      0 --:--:--  0:00:15 --:--:-- 20722
100  610k    0  610k    0     0  20406      0 --:--:--  0:00:30 --:--:-- 21206
100  922k    0  922k    0     0  20723      0 --:--:--  0:00:45 --:--:-- 21005
100 1235k    0 1235k    0     0  20882      0 --:--:--  0:01:00 --:--:-- 21497
100 1547k    0 1547k    0     0  20963      0 --:--:--  0:01:15 --:--:-- 21231
100 1857k    0 1857k    0     0  20993      0 --:--:--  0:01:30 --:--:-- 21267
100 2165k    0 2165k    0     0  21004      0 --:--:--  0:01:45 --:--:-- 21020
100 2473k    0 2473k    0     0  21000      0 --:--:--  0:02:00 --:--:-- 21081
100 2781k    0 2781k    0     0  21002      0 --:--:--  0:02:15 --:--:-- 20764
100 3007k    0 3007k    0     0  20993      0 --:--:--  0:02:26 --:--:-- 21063
```


**RxNetty (SSE)**

Finished OK (whereas in the non-SSE test it had to be cancelled). 8 minutes 21 seconds, started returning data right away. 

Interestingly, note this time denotes an issue when not in SSE mode, when it had to be cancelled after 7 minutes with no result at all. 

If RxNetty was going to take longer than those 7 minutes to send results but end up finishing OK, given the apparent proportions between non-SSE and SSE tests, this one would have taken much much longer than 8 minutes.

```
$ curl -H "Accept:text/event-stream" http://localhost:8083/items/10000 > outsse.rxnetty
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100 86361    0 86361    0     0   5673      0 --:--:--  0:00:15 --:--:--  6054
100  173k    0  173k    0     0   5893      0 --:--:--  0:00:30 --:--:--  6100
100  263k    0  263k    0     0   5961      0 --:--:--  0:00:45 --:--:--  6177
100  353k    0  353k    0     0   6016      0 --:--:--  0:01:00 --:--:--  6217
100  444k    0  444k    0     0   6050      0 --:--:--  0:01:15 --:--:--  6186
100  534k    0  534k    0     0   6061      0 --:--:--  0:01:30 --:--:--  6036
100  624k    0  624k    0     0   6076      0 --:--:--  0:01:45 --:--:--  6162
100  714k    0  714k    0     0   6088      0 --:--:--  0:02:00 --:--:--  6196
100  805k    0  805k    0     0   6100      0 --:--:--  0:02:15 --:--:--  6187
100  896k    0  896k    0     0   6108      0 --:--:--  0:02:30 --:--:--  6183
100  986k    0  986k    0     0   6114      0 --:--:--  0:02:45 --:--:--  6154
100 1076k    0 1076k    0     0   6119      0 --:--:--  0:03:00 --:--:--  6190
100 1167k    0 1167k    0     0   6123      0 --:--:--  0:03:15 --:--:--  6164
100 1257k    0 1257k    0     0   6127      0 --:--:--  0:03:30 --:--:--  6202
100 1348k    0 1348k    0     0   6131      0 --:--:--  0:03:45 --:--:--  6183
100 1438k    0 1438k    0     0   6133      0 --:--:--  0:04:00 --:--:--  6160
100 1529k    0 1529k    0     0   6137      0 --:--:--  0:04:15 --:--:--  6196
100 1619k    0 1619k    0     0   6138      0 --:--:--  0:04:30 --:--:--  6156
100 1710k    0 1710k    0     0   6139      0 --:--:--  0:04:45 --:--:--  6135
100 1800k    0 1800k    0     0   6140      0 --:--:--  0:05:00 --:--:--  6182
100 1890k    0 1890k    0     0   6140      0 --:--:--  0:05:15 --:--:--  6180
100 1980k    0 1980k    0     0   6140      0 --:--:--  0:05:30 --:--:--  6131
100 2070k    0 2070k    0     0   6141      0 --:--:--  0:05:45 --:--:--  6183
100 2160k    0 2160k    0     0   6141      0 --:--:--  0:06:00 --:--:--  6197
100 2250k    0 2250k    0     0   6141      0 --:--:--  0:06:15 --:--:--  6084
100 2339k    0 2339k    0     0   6139      0 --:--:--  0:06:30 --:--:--  5932
100 2429k    0 2429k    0     0   6139      0 --:--:--  0:06:45 --:--:--  6144
100 2519k    0 2519k    0     0   6139      0 --:--:--  0:07:00 --:--:--  6114
100 2609k    0 2609k    0     0   6139      0 --:--:--  0:07:15 --:--:--  6151
100 2699k    0 2699k    0     0   6140      0 --:--:--  0:07:30 --:--:--  6135
100 2789k    0 2789k    0     0   6140      0 --:--:--  0:07:45 --:--:--  6165
100 2879k    0 2879k    0     0   6140      0 --:--:--  0:08:00 --:--:--  6067
100 2970k    0 2970k    0     0   6141      0 --:--:--  0:08:15 --:--:--  6176
100 3007k    0 3007k    0     0   6141      0 --:--:--  0:08:21 --:--:--  6145
```


**Tomcat (SSE)**

Finished OK, 1 minute 54 seconds, started returning data right away as expected. **Slower than non-SSE**.

```
$ curl -H "Accept:text/event-stream" http://localhost:8084/items/10000 > outsse.tomcat
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  388k    0  388k    0     0  26500      0 --:--:--  0:00:15 --:--:-- 26772
100  781k    0  781k    0     0  26658      0 --:--:--  0:00:30 --:--:-- 26881
100 1173k    0 1173k    0     0  26710      0 --:--:--  0:00:45 --:--:-- 26925
100 1566k    0 1566k    0     0  26731      0 --:--:--  0:01:00 --:--:-- 26783
100 1960k    0 1960k    0     0  26764      0 --:--:--  0:01:15 --:--:-- 26894
100 2352k    0 2352k    0     0  26767      0 --:--:--  0:01:30 --:--:-- 26746
100 2746k    0 2746k    0     0  26782      0 --:--:--  0:01:45 --:--:-- 26888
100 3007k    0 3007k    0     0  26786      0 --:--:--  0:01:54 --:--:-- 26840
```


**Undertow (SSE)**

Finished OK, 1 minute 12 seconds, started returning data right away as expected. **Slower than non-SSE**.

```
$ curl -H "Accept:text/event-stream" http://localhost:8085/items/10000 > outsse.undertow
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  561k    0  561k    0     0  37720      0 --:--:--  0:00:15 --:--:-- 43895
100 1189k    0 1189k    0     0  40290      0 --:--:--  0:00:30 --:--:-- 43712
100 1831k    0 1831k    0     0  41467      0 --:--:--  0:00:45 --:--:-- 43945
100 2473k    0 2473k    0     0  42048      0 --:--:--  0:01:00 --:--:-- 43736
100 3007k    0 3007k    0     0  42345      0 --:--:--  0:01:12 --:--:-- 44045
```
