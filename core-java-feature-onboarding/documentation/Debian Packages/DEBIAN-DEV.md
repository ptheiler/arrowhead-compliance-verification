# Guide to Debian Packages

## Useful resources:

Debian New Maintainers' Guide: https://www.debian.org/doc/manuals/maint-guide/index.en.html

How to use jdeb with Maven: https://github.com/tcurdt/jdeb/blob/master/docs/maven.md

The Debconf Programmer's Tutorial: http://www.fifi.org/doc/debconf-doc/tutorial.html

**The following changes and additions are required to add Debian/Ubuntu package generation to an Arrowhead core system.**

### File: pom.xml

- Set `artifactId` to `arrowhead-SYSTEMNAME`, for example: `arrowhead-authorization`. If multiple versions of the same
core system exist add a postfix, like `arrowhead-serviceregistry-sql`.

- Under `<plugins>` add:

```xml
<plugin>
<artifactId>jdeb</artifactId>
<groupId>org.vafer</groupId>
<version>1.7</version>
<executions>
  <execution>
    <phase>package</phase>
    <goals>
      <goal>jdeb</goal>
    </goals>
    <configuration>
      <snapshotExpand>true</snapshotExpand>
      <snapshotTemplate>[YYMMddHHmm].${git.commit.id.abbrev}</snapshotTemplate>
      <deb>target/${project.artifactId}_${revision}.deb</deb>
      <verbose>true</verbose>
      <controlDir>${basedir}/src/deb/control</controlDir>
      <dataSet>

        <data>
          <type>file</type>
          <src>${project.build.directory}/${project.build.finalName}.jar</src>
          <mapper>
            <type>perm</type>
            <prefix>/usr/share/arrowhead</prefix>
            <user>arrowhead</user>
            <filemode>755</filemode>
          </mapper>
        </data>
        
        <data>
          <type>link</type>
          <linkName>/usr/share/arrowhead/${project.artifactId}.jar</linkName>
          <linkTarget>/usr/share/arrowhead/${project.build.finalName}.jar</linkTarget>
        </data>

      </dataSet>
    </configuration>
  </execution>
</executions>
</plugin>
```

- Additional data sections should be added for files to be installed with the core systems. Examples:

The service definition file, used to start and stop the daemon (should most likely always be there):    
```xml
<data>
  <type>file</type>
  <src>${project.basedir}/src/deb/arrowhead-authorization.service</src>
  <mapper>
    <type>perm</type>
    <prefix>/etc/systemd/system</prefix>
    <filemode>664</filemode>
  </mapper>
</data>
```

Additional libraries specific to a core system (not found in the `pom.xml` of the common module):
```xml
<data>
  <type>file</type>
  <src>${project.build.directory}/lib/bcprov-jdk15on-${bouncy.castle.version}.jar</src>
  <mapper>
    <type>perm</type>
    <prefix>/usr/share/arrowhead/lib</prefix>
  </mapper>
</data>
```

### File: src/deb/SYSTEM-NAME.service 

Service definition file, used to start/stop the daemon. Also contains a list of dependencies, which should be started
before this system.

```
[Unit]
Description=arrowhead-orchestrator
After=network.target mysql.target arrowhead-serviceregistry-sql.service
Requires=arrowhead-serviceregistry-sql.service arrowhead-authorization.service arrowhead-gatekeeper.service

[Service]
WorkingDirectory=/etc/arrowhead/systems/orchestrator
ExecStart=/usr/bin/java -jar /usr/share/arrowhead/arrowhead-orchestrator-4.1.1-SNAPSHOT.jar -d -daemon -tls
TimeoutStopSec=5
Type=simple
User=arrowhead
Group=arrowhead

[Install]
WantedBy=default.target
```

### File: src/deb/control 

Description file for the package manager. Contains an description of the system, the maintainer, and which packages
should be installed before this one. In general, all should depend on arrowhead-core-common.

```
Package: [[name]]
Version: [[version]]
Section: contrib/java
Priority: optional
Architecture: all
Maintainer: Thomas Pedersen <tp@cs.aau.dk>
Homepage: http://www.arrowhead.eu
Description: Arrowhead Gatekeeper System
Distribution: development
Depends: java-runtime-headless, virtual-mysql-server, arrowhead-core-common, arrowhead-serviceregistry-sql, arrowhead-authorization, arrowhead-gateway
```

### File: src/deb/postinst

The general structure of the post-installation script:

```bash
#!/bin/sh
# postinst script for arrowhead-authorization
#
# see: dh_installdeb(1)

set -e

. /usr/share/debconf/confmodule

SYSTEM_NAME="authorization"
PKG_NAME="arrowhead-authorization"

# summary of how this script can be called:
#        * <postinst> `configure' <most-recently-configured-version>
#        * <old-postinst> `abort-upgrade' <new version>
#        * <conflictor's-postinst> `abort-remove' `in-favour' <package>
#          <new-version>
#        * <postinst> `abort-remove'
#        * <deconfigured's-postinst> `abort-deconfigure' `in-favour'
#          <failed-install-package> <version> `removing'
#          <conflicting-package> <version>
# for details, see https://www.debian.org/doc/debian-policy/ or
# the debian-policy package

case "$1" in
    configure)
        . /usr/share/arrowhead/conf/ahconf.sh
        SYSTEM_DIR="${AH_SYSTEMS_DIR}/${SYSTEM_NAME}"
    ;;

    abort-upgrade|abort-remove|abort-deconfigure)
    ;;

    *)
        echo "postinst called with unknown argument \`$1'" >&2
        exit 1
    ;;
esac

# dh_installdeb will replace this with shell code automatically
# generated by other debhelper scripts.

#DEBHELPER#

exit 0
```

The following sections should be added under configure:

- Creation of MySQL databases. If database is used, it must start by creating the DB user by calling ah_db_user 
  function from ahconf.sh
  
```bash
echo "Configuring MySQL database..." >&2
ah_db_user
```

- Create a directory for configuration under `/etc/arrowhead/systems`

```bash
if [ ! -d "${SYSTEM_DIR}" ]; then
    mkdir -p ${SYSTEM_DIR}
fi
```

- Generate a signed system certificate in this dir (use functions in ahconf.sh again)

```bash
ah_cert_signed_system ${SYSTEM_NAME}
```

- Create 'default.conf' file in this dir containing all the config values for the core system

```bash
if [ ! -f "${SYSTEM_DIR}/default.conf" ]; then
    /bin/cat <<EOF >${SYSTEM_DIR}/default.conf
############################################
###       APPLICATION PARAMETERS         ###
############################################

# Database parameters (mandatory)
db_user=arrowhead
db_password=${AH_PASS_DB}
db_address=jdbc:mysql://127.0.0.1:3306/arrowhead

# Certificate related paths and passwords (mandatory in secure mode)
keystore=${SYSTEM_DIR}/${SYSTEM_NAME}.p12
keystorepass=${AH_PASS_CERT}
keypass=${AH_PASS_CERT}
truststore=${AH_CONF_DIR}/truststore.p12
truststorepass=${AH_PASS_CERT}

# Authorization web-server parameters
address=0.0.0.0
insecure_port=8444
secure_port=8445

# Service Registry web-server parameters (to register the Authorization services)
sr_address=0.0.0.0
sr_insecure_port=8442
sr_secure_port=8443

#Allow querying access to the authorization tables for application systems (true/false - only has effect in secure mode)
enable_auth_for_cloud=false

############################################
###          LOGGING PARAMETERS          ###
############################################

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
log4j.appender.FILE.File=/var/log/arrowhead/${SYSTEM_NAME}.log
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
    chown root:arrowhead ${SYSTEM_DIR}/default.conf
    chmod 640 ${SYSTEM_DIR}/default.conf
fi
```

- Reload/restart the daemon

```bash
echo "Restarting ${PKG_NAME}..." >&2
systemctl daemon-reload
systemctl restart ${PKG_NAME}.service
```

### File: src/deb/postrm 

Should delete files and directories created by postinst.

```bash
#!/bin/sh
# postrm script for arrowhead-gatekeeper
#
# see: dh_installdeb(1)

set -e

. /usr/share/debconf/confmodule

SYSTEM_NAME="gatekeeper"

# summary of how this script can be called:
#        * <postrm> `remove'
#        * <postrm> `purge'
#        * <old-postrm> `upgrade' <new-version>
#        * <new-postrm> `failed-upgrade' <old-version>
#        * <new-postrm> `abort-install'
#        * <new-postrm> `abort-install' <old-version>
#        * <new-postrm> `abort-upgrade' <old-version>
#        * <disappearer's-postrm> `disappear' <overwriter>
#          <overwriter-version>
# for details, see https://www.debian.org/doc/debian-policy/ or
# the debian-policy package


case "$1" in
    purge)
        AH_CONF_DIR="/etc/arrowhead"
        AH_SYSTEMS_DIR="${AH_CONF_DIR}/systems"
        SYSTEM_DIR="${AH_SYSTEMS_DIR}/${SYSTEM_NAME}"
        
        rm -f \
            /var/log/arrowhead/${SYSTEM_NAME}.log \
            ${SYSTEM_DIR}/default.conf \
            ${SYSTEM_DIR}/${SYSTEM_NAME}.p12
        rmdir ${SYSTEM_DIR} 2>/dev/null || true
        rmdir /var/log/arrowhead 2>/dev/null || true
        db_purge
    ;;
    remove|upgrade|failed-upgrade|abort-install|abort-upgrade|disappear)
    ;;

    *)
        echo "postrm called with unknown argument \`$1'" >&2
        exit 1
    ;;
esac

# dh_installdeb will replace this with shell code automatically
# generated by other debhelper scripts.

#DEBHELPER#

exit 0

```

### File: src/deb/preinst 

Currently does nothing.

```bash
#!/bin/sh
# preinst script for arrowhead-authorization
#
# see: dh_installdeb(1)

set -e

# summary of how this script can be called:
#        * <new-preinst> `install'
#        * <new-preinst> `install' <old-version>
#        * <new-preinst> `upgrade' <old-version>
#        * <old-preinst> `abort-upgrade' <new-version>
# for details, see https://www.debian.org/doc/debian-policy/ or
# the debian-policy package


case "$1" in
    install|upgrade)
    ;;

    abort-upgrade)
    ;;

    *)
        echo "preinst called with unknown argument \`$1'" >&2
        exit 1
    ;;
esac

# dh_installdeb will replace this with shell code automatically
# generated by other debhelper scripts.

#DEBHELPER#

exit 0

```

### File: src/deb/prerm 

Currently only stops the daemon.

```bash
#!/bin/sh
# prerm script for arrowhead-authorization
#
# see: dh_installdeb(1)

set -e

PKG_NAME="arrowhead-authorization"

# summary of how this script can be called:
#        * <prerm> `remove'
#        * <old-prerm> `upgrade' <new-version>
#        * <new-prerm> `failed-upgrade' <old-version>
#        * <conflictor's-prerm> `remove' `in-favour' <package> <new-version>
#        * <deconfigured's-prerm> `deconfigure' `in-favour'
#          <package-being-installed> <version> `removing'
#          <conflicting-package> <version>
# for details, see https://www.debian.org/doc/debian-policy/ or
# the debian-policy package


case "$1" in
    remove)
        systemctl stop ${PKG_NAME}.service
    ;;

    upgrade|deconfigure)
    ;;

    failed-upgrade)
    ;;

    *)
        echo "prerm called with unknown argument \`$1'" >&2
        exit 1
    ;;
esac

# dh_installdeb will replace this with shell code automatically
# generated by other debhelper scripts.

#DEBHELPER#

exit 0

```