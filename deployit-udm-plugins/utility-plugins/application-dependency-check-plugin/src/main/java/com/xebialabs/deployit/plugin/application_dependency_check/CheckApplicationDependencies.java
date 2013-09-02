package com.xebialabs.deployit.plugin.application_dependency_check;

import java.util.List;
import java.util.Map;

import com.xebialabs.deployit.plugin.api.deployment.planning.Contributor;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.specification.Deltas;
import com.xebialabs.deployit.plugin.api.udm.CompositePackage;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;

import static com.google.common.collect.Lists.newArrayList;

public class CheckApplicationDependencies {

    private static final String APPLICATION_DEPENDENCIES_PROPERTY_NAME = "applicationDependencies";

    @Contributor
    public void checkAllApplicationDependencies(Deltas deltas, DeploymentPlanningContext context) {
        List<String> missingDependencies = newArrayList();
        Version pkg = context.getDeployedApplication().getVersion();
        Environment env = context.getDeployedApplication().getEnvironment();
        checkApplicationDependencies(pkg, env, context, missingDependencies);
        if (!missingDependencies.isEmpty()) {
            throw new IllegalArgumentException("Cannot deploy because of missing dependencies: " + missingDependencies);
        }
    }

    private void checkApplicationDependencies(Version pkg, Environment env, DeploymentPlanningContext context, List<String> missingDependencies) {
        if (pkg.hasProperty(APPLICATION_DEPENDENCIES_PROPERTY_NAME)) {
            @SuppressWarnings("unchecked") Map<String, String> applicationDependencies = (Map<String, String>) pkg
                .getProperty(APPLICATION_DEPENDENCIES_PROPERTY_NAME);
            for (Map.Entry<String, String> each : applicationDependencies.entrySet()) {
                String requiredApplication = each.getKey();
                String requiredVersion = each.getValue();
                String requiredDeployedApplicationId = env.getId() + "/" + requiredApplication;
                if (!context.getRepository().exists(requiredDeployedApplicationId)) {
                    missingDependencies.add("Application [" + requiredApplication + "] has not been deployed to environment [" + env.getId() + "]. Version ["
                        + requiredVersion + "] is required.");
                    continue;
                }
                DeployedApplication requiredDeployedApplication = context.getRepository().read(requiredDeployedApplicationId);
                String version = requiredDeployedApplication.getVersion().getVersion();
                if (!version.equals(requiredVersion)) {
                    missingDependencies.add("Version [" + version + "] of application [" + requiredApplication + "] has been deployed to [" + env.getId()
                        + "] but version [" + requiredVersion + "] is required");
                }
            }
        }

        if (pkg instanceof CompositePackage) {
            for (Version each : ((CompositePackage) pkg).getPackages()) {
                checkApplicationDependencies(each, env, context, missingDependencies);
            }
        }
    }

    /*
    private boolean isUndeployment(Deltas deltas, DeploymentPlanningContext context) {
        boolean isUndeployment = true;
        for(Delta delta: deltas.getDeltas()) {
            if(delta.)
        }
        return isUndeployment;
    }
    */

}
