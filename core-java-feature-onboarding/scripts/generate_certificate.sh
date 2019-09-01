#!/usr/bin/env bash

set -e

AH_OPERATOR="aitia"
AH_COMPANY="arrowhead"
AH_COUNTRY="eu"
AH_PASS_CERT="123456"

cloud_name=${1}
name=${2}

dir=$(dirname "$0")
master_store=${dir}/../certificates/master.p12
cloud_store=${dir}/../certificates/${cloud_name}/${cloud_name}.p12

cloud_cn="${cloud_name}.${AH_OPERATOR}.arrowhead.eu"
cn="${name}.${cloud_cn}"
file="./${name}.p12"

echo "Generating ${file}..."

keytool -genkeypair \
    -alias "${cn}" \
    -keyalg RSA \
    -keysize 2048 \
    -dname "CN=${cn}, OU=${AH_OPERATOR}, O=${AH_COMPANY}, C=${AH_COUNTRY}" \
    -validity 3650 \
    -keypass ${AH_PASS_CERT} \
    -keystore "${file}" \
    -storepass ${AH_PASS_CERT} \
    -storetype PKCS12 \
    -ext BasicConstraints:"Subject is a CA\nPath Length Constraint: None"

keytool -export \
    -alias "arrowhead.eu" \
    -storepass ${AH_PASS_CERT} \
    -keystore "${master_store}" \
| keytool -import \
    -trustcacerts \
    -alias "arrowhead.eu" \
    -keystore "${file}" \
    -keypass ${AH_PASS_CERT} \
    -storepass ${AH_PASS_CERT} \
    -storetype PKCS12 \
    -noprompt

keytool -export \
    -alias "${cloud_cn}" \
    -storepass ${AH_PASS_CERT} \
    -keystore ${cloud_store} \
| keytool -import \
    -trustcacerts \
    -alias "${cloud_cn}" \
    -keystore "${file}" \
    -keypass ${AH_PASS_CERT} \
    -storepass ${AH_PASS_CERT} \
    -storetype PKCS12 \
    -noprompt

keytool -certreq \
    -alias "${cn}" \
    -keypass ${AH_PASS_CERT} \
    -keystore ${file} \
    -storepass ${AH_PASS_CERT} \
| keytool -gencert \
    -alias ${cloud_cn} \
    -keypass ${AH_PASS_CERT} \
    -keystore ${cloud_store} \
    -storepass ${AH_PASS_CERT} \
| keytool -importcert \
    -alias ${cn} \
    -keypass ${AH_PASS_CERT} \
    -keystore ${file} \
    -storepass ${AH_PASS_CERT} \
    -noprompt
