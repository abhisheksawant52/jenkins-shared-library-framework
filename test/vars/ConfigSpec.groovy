package vars

import org.acme.jenkins.Config
import spock.lang.Specification

/**
 * Unit spec for {@link org.acme.jenkins.Config}.
 */
class ConfigSpec extends Specification {

    def "applies defaults and accepts overrides"() {
        when:
        Config cfg = new Config(appName: 'payments-api', deployEnv: 'production')

        then:
        cfg.appName == 'payments-api'
        cfg.registry == 'registry.example.com'
        cfg.buildTool == 'gradle'
        cfg.deployEnv == 'production'
    }

    def "validate requires appName"() {
        when:
        new Config([:]).validate()

        then:
        thrown(IllegalArgumentException)
    }

    def "rejects unknown config keys"() {
        when:
        new Config(bogusKey: true)

        then:
        thrown(IllegalArgumentException)
    }

    def "validate rejects an unsupported build tool"() {
        when:
        new Config(appName: 'x', buildTool: 'make').validate()

        then:
        thrown(IllegalArgumentException)
    }
}
