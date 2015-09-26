#!/bin/sh

# TODO parameterise gdate / date
timestamp=$(gdate -u +"%Y-%m-%dT%H:%M:%SZ")
private_key="fred-secret-key"
api_key="fred-api-key"
#api_key="bad-api-key"

request="/jersey-hmac-auth-sample2/v1/pizza?apiKey=${api_key}"

curl -v -X POST --header "X-Auth-Version: 1" --header "X-Auth-Timestamp: $timestamp" --header "X-Auth-Signature: signature" "http://localhost:8080${request}"
