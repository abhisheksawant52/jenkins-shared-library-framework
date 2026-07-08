package org.acme.jenkins

/**
 * Config holds the resolved configuration for {@code standardPipeline}.
 *
 * User-supplied overrides are merged onto the defaults below in the
 * constructor, and {@link #validate()} enforces the required invariants.
 */
class Config implements Serializable {

    private static final long serialVersionUID = 1L

    /** Application / service name (required). */
    String appName

    /** Container registry host, e.g. 'registry.example.com'. */
    String registry = 'registry.example.com'

    /** Build tool used by runUnitTests: gradle | maven | npm. */
    String buildTool = 'gradle'

    /** Path to the Dockerfile relative to the repo root. */
    String dockerfile = 'Dockerfile'

    /** Git tag prefix used for semantic versioning. */
    String tagPrefix = 'v'

    /** Branch that is allowed to deploy. */
    String deployBranch = 'main'

    /** Target environment name for deployments. */
    String deployEnv = 'staging'

    /** Slack channel for build notifications (empty disables Slack). */
    String slackChannel = ''

    /** Whether to run the unit-test stage. */
    boolean runTests = true

    /** Whether to collect coverage during unit tests. */
    boolean coverage = false

    /** Whether to push the built image to the registry. */
    boolean pushImage = true

    /** Whether the deploy stage is enabled at all. */
    boolean deploy = true

    /** Overall pipeline timeout in minutes. */
    int timeoutMinutes = 60

    /**
     * Build a Config from a map of overrides.
     *
     * @param overrides map whose keys correspond to the fields above
     */
    Config(Map overrides = [:]) {
        overrides.each { key, value ->
            if (this.hasProperty(key as String)) {
                this."${key}" = value
            } else {
                throw new IllegalArgumentException("Unknown config key: '${key}'")
            }
        }
    }

    /**
     * Validate required fields and value ranges.
     *
     * @throws IllegalArgumentException if the configuration is invalid
     */
    void validate() {
        if (!appName?.trim()) {
            throw new IllegalArgumentException('Config: "appName" is required')
        }
        List<String> tools = ['gradle', 'maven', 'npm']
        if (!(buildTool in tools)) {
            throw new IllegalArgumentException(
                "Config: buildTool must be one of ${tools}, got '${buildTool}'")
        }
        if (timeoutMinutes <= 0) {
            throw new IllegalArgumentException('Config: timeoutMinutes must be positive')
        }
    }

    @Override
    String toString() {
        return "Config(appName=${appName}, registry=${registry}, buildTool=${buildTool}, " +
               "deployEnv=${deployEnv}, deployBranch=${deployBranch})"
    }
}
