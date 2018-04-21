app.config(function($httpProvider) {
	$httpProvider.interceptors.push(function($cookies) {
		return {
			'request': function(config) {
				if ($cookies.get('token') != undefined)
					config.headers['Authorization'] = 'Bearer ' + $cookies.get('token');
				
				return config;
			}
		};
	});
});

app.controller("appController", function($scope, $http, $cookies) {
	$scope.context = {
			user: null
	}
	
	$scope.isLogged = function () {
		return $scope.context.user != null
	}
	
	if ($cookies.get('token') != undefined) {
		$http.get("/user/me").then(function(response){
			$scope.context.user = response.data
		}, 
		function(response){
			// Failed to retrieve user data, probably due to token being invalid 
		})
	}
});