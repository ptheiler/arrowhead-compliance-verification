#!/bin/sh

curl -X POST "http://127.0.0.1:8434/onboarding/sharedKey" -H "accept: application/json" -H "Content-Type: application/json" -d "{\"name\":\"test with shared key\",\"sharedKey\":\"secret\"}"