#!/bin/sh

curl -X POST "http://127.0.0.1:8434/onboarding/plain" -H "accept: application/json" -H "Content-Type:
application/json" -d "{\"name\":\"test client\"}"