package ext.deployit.community.plugin.storedprocedures.deployed;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Ordering;
import com.google.common.io.PatternFilenameFilter;

import com.xebialabs.deployit.plugin.api.deployment.planning.Create;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.planning.Modify;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.api.udm.artifact.DerivedArtifact;
import com.xebialabs.deployit.plugin.api.validation.Placeholders;
import com.xebialabs.deployit.plugin.generic.ci.Folder;
import com.xebialabs.deployit.plugin.generic.deployed.AbstractDeployed;
import com.xebialabs.deployit.plugin.generic.step.ScriptExecutionStep;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.local.LocalFile;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "Scripts in the folder are executed against a Container based on a naming convention")
@Placeholders
public class ExecutedFolder<D extends Folder> extends AbstractDeployed<D> implements DerivedArtifact<D> {

    private Map<String, Object> freeMarkerContext = Collections.singletonMap("deployed", (Object) this);

    @Property(required = false, category = "Placeholders", description = "A key/value pair mapping of placeholders in the deployed artifact to their values. Special values are <ignore> and <empty>")
    private Map<String, String> placeholders = newHashMap();

    private OverthereFile placeholderProcessedFile;

    @Property(description = "Regular expression used to identify a script in the folder.  A successful match should returns a single group to which the rollbackScriptPostfix can be appended" +
            " in order to find the associated rollback script or the script's dependent subfolder.  e.g.([0-9]*-.*)\\.sql")
    private String scriptRecognitionRegex;

    @Property(required = true)
    private List<String> folderSequence;

    @Property(description = "Name of the executor script that will be executed for each script found in the folder.")
    private String executorScript;

    @Create
    public void executeCreate(DeploymentPlanningContext ctx, Delta delta) {
        final File root = getDerivedArtifactAsFile();
        for (String sub : folderSequence) {
            ScriptExecutionStep step = null;
            for (File script : identifyAndOrderScriptsInFolder(new File(root, sub))) {
                step = newScriptExecutionStep(script.getName(), this, getCreateOrder(), getCreateVerb());
                if (getCreateOptions().contains(STEP_OPTION_UPLOAD_ARTIFACT_DATA)) {
                    step.setArtifact(script);
                }
                ctx.addStep(step);
            }

            if (step != null) {
                ctx.addCheckpoint(step, delta, Operation.CREATE);
            }
        }
    }

    @Modify
    public void executeModify(DeploymentPlanningContext ctx, Delta delta) {
        final File root = getDerivedArtifactAsFile();
        for (String sub : folderSequence) {
            ScriptExecutionStep step = null;
            for (File script : identifyAndOrderScriptsInFolder(new File(root, sub))) {
                step = newScriptExecutionStep(script.getName(), this, getModifyOrder(), getModifyVerb());
                if (getModifyOptions().contains(STEP_OPTION_UPLOAD_ARTIFACT_DATA)) {
                    step.setArtifact(script);
                }
                ctx.addStep(step);
            }

            if (step != null) {
                ctx.addCheckpoint(step, delta, Operation.MODIFY);
            }
        }

    }

    public String getDescription(String script, String verb) {
        return String.format("%s %s on %s", verb, script, getContainer().getName());
    }

    protected static ScriptExecutionStep newScriptExecutionStep(String script, ExecutedFolder<?> deployed, int order, String verb) {
        return new ScriptExecutionStep(
                order,
                deployed.getExecutorScript(),
                deployed.getContainer(),
                deployed.freeMarkerContext,
                deployed.getDescription(script, verb)
        );
    }

    protected File getDerivedArtifactAsFile() {
        checkNotNull(getFile(), "%s has a null file property", this);
        checkArgument(getFile() instanceof LocalFile, "%s has a file that is not a LocalFile but a %s", this, getFile().getClass().getName());

        LocalFile localFile = (LocalFile) getFile();
        return localFile.getFile();
    }

    protected List<File> identifyAndOrderScriptsInFolder(File folder) {
        List<File> scriptsToRun = findScriptsToRun(folder, getScriptRecognitionRegex());
        return Ordering.from(new FilenameComparator()).sortedCopy(scriptsToRun);
    }

    protected List<File> findScriptsToRun(File folder, String pattern) {
        File[] scriptsToRun = folder.listFiles(new PatternFilenameFilter(pattern));
        scriptsToRun = scriptsToRun == null ? new File[0] : scriptsToRun;
        return newArrayList(scriptsToRun);
    }


    @Override
    public D getSourceArtifact() {
        return getDeployable();
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    @Override
    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    @Override
    public OverthereFile getFile() {
        return placeholderProcessedFile;
    }

    @Override
    public void setFile(final OverthereFile overthereFile) {
        placeholderProcessedFile = overthereFile;
    }

    public String getScriptRecognitionRegex() {
        return scriptRecognitionRegex;
    }

    public String getExecutorScript() {
        return resolveExpression(executorScript);
    }

    public void setExecutorScript(String executorScript) {
        this.executorScript = executorScript;
    }


    public static class FilenameComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
