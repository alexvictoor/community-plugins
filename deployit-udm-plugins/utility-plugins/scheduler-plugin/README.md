# Scheduler plugin #

# Overview #

The scheduler plugin allows to schedule the execution of the remaining steps after a given date.

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.8

# Installation #

Place the plugin JAR file into your `SERVER_HOME/plugins` directory. 

The date and the hours are defined as transient at the udm.DeployedApplication level. The format used to defined the date and the time are configurable using the `datePattern` and `timePattern` hidden properties. By default the patterns are configured as `dd/MM/yyyy` and `HH:mm:ss` respectively.

