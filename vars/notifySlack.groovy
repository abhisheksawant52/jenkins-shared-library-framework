#!/usr/bin/env groovy

import org.acme.jenkins.Notifier

/**
 * notifySlack — post a build status message to a Slack channel.
 *
 * The message body is rendered from the bundled resource template
 * <code>org/acme/jenkins/slack-message.json</code> which is loaded via
 * <code>libraryResource</code>.
 *
 * Example:
 *   notifySlack(
 *       channel: '#deployments',
 *       status:  'SUCCESS',
 *       message: 'payments-api 1.4.0 deployed'
 *   )
 *
 * @param config map with keys:
 *   channel  (required) target Slack channel, e.g. '#deployments'
 *   status   one of SUCCESS, FAILURE, UNSTABLE (drives the colour)
 *   message  human-readable message body
 *   credentialsId  Jenkins secret text id holding the webhook URL
 */
def call(Map config = [:]) {
    assert config.channel : 'notifySlack: "channel" is required'

    String status  = config.get('status', 'SUCCESS')
    String message = config.get('message', "Build ${env.BUILD_NUMBER}")
    String credId  = config.get('credentialsId', 'slack-webhook')

    String template = libraryResource 'org/acme/jenkins/slack-message.json'
    Notifier notifier = new Notifier(this)

    String payload = notifier.renderSlackPayload(
        template,
        channel: config.channel,
        status:  status,
        message: message,
        color:   notifier.colorFor(status),
        buildUrl: env.BUILD_URL ?: ''
    )

    withCredentials([string(credentialsId: credId, variable: 'SLACK_WEBHOOK')]) {
        notifier.postToWebhook(env.SLACK_WEBHOOK, payload)
    }

    echo "Slack notification sent to ${config.channel} (${status})"
}
