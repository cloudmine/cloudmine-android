This is the native Android SDK for Cloudmine. 

Setup
====

Eclipse
----

This assumes you already have the Android SDK installed, Eclipse installed, and the ADT Eclipse plug in installed

1. Go to File->New->Android Project
2. Enter a project name and click next
3. Select your SDK; the CloudMine android SDK is built against 2.3.3
4. Enter your package name and click Finish
5. In Package Explorer, right click your project and select "Properties"
6. Select Java Build Path, Libraries
7. Select Add External Jars and add the CloudMine android SDK jar
8. Copy the CloudMine android SDK jar to a folder called 'libs' in the root directory of your project

IntelliJ
----

This assumes you already have the Android SDK installed and IntelliJ installed

1. Go to File->New Project
2. Select Create Project from Scratch and click next
3. Enter your project name, module name, and select Android Module and hit Next
4. Keep hitting next until the project is created
5. Right click your module and select "Open Module Settings"
6. Select Project Settings -> Modules -> Your Module -> Dependencies
7. Click (+) Add, Jars or Directories, and select the CloudMine android SDK jar, and click OK
8. Grab a beer and continue on to the "both" section

Both
----

Open your main activity class and add

```java
private static final String APP_ID = "c1a...." //find this in your developer console
private static final String API_KEY = "3fc..." //find this in your developer console
```

at the top of the class definition. In onCreate, add calls to

```java
DeviceIdentifier.initialize(getApplicationContext()); //This initializes the unique ID that will be sent with each request to identify this user
CMApiCredentials.initialize(APP_ID, API_KEY); //This will initialize your credentials
```

and that's it! Enjoy developing without worrying about the backend.
