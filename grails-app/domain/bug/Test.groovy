package bug

import grails.gorm.MultiTenant

class Test implements MultiTenant<Test> {
    String name
    String description
    Date dateCreated

    static constraints = {
        description(nullable: true)
    }

    static mapping = {
        version false
    }
}