## Installing Arrowhead on a Debian based Linux distribution

Currently, this guide is known to work on Ubuntu Server 18.04 and Rasbian 2018-11-13, but it will likely work on other
version and Debian based Linux distribution also. The following is a quick guide on the essentials.

Note Rasbian installation has only been attempted with the included mysql-server and openjdk-9-jre-headless packages so
far. Also you should use Arrowhead packages after 19.11.2018, due to a bug with logname in earlier versions. 

### 1. Install Linux

Do a normal installation of Linux, and remember to update afterwards:

`sudo apt update && sudo apt dist-upgrade`

### 2. Install MySQL

Pick one of the options below.

#### 2a. MySQL 5.x (Ubuntu)

Install:

`sudo apt install mysql-server`

(Note that on Rasbian, this is actually a MariaDB behind the scenes but it should not be an issue.)

Check if running:

`sudo netstat -tap | grep mysql`

#### 2b. MySQL 8.x (Oracle)

First, get the latest repository package from <https://dev.mysql.com/downloads/repo/apt/>, eg.:

```bash
wget https://dev.mysql.com/get/mysql-apt-config_0.8.10-1_all.deb
sudo dpkg -i mysql-apt-config_0.8.10-1_all.deb
sudo apt update
```

To install the MySQL server, run:

```bash
sudo apt install mysql-server
```

### 3. Install Java

Pick one of the options below.

#### 3a. Install Java (OpenJDK)

Ubuntu users (and others?):

`sudo apt install openjdk-11-jre-headless`

Rasbian users do not have this newer version yet, so until then this will do:

`sudo apt install openjdk-9-jre-headless`

**NOTE:** Install JDK versions instead of JRE versions, if you plan to build the latest Debian Packages from source, using Maven. You just need to 
change the "-jre-" part to "-jdk-" in the package names. JDK needs more disk space. JRE versions can only run packaged Java applications, but can 
not build them from source code.

#### 3b. Install Java 11 (Oracle)

To install Oracle Java 11, add the repository first:

`sudo add-apt-repository ppa:linuxuprising/java`

`sudo apt update`

`sudo apt install oracle-java11-installer`

Check Java version:

`java -version`

#### 3c. Install Java 8 (Oracle)

As an alternative to Java 11, you may also use the older Java 8 version:

`sudo add-apt-repository ppa:webupd8team/java`

`sudo apt-get update`

`sudo apt-get install oracle-java8-installer`

### 4. Download/install Arrowhead 

Pick one of the options below.

#### 4a. Download an Arrowhead Debian Packages release

Check the GitHub releases site <https://github.com/arrowhead-f/core-java/releases> for the latest release and download
it: 

`wget -c https://github.com/arrowhead-f/core-java/releases/download/4.1.0/debian_packages.zip`

Unpack it:

```bash
unzip debian_packages.zip
cd debian_packages/
```

#### 4b. Build Arrowhead Debian Packages

**NOTE:** To compile Arrowhead yourself, you should have both the JDK and Maven installed. Raspbian users should probably do this on their PC and then copy the files to their Raspberry Pi.

To build the Debian packages yourself, start by cloning the repository:

`git clone https://github.com/arrowhead-f/core-java.git -b develop`

Build them with:

`mvn package`

Copy all the packages to your Arrowhead server/Raspberry Pi (you may have to start SSH server on it first with `sudo systemctl start ssh`:

```bash
scp target/arrowhead-*.deb X.X.X.X:~/
```

### 5. Install Arrowhead Core Debian Packages

Go to the folder where you copied the packages and then:

`sudo dpkg -i arrowhead-*.deb`

The installation process will show prompts asking for input parameters. Certificate passwords need to be at least 6 
character long!

Currently the created services are not added to the default runlevel, so they will not restart on reboot unless added manually.

### 6. Hints

#### Add a new application system

You can use the script `ah_gen_system` to generate certificate, a configuration template, and add the necessary
database entries for a new application system: 

```sudo ah_gen_system SYSTEM_NAME HOST PORT SERVICE_NAME```

If there is no service parameter only a consumer system will be generated, specify a service to generate a full provider
system. Examples:

```sudo ah_gen_system client1 127.0.0.1 8080```

```sudo ah_gen_system SecureTemperatureSensor 127.0.0.1 8461 IndoorTemperature```

Generated certificates will appear in the user's home directory.

#### Add a new cloud to a detached installation

Run the script `ah_gen_cloud` to generate a new certificate and update the databases on the existing cloud: 

```sudo ah_gen_cloud CLOUD_NAME HOST```

E.g. (the IP address should be that of the new cloud):

```sudo ah_gen_cloud testcloud2 127.0.0.1```

Use authorized mode to install the new cloud with the cloud and master certificate from the `/etc/arrowhead` folder.
Afterwards, call `ah_add_neighbor` on the new cloud (`ah_gen_cloud` will output the correct parameters).

#### Add a new neighbor to a cloud

Use the script `ah_add_neighbor` to add a neighboring cloud:

```ah_add_neighbor OPERATOR CLOUD_NAME HOST AUTH_INFO```

#### Other hints

Log files (log4j) are available in: `/var/log/arrowhead/*`

Output from systems are available with: `journalctl -u arrowhead-*.service`

Restart services: `sudo systemctl restart arrowhead-\*.service`

Configuration and certificates are found under: `/etc/arrowhead`

Generated passwords can be found in `/var/cache/debconf/passwords.dat`

Mysql database: `sudo mysql -u root`, to see the Arrowhead tables:

```SQL
use arrowhead;
show tables;
```

To open the MySQL for public access and create an user for this, run: `ah_mysql_public`. Script will output the username
and password you need to use.

`apt purge` can be used to remove configuration files, database, log files, etc. Use `sudo apt purge arrowhead-\*` to
remove everything arrowhead related.

For the provider and consumer example in the client skeletons, the script `sudo ah_gen_quickstart HOST` can be used to
generate the necessary certificates and database entries. `HOST` should be the IP address of where you intend to run
the systems. It will also output the certificate/keystore password. Note,
this script should only be used for test clouds on a clean installation.

To switch to insecure mode of all core services, remove `-tls` in the service files and restart them, e.g.:

`cd /etc/systemd/system/; sudo find -name arrowhead-\*.service -exec sed -i 's|^\(ExecStart=.*\) -tls$|\1|' {} \; && sudo systemctl daemon-reload && sudo systemctl restart arrowhead*.service`

To switch back to secure mode add `-tls` again, e.g.:

`cd /etc/systemd/system/; sudo find -name arrowhead-\*.service -exec sed -i 's|^\(ExecStart=.*\)$|\1 -tls|' {} \; && sudo systemctl daemon-reload && sudo systemctl restart arrowhead*.service`
