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

var t;

app.controller("appController", function($scope, $http, $cookies) {
	$scope.context = {
			user: null
	}
	
	$scope.isLogged = function () {
		return $scope.context.user != null
	}
	
	function hasRole(role)
	{
		if (!$scope.isLogged())
			return false;
		
		for (index in $scope.context.user.authorities)
			if ($scope.context.user.authorities[index].name == role)
				return true;
		
		return false;
	}
	
	$scope.isUserAdministrator = function() {
		return hasRole("ADMIN");
	}

	$scope.isUserStudent = function() {
		return hasRole("STUDENT");
	}

	$scope.isUserEmployer = function() {
		return hasRole("EMPLOYER");
	}
	
	$scope.isUser3rdParty = function() {
		return hasRole("3RD_PARTY");
	}
	
	$scope.isUserTeacher = function() {
		return hasRole("TEACHER");
	}
	
	t = $scope.context
	
	if ($cookies.get('token') != undefined) {
		$http.get("/user/me").then(function(response){
			$scope.context.user = response.data
		}, 
		function(response){
			// Failed to retrieve user data, probably due to token being invalid 
		})
	}
});