package org.acme.jenkins

/**
 * Docker wraps common {@code docker} CLI operations as pipeline-friendly
 * methods.
 *
 * Instances are created with a reference to the pipeline "steps" context
 * (usually {@code this} inside a var), which exposes {@code sh}, {@code echo}
 * and friends.
 */
class Docker implements Serializable {

    private static final long serialVersionUID = 1L

    /** The pipeline step context (CpsScript). */
    private final def steps

    Docker(steps) {
        this.steps = steps
    }

    /**
     * Build an image from a Dockerfile.
     *
     * @param ref        full image reference including tag (image:tag)
     * @param dockerfile path to the Dockerfile
     * @param context    build context directory
     */
    void build(String ref, String dockerfile = 'Dockerfile', String context = '.') {
        steps.sh "docker build -f ${dockerfile} -t ${ref} ${context}"
    }

    /**
     * Authenticate against a registry derived from the image reference.
     *
     * @param imageRef image reference used to infer the registry host
     * @param username registry username
     * @param password registry password or token
     */
    void login(String imageRef, String username, String password) {
        String registry = imageRef.contains('/') ? imageRef.split('/')[0] : ''
        steps.sh """
            echo "${password}" | docker login ${registry} -u "${username}" --password-stdin
        """.stripIndent().trim()
    }

    /**
     * Push a previously built image.
     *
     * @param ref full image reference including tag
     */
    void push(String ref) {
        steps.sh "docker push ${ref}"
    }

    /**
     * Remove an image from the local daemon, ignoring errors.
     *
     * @param ref full image reference including tag
     */
    void remove(String ref) {
        steps.sh "docker rmi ${ref} || true"
    }
}
