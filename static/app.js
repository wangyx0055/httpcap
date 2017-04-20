

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
    })
    .directive("contentWindow",function(){
        return {
            templateUrl:"views/content-window.html",
            restrict: 'E',
            scope:{
                message:'='
            },
            controller:["$scope",function($scope){


                var prettyPrint = function(json){
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

                $scope.$watch("message",function(message){
                    console.log(message)

                    if (!message)
                        return;

                    message.headers.forEach(function(header){
                        if (header.name == "Content-Type"){
                            $scope.editorOptions.mode = header.value.split(";")[0]
                            console.log("Content type: " + $scope.editorOptions.mode)
                        }
                    })

                    if (message.body)
                        $scope.body = prettyPrint(message.body)
                    else
                        $scope.body = null;
                })
            }]
        }
    })
    .factory("conversationListener",["$rootScope","$timeout",function($rootScope,$timeout){


        var subscriptions = [];

        var client;

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

        var connect = function() {

            console.log("Connecting....")
            var socket = new SockJS('/gs-guide-websocket');
            client = Stomp.over(socket);

            client.connect({}, function () {

                subscriptions.forEach(function (subscription) {
                        makeSubscription(subscription)
                })

                connected = true;

            }, function (m) {
                connected = false;
                console.log(m)
                $timeout(connect,500)
            });
        }

        connect()

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

                if (!subscription)
                    return;
                if (subscription.subscription)
                    subscription.subscription.unsubscribe();

                subscriptions.splice(subscriptions.indexOf(subscription),1)
            }
        }
    }])

    .controller("MainController",["$scope","PacketCapture","Interface","PacketCaptureStatus", function($scope,PacketCapture,Interface,PacketCaptureStatus){

        $scope.interfaces = Interface.query(function(ifs){
            if (ifs.length == 0){
                $scope.possiblePermissionError = true
            }
        });
        $scope.captures = PacketCapture.query();



        $scope.createFormData = function(){
            $scope.formData = {
                bufferSize : "8192"
            }
        };

        $scope.checkboxes = {}

        $scope.createCapture = function(input){
            new PacketCapture(input).$save(function(){
                $scope.formData = null;
                $scope.captures = PacketCapture.query();
            })
        }

        $scope.cancelCapture = function(){
            $scope.formData = null;
        }

        $scope.stopCapture = function(capture){
            PacketCaptureStatus.delete({id:capture.id},function(){
                $scope.captures = PacketCapture.query();
            })
        }

        $scope.deleteCapture = function(capture){
            capture.$delete(function(){
                $scope.captures = PacketCapture.query();
            })
        }

        $scope.$watch("checkboxes",function(checkboxes){

            var interfaces = []

            for (var i in checkboxes){
                if (checkboxes[i])
                    interfaces.push(i)
            }

            $scope.formData.interfaces = interfaces;

        },true)

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
            $scope.conversation  = conversation;
        }



        $scope.$watch("capture",function(){
            $scope.pages =  new Array(Math.ceil($scope.capture.httpInteractions.length/$scope.pageSize));
        },true)

        $scope.$on("$destroy",function(){
            conversationListener.unsubscribe(id)
        })
    }]);
