# Liquibase plugin #

This document describes the functionality provided by the Liquibase plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

## Overview

The Liquibase plugin provides a simple way to use Liquibase as a drop in replacement of the official database plugin.
This is a simple integration where SQL rollbacks are not handled yet.

## Requirements

* **Deployit requirements**
	* **Deployit**: version 3.8+
	* **Other Deployit Plugins**: None

## Installation

You need to install Liquibase on a host accessible by the DeployIt server.

## Liquibase execution

At each deployment, liquibase "update" command is executed 

## Configuration

### liquibase.Runner
This is the "container" of the liquibase plugin. A liquibase.Runner instance represents a liquibase installation. Below the configuration properties that needs to be set:
* *liquibaseJarPath*: path to the main liquibase jar file, i.e. liquibase.jar
* *liquibaseConfigurationPath*: path to the liquibase configuration file, i.e liquibase.properties
* *javaCmd*: command that will be used to launch liquibase java process. Default is "java"
* *driverClasspath*: java classpath used to get database drivers
* *generatedSqlPath*: optionnal path to a folder where generated sql commands will be logged

### liquibase.Changelog and liquibase.ExecutedChangelog
*liquibase.Changelog* and *liquibase.ExecutedChangelog* are respectively the deployable and deployed types of this plugin. 
*liquibase.Changelog* extends *generic.Folder* and should contain all the xml liquibase changelog files of the application package.
There is only one configuration property, *changeLogFile*, which specifies which is the entry point xml changelog file for liquibase.
