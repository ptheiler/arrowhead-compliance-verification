#!/bin/sh -e

. /usr/share/debconf/confmodule
. /usr/share/arrowhead/conf/ahconf.sh

if [ "$#" -lt 4 ]; then
    echo "Syntax: ${0} OPERATOR CLOUD_NAME HOST AUTH_INFO"
    exit 1
fi

AH_OPERATOR=${1}
CLOUD_NAME=${2}
CLOUD_HOST=${3}
CLOUD_64PUB=${4}

echo "Registering cloud '${CLOUD_NAME}' in database" >&2
mysql --defaults-extra-file="${AH_MYSQL_CONF}" -u arrowhead arrowhead <<EOF
    LOCK TABLES arrowhead_cloud WRITE, table_generator WRITE, neighbor_cloud WRITE;
    INSERT INTO arrowhead_cloud
        (id, address, authentication_info, cloud_name, gatekeeper_service_uri, operator, port, is_secure)
        SELECT
            next_val,
            '${CLOUD_HOST}',
            '${CLOUD_64PUB}',
            '${CLOUD_NAME}',
            'gatekeeper',
            '${AH_OPERATOR}',
            '8447',
            'Y'
            FROM table_generator;
    INSERT INTO neighbor_cloud (cloud_id) SELECT next_val FROM table_generator;
    UPDATE table_generator SET next_val = next_val + 1;
    UNLOCK TABLES;
EOF

echo "Restarting gateway and gatekeeper" >&2
systemctl restart arrowhead-gateway.service arrowhead-gatekeeper.service
