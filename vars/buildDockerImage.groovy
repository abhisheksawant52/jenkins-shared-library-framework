#!/usr/bin/env groovy

import org.acme.jenkins.Docker

/**
 * buildDockerImage — build and (optionally) push a Docker image.
 *
 * Example:
 *   buildDockerImage(
 *       image:      'registry.example.com/payments-api',
 *       tag:        '1.4.0',
 *       dockerfile: 'Dockerfile',
 *       push:       true,
 *       credentialsId: 'registry-creds'
 *   )
 *
 * @param config map with the following keys:
 *   image          (required) fully-qualified image name without tag
 *   tag            image tag, defaults to the short git SHA
 *   dockerfile     path to the Dockerfile, defaults to 'Dockerfile'
 *   context        build context directory, defaults to '.'
 *   push           whether to push after building, defaults to false
 *   credentialsId  Jenkins credentials id for the registry login
 * @return the full image reference that was built (image:tag)
 */
def call(Map config = [:]) {
    assert config.image : 'buildDockerImage: "image" is required'

    String tag        = config.get('tag', 'latest')
    String dockerfile = config.get('dockerfile', 'Dockerfile')
    String context    = config.get('context', '.')
    boolean push      = config.get('push', false)
    String fullRef    = "${config.image}:${tag}"

    Docker docker = new Docker(this)

    echo "Building image ${fullRef} from ${dockerfile}"
    docker.build(fullRef, dockerfile, context)

    if (push) {
        if (config.credentialsId) {
            withCredentials([usernamePassword(
                credentialsId: config.credentialsId,
                usernameVariable: 'REGISTRY_USER',
                passwordVariable: 'REGISTRY_PASS'
            )]) {
                docker.login(config.image, env.REGISTRY_USER, env.REGISTRY_PASS)
                docker.push(fullRef)
            }
        } else {
            docker.push(fullRef)
        }
    }

    return fullRef
}
