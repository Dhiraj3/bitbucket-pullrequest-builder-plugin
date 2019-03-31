package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

public class BitbucketContants {
    public static final String BUILD_CMD = "jenkins -t";
    public static final String MERGE_CMD = "jenkins -m";

    public static final String SRC_DEST = "\n\n**Source:** %s **Destination:** %s";
    public static final String REQUESTED_BY = "\n\n**Requested By:** %s";
    public static final String BUILD_START_PREFIX = "## :clock3: Build Started";
    public static final String BUILD_START_PREFIX_LOWER = BUILD_START_PREFIX.toLowerCase();
    public static final String BUILD_START_COMMENT = BUILD_START_PREFIX + REQUESTED_BY + SRC_DEST
            + "\n\n#### *Please wait for build to finish ...*";

    public static final String BUILD_SUCCESS_PREFIX = "## :thumbsup: Build Success";
    public static final String BUILD_SUCCESS_PREFIX_LOWER = BUILD_SUCCESS_PREFIX.toLowerCase();
    public static final String BUILD_FAILURE_PREFIX = "## :thumbsdown: Build Failure";
    public static final String BUILD_FAILURE_PREFIX_LOWER = BUILD_FAILURE_PREFIX.toLowerCase();
    public static final String BUILD_FINISH_SUFFIX = SRC_DEST + "\n\n**URL:** *%s*";

    public static final String MERGE_COMMIT_COMMENT = "Merged in %s (pull request #%s)";

    public static final String MERGE_SUCCESS_PREFIX = "## :lock: Merge Success";
    // public static final String MERGE_SUCCESS_PREFIX_LOWER =
    // MERGE_SUCCESS_PREFIX
    // .toLowerCase();
    public static final String MERGE_SUCCESS_COMMENT = MERGE_SUCCESS_PREFIX + REQUESTED_BY + "\n\n#### *%s*";

    public static final String MERGE_FAILURE_PREFIX = "## :warning: Merge Failure";
    public static final String MERGE_FAILURE_PREFIX_LOWER = MERGE_FAILURE_PREFIX.toLowerCase();
    public static final String MERGE_FAILURE_COMMENT = MERGE_FAILURE_PREFIX + REQUESTED_BY
            + "\n\n#### *Error while trying to merge Pull Request:*" + "\n\n#### *%s*";

    public static final String MERGE_NOT_ALLOWED_PREFIX = "## :warning: Merge Not Allowed";
    public static final String MERGE_NOT_ALLOWED_PREFIX_LOWER = MERGE_NOT_ALLOWED_PREFIX.toLowerCase();
    public static final String MERGE_NOT_ALLOWED_COMMENT = MERGE_NOT_ALLOWED_PREFIX
            + "\n\n#### *%s does NOT have Merge permissions. Please contact Jenkins Admin for more information.*";

    public static final String SELF_MERGE_NOT_ALLOWED_COMMENT = MERGE_NOT_ALLOWED_PREFIX
            + "\n\n#### *%s CANNOT Merge his/her own Pull Request. Please request another team member with"
            + " Merge permissions to review and merge.*";

    public static final String AUTHOR_CAN_MERGE_AFTER_APPROVALS = MERGE_NOT_ALLOWED_PREFIX
            + "\n\n#### *Author (%s) need minimum 2 approvals to self merge.*";

    public static final String POST_MERGE_JOB_MSG_TRIGGERED = "Post Merge Job %s triggered. URL: %s";

    public static int MIN_APPROVED_COUNT = 2;
}
