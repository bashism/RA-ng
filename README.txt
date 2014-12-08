ra-cli: the command-line interface
ra-ws: the web service server
ra-core: the core relational algebra logic

REST API build steps:
        - ra-core: mvn install
        - ra-ws: mvn package
        - if not already, service tomcat start
        - cp sample.db /sample.db
        - cp target/ra-ws.war $CATALINA_HOME/webapps
        - point browser to localhost:8080/ra-ws/api?query=

CLI build steps
        - ra-core: mvn install
        - ra-cli: mvn package
        - optional: zip up dependency and ra-cli.jar
        - optional: unzip ra-cli.jar in another directory
        - run java -jar ra-cli.jar

Specific Limitations:
	- due to refactoring to use WITH statements, \rename is broken
	- attribute validation is not implemented

