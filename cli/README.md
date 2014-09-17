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

configuration:

  hurl can be configured to use the auth parms in multiple ways:	
    * environment variables HURL_APIKEY & HURL_SECRET 
    * file ~/.hurl/default.conf containing these two params
        apiKey <api key>
  	secret <secret key>
    * runtime arguments (see optional arguments below)

positional arguments:
  url

optional arguments:
  --configure		 configure api key & secret key for current session
  -h, --help             show this help message and exit
  -X {GET,POST}, --request {GET,POST}
                         GET (default) or POST (default: GET)
  --apiKey APIKEY
  --secretKey SECRETKEY
  -v, --verbose          Prints additional information to stderr (default: false)
  --data DATA, --data-binary DATA
                         The data to use in a POST (or @filename for a file full of data)
```
