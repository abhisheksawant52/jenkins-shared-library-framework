#!/usr/bin/env groovy

/**
 * runUnitTests — run a project's unit test suite and publish results.
 *
 * Supports a few common build tools out of the box and archives JUnit XML
 * reports so the Jenkins test-trend graph is populated.
 *
 * Example:
 *   runUnitTests(tool: 'gradle', coverage: true)
 *
 * @param config map with keys:
 *   tool      one of 'gradle', 'maven', 'npm' (defaults to 'gradle')
 *   coverage  whether to collect coverage, defaults to false
 *   reports   JUnit report glob, defaults per tool
 */
def call(Map config = [:]) {
    String tool     = config.get('tool', 'gradle')
    boolean coverage = config.get('coverage', false)

    Map defaultReports = [
        gradle: '**/build/test-results/test/*.xml',
        maven:  '**/target/surefire-reports/*.xml',
        npm:    '**/junit.xml'
    ]
    String reports = config.get('reports', defaultReports[tool])

    echo "Running unit tests with ${tool} (coverage=${coverage})"

    try {
        switch (tool) {
            case 'gradle':
                sh coverage ? './gradlew test jacocoTestReport --no-daemon' : './gradlew test --no-daemon'
                break
            case 'maven':
                sh coverage ? 'mvn -B test jacoco:report' : 'mvn -B test'
                break
            case 'npm':
                sh 'npm ci'
                sh coverage ? 'npm run test:coverage' : 'npm test'
                break
            default:
                error "runUnitTests: unsupported tool '${tool}'"
        }
    } finally {
        junit allowEmptyResults: true, testResults: reports
    }
}
