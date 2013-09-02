package ext.deployit.community.plugin.overthere;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.reflect.PropertyDescriptor;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.deployit.plugin.api.udm.Deployed;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.plugin.overthere.HostContainer;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class CredentialProcessor {

    static final Set<Type> SUPPORTED_TYPES = of(Type.valueOf("overthere.AliasSshHost"), Type.valueOf("overthere.AliasCifsHost"));

    @PrePlanProcessor
    public List<Step> injectPersonalCredentials(DeltaSpecification specification) {
        final Boolean checkConnection = isCheckConnection(specification.getDeployedApplication());
        final Set<Host> hosts = newHashSet();
        hosts.addAll(newHashSet(transform(specification.getDeltas(), DEPLOYED_TO_HOST)));
        hosts.addAll(newHashSet(transform(specification.getDeltas(), PREVIOUS_TO_HOST)));

        final Set<Host> aliasHosts = Sets.filter(hosts, IS_SUPPORTED_TYPES);
        logger.debug("Credential Hosts {}", aliasHosts);

        final Iterable<List<Step>> transform = transform(aliasHosts, new Function<Host, List<Step>>() {
            @Override
            public List<Step> apply(final Host host) {
                logger.debug("CredentialProcessor injects credentials in a host {} ", host.getId());
                setCredentials(host, "username", "password");
                return (checkConnection ? host.checkConnection() : Collections.EMPTY_LIST);
            }
        });
        return newArrayList(concat(transform));
    }

    private Boolean isCheckConnection(final DeployedApplication deployedApplication) {
        if (!deployedApplication.hasProperty("checkConnection")) {
            return false;
        }
        return deployedApplication.getProperty("checkConnection");
    }

    void setCredentials(final Host host, final String usernamePropertyName, final String passwordPropertyName) {
        final Credential credential = host.getProperty("credential");
        logger.debug("set {} property on host {}", usernamePropertyName, host.getId());
        host.setProperty(usernamePropertyName, credential.getUsername());
        logger.debug("set {} property on host {}", passwordPropertyName, host.getId());
        host.setProperty(passwordPropertyName, credential.getPassword());
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

    private static Predicate<Host> IS_SUPPORTED_TYPES = new Predicate<Host>() {
        @Override
        public boolean apply(final Host input) {
            return input != null && SUPPORTED_TYPES.contains(input.getType());
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

    protected static final Logger logger = LoggerFactory.getLogger(CredentialProcessor.class);
}
