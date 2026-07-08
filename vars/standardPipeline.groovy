#!/usr/bin/env groovy

import org.acme.jenkins.Config
import org.acme.jenkins.Docker
import org.acme.jenkins.GitUtils
import org.acme.jenkins.Notifier

/**
 * standardPipeline — an opinionated, end-to-end declarative pipeline.
 *
 * Wraps the common build/test/scan/package/deploy lifecycle so that a
 * consuming repository only needs a handful of lines in its Jenkinsfile:
 *
 *   standardPipeline(
 *       appName:      'payments-api',
 *       registry:     'registry.example.com',
 *       slackChannel: '#deployments',
 *       deployBranch: 'main'
 *   )
 *
 * @param userConfig map of overrides merged onto sensible defaults
 *                    (see org.acme.jenkins.Config for the full schema).
 */
def call(Map userConfig = [:]) {
    Config cfg = new Config(userConfig)
    cfg.validate()

    pipeline {
        agent any

        options {
            timestamps()
            disableConcurrentBuilds()
            buildDiscarder(logRotator(numToKeepStr: '20'))
            timeout(time: cfg.timeoutMinutes, unit: 'MINUTES')
        }

        environment {
            APP_NAME  = "${cfg.appName}"
            REGISTRY  = "${cfg.registry}"
            IMAGE_TAG = "${cfg.registry}/${cfg.appName}"
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                    script {
                        env.GIT_SHA     = new GitUtils(this).shortSha()
                        env.APP_VERSION = semanticVersion(prefix: cfg.tagPrefix)
                        echo "Building ${cfg.appName} @ ${env.APP_VERSION} (${env.GIT_SHA})"
                    }
                }
            }

            stage('Unit Tests') {
                when { expression { cfg.runTests } }
                steps {
                    runUnitTests(tool: cfg.buildTool, coverage: cfg.coverage)
                }
            }

            stage('Build Image') {
                steps {
                    script {
                        buildDockerImage(
                            image:      "${IMAGE_TAG}",
                            tag:        "${env.APP_VERSION}",
                            dockerfile: cfg.dockerfile,
                            push:       cfg.pushImage
                        )
                    }
                }
            }

            stage('Deploy') {
                when {
                    expression { cfg.deploy && env.BRANCH_NAME == cfg.deployBranch }
                }
                steps {
                    echo "Deploying ${cfg.appName}:${env.APP_VERSION} to ${cfg.deployEnv}"
                    sh "./deploy.sh ${cfg.deployEnv} ${env.APP_VERSION}"
                }
            }
        }

        post {
            success {
                script {
                    if (cfg.slackChannel) {
                        notifySlack(
                            channel: cfg.slackChannel,
                            status:  'SUCCESS',
                            message: "${cfg.appName} ${env.APP_VERSION} built successfully"
                        )
                    }
                }
            }
            failure {
                script {
                    if (cfg.slackChannel) {
                        notifySlack(
                            channel: cfg.slackChannel,
                            status:  'FAILURE',
                            message: "${cfg.appName} build #${env.BUILD_NUMBER} failed"
                        )
                    }
                }
            }
            always {
                cleanWs()
            }
        }
    }
}
