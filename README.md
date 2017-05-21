# Play Livy Module - Scala
[Apache Livy](https://livy.io) is a REST service for Apache Spark. With Livy you can create remote Spark interactive sessions using Livy's REST API. Livy also has a [Scala Client](https://github.com/cloudera/livy/tree/master/scala-api/src/main/scala/com/cloudera/livy/scalaapi) that can be used to submit Scala code to run on Livy session.

Play Livy Module can be used in your Play app to submit code to Livy Scala Client. Apart from submitting jobs, you can also do the following using `LivyManager`

- Upload Jar files of related code and other files to Livy Session, so that the code can run smoothly on remote session without errors (Class Not Found mainly).
- Schedule a refresh job which pings the Livy Session to keep it alive.
- Change specific Spark configurations related to the Livy session created.
- Stop the remote contexts related to Spark and Livy session when the app stops automatically.

## Configurations

You can find all configurable options in [application.conf](./sample/conf/application.conf) in sample project. We discuss these below.

### Livy URI

You must specify a Livy URI in your Play configuration file. This URI is used to contact Livy Server. The property to set is `livy.uri`, and it must be of the form: `http://address.livy.server` or `http://IP:PORT`.

### Refresh Job

A refresh Job can be used to keep the remote session alive while your app is still running. There might be a situation where your app receives no request where Livy session is used for a long period, and the Livy Server decides to shut down your session. Therefore, this refresh job can keep your session up if there is no request by submitting a sample job every 900 seconds (15 minutes is default) or so.

You can change the configurations with properties:
```
livy{
    refreshJob{
        start=true
        # interval in seconds
        interval = 900
    }
}
```

### Upload Files

You can upload files with this module in two different ways:

1. Using `LivyManager.uploadFile` function programmatically by giving it a path.
2. Giving the paths in a list in Play app configuration using the following properties:
```
livy{
    files{
        # whether to upload jar and other files on start
        toUpload = true
        list = [
            "/Users/username/.ivy2/local/luqman.sahaf/play-livy_2.11/1.0-SNAPSHOT/jars/play-livy_2.11.jar",
            "/Users/username/.ivy2/local/luqman.sahaf/sample_2.11/1.0-SNAPSHOT/jars/sample_2.11.jar"
        ]
        # wait for files to upload to Livy Session for seconds:
        wait = 120
    }
}
```

### Spark Configurations

You can also control number of executors in your Livy Session, their memory and cores, etc. via the following configurations:

```
# Spark options
spark{
  driver {
    # extraClassPath = ""
    # memory = "512m"
  }
  executor{
    # extraClassPath=""
    # memory = "512m"
    # cores = 1
    instances=1
  }
}
```
The format for these option is same as used in configurations in `spark-defaults.conf` in Spark Project. Configurations other than those specified above will not have any effect.

## Example

To run example follow the guide in [sample](./sample) project.

## Logger

The logger for Play Livy Module is: `luqman.sahaf.playlivy`, and can be set in [logback.xml](./sample/conf/logback.xml).


