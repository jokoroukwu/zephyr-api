# Zephyr-API
An abstraction over the undocumented API of «Zephyr for JIRA Server» test management tool.

## Overview
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

The library should be fully compatible with Java 8.<br>

## Setup
Simply add the dependency:

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

## Licence

Copyright 2022 John Okoroukwu

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.