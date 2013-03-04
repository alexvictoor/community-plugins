#!/bin/sh
<#assign options = ""> 
<#if deployed.container.liquibaseConfigurationPath??>
  <#assign options = options + " --defaultsFile=${deployed.container.liquibaseConfigurationPath}"> 
</#if>
<#if deployed.container.driverClasspath??>
  <#assign options = options + " --classpath=${deployed.container.driverClasspath}"> 
</#if>

<#if deployed.changeLogPath??>
  mkdir -p ${deployed.changeLogPath}
  mv changelog ${deployed.changeLogPath}
</#if>

${deployed.container.javaCmd} -jar ${deployed.container.liquibaseJarPath} ${options} --changeLogFile="${deployed.changeLogFile}" updateSQL
generateStatus=$?
if [ $generateStatus == 0 ]; then
  ${deployed.container.javaCmd} -jar ${deployed.container.liquibaseJarPath} ${options} --changeLogFile="${deployed.changeLogFile}" update
else
   exit $generateStatus  
fi

exit 0
