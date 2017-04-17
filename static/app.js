

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
        return $resource("/capture/:id",{id:"@id"}, {'update': { method:'PUT' }})
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
    })
    .factory("conversationListener",["$rootScope",function($rootScope){
        var socket = new SockJS('/gs-guide-websocket');

        var subscriptions = [];

        var client = Stomp.over(socket);

        var connected = false;



        var makeSubscription = function(subscription){
            subscription.subscription = client.subscribe("/capture/" + subscription.captureId,function(e){
                console.log(e)

                var item = JSON.parse(e.body)
                console.log(item)
                subscription.callback(item)
                $rootScope.$digest()
            });
        }

        client.connect({},function(){

            subscriptions.forEach(function(subscription){
                if (!subscription.subscription)
                    makeSubscription(subscription)
            })

            connected = true;

        },function(m){
            connected = false;
            console.log(m)
        });

        return {
            subscribe:function(captureId, callback){

                var subsciption = {
                    captureId:captureId,
                    callback:callback
                };

                subscriptions.push(subsciption);

                if (connected)
                makeSubscription(subsciption);

            },
            unsubscribe:function(captureId){



                var subscription = null;

                subscriptions.forEach(function(s){
                    if (s.captureId == captureId)
                        subscription = s;
                });

                console.log("Unsubscribing: " + subscription)

                if (subscription.subscription)
                    subscription.subscription.unsubscribe();

                subscriptions.splice(subscriptions.indexOf(subscription),1)
            }
        }
    }])

    .controller("MainController",["$scope","PacketCapture","Interface", function($scope,PacketCapture,Interface){

        $scope.interfaces = Interface.query();
        $scope.captures = PacketCapture.query();

        $scope.formData = {}

        $scope.createCapture = function(input){
            new PacketCapture(input).$save(function(){
                $scope.formData = {}
                $scope.captures = PacketCapture.query();
            })
        }

        $scope.stopCapture = function(capture){
            capture.running = false;
            capture.$update(function(){
                $scope.captures = PacketCapture.query();
            })
        }

        $scope.deleteCapture = function(capture){
            capture.$delete(function(){
                $scope.captures = PacketCapture.query();
            })
        }

    }])

    .controller("PacketListController",["$scope","PacketCapture","$routeParams","conversationListener",function($scope,PacketCapture,$routeParams,conversationListener){



        $scope.currentPage = 0;
        $scope.pageSize = 10;

        var id = $routeParams.id;


        $scope.setPage = function(page){
            $scope.currentPage = page;
        }

        $scope.capture = PacketCapture.get({id:$routeParams.id},function(){
            console.log($scope.capture)
            $scope.pages =  new Array(Math.ceil($scope.capture.httpInteractions.length/$scope.pageSize));

            conversationListener.subscribe(id,function(item){
                $scope.capture.httpInteractions.push(item)
            })
        })

        $scope.setConversation = function(conversation){
            console.log(conversation)
            $scope.conversation  = conversation;
            if (conversation) {
                $scope.responseBody = $scope.prettyPrint(conversation.response.body)
                $scope.requestBody = $scope.prettyPrint(conversation.request.body)

            }
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
            lineNumbers: true,
            readOnly: 'nocursor',
            theme:"dracula",
            mode: 'text/javascript',
        };

        $scope.$watch("capture",function(){
            $scope.pages =  new Array(Math.ceil($scope.capture.httpInteractions.length/$scope.pageSize));
        },true)

        $scope.$on("$destroy",function(){
            conversationListener.unsubscribe(id)
        })
    }]);
