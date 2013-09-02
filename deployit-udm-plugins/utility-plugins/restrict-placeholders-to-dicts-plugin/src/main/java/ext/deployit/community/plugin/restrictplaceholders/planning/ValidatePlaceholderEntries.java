package ext.deployit.community.plugin.restrictplaceholders.planning;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.DESTROY;
import static ext.deployit.community.plugin.restrictplaceholders.util.Dictionaries.consolidatedDictionary;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.artifact.DerivedArtifact;

public class ValidatePlaceholderEntries {
    protected static final String LIMIT_PLACEHOLDERS_PROPERTY = "limitPlaceholdersToDictionaries";
    protected static final String LIMIT_PLACEHOLDER_VALUES_PROPERTY = "limitPlaceholderValuesToDictionaries";

    protected static final List<Step> NO_STEPS = ImmutableList.of();

    @PrePlanProcessor
    public List<Step> validatePlaceholders(DeltaSpecification specification) {
        List<DerivedArtifact<?>> artifactsWithPlaceholders = newArrayList();
        for (Delta delta : specification.getDeltas()) {
            // only interested in those deployeds that will be present *after* the deployment
            if (!delta.getOperation().equals(DESTROY) && (delta.getDeployed() instanceof DerivedArtifact)) {
                artifactsWithPlaceholders.add((DerivedArtifact<?>) delta.getDeployed());
            }
        }

        if (artifactsWithPlaceholders.isEmpty()) {
            return NO_STEPS;
        }

        Environment targetEnvironment = specification.getDeployedApplication().getEnvironment();
        // may (?) be null
        boolean limitPlaceholders =
            TRUE.equals(targetEnvironment.getProperty(LIMIT_PLACEHOLDERS_PROPERTY)); 
        boolean limitPlaceholderValues =
            TRUE.equals(targetEnvironment.getProperty(LIMIT_PLACEHOLDER_VALUES_PROPERTY));

        if (!limitPlaceholders && !limitPlaceholderValues) {
            // no restrictions, so nothing to do
            return NO_STEPS;
        }

        Map<String, String> resolvedDictionary = consolidatedDictionary(targetEnvironment);
        ImmutableList.Builder<String> validationErrors = ImmutableList.builder();
        for (DerivedArtifact<?> artifactWithPlaceholders : artifactsWithPlaceholders) {
            // non-dictionary keys are definitely not allowed
            for (Entry<String, String> placeholderAndValue
                    : artifactWithPlaceholders.getPlaceholders().entrySet()) {
                String validationError = checkPlaceholder(placeholderAndValue.getKey(),
                        placeholderAndValue.getValue(), resolvedDictionary, limitPlaceholderValues);
                if (validationError != null) {
                    validationErrors.add(format("%s: %s", artifactWithPlaceholders.getName(),
                            validationError));
                }
            }
        }
        List<String> errors = validationErrors.build();
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(buildErrorMessage(
                    specification.getDeployedApplication(), errors));
        }
        return NO_STEPS;
    }

    protected static String checkPlaceholder(String placeholder, String value,
            Map<String, String> dictionary, boolean valueMustMatchDictionary) {
        if (!dictionary.containsKey(placeholder)) {
            return format("'%s' not found in any dictionary", placeholder);
        } else if (valueMustMatchDictionary && !dictionary.get(placeholder).equals(value)) {
            return format("Value '%s' for '%s' does not match dictionary value '%s'", value,
                    placeholder, dictionary.get(placeholder));
        } else {
            return null;
        }
    }

    protected static String buildErrorMessage(DeployedApplication deployedApplication,
                                     List<String> validationErrors) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Cannot deploy '").append(deployedApplication.getName())
                .append("' (version ").append(deployedApplication.getVersion().getVersion())
                .append(") to '").append(deployedApplication.getEnvironment().getName())
                .append("' due to the following errors:");
        for (String validationError : validationErrors) {
            errorMessage.append("\n - ").append(validationError);
        }
        return errorMessage.toString();
    }
}
