angular.module("schicwp.httpcap")
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

        $scope.checkboxes = {};

        $scope.createCapture = function(input){
            new PacketCapture(input).$save(function(){
                $scope.formData = null;
                $scope.captures = PacketCapture.query();
            })
        };

        $scope.cancelCapture = function(){
            $scope.formData = null;
        };

        $scope.stopCapture = function(capture){
            PacketCaptureStatus.delete({id:capture.id},function(){
                $scope.captures = PacketCapture.query();
            })
        };

        $scope.deleteCapture = function(capture){
            capture.$delete(function(){
                $scope.captures = PacketCapture.query();
            })
        };

        $scope.$watch("checkboxes",function(checkboxes){

            var interfaces = [];

            for (var i in checkboxes){
                if (checkboxes.hasOwnProperty(i) &&checkboxes[i])
                    interfaces.push(i)
            }

            if ($scope.formData)
                $scope.formData.interfaces = interfaces;

        },true)

    }]);
