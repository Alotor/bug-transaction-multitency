package bug

import grails.converters.JSON
import grails.gorm.multitenancy.CurrentTenant

@CurrentTenant
class TestController {
    TestService testService

    def create() {
        // Access to the session before the service
        def value = Test.get(1)
        println(">>> " + value)

        // Execute the "transactional" service
        testService.execute()
        render([:] as JSON)
    }
}
