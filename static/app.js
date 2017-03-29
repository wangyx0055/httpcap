angular.module("schicwp.httpcap",["ngResource","ngRoute",'ui.codemirror'])
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
        return $resource("/capture/:id")
    }])
    .factory("Interface",["$resource",function($resource){
        return $resource("/interface/:id")
    }])

    .controller("MainController",["$scope","PacketCapture","Interface",function($scope,PacketCapture,Interface){
        $scope.interfaces = Interface.query();
        $scope.captures = PacketCapture.query();

        $scope.formData = {}

        $scope.createCapture = function(input){
            new PacketCapture(input).$save(function(){
                $scope.formData = {}
                $scope.captures = PacketCapture.query();
            })
        }

    }])
    .controller("PacketListController",["$scope","PacketCapture","$routeParams",function($scope,PacketCapture,$routeParams){
        $scope.capture = PacketCapture.get({id:$routeParams.id})

        $scope.setConversation = function(conversation){
            console.log(conversation)
            $scope.conversation = conversation;
            $scope.responseBody = $scope.prettyPrint(conversation.response.body)
        }

        $scope.prettyPrint = function(json){
            try {
                return JSON.stringify(JSON.parse(json), null, 2)
            }catch (e){
                return json;
            }
        }

        $scope.editorOptions = {
            lineWrapping : true,
            lineNumbers: false,
            readOnly: 'nocursor',
            mode: 'text/javascript',
        };
    }]);
