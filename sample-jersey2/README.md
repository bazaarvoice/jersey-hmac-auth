# sample-jersey2

This is a minimal example of using jersey-hmac-auth with a Jersey 2.x
application.

## Running the example

You can run the sample web application using:

`mvn tomcat7:run`

This will start Tomcat on port 8080 and deploy the war file. To issue a
sample request against the application, open a separate terminal and run
`sh test.sh` from the [src/test/resources](src/test/resources) directory. In
the console output for the application you should see the log message
"Baking a pizza for fred." You may stop the application at any time by
pressing Ctrl+C (SIGINT).
