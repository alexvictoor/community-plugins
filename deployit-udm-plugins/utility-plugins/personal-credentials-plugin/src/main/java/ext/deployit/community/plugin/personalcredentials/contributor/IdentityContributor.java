package ext.deployit.community.plugin.personalcredentials.contributor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.reflect.Descriptor;
import com.xebialabs.deployit.plugin.api.reflect.PropertyDescriptor;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugin.overthere.HostContainer;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

public class IdentityContributor {

    @PrePlanProcessor
    public List<Step> injectPersonalCredentials(DeltaSpecification specification) {
        final List<Delta> deltas = specification.getDeltas();
        final DeployedApplication deployedApplication = specification.getDeployedApplication();
        final Environment environment = deployedApplication.getEnvironment();

        Boolean override = environment.getProperty("overrideHostCredentials");
        if (!override)
            return null;

        final Set<Host> hosts = ImmutableSet.<Host>builder()
                .addAll(filter(transform(deltas, DEPLOYED_TO_HOST), notNull()))
                .addAll(filter(transform(deltas, PREVIOUS_TO_HOST), notNull()))
                .build();

        logger.debug("Hosts {}", hosts);

        final Boolean perOsCredential = isPerOsCredential(deployedApplication);

        final Iterable<List<Step>> transform = transform(hosts, new Function<Host, List<Step>>() {
            @Override
            public List<Step> apply(final Host host) {
                if (perOsCredential) {
                    switch (host.getOs()) {
                        case WINDOWS:
                            logger.debug("IdentityContributor injects credentials in a {} host {}", "WINDOWS", host.getId());
                            setCredentials(host, "windowsUsername", "windowsPassword");
                            break;
                        case UNIX:
                            logger.debug("IdentityContributor injects credentials in a {} host {}", "UNIX", host.getId());
                            setCredentials(host, "unixUsername", "unixPassword");
                            break;
                    }
                } else {
                    logger.debug("IdentityContributor injects credentials in a host {} ", host.getId());
                    setCredentials(host, "username", "password");
                }

                if (!deployedApplication.hasProperty("checkConnection")) {
                    return null;
                }
                final Boolean checkConnection = deployedApplication.getProperty("checkConnection");
                return (checkConnection ? host.checkConnection() : Collections.EMPTY_LIST);
            }

            private void setCredentials(final Host host, final String usernamePropertyName, final String passwordPropertyName) {
                final String username = deployedApplication.getProperty(usernamePropertyName);
                final String password = deployedApplication.getProperty(passwordPropertyName);

                if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
                    final Descriptor descriptor = deployedApplication.getType().getDescriptor();
                    final String usernameLabel = descriptor.getPropertyDescriptor(usernamePropertyName).getLabel();
                    final String passwordLabel = descriptor.getPropertyDescriptor(passwordPropertyName).getLabel();
                    throw new RuntimeException(format("Cannot find personal credentials for host (%s/%s), please provide values for the '%s' and '%s' properties",
                            host.getId(),
                            host.getOs().toString(),
                            usernameLabel, passwordLabel
                    ));
                }
                host.setProperty("username", username);
                host.setProperty("password", password);
            }
        });
        return newArrayList(concat(transform));
    }

    private Boolean isPerOsCredential(final DeployedApplication deployedApplication) {
        if (deployedApplication.hasProperty("unixUsername") && deployedApplication.hasProperty("unixPassword") &&
                deployedApplication.hasProperty("windowsUsername") && deployedApplication.hasProperty("windowsPassword")) {
            return true;
        }
        if (deployedApplication.hasProperty("username") && deployedApplication.hasProperty("password")) {
            return false;
        }
        throw new RuntimeException("Invalid configuration on udm.DeployedApplication for personal-credentials plugin"
                + ", either set 'username' & 'password' properties or "
                + ", either set 'unixUsername' & 'unixPassword' & 'windowsUsername' & 'windowsPassword' properties.");
    }

    private static final Function<Delta, Host> DEPLOYED_TO_HOST = new ToHost() {
        public Host apply(Delta input) {
            return toHost(input.getDeployed());
        }
    };

    private static final Function<Delta, Host> PREVIOUS_TO_HOST = new ToHost() {
        public Host apply(Delta input) {
            return toHost(input.getPrevious());
        }
    };

    static abstract class ToHost implements Function<Delta, Host> {
        protected Host toHost(Deployed<?, ?> deployed) {
            if (deployed == null) {
                return null;
            }
            return toHost(deployed.getContainer());
        }

        private Host toHost(final ConfigurationItem item) {
            if (item instanceof Host) {
                return (Host) item;
            }
            if (item instanceof HostContainer) {
                HostContainer hostContainer = (HostContainer) item;
                return hostContainer.getHost();
            }
            final Collection<PropertyDescriptor> propertyDescriptors = item.getType().getDescriptor().getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getReferencedType() == null)
                    continue;
                if (propertyDescriptor.getReferencedType().instanceOf(Type.valueOf(Host.class))
                        || propertyDescriptor.isAsContainment()) {
                    final Host host = toHost((ConfigurationItem) propertyDescriptor.get(item));
                    if (host != null)
                        return host;
                }
            }
            return null;
        }
    }

    protected static final Logger logger = LoggerFactory.getLogger(IdentityContributor.class);
}
