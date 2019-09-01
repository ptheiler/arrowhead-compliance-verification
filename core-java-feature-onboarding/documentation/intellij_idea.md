# Setting up an Intellij IDEA project for Arrowhead development

## Creating a compound project

Here I will set up a compound project, where all three java repositories can be shared in one Intellij IDEA workspace.
You can skip this and do a normal _Checkout from Version Control_, to have them in separate Intellij windows.

1. From Intellij's welcome screen, click _"Create New Project"_ -> _Maven_ -> _Next_

1. Input the following (don't worry about version, it will be overridden by sub-projects anyway):
    
    **GroupId:** eu\
    **ArtifactId:** arrowhead\
    **Version:** 1.0-SNAPSHOT

1. Click _Next_

1. Input (feel free to change the location to anything you like):
    
    **Project name**: arrowhead\
    **Project location**: ~/arrowhead

1. Click _Finish_

1. Delete the src folder

1. Add `<packaging>pom</packaging>` to pom.xml (just under `<version>1.0-SNAPSHOT</version>`)

### Create the core-java module

1. Click _VCS_ (menu) -> _Checkout from version control_ -> _Git_

1. Input (remember to change path if you created the project elsewhere):
    
    **Url:** https://github.com/arrowhead-f/core-java.git \
    **Directory:** arrowhead/core-java

1. Choose _No_ to open the new project

1. Optional: In the bottom right corner of the window, change the Git branch.

1. Add to pom.xml under the packaging line:
    
    ```xml
    <modules>
        <module>core-java</module>
    </modules>
    ```

1. **Linux:** Open a terminal and create symbolic links (this may not be available on all branches):\
    **Windows:** Either copy the files manually, or search the web for how to create symbolic links. 

    ```bash
    cd .idea
    ln -s ../../core-java/.idea/codeStyles
    mkdir runConfigurations
    cd runConfigurations
    ln -s ../../core-java/.idea/runConfigurations/* .
    ```

1. (This step can be skipped until all modules are created) Click _File_ (menu) -> _Close Project_, then reopen the
    project. Import maven project and configure Hibernate if asked. It should now be possible to build and run the core
    systems (see below).

### Create the client-java module

1. Click _VCS_ (menu) -> _Checkout from version control_ -> _Git_

1. Input (remember to change path if you created the project elsewhere):
    
    **Url:** https://github.com/arrowhead-f/client-java.git \
    **Directory:** arrowhead/client-java

1. Choose _No_ to open the new project

1. Optional: In the bottom right corner of the window, change the Git branch.

1. Add to pom.xml under the modules section:

    ```xml
    <module>client-java</module>
    ```

1. **Linux:** Open a terminal and create symbolic links (this may not be available on all branches):\
    **Windows:** Either copy the files manually, or search the web for how to create symbolic links. 

    ```bash
    cd .idea
    mkdir runConfigurations
    cd runConfigurations
    ln -s ../../client-java/.idea/runConfigurations/* .
    ```

1. (This step can be skipped until all modules are created) Click _File_ (menu) -> _Close Project_, then reopen the
    project. Import maven project and configure Hibernate if asked. It should now be possible to build and run the core
    systems (see below).

### Create the sos-examples module

1. Click _VCS_ (menu) -> _Checkout from version control_ -> _Git_

1. Input (remember to change path if you created the project elsewhere):
    
    **Url:** https://github.com/arrowhead-f/sos-examples.git \
    **Directory:** arrowhead/sos-examples

1. Choose _No_ to open the new project

1. Optional: In the bottom right corner of the window, change the Git branch.

1. Add to pom.xml under the modules section:

    ```xml
    <module>sos-examples</module>
    ```

1. Click _File_ (menu) -> _Close Project_, then reopen the project. Import maven project and configure Hibernate if
    asked. It should now be possible to build and run the core systems (see below).

## Create the database

First you need to install mysql and have a user which can create databases (root will do, but you may have to
reconfigure MySQL or set a password for it to work, consult the MySQL documentation for this). Note that this user
should not be named _arrowhead_, we'll create that later.

1. In Intellij, open the _Database_ pane.

1. Click _New_ (first icon) -> _Data Source_ -> _MySQL_

1. Enter your username and password for the database (the rest should be fine) and click _Ok_

1. Right click on the database _@localhost_ -> _Run SQL Script..._

1. Choose the SQL file _core-java/scripts/create_database_and_user.sql_ (may not be available on all branches).

You now have an empty Arrowhead database and an _arrowhead_ user for it.

## Run the core systems

For the secure versions pick the configuration ending with _(TLS)_:

1. Run the _ServiceRegistryMain_ configuration first.

1. Then run the _Core Systems_ configuration to start the remaining systems.

1. When asked to stop the Service Registry, click _Cancel_.

## Run the client systems

First, you should start the consumer to get the base64 encoded authentication info for the secure mode:

1. Run _ConsumerMain_. It will fail the first time around, this is okay.

1. Find the _Arrowhead_system_ table in the _Database_ pane and double click on it.

1. Insert a new row with (note that the id should be lower than the next one generated by hibernate, the easiest
    solution is to pick one lower than the existing ids):

    **id:** 1\
    **address:** 0.0.0.0\
    **authentication_info:** `<null>` (for secure systems insert the base64 string from the consumer output)\
    **port:** 80 (443 for secure systems)\
    **system_name:** consumer-demo

1. Insert the record in the _intra_cloud_authorization_ table also. You can find the necessary ids in the
    _arrowhead_system_ and _arrowhead_service_ tables.

1. Run _Demo Systems_ configuration to start all demo systems. For the secure versions pick the configuration ending
with _(TLS)_.
