# Student Portal
 
This is implementation of `Student Portal` Web Application. 

##Prerequisite
- \>= JDK 8.0

##Stack
- Spring MVC
- MySQL
- Tomcat

##Build and Run

####For development

1. package the java project or launch Application from your console
    * bash mvnw clean package 
2. launch the backend spring application
    * bash mvnw cargo:run
3. go on http://localhost:8080/portal

##For production (one war, souce code optimized):

1. in the parent project directory execute
    * bash mvnw clean package 

2. copy `portal-[VERSION].RELEASE.war` at `target` folder into `Tomcat`'s webapps. 

##LICENSE

The `Student Portal` is distributed under the [Apache License](https://bitbucket.org/lvncnt/licenses/raw/master/LICENSE), and you are free to make modifications and improvements to the source code.