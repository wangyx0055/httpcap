

angular.module("schicwp.httpcap",["ngResource","ngRoute",'ui.codemirror','angularMoment'])
    .config(function($routeProvider) {
        $routeProvider
            .when("/", {
                templateUrl : "views/main.html",
                controller:"MainController"
            })
            .when("/capture/:id", {
                templateUrl : "views/capture.html",
                controller:"PacketListController"
            })
    })
    .factory("PacketCapture",["$resource",function($resource){
        return $resource("/capture/:id",{id:"@id"}, {'update': { method:'PUT' }})
    }])
    .factory("PacketCaptureConversation",["$resource",function($resource){
        return $resource("/capture/:captureId/conversation/:id",{id:"@id"})
    }])
    .factory("PacketCaptureStatus",["$resource",function($resource){
        return $resource("/capture/:id/status",{id:"@id"})
    }])
    .factory("Interface",["$resource",function($resource){
        return $resource("/interface/:id")
    }])
    .filter('startFrom', function() {
        return function(input, start) {
            start = +start; //parse to int
            if (input)
                return input.slice(start);
            else
                return input;
        }
    });


