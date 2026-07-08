package org.acme.jenkins

/**
 * GitUtils exposes small git helpers used across the library, each shelling
 * out through the pipeline {@code sh} step and returning trimmed stdout.
 */
class GitUtils implements Serializable {

    private static final long serialVersionUID = 1L

    private final def steps

    GitUtils(steps) {
        this.steps = steps
    }

    /**
     * @return the abbreviated commit SHA of HEAD
     */
    String shortSha() {
        return steps.sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    }

    /**
     * @return the current branch name
     */
    String branch() {
        return steps.sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
    }

    /**
     * Find the most recent tag matching the given prefix.
     *
     * @param prefix tag prefix, e.g. 'v'
     * @return the matching tag, or an empty string when none exist
     */
    String latestTag(String prefix = 'v') {
        return steps.sh(
            script: "git describe --tags --abbrev=0 --match '${prefix}*' 2>/dev/null || true",
            returnStdout: true
        ).trim()
    }

    /**
     * Count commits between a ref and HEAD.
     *
     * @param ref the starting ref (typically a tag)
     * @return number of commits since {@code ref}
     */
    int commitsSince(String ref) {
        String out = steps.sh(
            script: "git rev-list ${ref}..HEAD --count",
            returnStdout: true
        ).trim()
        return out ? out as int : 0
    }

    /**
     * @return total number of commits reachable from HEAD
     */
    int commitCount() {
        String out = steps.sh(
            script: 'git rev-list HEAD --count',
            returnStdout: true
        ).trim()
        return out ? out as int : 0
    }
}
