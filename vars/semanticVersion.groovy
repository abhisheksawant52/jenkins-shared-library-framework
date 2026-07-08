#!/usr/bin/env groovy

import org.acme.jenkins.GitUtils

/**
 * semanticVersion — derive a SemVer-style version string for the build.
 *
 * The version is computed from the most recent matching git tag plus the
 * number of commits since that tag and the short SHA, producing values like:
 *
 *   1.4.0            (exactly on a tag)
 *   1.4.0-3+abc1234  (3 commits after v1.4.0)
 *   0.0.0-5+abc1234  (no tags yet)
 *
 * Example:
 *   def version = semanticVersion(prefix: 'v')
 *
 * @param config map with keys:
 *   prefix  tag prefix to strip, defaults to 'v'
 * @return the computed version string
 */
String call(Map config = [:]) {
    String prefix = config.get('prefix', 'v')
    GitUtils git = new GitUtils(this)

    String lastTag = git.latestTag(prefix)
    String shortSha = git.shortSha()

    if (!lastTag) {
        int total = git.commitCount()
        return "0.0.0-${total}+${shortSha}"
    }

    String base = lastTag.startsWith(prefix) ? lastTag.substring(prefix.length()) : lastTag
    int distance = git.commitsSince(lastTag)

    if (distance == 0) {
        return base
    }
    return "${base}-${distance}+${shortSha}"
}
