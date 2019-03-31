package bitbucketpullrequestbuilder.bitbucketpullrequestbuilder;

import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketParticipant;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketPullRequestResponseValue;
import bitbucketpullrequestbuilder.bitbucketpullrequestbuilder.bitbucket.BitbucketUser;

import java.util.ArrayList;
import java.util.List;

public class BitbucketUtil {
    public static int getApprovedCount(BitbucketPullRequestResponseValue pullRequest) {
        int approvedCount = 0;

        List<BitbucketUser> approvers = getApprovers(pullRequest);

        BitbucketParticipant[] participants = pullRequest.getBitbucketParticipant();
        BitbucketUser author = pullRequest.getAuthor();

        if (participants == null) {
            return approvedCount;
        }

        for (BitbucketUser approver : approvers) {
            if (!approver.getUsername().equalsIgnoreCase(author.getUsername())
                    && !approver.getUsername().contains("jenkins")) {
                approvedCount++;
            }
        }
        return approvedCount;
    }

    public static List<BitbucketUser> getApprovers(BitbucketPullRequestResponseValue pullRequest) {
        List<BitbucketUser> approvers = new ArrayList<BitbucketUser>();

        BitbucketParticipant[] participants = pullRequest.getBitbucketParticipant();
        BitbucketUser author = pullRequest.getAuthor();

        if (participants == null) {
            return approvers;
        }

        for (int i = 0; i < participants.length; i++) {
            if (participants[i].getApproved()) {
                approvers.add(participants[i].getUser());
            }
        }
        return approvers;
    }
}
