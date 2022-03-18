## Zephyr-API
A simple library for publishing test results
to 'Zephyr for JIRA Server' test management tool.

### Main purpose
Zephyr for JIRA is a great test management tool.<br>
Unfortunately the official public API does not fully support
all features commonly used in test automation, such as
DDT (at least at the moment this library was developed).<br>
The sole purpose of this library is to add that missing
functionality. 
It abstracts the <u>undocumented</u> Zephyr
for JIRA API and provides an
[entrypoint](src/main/kotlin/io/github/jokoroukwu/zephyrapi/api/ZephyrClient.kt)
which embodies all the work necessary
to publish test results to Zephyr.

## Setup
The library should be fully compatible with Java 8.<br>
Simply add the dependency.

Gradle:
```Groovy
dependencies {
    implementation "io.github.jokoroukwu:zephyr-api:0.1.1"
}
```

Maven:
```XML
<dependency>
    <groupId>io.github.jokoroukwu</groupId>
    <artifactId>zephyr-api</artifactId>
    <version>0.1.1</version>
</dependency>
```


The library also requires configuration which can be specified
in ```zephyr-config.yml```.<br>
The file should have the following format:

```YAML
# The timezone used to display Zephyr test result start and end time
time-zone: GMT+3

# Your JIRA server project key
project-key: PROJKEY

# Your JIRA server URL
jira-url: https://your-jira-server:8089

# Your JIRA credentials.
username: ${username:?err}
password: ${password:?err}
```

Placeholders like ```${password:?err}``` may be used anywhere in the file.
They will be substituted with
either environment variables or system properties with
the former taking precedence over the latter.<br><br>

The path to ```zephyr-config.yml``` file is resolved as follows (whichever succeeds first):

- If ```ZEPHYR_CONFIG``` environment variable is set, then its value is used as an absolute path.
- If ```zephyr.config``` system property is set, then its value is used as an absolute path.
- Finally, classpath resources are scanned for ```zephyr-config.yml``` file.

## Usage

A test method needs to be annotated with ```@TestCaseKey```
to have its result collected,
properly mapped to the existing test case
and published to Zephyr.
Example:
```java
    @TestCaseKey("MYPROJ-123")
    @Test
    public void should_test_something(){
        
    }
```
You may optionally annotate any method with ```@Step```
to map the method's result to
the corresponding Zephyr step. Example:
```Java
    @Step(0, "create user")
    public void createUser(User user){

    }
```

## Licence

Copyright 2022 John Okoroukwu

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.