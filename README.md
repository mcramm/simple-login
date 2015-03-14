# simple-login

A Leiningen template to quickly get set up with a login/registration page. It
also sets up some basic storage options (in-memory and postgres).

The page has no styling applied to it and is pretty bare-bones.

# Installation

This project isn't on clojars yet, so you'll need to clone down this repo and
install it locally with Leiningen.

From this projects root, run `lein install` then follow the usage steps below

## Usage

*NOTE*: Use at your own risk. This template should be a good bare-bones place to
get started, but I would evaluate it before using it in production.

`lein new simple-login <your-project-name>`

# Libraries Used

* [Hiccup](https://github.com/weavejester/hiccup) for the views
* [Crypto-password](https://github.com/weavejester/crypto-password) for password encryption
* [Buddy](https://github.com/funcool/buddy) for authentication and authorization
* [Compojure](https://github.com/weavejester/compojure)
* [JDBC](https://github.com/clojure/java.jdbc), postgresql and [c3p0](http://clojure-doc.org/articles/ecosystem/java_jdbc/connection_pooling.html) for Postgres storage, connection pooling
* [Environ](https://github.com/weavejester/environ) for environment config
* [Timbe](https://github.com/ptaoussanis/timbre) for logging

## Contributing

Pull Requests and feedback are welcome. I threw this together in a few hours, so
I'm missing some key things (like tests :scream:), and the code could use some
refactoring.

## License

Copyright © 2015 G. Micheal Cramm

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
