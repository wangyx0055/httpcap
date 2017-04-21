angular.module("schicwp.httpcap")
    .factory("conversationListener",["$rootScope","$timeout",function($rootScope,$timeout){

        var subscriptions = [];
        var client;
        var connected = false;

        var makeSubscription = function(subscription){
            subscription.subscription = client.subscribe("/capture/" + subscription.captureId,function(e){

                subscription.callback(JSON.parse(e.body));
                $rootScope.$digest()
            });
        };

        var connect = function() {

            var socket = new SockJS('/gs-guide-websocket');
            client = Stomp.over(socket);

            client.connect({}, function () {

                subscriptions.forEach(function (subscription) {
                    makeSubscription(subscription)
                });

                connected = true;

            }, function () {
                connected = false;
                $timeout(connect,500)
            });
        };

        //do initial connection
        connect();

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

                if (!subscription)
                    return;
                if (subscription.subscription)
                    subscription.subscription.unsubscribe();

                subscriptions.splice(subscriptions.indexOf(subscription),1)
            }
        }
    }]);
