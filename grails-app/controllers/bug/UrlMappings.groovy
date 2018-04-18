package bug.multitenancy

class UrlMappings {

    static mappings = {
        "/test"(controller: 'test', action: [GET: 'create'])
    }
}
