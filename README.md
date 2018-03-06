# OSB XQuery Runner
A Java Wrapper for the Oracle Service Bus XQuery 1.0 Engine

This is a single class utility that serves as a quick wrapper to run a single XQuery file on the Oracle Service Bus 12c XQuery 1.0 Engine, it can be used directly or through the ant buildfile.

## Requirements
* Oracle Service Bus 12c installation (requires jars found on it)
* JDK 8
* ANT (tested on 1.9.9)

## Before running
The ORACLE_HOME environment variable must be set because the ANT buildfile requires it to find the necessary jar files to compile and run.
```bash
# Bash
export ORACLE_HOME=/home/user/Oracle/Middleware/Oracle_Home
```
```bat
:: Windows BAT
set ORACLE_HOME=/home/user/Oracle/Middleware/Oracle_Home
```

## Usage
```bash
ant test
```
Runs a basic test to ensure the wrapper is working properly

```bash
ant run -DxqueryRunnerParams "xqueryToRun.xqy output.xml param1=parameter1.xml param2=parameter2 paramN=parameterN"
```
Runs the specified XQuery file with an output xml and parameters.

### Parameters support
Parameters must have the xquery parameter name specified like **"parameter=value"**.
 
The runner will determine internally the value type based on the xquery file declaration, if it is an "anyType" will use the parameter value as a xml file path to look for, if not it will use the value as is.

The wrapper for now supports only *xs:anyType*, *xs:int/integer/long* and *xs:string*.

## Status
Alpha 0.1
