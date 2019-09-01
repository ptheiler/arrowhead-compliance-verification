#!/bin/sh

AH_CONF_DIR="/etc/arrowhead"
AH_CLOUDS_DIR="${AH_CONF_DIR}/clouds"
AH_SYSTEMS_DIR="${AH_CONF_DIR}/systems"
AH_MYSQL_CONF="${AH_CONF_DIR}/mysql.cnf"

db_get arrowhead-core-common/mysql_password; AH_PASS_DB=$RET
db_get arrowhead-core-common/cert_password; AH_PASS_CERT=$RET
db_get arrowhead-core-common/cloudname; AH_CLOUD_NAME=$RET
db_get arrowhead-core-common/operator; AH_OPERATOR=$RET
db_get arrowhead-core-common/company; AH_COMPANY=$RET
db_get arrowhead-core-common/country; AH_COUNTRY=$RET

ah_cert () {
    dst_path=${1}
    dst_name=${2}
    cn=${3}

    file="${dst_path}/${dst_name}.p12"

    # The command has been renamed in newer versions of keytool
    gen_cmd="-genkeypair"
    keytool ${gen_cmd} --help >/dev/null 2>&1 || gen_cmd='-genkey'

    if [ ! -f "${file}" ]; then
        keytool ${gen_cmd} \
            -alias ${dst_name} \
            -keyalg RSA \
            -keysize 2048 \
            -dname "CN=${cn}, OU=${AH_OPERATOR}, O=${AH_COMPANY}, C=${AH_COUNTRY}" \
            -validity 3650 \
            -keypass ${AH_PASS_CERT} \
            -keystore ${file} \
            -storepass ${AH_PASS_CERT} \
            -storetype PKCS12 \
            -ext BasicConstraints:"Subject is a CA\nPath Length Constraint: None"

        chown :arrowhead ${file}
        chmod 640 ${file}
    fi
}

ah_cert_export () {
    src_path=${1}
    dst_name=${2}
    dst_path=${3}

    src_file="${src_path}/${dst_name}.p12"
    dst_file="${dst_path}/${dst_name}.crt"

    if [ ! -f "${dst_file}" ]; then
        keytool -exportcert \
            -rfc \
            -alias ${dst_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | openssl x509 \
            -out ${dst_file}

        chown :arrowhead ${dst_file}
        chmod 640 ${dst_file}
    fi
}

ah_cert_export_pub () {
    src_path=${1}
    dst_name=${2}
    dst_path=${3}

    src_file="${src_path}/${dst_name}.p12"
    dst_file="${dst_path}/${dst_name}.pub"

    if [ ! -f "${dst_file}" ]; then
        keytool -exportcert \
            -rfc \
            -alias ${dst_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | openssl x509 \
            -out ${dst_file} \
            -noout \
            -pubkey

        chown :arrowhead ${dst_file}
        chmod 640 ${dst_file}
    fi
}

ah_cert_import () {
    src_path=${1}
    src_name=${2}
    dst_path=${3}
    dst_name=${4}

    src_file="${src_path}/${src_name}.crt"
    dst_file="${dst_path}/${dst_name}.p12"

    keytool -import \
        -trustcacerts \
        -file ${src_file} \
        -alias ${src_name} \
        -keystore ${dst_file} \
        -keypass ${AH_PASS_CERT} \
        -storepass ${AH_PASS_CERT} \
        -storetype PKCS12 \
        -noprompt
}

ah_cert_signed () {
    dst_path=${1}
    dst_name=${2}
    cn=${3}
    src_path=${4}
    src_name=${5}

    src_file="${src_path}/${src_name}.p12"
    dst_file="${dst_path}/${dst_name}.p12"
    
    if [ ! -f "${dst_file}" ]; then
        ah_cert ${dst_path} ${dst_name} ${cn}

        keytool -export \
            -alias ${src_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | keytool -import \
            -trustcacerts \
            -alias ${src_name} \
            -keystore ${dst_file} \
            -keypass ${AH_PASS_CERT} \
            -storepass ${AH_PASS_CERT} \
            -storetype PKCS12 \
            -noprompt

        keytool -certreq \
            -alias ${dst_name} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${dst_file} \
            -storepass ${AH_PASS_CERT} \
        | keytool -gencert \
            -alias ${src_name} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${src_file} \
            -storepass ${AH_PASS_CERT} \
        | keytool -importcert \
            -alias ${dst_name} \
            -keypass ${AH_PASS_CERT} \
            -keystore ${dst_file} \
            -storepass ${AH_PASS_CERT} \
            -noprompt
    fi
}

ah_cert_signed_system () {
    name=${1}

    path="${AH_SYSTEMS_DIR}/${name}"
    file="${path}/${name}.p12"

    if [ ! -f "${file}" ]; then
        ah_cert_signed \
            "${path}" \
            ${name} \
            "${name}.${AH_CLOUD_NAME}.${AH_OPERATOR}.arrowhead.eu" \
            ${AH_CLOUDS_DIR} \
            ${AH_CLOUD_NAME}

        ah_cert_import "${AH_CONF_DIR}" "master" "${path}" ${name}
    fi
}

ah_cert_trust () {
    dst_path=${1}
    src_path=${2}
    src_name=${3}

    src_file="${src_path}/${src_name}.p12"
    dst_file="${dst_path}/truststore.p12"
    
    if [ ! -f "${dst_file}" ]; then
        keytool -export \
            -alias ${src_name} \
            -storepass ${AH_PASS_CERT} \
            -keystore ${src_file} \
        | keytool -import \
            -trustcacerts \
            -alias ${src_name} \
            -keystore ${dst_file} \
            -keypass ${AH_PASS_CERT} \
            -storepass ${AH_PASS_CERT} \
            -storetype PKCS12 \
            -noprompt

        chown :arrowhead ${dst_file}
        chmod 640 ${dst_file}
    fi
}

ah_db_user () {
    if [ ! -f "${AH_MYSQL_CONF}" ]; then
        touch "${AH_MYSQL_CONF}"
        chmod 0600 "${AH_MYSQL_CONF}"
        cat >"${AH_MYSQL_CONF}" <<EOF
[client]
password="${AH_PASS_DB}"
EOF
    fi

    if ! mysql --defaults-extra-file="${AH_MYSQL_CONF}" -u arrowhead -e "SHOW DATABASES" >/dev/null 2>/dev/null; then
        if mysql -u root -e "SHOW DATABASES" >/dev/null 2>/dev/null; then
            mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS arrowhead;
CREATE USER IF NOT EXISTS arrowhead@localhost IDENTIFIED BY '${AH_PASS_DB}';
GRANT ALL PRIVILEGES ON arrowhead.* TO arrowhead@'localhost';
FLUSH PRIVILEGES;
EOF
        else
            db_input critical arrowhead-core-common/mysql_password_root || true
            db_go || true
            db_get arrowhead-core-common/mysql_password_root; AH_MYSQL_ROOT=$RET
            db_unregister arrowhead-core-common/mysql_password_root

            OPT_FILE="$(mktemp -q --tmpdir "arrowhead-core-common.XXXXXX")"
            trap 'rm -f "${OPT_FILE}"' EXIT
            chmod 0600 "${OPT_FILE}"

            cat >"${OPT_FILE}" <<EOF
[client]
password="${AH_MYSQL_ROOT}"
EOF

            mysql --defaults-extra-file="${OPT_FILE}" -u root <<EOF
CREATE DATABASE IF NOT EXISTS arrowhead;
CREATE USER IF NOT EXISTS arrowhead@localhost IDENTIFIED BY '${AH_PASS_DB}';
GRANT ALL PRIVILEGES ON arrowhead.* TO arrowhead@'localhost';
FLUSH PRIVILEGES;
EOF
        fi
    fi
}

#TODO modify the method slightly so it appends the log4j properties to an already existing default.conf
ah_log4j_conf () {
    system_name=${1}

    file="${AH_SYSTEMS_DIR}/${system_name}/log4j.properties"
    
    if [ ! -f "${file}" ]; then
        /bin/cat <<EOF >${file}
# Define the root logger with appender file
log4j.rootLogger=INFO, DB, FILE

# Database related config
# Define the DB appender
log4j.appender.DB=org.apache.log4j.jdbc.JDBCAppender
# Set Database URL
log4j.appender.DB.URL=jdbc:mysql://127.0.0.1:3306/arrowhead
# Set database user name and password
log4j.appender.DB.user=arrowhead
log4j.appender.DB.password=${AH_PASS_DB}
# Set the SQL statement to be executed.
log4j.appender.DB.sql=INSERT INTO logs(id, date, origin, level, message) VALUES(DEFAULT,'%d{yyyy-MM-dd HH:mm:ss}','%C','%p','%m')
# Define the layout for file appender
log4j.appender.DB.layout=org.apache.log4j.PatternLayout
# Disable Hibernate verbose logging
log4j.logger.org.hibernate=fatal

# File related config
# Define the file appender
log4j.appender.FILE=org.apache.log4j.FileAppender
# Set the name of the file
log4j.appender.FILE.File=/var/log/arrowhead/${system_name}.log
# Set the immediate flush to true (default)
log4j.appender.FILE.ImmediateFlush=true
# Set the threshold to debug mode
log4j.appender.FILE.Threshold=debug
# Set the append to false, overwrite
log4j.appender.FILE.Append=false
# Define the layout for file appender
log4j.appender.FILE.layout=org.apache.log4j.PatternLayout
log4j.appender.FILE.layout.conversionPattern=%d{yyyy-MM-dd HH:mm:ss}, %C, %p, %m%n
EOF
        chown root:arrowhead ${file}
        chmod 640 ${file}
    fi
}
