package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by nishio
 */
public class BitbucketBuildTrigger extends Trigger<AbstractProject<?, ?>> {
    private static final Logger LOG = LoggerFactory.getLogger(BitbucketBuildTrigger.class.getName());
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("(?s)[,;\\s]\\s*");

    private static final String POST_MERGE_JOB_MSG_NOT_CONFIGURED = "Post Merge Job NOT configured, nothing to do";
    private static final String POST_MERGE_JOB_MSG_NOT_TRIGGERED = "Could NOT trigger Post Merge Job %s,"
        + " Reason: Not found";

    private final String projectPath;
    private final String cron;
    private final String username;
    private final String password;
    private final String repositoryOwner;
    private final String repositoryName;
    private final String targetBranch;
    private final String postMergeJobName;
    private final String admins;
    private final String ciSkipPhrases;
    private Set<String> adminsList;
    private transient BitbucketPullRequestsBuilder bitbucketPullRequestsBuilder;
    private AbstractProject<?, ?> postMergeJob;
    private String postMergeJobMessage = POST_MERGE_JOB_MSG_NOT_CONFIGURED;
    private AbstractProject<?, ?> project;

    @Extension
    public static final BitbucketBuildTriggerDescriptor descriptor = new BitbucketBuildTriggerDescriptor();

    @DataBoundConstructor
    public BitbucketBuildTrigger(String projectPath, String cron, String username, String password,
        String repositoryOwner, String repositoryName, String targetBranch, String postMergeJobName, String admins,
        String ciSkipPhrases) throws ANTLRException {
        super(cron);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Instantiating BitbucketBuildTrigger object: projectPath={}, cron={}"
                + ", username={}, password={}, repositoryOwner={}, repositoryName={}"
                + ", targetBranch={}, postMergeJobName={}, admins={}, ciSkipPhrases={}", new Object[] { projectPath,
                cron, username, password, repositoryOwner, repositoryName, targetBranch, postMergeJobName, admins,
                ciSkipPhrases });
        }
        this.projectPath = projectPath;
        this.cron = cron.trim();
        this.username = username.trim();
        this.password = password.trim();
        this.repositoryOwner = repositoryOwner.trim();
        this.repositoryName = repositoryName.trim();
        this.targetBranch = targetBranch.trim();
        this.postMergeJobName = postMergeJobName.trim();
        this.admins = admins;
        this.ciSkipPhrases = ciSkipPhrases;
        this.setAdminsList();
    }

    public String getProjectPath() {
        LOG.debug(projectPath);
        return projectPath;
    }

    public String getCron() {
        LOG.debug(cron);
        return cron;
    }

    public String getUsername() {
        LOG.debug(username);
        return username;
    }

    public String getPassword() {
        LOG.debug(password);
        return password;
    }

    public String getRepositoryOwner() {
        LOG.debug(repositoryOwner);
        return repositoryOwner;
    }

    public String getRepositoryName() {
        LOG.debug(repositoryName);
        return repositoryName;
    }

    public String getTargetBranch() {
        LOG.debug(targetBranch);
        return targetBranch;
    }

    public String getPostMergeJobName() {
        LOG.debug(postMergeJobName);
        return postMergeJobName;
    }

    public AbstractProject<?, ?> getPostMergeJob() {
        if (postMergeJob != null) {
            LOG.debug(postMergeJob.getFullName());
        }
        else {
            LOG.debug("Post MergeJob is null");
        }
        return postMergeJob;
    }

    public String getPostMergeJobMessage() {
        return postMergeJobMessage;
    }

    public String getAdmins() {
        LOG.debug(admins);
        return admins;
    }

    public Set<String> getAdminsList() {
        LOG.debug(Arrays.toString((String[]) adminsList.toArray(new String[adminsList.size()])));
        return adminsList;
    }

    public String getCiSkipPhrases() {
        LOG.debug(ciSkipPhrases);
        return ciSkipPhrases;
    }

    private void setAdminsList() {
        LOG.debug(admins);
        if (admins == null || admins.trim().length() == 0) {
            adminsList = new HashSet<String>();
        }
        else {
            String[] users = DELIMITER_PATTERN.split(admins);
            adminsList = new HashSet<String>(users.length);
            for (String u : users) {
                adminsList.add(u.trim().toLowerCase());
            }
        }
    }

    private void setPostMergeJob() {
        if (postMergeJobName != null && postMergeJobName.length() > 0) {
            postMergeJob = Jenkins.getInstance().getItemByFullName(postMergeJobName, AbstractProject.class);
            if (postMergeJob == null) {
                LOG.warn("job={} => Could NOT find Post Merge Job {} in Jenkins", this.project.getDisplayName(),
                    postMergeJobName);
                postMergeJobMessage = String.format(POST_MERGE_JOB_MSG_NOT_TRIGGERED, postMergeJobName);
            }
            else {
                LOG.info("job={} => Post Merge Job {} found", this.project.getDisplayName(),
                    postMergeJob.getDisplayName());
            }
        }
        else {
            LOG.info("job={} => Post Merge Job is Blank", this.project.getDisplayName());
        }
    }

    @Override
    public void start(AbstractProject<?, ?> project, boolean newInstance) {
        LOG.debug("project displayName={}, newInstance=", project.getDisplayName(), newInstance);
        try {
            this.project = project;
            this.setPostMergeJob();
            this.bitbucketPullRequestsBuilder = BitbucketPullRequestsBuilder.getBuilder();
            this.bitbucketPullRequestsBuilder.setProject(project);
            this.bitbucketPullRequestsBuilder.setTrigger(this);
            this.bitbucketPullRequestsBuilder.setupBuilder();
        }
        catch (IllegalStateException e) {
            LOG.error("Can't start trigger", e);
            return;
        }
        super.start(project, newInstance);
    }

    public static BitbucketBuildTrigger getTrigger(AbstractProject project) {
        LOG.debug("project displayName={}", project.getDisplayName());
        return (BitbucketBuildTrigger) project.getTrigger(BitbucketBuildTrigger.class);
    }

    public BitbucketPullRequestsBuilder getBuilder() {
        LOG.debug(bitbucketPullRequestsBuilder.toString());
        return bitbucketPullRequestsBuilder;
    }

    public QueueTaskFuture<?> startJob(BitbucketCause cause) {
        LOG.debug("cause shortDescription={}", cause.getShortDescription());

        Map<String, ParameterValue> values = new HashMap<String, ParameterValue>();
        values.put("sourceBranch", new StringParameterValue("sourceBranch", cause.getSourceBranch()));
        values.put("targetBranch", new StringParameterValue("targetBranch", cause.getTargetBranch()));
        values.put("repositoryOwner", new StringParameterValue("repositoryOwner", cause.getRepositoryOwner()));
        values.put("repositoryName", new StringParameterValue("repositoryName", cause.getRepositoryName()));
        values.put("pullRequestId", new StringParameterValue("pullRequestId", cause.getPullRequestId()));
        values.put("destinationRepositoryOwner",
            new StringParameterValue("destinationRepositoryOwner", cause.getDestinationRepositoryOwner()));
        values.put("destinationRepositoryName",
            new StringParameterValue("destinationRepositoryName", cause.getDestinationRepositoryName()));
        values.put("pullRequestTitle", new StringParameterValue("pullRequestTitle", cause.getPullRequestTitle()));

        LOG.info("job={}, Triggering Build ...", this.job.getDisplayName());

        return this.job.scheduleBuild2(0, cause, new ParametersAction(new ArrayList(values.values())));
    }

    @Override
    public void run() {
        if (this.getBuilder().getProject().isDisabled()) {
            LOG.info("projectPath={}, Build Disabled. Skipping ...", projectPath);
        }
        else {
            LOG.info("\n****************\nPull Request Job Triggered: repositoryName={}, targetBranch={}, cron={}\n",
                new Object[] { this.repositoryName, this.targetBranch, this.cron });
            this.bitbucketPullRequestsBuilder.run();
        }
        this.getDescriptor().save();
    }

    @Override
    public void stop() {
        LOG.info("\nPull Request Job Stopped.\n****************\n");
        super.stop();
    }

    @Override
    public String toString() {
        return new StringBuilder("BitbucketBuildTrigger [projectPath=").append(projectPath).append(", cron=")
            .append(cron).append(", username=").append(username).append(", password=").append(password)
            .append(", repositoryOwner=").append(repositoryOwner).append(", repositoryName=").append(repositoryName)
            .append(", targetBranch=").append(targetBranch).append(", postMergeJobName=").append(postMergeJobName)
            .append(", admins=").append(admins).append(", ciSkipPhrases=").append(ciSkipPhrases).append("]").toString();
    }

    public static final class BitbucketBuildTriggerDescriptor extends TriggerDescriptor {
        private static final String DISPLAY_NAME = "Bitbucket Pull Request Build Trigger";

        public BitbucketBuildTriggerDescriptor() {
            LOG.debug("INIT");
            load();
        }

        @Override
        public boolean isApplicable(Item item) {
            LOG.debug("item displayName={}", item.getDisplayName());
            return true;
        }

        @Override
        public String getDisplayName() {
            LOG.debug(DISPLAY_NAME);
            return DISPLAY_NAME;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            LOG.debug("req url={}, json={}", req.getRequestURLWithQueryString(), json.toString());
            save();
            return super.configure(req, json);
        }
    }

}
