# Restrict Placeholders to Dictionaries plugin

# Overview #

The Restrict Placeholders to Dictionaries plugin is a Deployit plugin that allows administrators to control whether placeholder keys and values for deployments may be specified and/or overridden at deployment-time, or must be pre-defined in a dictionary associated to the target environment.

# Requirements #

* **Deployit requirements**
  * **Deployit**: version 3.8

# Installation #

Place the plugin JAR file into your `SERVER_HOME/plugins` directory.

# Usage

The Restrict Placeholders to Dictionaries plugin allows administrators to control whether placeholder keys and values for deployments may be specified and/or overridden at deployment-time, or must be pre-defined in a dictionary associated to the target environment.

The plugin adds the following two configuration options to each environment:

* `limitPlaceholdersToDictionaries` (default `false`): if set, will not allow a deployment to proceed if any deployed items contains a placeholder that is not present in any of the dictionaries linked to the target environment
* `limitPlaceholderValuesToDictionaries` (default `false`): if set, will not allow a deployment to proceed if any deployed items contains a placeholder that is not present in any of the dictionaries linked to the target environment `or` if any deployed item contains a placeholder whose value does not match the corresponding dictionary entry

The first condition, therefore, prevents placeholders not present in dictionaries from being set at deployment time. The second condition restricts this further by even disallowing the _overriding_ of values retrieved from a dictionary at deployment time.

## Examples

* Dictionary entries: `FOO=Hello`
* Deployed item placeholders: `FOO`: `Hello`, `BAR`: `entered by user`
* `limitPlaceholdersToDictionaries`: `true`
* `limitPlaceholderValuesToDictionaries`: `false`
* Result: deployment **not** allowed (`BAR` not present in any dictionary)

* Dictionary entries: `FOO=Hello`, `BAR=World`
* Deployed item placeholder: `FOO`: `Hallo`, `BAR`: `Deployit`
* `limitPlaceholdersToDictionaries`: `true`
* `limitPlaceholderValuesToDictionaries`: `false`
* Result: deployment allowed (all placeholders present in a dictionary)

* Dictionary entries: `FOO=Hello`, `BAR=World`
* Deployed item placeholder: `FOO`: `Hallo`, `BAR`: `Deployit`
* `limitPlaceholdersToDictionaries`: N/A
* `limitPlaceholderValuesToDictionaries`: `true`
* Result: deployment **not** allowed (value for `BAR` does not match dictionary)

* Dictionary entries: `FOO=Hello`, `BAR=World`
* Deployed item placeholder: `FOO`: `Hallo`, `BAR`: `World`
* `limitPlaceholdersToDictionaries`: N/A
* `limitPlaceholderValuesToDictionaries`: `true`
* Result: deployment allowed (all placeholders using dictionary values)

If either of the conditions is not met when required for a target environment, a validation error is generated during the deployment planning phase.