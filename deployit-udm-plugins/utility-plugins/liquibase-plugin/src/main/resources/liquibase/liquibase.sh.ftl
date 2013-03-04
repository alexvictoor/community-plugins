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

<#assign ouptutfile = "">
<#if deployed.container.generatedSqlPath??>
  GENERATED_SQL_DIR=${deployed.container.generatedSqlPath}/$(date +%Y_%m_%d_%H:%M)
  mkdir -p $GENERATED_SQL_DIR
  GENERATED_SQL_FILE=$GENERATED_SQL_DIR/update.sql
  echo "liquibase will generate update.sql to " $GENERATED_SQL_FILE
  <#assign ouptutfile = " 1>">
<#else>
  GENERATED_SQL_FILE=
  <#assign ouptutfile = " 1>&1"> 
</#if>

${deployed.container.javaCmd} -jar ${deployed.container.liquibaseJarPath} ${options} --changeLogFile="${deployed.changeLogFile}" updateSQL ${ouptutfile}$GENERATED_SQL_FILE
generateStatus=$?
if [ $generateStatus == 0 ]; then
  ${deployed.container.javaCmd} -jar ${deployed.container.liquibaseJarPath} ${options} --changeLogFile="${deployed.changeLogFile}" update
else
   exit $generateStatus
fi

exit 0
