# Purpose
A cloud endpoint limitation is to not let the developper defines its client ids list depending of an external flag.   
For instance a flag representing our current environment.   
If I need to define a different set of client Ids wether I am on dev, staging, or production environment we are kind of
stuck.   

This small google cloud enpoint project aims at illustrating how we can define a list of client ids
in a different source than the code itself.  
e.g: a resource file, a cloud storage file...

In this particular case, we define a list of client Ids in resource files.

# How we do it
Google cloud endpoint, allows to overide the endpoint authenticator.   
An authenticator is service in front of the endpoint that will authenticate the caller of the request.   
In particular, we identify him.   
By default, it is the Google EndpointsAuthenticator that is called.   
To keep the verfication standard our custom authenticator still rely on it, and only reimplment the client Ids whitelisting
logic.   
That means we don't have to specify any client ids in the @Api or @ApiMethod annotation. 

# Building the project
In the pom file, set "endpoints.project.id" property with a valid GCP project.   
Launch `mvn clean install -Dfoo_environment={LOCAL, DEV, STAGING, PRODUCTION}`.   
**Note**: We bind the endpoints-framework:openApiDocs goal to the post-integration-test phase.

# Launching locally
Launch `mvn appengine:run`.  
The port is set to 4242.   
Open an HTTP client and GET `http://localhost:4242/_ah/api/helloworld/v1/message/whatever`    
**Note**: This endpoint is authenticated, so it requires an Authorisation header with a valid token with at least the scope 'https://www.googleapis.com/auth/userinfo.email'.

# Deploy
## Endpoint configuration
`gcloud endpoints services deploy target/openapi-docs/openapi.json`
## API backend
`mvn appengine:deploy -Dfoo_environment={DEV, STAGING, PRODUCTION}`