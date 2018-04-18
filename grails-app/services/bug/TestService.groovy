package bug

import grails.transaction.Transactional

@Transactional
class TestService {
    void execute() {
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)
        new Test(name: "one", description: "one").save(flush: true)

        // We expect that the previous data is not stored!
        // But it is
        throw new RuntimeException("Failing")
    }
}