angular.module("schicwp.httpcap")
    .controller("PacketListController",["$scope","PacketCapture","PacketCaptureConversation","$routeParams","conversationListener","$location",
    function($scope,PacketCapture,PacketCaptureConversation,$routeParams,conversationListener,$location){

        $scope.currentPage = 0;
        $scope.pageSize = 10;

        var id = $routeParams.id;


        $scope.setPage = function(page){
            $scope.currentPage = page;
        };

        $scope.nextPage = function(){
            $scope.currentPage++;

            if ($scope.currentPage > $scope.pages.length -1)
                $scope.currentPage = $scope.pages.length -1;
        };

        $scope.previousPage = function(){
            $scope.currentPage--;

            if ($scope.currentPage < 0)
                $scope.currentPage = 0;
        };

        $scope.capture = PacketCapture.get({id:$routeParams.id},function(){

            $scope.capture.httpInteractions = PacketCaptureConversation.query({captureId:$routeParams.id},function(){
                $scope.pages =  new Array(Math.ceil($scope.capture.httpInteractions.length/$scope.pageSize));

                conversationListener.subscribe(id,function(item){
                    $scope.capture.httpInteractions.push(item)
                })
            })

        },function(){
            $location.path("/")
        });

        $scope.setConversation = function(conversation){
            $scope.conversation  = conversation;
        };



        $scope.$watch("capture",function(){
            if ($scope.capture.httpInteractions)
                $scope.pages =  new Array(Math.ceil($scope.capture.httpInteractions.length/$scope.pageSize));
        },true);

        $scope.$on("$destroy",function(){
            conversationListener.unsubscribe(id)
        })
    }]);
