#!/bin/bash -e

. /usr/share/debconf/confmodule
. /usr/share/arrowhead/conf/ahconf.sh

do_mysql_conf() {
    BAK="$(dirname "$1")/$(basename "$1").ah_bak"

    if [[ -e "${BAK}" ]]; then
        echo "'${BAK}' exists, please remove it and try again" >&2
        exit 1
    fi

    cp -v "$1" "${BAK}"

    echo "Setting bind-address = 0.0.0.0 in '$1'" >&2
    sed -i 's/^\(bind-address[ \t]*=[ \t]*\).*$/\10.0.0.0/' "$1"
}

MYSQL_CONF_UBUNTU="/etc/mysql/mysql.conf.d/mysqld.cnf"
MARIADB_CONF_PI="/etc/mysql/mariadb.conf.d/50-server.cnf"

if [[ -r "${MYSQL_CONF_UBUNTU}" ]]; then
    do_mysql_conf "${MYSQL_CONF_UBUNTU}"
elif [[ -r "${MARIADB_CONF_PI}" ]]; then
    do_mysql_conf "${MARIADB_CONF_PI}"
fi

if [ $(mysql --defaults-extra-file="${AH_MYSQL_CONF}" -u arrowhead -sse "SELECT EXISTS(SELECT 1 FROM mysql.user WHERE user = 'arrowhead' AND host = '%')") != 1 ]; then
    mysql -e "CREATE USER arrowhead@'%' IDENTIFIED BY '${AH_PASS_DB}';"
    mysql -e "GRANT ALL PRIVILEGES ON arrowhead.* TO arrowhead@'%';"
    mysql -e "FLUSH PRIVILEGES;"
fi
systemctl restart mysql

echo >&2
echo "Use MySQL user 'arrowhead' with password '${AH_PASS_DB}'" >&2
echo >&2
