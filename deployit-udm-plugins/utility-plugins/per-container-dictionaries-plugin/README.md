# Per-Container Dictionary plugin

# Overview #

The Per-Container Dictionary plugin is a Deployit plugin that supports per-container placeholder values.

# Requirements #

* **Deployit requirements**
  * **Deployit**: version 3.8

# Installation #

Place the plugin JAR file into your `SERVER_HOME/plugins` directory. 
	
# Configuration<a name="configuration"/>

In order to be able to store values for a container, the container type must define a property "dictionary" that refers to a `udm.Dictionary`. The plugin pre-defines these for `overthere.Host` and `generic.Container`; to add this property to additional containers add the following type modification for the desired containers to SERVER_HOME/ext/synthetic.xml:

	<type-modification type="...">
    	<property name="dictionary" kind="ci" referenced-type="udm.Dictionary" required="false" category="Dictionary" />
    	<property name="generateContainerDictionary" kind="boolean" default="true" required="false" category="Dictionary"/>
        <property name="containerDictionaryKeySeparator" hidden="true" default="_" description="separator used to compute the key in the generated container dictionary"/>
        <property name="excludePasswords" hidden="true" default="true" kind="boolean" description="if true, do not put the property whose kind is 'password' "/>
	</type-modification>

# Usage

The per-container dictionaries plugin supports replacement of placeholders in deployed artifacts with values defined on the target container, as opposed to coming from a dictionary (or the user).

The per-container-dictionaries-plugin _only_ works for placeholders within files, such as configuration files, EAR files or web content. Only keys _inside_ the files are replaced - the plugin does not support replacement of CI-level properties such as _targetPath_, or replacement of properties in resources such as datasources or queues.

In order to retrieve a value from the deployed's container, the container must define a `dictionary` property (see [Configuration](#configuration)). If the placeholder's value is set to `<per-container>`, the value will be retrieved from the container's dictionary by looking for an entry with the matching key, as for regular environment dictionaries.

if the 'generateContainerDictionary' is true, an additional dictionary is generated containing the container's properties (recursively).

Example of a generated dictionary for a www.ApacheServer configuration item:

```
{container_host_username=scott, container_host_id=Infrastructure/deployit.vm, container_host_generateContainerDictionary=true, container_configurationFragmentDirectory=/opt/apache/conf, container_host_port=22, container_host_deploymentGroup=<empty>, container_stopWaitTime=10, container_startWaitTime=10, container_startCommand=apachectl start, container_host_privateKeyFile=<empty>, container_host_connectionType=SFTP, container_host_temporaryDirectoryPath=<empty>, container_generateContainerDictionary=true, container_deploymentGroup=<empty>, container_restartCommand=<empty>, container_host_address=deployit.vm, container_host_sudoUsername=<empty>, container_name=myApacheServer, container_defaultDocumentRoot=/opt/apache/doc, container_restartWaitTime=10, container_host_name=deployit.vm, container_host_os=UNIX, container_id=Infrastructure/deployit.vm/myApacheServer, container_stopCommand=apachectl stop}
```
once this dictionary is defined it can be used to compose key's values.

Example:
LOG_FILENAME: {{container_host_address}}.log


### Step-by-step

1. If necessary, add a `dictionary` property to your container type in synthetic.xml (see above).

2. Insert the placeholders in artifact files, e.g. configuration files.

3. Define a dictionary for your environment, say ENV_DICT, that contains the key you just defined and the value `<per-container>`. Link this dictionary to your environment.

4. Define a dictionary for your container, say CONTAINER_DICT, that contains the key you just defined and the value you want it to be on that containers. Link this dictionary to your container.

5. Placeholder values are resolved when the plan is generated. When using the [Deployit UI](http://docs.xebialabs.com/releases/latest/deployit/guimanual.html#initial-deployment), this occurs when you press _Next_ and are taken to the _Step list_ screen before deployment.