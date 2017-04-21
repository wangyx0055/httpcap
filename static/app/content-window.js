angular.module("schicwp.httpcap")
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
                };

                $scope.editorOptions = {
                    lineWrapping : true,
                    lineNumbers: true,
                    readOnly: 'nocursor',
                    theme:"dracula",
                    mode: 'text/javascript'
                };

                $scope.$watch("message",function(message){

                    if (!message)
                        return;

                    message.headers.forEach(function(header){
                        if (header.name == "Content-Type"){
                            $scope.editorOptions.mode = header.value.split(";")[0];
                            console.log("Content type: " + $scope.editorOptions.mode)
                        }
                    });

                    if (message.body)
                        $scope.body = prettyPrint(message.body);
                    else
                        $scope.body = null;
                })
            }]
        }
    });
