
Note that the pom references the original pom using a relative path: ../../arjuna for byteman scripts
which will need to be updated if the modules are moved around

To build use the community profile: `mvn clean test -Pcommunity`

I have temporarily excluded the byteman tests (cannot find byteman jar please set environment variable BYTEMAN_HOME)
and also one hanging test (LogStressTest2)

