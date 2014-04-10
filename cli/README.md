Cli Client
==========

Use hurl as a simple curl-like interface to work with endpoints that are protected by this jersey-hmac-auth module.

Usage
-----

First, `mvn package` or `mvn install` this cli module.

```
export PATH=~/work/jersey-hmac-auth/cli:$PATH
hurl --apiKey somekey --secretKey somesecret URL
```


```
hurl -h
usage: hurl [-h] [-X {GET,POST}] --apiKey APIKEY --secretKey SECRETKEY [-v] [--data DATA] url

Like curl, for hmac-protected resources

positional arguments:
  url

optional arguments:
  -h, --help             show this help message and exit
  -X {GET,POST}, --request {GET,POST}
                         GET (default) or POST (default: GET)
  --apiKey APIKEY
  --secretKey SECRETKEY
  -v, --verbose          Prints additional information to stderr (default: false)
  --data DATA, --data-binary DATA
                         The data to use in a POST (or @filename for a file full of data)
```
