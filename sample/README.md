# Sample

This is a sample project to explain how to use [Play-Livy-Module](../play-livy).

## <a name="compile"></a>Compile

First compile and publish sample locally:
```
cd sample
activator clean publish-local
```

Also, you may want to upload the sample jar to Livy Server by specifying these in app configuration.

Search for the following lines in the logs of compilation for `sample`:

```
[info] 	published sample_2.11 to /Users/username/.ivy2/local/com.github.luqmansahaf/sample_2.11/1.0/jars/sample_2.11.jar
```

We will use these paths in configuration setup.

## Configuration Setup

Following configuration in [application.conf](./conf/application.conf) must be set:

### Livy URI
`livy.uri` to the URL of Livy Server. It can take form: `http://address.to.livy.server` or `http://IP:PORT`.


### File Upload

In order to be able to run Pi Example in [Application.scala](./app/controllers/Application.scala), we will have to upload the following jars to Livy Session:

- `com.cloudera.livy#livy-scala-api_2.11;0.3.0`
- `com.github.luqmansahaf#play-livy_2.11;1.0`
- `com.github.luqmansahaf#sample_2.11;1.0`

The `play-livy` jar can be found on [maven repository](https://search.maven.org/#artifactdetails%7Ccom.github.luqmansahaf%7Cplay-livy_2.11%7C1.0%7Cjar)

The last `sample` jar is published locally in Compile step above, and you can locate them under the path discussed [above](#compile).

All these jars are required because, the remote session will look for the code in its classpath.

Livy Server can use two modes to run Spark sessions:
- local
- YARN

Follow the steps accordingly:

#### Local

If you are running Livy locally, then you cannot upload files to Livy session via `LivyManager.uploadFile` function or configurations due to [this reason](https://groups.google.com/a/cloudera.org/forum/#!topic/livy-user/BDqklzO-tGU).

Therefore, you will have to put the above three jars in Livy installation directory yourself. Place the [Livy Scala API](https://mvnrepository.com/artifact/com.cloudera.livy/livy-scala-api_2.11/0.3.0) library in `<livy-installtion-dir>/repl_2.11-jars` directory and place the other two jars under the `<livy-installtion-dir>/rsc-jars` directory.

Also, set `livy.files.toUpload` to `false`.

#### YARN

If Livy is running with YARN mode, then either you can place the above jars yourself as described in Local step or you can specify the following configurations in application.conf:

```conf
livy{
    files{
        # whether to upload jar and other files on start
        toUpload = true
        list = [
            "/Users/username/.ivy2/cache/com.github.luqmansahaf/play-livy_2.11/jars/play-livy_2.11-1.0.jar",
            "/Users/username/.ivy2/local/com.github.luqmansahaf/sample_2.11/1.0/jars/sample_2.11.jar"
        ]
        # wait for files to upload to Livy Session for seconds:
        wait = 120
  }
}
```

Livy Module will upload [Livy Scala API](https://mvnrepository.com/artifact/com.cloudera.livy/livy-scala-api_2.11/0.3.0) Library on its own.

Alternatively in your own program, you can also upload files using `LivyManager.uploadFile(path: String)` function.

### Refresh Job
You can optionally schedule a refresh job to keep Livy Session alive. It's not scheduled by default. Change the configurations to something like this:

```conf
livy{
    # Refresh Job is used to refresh Livy session every x seconds interval
    refreshJob {
        # whether to start a refresh job or not
        start = true
        # interval in seconds
        interval = 900
  }
}
```

## Run Example

Run the example app with following command:

```
actiavtor run
# sbt run
```

Run the following command in another shell to initialize the controller:

```
> curl localhost:9000/

# after some delay following output should appear:
Sample Project for Play Livy Module
```

After the controller is initialized run the following command in the other shell:

```
curl localhost:9000/runPiExample/50
```

After some delay, the output should be estimated value of Pi: `3.141924628384926`. The last number in url (50) is the number of slices. You can change it to specify a different natural number. [`runPiExample`](./app/controllers/Application.scala) function submits Pi estimation code to Livy, which in turn calculates the value remotely on Spark and gets back the result.

_Happy Coding!_
