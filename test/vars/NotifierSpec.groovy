package vars

import org.acme.jenkins.Notifier
import spock.lang.Specification

/**
 * Unit spec for {@link org.acme.jenkins.Notifier}.
 *
 * Runs with Spock; the pipeline "steps" context is stubbed so the pure
 * rendering / colour logic can be exercised without a running Jenkins.
 */
class NotifierSpec extends Specification {

    def steps = Mock(Object)
    Notifier notifier = new Notifier(steps)

    def "colorFor maps known statuses to hex colours"() {
        expect:
        notifier.colorFor(status) == expected

        where:
        status     || expected
        'SUCCESS'  || '#36a64f'
        'failure'  || '#d00000'
        'UNSTABLE' || '#daa038'
        'weird'    || '#cccccc'
    }

    def "renderSlackPayload substitutes tokens and escapes quotes"() {
        given:
        String template = '{"channel":"${channel}","text":"${message}"}'

        when:
        String out = notifier.renderSlackPayload(template, [
            channel: '#ci',
            message: 'he said "hi"'
        ])

        then:
        out == '{"channel":"#ci","text":"he said \\"hi\\""}'
    }
}
