# Almhirt - A CQRS Framework for Scala

Warning! This project is still in an early phase! Everything is subject to change!
The API isn't well documented yet. 

## What is Almhirt?

Almhirt aims to be an application framework with a focus on CQRS but it doesn't force one to use the event sourcing part.
What Almhirt forces to do is using commands and events.
It's goal is to help building immutable domain models, avoid exceptional code paths and using a message driven design.

## What's the motivation?

Dive into Scala and build something that might be useful. 

## Is this all new?

No. Inspiration was taken from
* [Towards an immutable domain model by Erik Rozendaal](http://blog.zilverline.com/2011/02/01/towards-an-immutable-domain-model-introduction-part-1/)
* [Building an Event-Sourced Web Application by Martin Krasser](http://krasserm.blogspot.de/2011/11/building-event-sourced-web-application.html)
* [BlueEyes](http://noelwelsh.com/blueeyes/concurrency.html)

## Why this name?

* It starts with an 'a'.
* Nobody else will choose this name so we can skip the toplevel domain in package paths.
* The almhirt.org domain was still available.
* Isn't data like sheep? Almhirt will be a shephard to keep the sheep in their corral.

## Is help welcome?

Absolutely yes!

## What's the license?

Almhirt is available under the [Apache license, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

Copyright 2012,2013 Christian Douven

