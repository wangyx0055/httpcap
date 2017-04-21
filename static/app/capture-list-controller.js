angular.module("schicwp.httpcap")
    .controller("CaptureListController",["$scope","PacketCapture","$interval", function($scope,PacketCapture,$interval){
        $scope.captures = PacketCapture.query();

        $interval(function(){
            $scope.captures = PacketCapture.query();
        },10000);

    }]);
