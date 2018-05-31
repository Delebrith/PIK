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

app.controller("appController", function($scope, $http, $cookies, $location) {
	$scope.context = {
			user: null
	}

	$scope.pages = {
			projectForm: "project-form"
	}
	
	$scope.isPage = function (page) {
		if ($location.hash().indexOf(page) == 0)
		{
			remaining = $location.hash().substring(page.length);
			
			if (remaining.length == 0)
				return true;
			
			return ["/", "\\"].indexOf(remaining) > -1;
		}
		
		return false;
	}
	
	$scope.isAnyPage = function() {
		for (p in $scope.pages)
			if ($scope.pages.hasOwnProperty(p) && $scope.isPage($scope.pages[p]))
				return true;
		
		return false;
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
		return hasRole("THIRD_PARTY");
	}
	
	$scope.isUserTeacher = function() {
		return hasRole("TEACHER");
	}
	
	if ($cookies.get('token') != undefined) {
		$http.get("/user/me").then(function(response){
			$scope.context.user = response.data
		}, 
		function(response){
			// Failed to retrieve user data, probably due to token being invalid 
		})
	}
	
	$scope.getUsersByRoleAndName = function(role, name, number, responseHandler) {
		var response = $http.get(
				"/user/" + encodeURIComponent(role) 
				+ "/" + encodeURIComponent(name)
				+ "/" + encodeURIComponent(number));
		
	    response.then(responseHandler);
	}
});