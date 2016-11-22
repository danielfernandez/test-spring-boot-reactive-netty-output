Test repository for output behaviour in Netty
---------------------------------------------

This repository is aimed at testing the differences in output behaviour (specifically time-to-first-output)
between Spring Boot + Spring Web Reactive applications running on Netty and on other supported servers.

The idea to be tested is that Netty might not be sending any results to the browser until the entire output is
available, which shouldn't happen.
