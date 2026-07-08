package org.acme.jenkins

/**
 * Notifier renders and delivers notification payloads (currently Slack).
 *
 * Templates are passed in as strings (loaded via {@code libraryResource} in the
 * calling var) and rendered with simple {@code ${...}} token substitution to
 * keep the class free of pipeline-only APIs where possible.
 */
class Notifier implements Serializable {

    private static final long serialVersionUID = 1L

    private final def steps

    Notifier(steps) {
        this.steps = steps
    }

    /** Slack attachment colours keyed by build status. */
    private static final Map<String, String> COLORS = [
        SUCCESS:  '#36a64f',
        FAILURE:  '#d00000',
        UNSTABLE: '#daa038',
        ABORTED:  '#808080'
    ]

    /**
     * @param status build status, case-insensitive
     * @return the hex colour to use for the Slack attachment
     */
    String colorFor(String status) {
        return COLORS.get(status?.toUpperCase(), '#cccccc')
    }

    /**
     * Render a Slack payload by substituting {@code ${key}} tokens in the
     * template with the supplied bindings.
     *
     * @param template the raw JSON template string
     * @param bindings map of token name to replacement value
     * @return the rendered JSON payload
     */
    String renderSlackPayload(String template, Map bindings) {
        String rendered = template
        bindings.each { key, value ->
            String safe = (value == null ? '' : value.toString())
                .replace('\\', '\\\\')
                .replace('"', '\\"')
            rendered = rendered.replace('${' + key + '}', safe)
        }
        return rendered
    }

    /**
     * POST a JSON payload to an incoming webhook URL using curl.
     *
     * @param webhookUrl the incoming webhook URL
     * @param payload    the JSON body to send
     */
    void postToWebhook(String webhookUrl, String payload) {
        steps.sh(
            label: 'post-slack',
            script: """
                curl -sS -X POST -H 'Content-type: application/json' \
                    --data '${payload}' '${webhookUrl}'
            """.stripIndent().trim()
        )
    }
}
